package com.fluidtouch.noteshelf.document.imageedit.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


public class StickerImageView extends StickerView {


    public StickerImageView(Context context) {
        super(context);
    }

    public StickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    protected RectF getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(RectF boundingRect) {
        this.boundingRect = boundingRect;
    }

    public void setImageBitmap(Bitmap bmp) {
        this.bitmap = bmp;
    }

    public void setImageResource(int res_id) {
        iv_main.setImageResource(res_id);
    }

    public void setImageDrawable(Drawable drawable) {
        iv_main.setImageDrawable(drawable);
    }

    public void setOnRotationCallback(Callbacks callback) {
        parentCallback = callback;
    }


}
