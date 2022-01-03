package com.fluidtouch.noteshelf.document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.util.SizeF;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTPdfDocumentRef;
import com.fluidtouch.noteshelf.zoomlayout.FTPageContentHolderView;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.utils.FTGLUtils;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FTTextureManager {
    public interface TextureGenerationCallBack {
        void onCompletion(int textureID);
    }

    private class FTTexture {
        int textureID;
        String textureKey;
        String docID;
    }

    private class FTTextureOperation {
        FTNoteshelfPage page;
        Float scale;
        TextureGenerationCallBack callBack;

        String key() {
            String key;
            FTPdfDocumentRef pdfRef = page.getFTPdfDocumentRef();
            if (pdfRef != null) {
                key = "Key" + page.associatedPDFFileName + "_" + (page.associatedPDFKitPageIndex - 1) + "-" + page.pageIndex() + "_" + scale;
            } else {
                key = "Key" + page.associatedPDFFileName + "_" + page.pageIndex() + "_" + scale;
            }

            return key;
        }
    }

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private Future currentFuture;
    private ArrayList<FTTexture> textureList = new ArrayList<>();
    private ArrayList<FTTextureOperation> textureOperations = new ArrayList<>();
    private FTRenderManager renderManager;
    private FTTextureOperation currentOperation = null;
    private final android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

    private static FTTextureManager sharedInstance = null;

    public static FTTextureManager sharedInstance() {
        if (null == sharedInstance) {
            sharedInstance = new FTTextureManager();
        }
        return sharedInstance;
    }

    private FTTextureManager() {
    }

    public void texture(FTNoteshelfPage page, float scale, TextureGenerationCallBack callBack) {
        FTTextureOperation operation = new FTTextureOperation();
        operation.page = page;
        operation.scale = textureScale(scale);
        operation.callBack = callBack;
        synchronized (textureOperations) {
            textureOperations.add(operation);
        }
        startExecution();
    }

    private class TextureRunnable implements Runnable {
        @Override
        public void run() {
            while (!textureOperations.isEmpty()) {
                synchronized (textureOperations) {
                    if (textureOperations.size() > 0) {
                        currentOperation = textureOperations.get(0);
                        textureOperations.remove(0);
                    }
                }

                if (null != currentOperation && null != currentOperation.page) {
                    FTNoteshelfPage currentPage = currentOperation.page;
                    float texScale = currentOperation.scale;
                    String key = currentOperation.key();
                    FTTexture texture = textureForKey(key);
                    if (null == texture) {
                        java.io.File file = new java.io.File(context().getCacheDir(), "pageBackground");
                        FTPageContentHolderView.PageBgType bgType = FTPageContentHolderView.PageBgType.NORMAL;
                        Bitmap bitmap;
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        String fileName = currentPage.associatedPDFFileName + "_" + currentPage.associatedPageIndex;
                        if (texScale == 1) {
                            fileName = fileName + "_normal.jpeg";
                            bgType = FTPageContentHolderView.PageBgType.NORMAL;
                        } else if (texScale == 1.5) {
                            fileName = fileName + "_medium.jpeg";
                            bgType = FTPageContentHolderView.PageBgType.MEDIUM;
                        } else {
                            fileName = fileName + "_high.jpeg";
                            bgType = FTPageContentHolderView.PageBgType.HIGH;
                        }
                        File bitmapFile = new File(file.getPath(), fileName);
                        if (bitmapFile.exists() && bitmapFile.length() / 1024 > 0) {
                            bitmap = BitmapUtil.getBitmap(FileUriUtils.getUri(bitmapFile));
                        } else {
                            SizeF maxSize = FTRenderManager.maxTextureSize(FTRenderMode.onScreen);
                            SizeF scaledSize = FTGeometryUtils.scaleSize(maxSize, texScale);
                            bitmap = pageBackgroundImageOfSize(currentPage, scaledSize);
                            savePageBackground(bitmap, file, fileName);
                        }
                        FTRenderManager renderManager = getRenderManager();
                        synchronized (renderManager) {
                            int textureID = getRenderManager().generateTexture(bitmap, "texturepool", "Page Background");
                            texture = new FTTexture();
                            texture.textureID = textureID;
                            texture.textureKey = key;
                            texture.docID = currentPage.getParentDocument().getDocumentUUID();
                        }
                    }
                    int texID = 0;
                    if (null != texture) {
                        addTexture(texture);
                        texID = texture.textureID;
                    }
                    final int textureToReturn = texID;
                    FTTextureOperation operation = currentOperation;
                    handler.post(() -> {
                        if (null != operation.callBack) {
                            operation.callBack.onCompletion(textureToReturn);
                        }
                    });
                }
            }
        }
    }

    private Context context() {
        return FTApp.getInstance().getApplicationContext();
    }

    private FTRenderManager getRenderManager() {
        if (null == renderManager) {
            renderManager = new FTRenderManager(context(), FTRenderMode.offScreen);
        }
        return renderManager;
    }

    private void startExecution() {
//        FTGLContextFactory.getInstance().setCurrentContextAsShared();
//        int[] maxTextureSize = new int[1];
//        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE,maxTextureSize,0);
        if (currentFuture == null || currentFuture.isDone()) {
            deleteUnUsedTextures();
            currentFuture = singleThreadExecutor.submit(new TextureRunnable());
        } else {
            //For debugging
            Log.i("Testing", "Testing");
        }
    }

    private FTTexture textureForKey(String key) {
        synchronized (textureList) {
            int count = textureList.size();
            FTTexture texturre = null;
            for (int i = 0; i < count; i++) {
                FTTexture obj = textureList.get(i);
                if (obj.textureKey.equals(key)) {
                    texturre = obj;
                    break;
                }
            }
            return texturre;
        }
    }

    private void addTexture(FTTexture texture) {
        synchronized (textureList) {
            Integer index = textureList.indexOf(texture);
            if (index != -1) {
                textureList.remove(texture);
            }
            textureList.add(0, texture);
        }
    }

    private void deleteUnUsedTextures() {
        synchronized (textureList) {
            int count = textureList.size();
            while (textureList.size() > 5) {
                FTTexture texture = textureList.get(5);
                FTGLUtils.deleteGLTexture(texture.textureID, true, "Page Background");
                Log.i("Texture Usage", "Texture in deletted 1: " + texture.textureID);
                textureList.remove(5);
            }
        }
    }

    public void deleteAllTextures(String documentID) {
        synchronized (textureOperations) {
            textureOperations.clear();
        }
        synchronized (textureList) {
            ArrayList<FTTexture> texturesToDel = new ArrayList<>();
            int count = textureList.size();
            for (int i = 0; i < count; i++) {
                FTTexture texture = textureList.get(i);
                if ((null == documentID) || (texture.docID.equals(documentID))) {
                    texturesToDel.add(texture);
                    FTGLUtils.deleteGLTexture(texture.textureID, true, "Page Background");
                    Log.i("Texture Usage", "Texture in deletted: " + texture.textureID);
                }
            }
            textureList.removeAll(texturesToDel);
            if (textureList.size() == 0 && null != renderManager) {
                synchronized (renderManager) {
                    renderManager.destroy();
                    renderManager = null;
                }
            }
        }
    }

    private void savePageBackground(Bitmap bitmap, File parentFile, String fileName) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(new Runnable() {
            @Override
            public void run() {
                BitmapUtil.saveBitmap(bitmap, parentFile.getPath(), fileName);
                File[] folderFiles = parentFile.listFiles();

                // Sort files in ascending order base on last modification
                Arrays.sort(folderFiles, new Comparator() {
                    public int compare(Object o1, Object o2) {

                        if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                            return -1;
                        } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                            return +1;
                        } else {
                            return 0;
                        }
                    }

                });
                for (int i = 10; i < folderFiles.length; i++)
                    folderFiles[i].delete();
                es.shutdownNow();
            }
        });
    }

    private float textureScale(float scale) {
        if (scale <= 1.5) {
            return 1f;
        } else if (scale <= 3) {
            return 1.5f;
        }
        return 2f;
    }

    private synchronized Bitmap pageBackgroundImageOfSize(FTNoteshelfPage page, SizeF size) {
        FTPdfDocumentRef pdfRef = page.getFTPdfDocumentRef();
        Bitmap pageBitmap;
        Context context = FTApp.getInstance().getApplicationContext();
        if (pdfRef != null)
            pageBitmap = pdfRef.pageBackgroundImageOfSize(size,
                    page.associatedPDFKitPageIndex - 1,
                    page.getParentDocument().getDocumentVersion(context));
        else {
            pageBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(pageBitmap);
            canvas.drawColor(Color.WHITE);
        }
        return pageBitmap;
    }
}
