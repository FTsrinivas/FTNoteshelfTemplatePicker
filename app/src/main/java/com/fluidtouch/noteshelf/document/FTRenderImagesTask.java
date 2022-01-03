package com.fluidtouch.noteshelf.document;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;

import com.fluidtouch.noteshelf.document.views.FTTileImageView;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.renderer.FTOffscreenBitmap;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreenu on 2019-09-12
 */
public class FTRenderImagesTask extends AsyncTask<Void, Bitmap, Void> {
    private Context context;
    private RectF renderingPageRect;
    private boolean isRendering = true;
    private float currentScale;
    private List<FTTileImageView> tileArray = new ArrayList<>();
    private FTNoteshelfPage page;
    private FTRenderManager renderManager;

    public  int pageTexture = 0;

    private boolean pause = false;
    private int index;

    public FTRenderImagesTask(Context context, FTNoteshelfPage page, float currentScale, RectF renderingPageRect, int index) {
        this.page = page;
        this.context = context;
        this.currentScale = currentScale;
        this.renderingPageRect = renderingPageRect;
        this.index = index;
        this.renderManager = FTRenderManagerProvider.getInstance().attachManager(context);
    }

    public void updateDetails(float currentScale, RectF renderingPageRect, int index) {
        this.currentScale = currentScale;
        this.renderingPageRect = renderingPageRect;
        this.index = index;
    }

    @Override
    protected void onPreExecute() {

    }

    @SuppressLint("WrongThread")
    @Override
    protected Void doInBackground(Void... voids) {
//        int texture = page.bgTexture(index);
        while (!pause && isRendering) {
            while (!tileArray.isEmpty() && isRendering) {
                FTTileImageView imageView = tileArray.get(0);
                if (null != imageView) {
                    synchronized (renderManager) {
                        FTOffscreenBitmap offscreenBitmap = renderManager.generateTileImage(page.getPageAnnotations(),
                                pageTexture,
                                false,
                                imageView.getRectF(),
                                currentScale,
                                renderingPageRect,
                                "RenderImageTask image generation");
                        if (offscreenBitmap.image != null) {
                            imageView.setImageBitmap(Bitmap.createBitmap(offscreenBitmap.image));
                            offscreenBitmap.image.recycle();
                            offscreenBitmap.image = null;
                        }
                    }
                }
                if (tileArray.size() > 0) {
                    tileArray.remove(0);
                }
            }
            synchronized (page.pauseWatcher) {
                try {
                    if (isRendering) {
                        pause();
                        page.pauseWatcher.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (renderManager) {
            FTRenderManagerProvider.getInstance().detachManager(renderManager);
        }
        return null;
    }

    public void stop() {
        clearAll();
        isRendering = false;
        if (pause) {
            wakeUp();
        }
    }

    public void addTile(FTTileImageView tile) {
        if (!tileArray.contains(tile)) {
            tileArray.add(tile);
            wakeUp();
        }
    }

    private void pause() {
        pause = true;
    }

    private void wakeUp() {
        synchronized (page.pauseWatcher) {
            pause = false;
            page.pauseWatcher.notify();
        }
    }

    public void clearAll() {
        tileArray.clear();
    }
}
