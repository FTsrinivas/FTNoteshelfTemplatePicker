package com.fluidtouch.noteshelf.zoomlayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.FTRenderImagesTask;
import com.fluidtouch.noteshelf.document.FTTextureManager;
import com.fluidtouch.noteshelf.document.FTTextureManager.TextureGenerationCallBack;
import com.fluidtouch.noteshelf.document.views.FTTileImageView;
import com.fluidtouch.noteshelf.document.views.TileView;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.renderer.offscreenRenderer.FTOffscreenRenderer;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.widget.LinearLayout.HORIZONTAL;

public class FTPageContentHolderView extends FrameLayout implements FTTileImageView.FTTileCallback {
    private final int TILE_LENGTH = FTRenderManager.TILE_LENGTH;
    public RectF visibleRect;
    public boolean requestNewPageBg;
    public boolean generatingPageBg;
    RectF pageRect;
    private ImageView backgroundThumbnailView;

    private FTNoteshelfPage currentPage;
    private FTRenderImagesTask renderImagesTask;

    private Context mContext;
    private String pageUid = "";
    private TileView tileView;
    private Size preSize = new Size(0, 0);
    private CallBacks callBacks;
    private Bitmap pageBitmap;
    private int bgTexture = 0;

    Runnable task = () -> {
        setTileView(this);
    };
    private ExecutorService executorService; // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Observer onThumbnailUpdate = (observable, arg) -> {
        try {
            if (arg instanceof FTPageThumbnail.FTThumbnail) {
                backgroundThumbnailView.setImageBitmap(((FTPageThumbnail.FTThumbnail) arg).getThumbImage());
            }
        } catch (Exception e) {

        }
    };

    public FTPageContentHolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(null != currentPage) {
                    if (visibleRect == null)
                        setTileView(v);
                    else {
                        Size size = new Size(v.getWidth(), v.getHeight());
                        if (!preSize.equals(size)) {
                            if (handler != null)
                                handler.removeCallbacks(task);
                            handler.postDelayed(task, 300);
                        }
                    }
                }
            }
        });
    }

    @Override
    public FTRenderImagesTask getRenderingTask() {
        return renderImagesTask;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (null == backgroundThumbnailView) {
            backgroundThumbnailView = findViewById(R.id.thumbnailView);
        }

        if (null == tileView) {
            tileView = findViewById(R.id.layTileView);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.i("OffscreenCreation", "window is detached");
        handler.removeCallbacks(task);
        if (renderImagesTask != null) {
            renderImagesTask.stop();
            renderImagesTask = null;
        }
        if (pageBitmap != null) {
            pageBitmap.recycle();
            pageBitmap = null;
        }
        if (executorService != null)
            executorService.shutdownNow();
        ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + pageUid, onThumbnailUpdate);
        tileView.removeAllViews();
        backgroundThumbnailView = null;
        tileView = null;
        super.onDetachedFromWindow();
    }

    public void setPage(FTNoteshelfPage page) {
        if (null == currentPage || !currentPage.uuid.equals(page.uuid)) {
            page.setIsinUse(true);
            currentPage = page;
            currentPage.setIsinUse(true);
            pageUid = currentPage.uuid;
            ObservingService.getInstance().addObserver(FTPageThumbnail.strObserver + pageUid, onThumbnailUpdate);
            currentPage.thumbnail().thumbnailImage(getContext());
        }
    }

    public synchronized void updateBackgroundImage() {
        if (null != currentPage && currentPage.isPageDirty()) {
            currentPage.thumbnail().requestNewThumbnail();
        }
    }

    public void setCallBacks(CallBacks callBacks) {
        this.callBacks = callBacks;
    }

    public void setTileView(View v) {
        Size size = new Size(v.getWidth(), v.getHeight());
        if (!preSize.equals(size) && FTDocumentActivity.SCROLL_STATE == ViewPager.SCROLL_STATE_IDLE) {
            pageRect = new RectF(getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight());
            if (visibleRect == null)
                visibleRect = new RectF(getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight());
                preSize = new Size(v.getWidth(), v.getHeight());
            setTiles();
        }
    }

    public void refreshTiles(RectF refreshRect) {
        if (renderImagesTask != null)
            ObservingService.getInstance().postNotification("onRefresh" + pageUid, refreshRect);
    }

    public void refreshTiles(boolean isZoomed, RectF visibleRect) {
        if (isZoomed) {
            this.visibleRect = visibleRect;
        } else if (this.visibleRect != null) {
            if (pageRect != null && !pageRect.equals(visibleRect)) {
                RectF rectF = new RectF();
                float left = pageRect.left - (visibleRect.left > 0 ? 0 : visibleRect.left);
                float top = pageRect.top - (visibleRect.top > 0 ? 0 : visibleRect.top);
                float width = this.visibleRect.width();
                float height = this.visibleRect.height();
                rectF.left = left;
                rectF.top = top;
                rectF.right = rectF.left + width;
                rectF.bottom = rectF.top + height;
                if (renderImagesTask != null)
                    ObservingService.getInstance().postNotification("onPanning" + pageUid, rectF);
            }
        }
    }

    public void setTiles() {
        int columns = (int) (pageRect.width() / TILE_LENGTH + (pageRect.width() % TILE_LENGTH > 0 ? 1 : 0));
        int rows = (int) (pageRect.height() / TILE_LENGTH + (pageRect.height() % TILE_LENGTH > 0 ? 1 : 0));
        if (null == tileView) {
            tileView = new TileView(mContext);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            tileView.setLayoutParams(params);
            addView(tileView, 1);
        }

        FTTextureManager.sharedInstance().texture(currentPage, callBacks.getContainerScale(), new TextureGenerationCallBack() {
            @Override
            public void onCompletion(int textureID) {
                bgTexture = textureID;
                generateTiles(rows, columns,textureID);
            }
        });
    }

    private void generateTiles(int rows, int columns,int textureID) {
        if (tileView == null || currentPage == null)
            return;
        tileView.removeAllViews();
        if (renderImagesTask != null) {
            renderImagesTask.clearAll();
            renderImagesTask.updateDetails(callBacks.getContainerScale(), pageRect, PageBgType.getOrdinal(callBacks.getTextureScale()));
        } else {
            renderImagesTask = new FTRenderImagesTask(mContext, currentPage, callBacks.getContainerScale(), pageRect, PageBgType.getOrdinal(callBacks.getTextureScale()));
        }
        renderImagesTask.pageTexture = textureID;
        if (visibleRect == null)
            visibleRect = new RectF(getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight());
        for (int i = 0; i < rows; i++) {
            LinearLayout wrapper = new LinearLayout(FTPageContentHolderView.this.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            wrapper.setLayoutParams(params);
            wrapper.setOrientation(HORIZONTAL);
            for (int j = 0; j < columns; j++) {
                float left = (j * TILE_LENGTH) + pageRect.left;
                float top = (i * TILE_LENGTH) + pageRect.top;
                RectF rectF = new RectF(left, top, left + TILE_LENGTH, top + TILE_LENGTH);
                FTTileImageView image = new FTTileImageView(mContext, FTPageContentHolderView.this);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(TILE_LENGTH, TILE_LENGTH);
                image.setLayoutParams(params2);
                image.setImageBitmap(null);
                image.setPageUid(pageUid);
                image.setRectF(rectF);
                image.generateTiles(visibleRect);
                wrapper.addView(image);
            }
            tileView.addView(wrapper);
        }
        if (renderImagesTask.getStatus() != AsyncTask.Status.RUNNING && renderImagesTask.getStatus() != AsyncTask.Status.FINISHED) {
            renderImagesTask.executeOnExecutor(Executors.newSingleThreadExecutor());
        }
    }

    public void removeHandler() {
        if (handler != null)
            handler.removeCallbacks(task);
    }

    public void setContentScale(float scale) {
        ObservingService.getInstance().postNotification("onScaled" + pageUid, scale);
    }

    public RectF getPageRect() {
        return pageRect;
    }

    public synchronized void savePageBackground(Bitmap bitmap, File parentFile, String fileName) {
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

    public int getBGTexturre() {
        return  bgTexture;
    }

    public enum PageBgType {
        NORMAL, MEDIUM, HIGH;

        public static int getOrdinal(float scale) {
            if (scale == 1f) {
                return NORMAL.ordinal();
            } else if (scale == 1.5f) {
                return MEDIUM.ordinal();
            } else {
                return HIGH.ordinal();
            }
        }
    }

    public interface CallBacks {
        float getContainerScale();

        float getTextureScale();

        RectF visibleFrame();

        RectF getVisibleRect();

        Activity getActivity();
    }
}
