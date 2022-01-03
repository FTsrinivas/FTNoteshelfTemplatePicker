package com.fluidtouch.noteshelf.document.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.FTRenderImagesTask;

import java.util.Observer;

public class FTTileImageView extends AppCompatImageView {
    private final Context mContext;
    private FTTileCallback tileCallback = null;
    private String pageUid = "";
    private Bitmap bitmap;
    private RectF rectF;

    Observer onPanning = ((observable, arg) -> {
        if (rectF != null) {
            RectF refreshRect = (RectF) arg;
            if (RectF.intersects(refreshRect, rectF) || refreshRect.contains(rectF) || rectF.contains(refreshRect)) {
                if (bitmap == null & tileCallback.getRenderingTask() != null) {
                    tileCallback.getRenderingTask().addTile(FTTileImageView.this);
                }
            } else {
                if (null != bitmap) {
                    bitmap.recycle();
                }
                bitmap = null;
                setImageBitmap(null);
            }
        }
    });

    Observer onRefresh = ((observable, arg) -> {
        if (rectF != null) {
            RectF refreshRect = (RectF) arg;
            if (RectF.intersects(refreshRect, rectF) || refreshRect.contains(rectF) || rectF.contains(refreshRect)) {
                try {
                    tileCallback.getRenderingTask().addTile(FTTileImageView.this);
                } catch (Exception e) {
                    Log.v("Tile", "Rendering Task - Refresh");
                }
            }
        }
    });

    Observer onScaled = (o, arg) -> {
        float scale = (float) arg;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = (int) (layoutParams.width * scale);//(TILE_LENGTH * scale / previousScale);
        layoutParams.height = (int) (layoutParams.height * scale);//(TILE_LENGTH * scale / previousScale);
    };

    public FTTileImageView(Context context, FTTileCallback tileCallback) {
        super(context);
        mContext = context;
        this.tileCallback = tileCallback;
        setScaleType(ImageView.ScaleType.FIT_XY);
//        setBackgroundColor(Color.GREEN);
//        setPadding(5, 5, 5, 5);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != bitmap) {
            bitmap.recycle();
            setImageBitmap(null);
        }
        ObservingService.getInstance().removeObserver("onPanning" + pageUid, onPanning);
        ObservingService.getInstance().removeObserver("onRefresh" + pageUid, onRefresh);
        ObservingService.getInstance().removeObserver("onScaled" + pageUid, onScaled);
        rectF = null;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        ((Activity) mContext).runOnUiThread(() -> {
            FTTileImageView.super.setImageBitmap(null);
            if (null != bitmap)
                bitmap.recycle();
            bitmap = bm;
            FTTileImageView.super.setImageBitmap(bm);
        });
    }

    public RectF getRectF() {
        if (this.rectF == null) this.rectF = new RectF();
        return this.rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public void setPageUid(String uid) {
        pageUid = uid;
        ObservingService.getInstance().addObserver("onPanning" + pageUid, onPanning);
        ObservingService.getInstance().addObserver("onRefresh" + pageUid, onRefresh);
        ObservingService.getInstance().addObserver("onScaled" + pageUid, onScaled);
    }

    public void generateTiles(RectF refreshRect) {
        if (bitmap == null && (RectF.intersects(refreshRect, rectF) || refreshRect.contains(rectF) || rectF.contains(refreshRect))) {
            tileCallback.getRenderingTask().addTile(FTTileImageView.this);
        }
    }

    public interface FTTileCallback {
        FTRenderImagesTask getRenderingTask();
    }
}
