package com.fluidtouch.noteshelf.store.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SizeF;

import androidx.fragment.app.FragmentManager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTPdfDocumentRef;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.store.ui.FTAddPaperThemeDialog;
import com.fluidtouch.noteshelf2.R;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import org.benjinus.pdfium.PdfiumSDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FTCreateCustomPaper {

    private Context mContext;
    private Uri mUri;
    private FragmentManager mFragmentManager;
     Bitmap bitmap=null;

    public FTCreateCustomPaper(Context context, Uri uri, FragmentManager fragmentManager) {
        mContext = context;
        mUri = uri;
        mFragmentManager = fragmentManager;
    }

    public FTCreateCustomPaper(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
    }
    public FTCreateCustomPaper(){}

    public void create() {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mUri);
            int rotation = FileUriUtils.getCapturedImageOrientation(mContext, mUri);
            if (rotation > 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            }
            if (bitmap == null) {
                ParcelFileDescriptor fileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r", null);
                InputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                final File inputFile = FTFileManagerUtil.createFileFromInputStream(inputStream, mContext.getCacheDir() + "/" + FTFileManagerUtil.getFileNameFromUri(mContext, mUri));
                FTPdfDocumentRef pdfDocumentRef = new FTPdfDocumentRef(mContext, FTUrl.fromFile(inputFile), "");
                bitmap = pdfDocumentRef.pageBackgroundImageOfSize(new SizeF(272f, 340f), 0, FTConstants.DOC_VERSION);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FTAddPaperThemeDialog ftAddThemeDialog = FTAddPaperThemeDialog.newInstance(new FTAddPaperThemeDialog.DialogResult() {
            @Override
            public void onDataSubmit(String name, Bitmap bitmap) {
                FTSmartDialog smartDialog = new FTSmartDialog()
                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(mContext.getString(R.string.creating))
                        .show(mFragmentManager);
                AsyncTask task = new AsyncTask() {

                    @Override
                    protected Boolean doInBackground(Object[] objects) {
                        String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(mContext, mUri);
                        if (mimeType.equals(mContext.getString(R.string.mime_type_application_pdf))) {
                            return createFromPdf(name);
                        } else if (mimeType.contains("image")) {
                            return createFromImage(name);
                        } else {
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        if (smartDialog != null)
                            smartDialog.dismissAllowingStateLoss();
                        if ((boolean) o) {
                            FTNTheme theme = new FTNPaperTheme();
                            theme.themeName = name;
                            theme.setCategoryName(mContext.getString(R.string.custom));
                            theme.packName = name + ".nsp";
                            theme.ftThemeType = FTNThemeCategory.FTThemeType.PAPER;
                            theme.isCustomTheme = true;
                            ObservingService.getInstance().postNotification("addCustomTheme", theme);
                        }
                    }
                };
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        }, bitmap);
        ftAddThemeDialog.show(mFragmentManager, "ftAddThemeDialog");
    }

    private boolean createFromPdf(String name) {
        try {
            ParcelFileDescriptor fileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r", null);
            InputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            final File inputFile = FTFileManagerUtil.createFileFromInputStream(inputStream, mContext.getCacheDir() + "/" + FTFileManagerUtil.getFileNameFromUri(mContext, mUri));

            String templateName = "template";
            int device = FTApp.getInstance().getCurActCtx().getResources().getInteger(R.integer.device);
            if (device == 600) {
                templateName += "_" + device;
            }
            templateName += ".pdf";

            File outputFile = new File(FTConstants.CUSTOM_PAPERS_PATH + name + ".nsp", templateName);
            Bitmap backgroundImage;
            PdfiumSDK pdfiumSDK = new PdfiumSDK(this.mContext);
            if (isPasswordProtected(inputFile)) {
                //Bitmap backgroundImage = Bitmap.createBitmap(272, 340, Bitmap.Config.ARGB_8888);
                backgroundImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pwd_protected_bg);
                //backgroundImage = Bitmap.createScaledBitmap(backgroundImage, 272, 340, false);
                backgroundImage = Bitmap.createScaledBitmap(backgroundImage, 116, 143, false);

            } else {
                FTPdfDocumentRef pdfDocumentRef = new FTPdfDocumentRef(mContext, FTUrl.fromFile(inputFile), "");
                backgroundImage = pdfDocumentRef.pageBackgroundImageOfSize(new SizeF(272f, 340f), 0, FTConstants.DOC_VERSION);
            }

            boolean isSaved = BitmapUtil.saveBitmap(backgroundImage, FTConstants.CUSTOM_PAPERS_PATH + name + ".nsp", "thumbnail@2x.png");
            if (!isSaved) {
                return false;
            }
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            try {
                FTDocumentUtils.copyFile(inputFile, outputFile);
            } catch (IOException e) {
                e.printStackTrace();
                if (inputFile != null)
                    inputFile.delete();
                return false;
            }
            if (inputFile != null)
                inputFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isPasswordProtected(File pdfFile) {
        try
        {
            PDDocument document = PDDocument.load(new File(pdfFile.getPath()));
            if(document.isEncrypted()) {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    public boolean createFromImage(String name) {
        Bitmap bitmap = null;
        try {
            File zipperDir = new File(ZipUtil.zipFolderPath());
            FTFileManagerUtil.deleteRecursive(zipperDir);
            File tempFile = new File(ZipUtil.zipFolderPath());
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }

            InputStream inputStream;
            String fileName;
            //This logic is for the scanned document
            ParcelFileDescriptor fileDescriptor = mContext.getContentResolver().openFileDescriptor(mUri, "r", null);
            inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            fileName = FTFileManagerUtil.getFileNameFromUri(mContext, mUri);
            File importedFile = FTFileManagerUtil.createFileFromInputStream(inputStream, ZipUtil.zipFolderPath().concat(fileName));
            bitmap = BitmapFactory.decodeFile(importedFile.getAbsolutePath());
            int rotation = FileUriUtils.getCapturedImageOrientation(mContext, FileUriUtils.getUriForFile(mContext, importedFile));
            if (rotation > 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            }
            int thumbnailWidth = 0;
            int thumbnailHeight = 0;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                thumbnailWidth = 274;
                thumbnailHeight = 187;
            } else {
                thumbnailWidth = 272;
                thumbnailHeight = 340;
            }
            Bitmap backgroundImage = BitmapUtil.getResizedBitmap(bitmap, thumbnailWidth, thumbnailHeight, false);
            boolean isSaved = BitmapUtil.saveBitmap(backgroundImage, FTConstants.CUSTOM_PAPERS_PATH + name + ".nsp", "thumbnail@2x.png");
            if (!isSaved)
                return false;

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();


            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);


            paint.setColor(Color.BLUE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);


            // write the document content
            String templateName = "template";
            templateName += ".pdf";

            File outputFile = new File(FTConstants.CUSTOM_PAPERS_PATH + name + ".nsp", templateName);
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            document.writeTo(new FileOutputStream(outputFile));

            // close the document
            document.close();
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
            bitmap.recycle();
            return false;
        }
        return true;
    }
}
