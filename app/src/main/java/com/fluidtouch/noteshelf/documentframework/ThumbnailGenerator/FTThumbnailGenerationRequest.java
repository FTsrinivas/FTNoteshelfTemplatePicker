package com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.document.FTLock;
import com.fluidtouch.noteshelf.document.FTTextureManager;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.renderer.FTOffscreenBitmap;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;

import java.util.Date;
import java.util.concurrent.Semaphore;

class FTThumbnailGenerationRequest {
    public FTNoteshelfPage page;
    private FTThumbnailGenerationRequestDelegate delegate;
    private Context mContext;
    private FTRenderMode ftRenderMode = FTRenderMode.thumbnailGen;
    private int textureIDToUse = 0;

    static FTThumbnailGenerationRequest thumbnailRequestForPage(Context context, FTNoteshelfPage inPage, FTThumbnailGenerationRequestDelegate inDelegate) {
        FTThumbnailGenerationRequest request = new FTThumbnailGenerationRequest();
        request.page = inPage;
        request.delegate = inDelegate;
        request.mContext = context;
        return request;
    }

    void executeRequest() {
        final FTNoteshelfPage pageToConsider = page;

        final FTRenderManager renderManager = FTThumbnailGenerator.sharedThumbnailGenerator(ftRenderMode).thumbnailOffscreenRenderer(getContext());

        FTLock lock = new FTLock();
        FTTextureManager.sharedInstance().texture(pageToConsider, 1, new FTTextureManager.TextureGenerationCallBack() {
            @Override
            public void onCompletion(int textureID) {
                Log.i("Texture Usage","Texture in use begin: "+textureID);
                textureIDToUse = textureID;
                lock.signal();
            }
        });
        lock.waitTillSignal();

        FTOffscreenBitmap bitmapInfo;
        synchronized (renderManager) {
            bitmapInfo = renderManager.generateFullImage(pageToConsider.getPageAnnotations(),
                    textureIDToUse,
                    pageToConsider.getPageRect(),
                    0.0f,
                    false,
                    1,
                    "Thumbnail image generation");
        }
        Log.i("Texture Usage","Texture in use end: "+textureIDToUse);
        final Bitmap thumbnail = Bitmap.createBitmap(bitmapInfo.image);
        bitmapInfo.image.recycle();
        bitmapInfo.image = null;

        ((AppCompatActivity) getContext()).runOnUiThread(() -> {
            page.thumbnail().updateThumbnail(thumbnail, new Date());//ToDo: this should be in FTThumbnailGenerator
            delegate.didCompleteRequest(FTThumbnailGenerationRequest.this, thumbnail);
        });
    }

    private Context getContext() {
        return mContext;
    }

    public void setFtRenderMode(FTRenderMode mode) {
        ftRenderMode = mode;
    }

    public interface FTThumbnailGenerationRequestDelegate {
        void didCompleteRequest(FTThumbnailGenerationRequest request, Bitmap image);
    }
}
