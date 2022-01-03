package com.fluidtouch.noteshelf.document.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public class TileView extends LinearLayout {

    public TileView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public TileView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public TileView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    @Override
    public void removeAllViews() {
        while (getChildCount() > 0) {
            LinearLayout linearLayout = (LinearLayout) getChildAt(0);
            while (linearLayout.getChildCount() > 0) {
                FTTileImageView view = (FTTileImageView) linearLayout.getChildAt(0);
                linearLayout.removeView(view);
            }
            removeView(linearLayout);
        }
    }
}
