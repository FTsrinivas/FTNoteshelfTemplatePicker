package com.fluidtouch.noteshelf.zoomlayout;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.document.FTDocumentPageFragment;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTMotionEventHelper;

public class FTZoomLayout extends FTZoomableLayout {
    FTDocumentPageFragment.ZoomTouchListener mZoomTouchListener;
    private Context mContext;
    private boolean fromOutside = false;
    private int visibleRectOffset = 50;

    public FTZoomLayout(Context context) {
        super(context);
        init(context);
    }

    public FTZoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setZoomTouchListener(FTDocumentPageFragment.ZoomTouchListener zoomTouchListener) {
        mZoomTouchListener = zoomTouchListener;
    }

    public void setCallbacksListener(FTZoomLayoutContainerCallback containerCallback) {
        this.mContainerCallback = containerCallback;
    }

    private void init(Context context) {
        mContext = context;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean consumed = false;
        float x = ev.getX();
        float y = ev.getY();

        View zoomableView = getChildAt(0);
        RectF visibleRect = new RectF(zoomableView.getLeft() - visibleRectOffset, zoomableView.getTop() - visibleRectOffset, zoomableView.getRight() + visibleRectOffset, zoomableView.getBottom() + visibleRectOffset);
        if (visibleRect.contains(x, y)) {
            if (mZoomTouchListener != null) {
                if (fromOutside) {
                    int currentAction = ev.getAction();
                    ev.setAction(MotionEvent.ACTION_DOWN);
                    mZoomTouchListener.onTouch(ev);
                    fromOutside = false;
                    ev.setAction(currentAction);
                }
                mZoomTouchListener.onTouch(ev);
            }
            consumed = super.onTouchEvent(ev);
        } else {
            if (!fromOutside) {
                fromOutside = true;
                if (mZoomTouchListener != null) {
                    ev.setAction(MotionEvent.ACTION_UP);
                    mZoomTouchListener.onTouch(ev);
                }
            }

            mZoomTouchListener.onOutsideTouch(ev);

        }
        return true;
    }

    @Override
    public boolean allowsFreeScroll(MotionEvent event) {
        boolean stylusEnabled = FTApp.getPref().isStylusEnabled();

        if ((mContainerCallback.currentMode() == FTToolBarTools.VIEW))
            return true;
        else if (stylusEnabled && !FTMotionEventHelper.hasAnyStylustouch(event))
            return true;
        else return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public interface FTZoomLayoutContainerCallback {
        FTToolBarTools currentMode();

        void setAudioMode(boolean enabled);

        boolean isInsideAudio(MotionEvent event);

        float getOriginalScale();
    }
}
