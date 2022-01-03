package com.fluidtouch.noteshelf.zoomlayout.Gestures;

import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import static java.lang.Math.abs;

public class FTPinchGestureDetector implements ScaleGestureDetector.OnScaleGestureListener {
    private long _scaleGestureStartTime = 0;
    private float previousScale = 1;
    private float recieved = 1;

    private ScaleGestureDetector mScaleGestureDetector;
    private FTGestureRecognizerDelegate delegate;

    private boolean isEnabled = true;
    public FTGestureRecognizerState state;

    private MotionEvent currentEvent;

    public FTPinchGestureDetector(Context context, FTGestureRecognizerDelegate inDelegate) {
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mScaleGestureDetector.setStylusScaleEnabled(true);
        mScaleGestureDetector.setQuickScaleEnabled(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // Quick scale doesn't play nice with the current implementation
            mScaleGestureDetector.setQuickScaleEnabled(false);
        }
        delegate = inDelegate;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean consume = false;
        if (FTMotionEventHelper.hasAnyStylustouch(ev)) {
            setState(FTGestureRecognizerState.FAILED);
            return false;
        }
//        if(ev.getTouchMajor() > FTMotionEventHelper.majorTouchRadius) {
//            return false;
//        }
        if (isEnabled) {
            consume = mScaleGestureDetector.onTouchEvent(ev);
        }
        return consume;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (!isEnabled) {
            return false;
        }
        setState(FTGestureRecognizerState.POSSIBLE);
        if (null != delegate && !delegate.shouldReceiveTouch()) {
            setState(FTGestureRecognizerState.FAILED);
            return false;
        }
        _scaleGestureStartTime = detector.getEventTime();
        previousScale = detector.getScaleFactor();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (state == FTGestureRecognizerState.RECOGNIZED
                || state == FTGestureRecognizerState.FAILED
                || !isEnabled) {
            return false;
        }
        if ((detector.getEventTime() - _scaleGestureStartTime) > 300) {
            setState(FTGestureRecognizerState.FAILED);
            return false;
        }

        float scaleFactor = detector.getScaleFactor();
        if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
            return false;
        }

        float differenceScale = abs(previousScale - scaleFactor);
        if (scaleFactor > 0.3) {
            setState(FTGestureRecognizerState.RECOGNIZED);
            if (null != delegate) {
                delegate.didRecognizedGesture();
            }
        }
        return (state == FTGestureRecognizerState.RECOGNIZED);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        setState(FTGestureRecognizerState.POSSIBLE);
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean getEnabled() {
        return isEnabled;
    }

    public float scale() {
        return mScaleGestureDetector.getScaleFactor();
    }

    private void setState(FTGestureRecognizerState inState) {
        state = inState;
    }
}