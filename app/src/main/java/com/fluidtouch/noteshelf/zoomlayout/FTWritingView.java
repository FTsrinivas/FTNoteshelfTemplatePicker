package com.fluidtouch.noteshelf.zoomlayout;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.views.FTDrawingView;
import com.fluidtouch.noteshelf2.R;

public class FTWritingView extends FrameLayout {
    private FTDrawingView _drawingView;
    private float scale;
    private boolean isReady = false;

    public FTWritingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void performLayout(int size, FTDrawingView.DrawingViewCallbacksListener listener) {
        if (isReady) {
            _drawingView = ((FTDocumentActivity) getContext()).getDrawingView(size);
            FrameLayout parent = ((FrameLayout) _drawingView.getParent());
            if (null != parent)
                parent.removeAllViews();
            _drawingView.scale = scale;
            _drawingView.setListeners(listener);
            addView(_drawingView);
            _drawingView.setBackgroundResource(R.drawable.clear_dotted_rectangle);
            setAvoidRenderingForNow(false);
            _drawingView.setIsCurrentPage(true);
        }
    }

    public void setContentScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setReady() {
        this.isReady = true;
    }

    public FTDrawingView getDrawingView() {
//        if (null == _drawingView) {
//            performLayout();
//        }
        return _drawingView;
    }

    public void setAvoidRenderingForNow(boolean avoid) {
        getDrawingView().setAvoidRenderingForNow(avoid);
    }

    public void removeDrawingView() {
        if (_drawingView != null)
            _drawingView.setIsCurrentPage(false);
//        removeView(_drawingView);
    }
}
