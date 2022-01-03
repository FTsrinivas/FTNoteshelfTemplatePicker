package com.fluidtouch.noteshelf.document.search;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

public class FTTextHighlightView extends FrameLayout {

    public FTTextHighlightView(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public FTTextHighlightView(Context context) {
        super(context);
    }

    public void addChildView(RectF rectF, ParentRectInfo parentRectInfo) {
        rectF = FTGeometryUtils.scaleRect(rectF, parentRectInfo.scaleFactor);
        int width = Math.round(Math.abs(rectF.width()));
        int height = Math.round(Math.abs(rectF.height()));
        View childView = new View(getContext());
        childView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        childView.setX(Math.abs(rectF.left));
        childView.setY(Math.abs(rectF.top));
        childView.setBackgroundResource(R.color.text_highlight);
        childView.setAlpha(0.4f);
        addView(childView);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllViews();
    }

    public static class ParentRectInfo {
        public float width, height, scaleFactor;
        public int rotation;
    }
}