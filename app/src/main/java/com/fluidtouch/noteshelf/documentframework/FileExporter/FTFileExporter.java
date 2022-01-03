package com.fluidtouch.noteshelf.documentframework.FileExporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.document.FTLock;
import com.fluidtouch.noteshelf.document.FTTextureManager;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPDF;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTPdfDocumentRef;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.pdfexport.FTPdfCreator;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.FTOffscreenBitmap;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Vineet on 11/09/2019
 **/

public class FTFileExporter {
    private boolean isCancelled;

    public void exportNotebooks(@NonNull Context context, @NonNull List<FTShelfItem> notebooks, @NonNull String exportFormat, FileExporterCallback callback) {
        if (!notebooks.isEmpty()) {
            initTempFolder();
            FTNoteshelfDocument selectedDocument = FTDocumentFactory.documentForItemAtURL(notebooks.get(0).getFileURL());
            switch (exportFormat) {
                case FTConstants.NSA_EXTENSION:
                    String path = context.getCacheDir() + "/nsaExport/";
                    File fileExport = new File(path);
                    if (!fileExport.exists())
                        fileExport.mkdirs();
                    else {
                        FTFileManagerUtil.deleteFilesInsideFolder(fileExport);
                    }
                    if (this.isCancelled) {
                        return;
                    }
                    AsyncTask.execute(() -> ZipUtil.zip(context, selectedDocument.getFileURL().getPath(), path, FTConstants.NSA_EXTENSION, (file, zipError) -> {
                        FTLog.debug(FTFileExporter.class.getName(), "Generated .nsa for notebook export");
                        if (this.isCancelled) {
                            if (file != null) file.delete();
                            return;
                        }
                        Error exportError = null;
                        if (file == null || !file.exists() || zipError != null) {
                            exportError = new Error();
                        }
                        callback.onFinishedExporting(file, exportError);
                    }));
                    break;
                case FTConstants.PDF_EXTENSION:
                case FTConstants.PNG_EXTENSION:
                    selectedDocument.openDocument(context, (success, error) -> {
                        if (success && error == null) {
                            exportPages(context, selectedDocument.pages(context), exportFormat, callback);
                        } else {
                            callback.onFinishedExporting(null, new Error());
                        }
                    });
                    break;
            }
        } else {
            callback.onFinishedExporting(null, new Error());
        }
    }

    public void exportPages(@NonNull Context context, @NonNull List<FTNoteshelfPage> pages, @NonNull String exportFormat, FileExporterCallback callback) {
        if (!pages.isEmpty()) {
            initTempFolder();
            FTNoteshelfDocument selectedDocument = FTDocumentFactory.documentForItemAtURL(pages.get(0).getParentDocument().getFileURL());
            switch (exportFormat) {
                case FTConstants.PDF_EXTENSION:
                    selectedDocument.openDocument(context, (success, error) -> {
                        if (FTFileExporter.this.isCancelled) return;
                        if (success && error == null) {
                            new FTPdfCreator(context).noteshelfDocument(selectedDocument).pages(pages).onCreateResponse(new FTPdfCreator.OnCreateResponse() {
                                @Override
                                public void onPdfCreated(File file) {
                                    FTLog.debug(FTFileExporter.class.getName(), "Generated .pdf for notebook export");
                                    if (FTFileExporter.this.isCancelled) {
                                        if (file != null) file.delete();
                                        return;
                                    }
                                    callback.onFinishedExporting(file, null);
                                }

                                @Override
                                public void onPdfCreateFailed() {
                                    callback.onFinishedExporting(null, new Error());
                                }
                            }).Create();
                        } else {
                            callback.onFinishedExporting(null, new Error());
                        }
                    });
                    break;
                case FTConstants.PNG_EXTENSION:
                    selectedDocument.openDocument(context, (success, error) -> {
                        if (FTFileExporter.this.isCancelled) {
                            callback.onFinishedExporting(null, new Error(context.getString(R.string.cancelled)));
                            return;
                        }
                        if (success && error == null) {
                            File imageDir = generateImageFolder(context, pages, selectedDocument.getDisplayTitle(context));
                            FTLog.debug(FTFileExporter.class.getName(), "Generated png .zip for notebook export");
                            if (FTFileExporter.this.isCancelled) {
                                imageDir.delete();
                                return;
                            }
                            if (pages.size() == 1) {
                                File pageImageFile = null;
                                File[] files = imageDir.listFiles();
                                if (files != null && files.length > 0) {
                                    pageImageFile = files[0];
                                }
                                Error exportError = null;
                                if (pageImageFile == null) {
                                    exportError = new Error();
                                }
                                callback.onFinishedExporting(pageImageFile, exportError);
                            } else {
                                ZipUtil.zip(context, imageDir.getPath(), imageDir.getParent() + "/", FTConstants.ZIP_EXTENSION, (file, zipError) -> {
                                    Error exportError = null;
                                    if (this.isCancelled) {
                                        if (file != null) file.delete();
                                        return;
                                    }
                                    if (file == null || !file.exists() || zipError != null) {
                                        exportError = new Error();
                                    }
                                    callback.onFinishedExporting(file, exportError);
                                });
                            }
                        } else {
                            callback.onFinishedExporting(null, new Error());
                        }
                    });
            }
        } else {
            callback.onFinishedExporting(null, new Error());
        }
    }

    public void cancelExporting() {
        this.isCancelled = true;
    }

    private File generateImageFolder(Context context, List<FTNoteshelfPage> pages, String bookName) {
        File imageCacheDir = new File(context.getCacheDir() + "/imageExport");
        if (imageCacheDir.exists())
            FTFileManagerUtil.deleteFilesInsideFolder(imageCacheDir);
        File imageDir = new File(context.getCacheDir() + "/imageExport", bookName);
        if (!imageDir.exists())
            imageDir.mkdirs();

        FTRenderManager renderManager = new FTRenderManager(context, FTRenderMode.offScreen);
        float density = context.getResources().getDisplayMetrics().density;

        for (FTNoteshelfPage eachPage : pages) {
            int[] texture = new int[1];
            texture[0] = 0;
            FTLock lock = new FTLock();
            FTTextureManager.sharedInstance().texture(eachPage, density, new FTTextureManager.TextureGenerationCallBack() {
                @Override
                public void onCompletion(int textureID) {
                    texture[0] = textureID;
                    lock.signal();
                }
            });
            lock.waitTillSignal();;
            FTOffscreenBitmap bitmapInfo = renderManager.generateFullImage(eachPage.getPageAnnotations(),
                    texture[0],
                    eachPage.getPageRect(),0,
                    false,
                    1,
                    "File exporter image generation");
            createPngFileForBitmap(bitmapInfo.image, eachPage.getParentDocument().getDisplayTitle(context) + " P" + (eachPage.pageIndex() + 1), imageDir.getPath());
            bitmapInfo.image.recycle();
            bitmapInfo.image = null;
        }
        renderManager.destroy();
        return imageDir;
    }

    private void createPngFileForBitmap(Bitmap pageBitmap, String imageFileName, String
            parentDirPath) {
        File imageFile = new File(parentDirPath, imageFileName + FTConstants.PNG_EXTENSION);
        if (pageBitmap != null) {
            try {
                imageFile.createNewFile();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.reset();
                pageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bitmapData = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(bitmapData);

                bos.reset();
                bos.flush();
                bos.close();
                fos.flush();
                fos.close();
            } catch (IOException e) {
                FTLog.debug(FTFileExporter.class.getName(), e.getMessage());
            }
        }
    }

    private void initTempFolder() {
        File zipperDir = new File(ZipUtil.zipFolderPath());
        if (zipperDir.exists()) {
            for (File file : zipperDir.listFiles()) {
                FTFileManagerUtil.deleteRecursive(file);
            }
        }
        if (!zipperDir.exists())
            zipperDir.mkdirs();
    }

    public interface FileExporterCallback {
        void onFinishedExporting(File file, Error error);
    }
}