package com.fluidtouch.noteshelf.document.views;

import android.content.Context;
import android.graphics.Bitmap;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;

class FTTileGenerationRequest {
    public FTNoteshelfPage page;
    FTTileImageView ftTileImageView;
    FTRenderManager renderManager;
    float scale = 1;
    private Context mContext;

    static FTTileGenerationRequest thumbnailRequestForPage(Context context, FTNoteshelfPage inPage, FTTileImageView ftTileImageView, FTRenderManager renderManager, float scale) {
        FTTileGenerationRequest request = new FTTileGenerationRequest();
        request.page = inPage;
        request.mContext = context;
        request.ftTileImageView = ftTileImageView;
        request.renderManager = renderManager;
        request.scale = scale;
        return request;
    }

    void executeRequest() {
//        FTOffscreenBitmap offscreenBitmap = renderManager.getImageForAnnotations(page.getPageAnnotations(), null,
//                page.getPageRect(), 0.0f, true, ftTileImageView.getRectF(), scale);
//        ((AppCompatActivity) getContext()).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ftTileImageView.setImageBitmap(offscreenBitmap.image);
//            }
//        });

    }

    private Context getContext() {
        return mContext;
    }

    public interface FTTileGenerationRequestDelegate {
        void didCompleteRequest(FTTileGenerationRequest request, Bitmap image);
    }

}
