package com.fluidtouch.noteshelf.zoomlayout.Gestures;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.renderingengine.annotation.FTAnnotationUtils;

import static java.lang.Math.sqrt;
import static java.lang.StrictMath.abs;

public class FTPanGestureRecognizer implements GestureDetector.OnGestureListener {

    private GestureDetector panGestureDetector;
    public FTGestureRecognizerState state;
    private boolean isEnabled = true;
    private FTGestureRecognizerDelegate delegate;

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean getEnabled() {
        return isEnabled;
    }

    private PointF touchBeganPoint1;
    private PointF touchBeganPoint2;

    private long touchDownTimer = 0;

    public FTPanGestureRecognizer(Context context, FTGestureRecognizerDelegate inDelegate) {
        panGestureDetector = new GestureDetector(context, this);
        panGestureDetector.setIsLongpressEnabled(false);
        delegate = inDelegate;
    }

    private void reset() {
        state = FTGestureRecognizerState.POSSIBLE;
        touchDownTimer = 0;
    }

    public boolean onTouchEvent(MotionEvent ev, boolean allowsFreeScroll) {
        boolean consume = false;
//        if(ev.getTouchMajor() > FTMotionEventHelper.majorTouchRadius) {
//            return false;
//        }
        if (FTMotionEventHelper.hasAnyStylustouch(ev) && !allowsFreeScroll) {
            state = FTGestureRecognizerState.FAILED;
            return false;
        }
        if (isEnabled) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                if (touchDownTimer == 0) {
                    touchDownTimer = ev.getDownTime();
                }
                if (allowsFreeScroll || ev.getPointerCount() == 2) {
                    long currentTime = ev.getDownTime();
                    if (currentTime - touchDownTimer > 300) {
                        state = FTGestureRecognizerState.FAILED;
                    } else if (FTMotionEventHelper.hasAnyStylustouch(ev) && !allowsFreeScroll) {
                        state = FTGestureRecognizerState.FAILED;
                    } else {
                        if (allowsFreeScroll) {
                            state = FTGestureRecognizerState.POSSIBLE;
                            consume = panGestureDetector.onTouchEvent(ev);
                        } else {
                            touchBeganPoint1 = point(ev, 0);
                            touchBeganPoint2 = point(ev, 1);
                            float distamce = FTAnnotationUtils.distanceBetween2Points(touchBeganPoint1, touchBeganPoint2);

                            float density = FTApp.getInstance().getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
                            distamce = distamce / density;

                            if (distamce > 200) {
                                state = FTGestureRecognizerState.FAILED;
                            } else {
                                state = FTGestureRecognizerState.POSSIBLE;
                                consume = panGestureDetector.onTouchEvent(ev);
                            }
                        }
                    }
                }
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE && ((ev.getPointerCount() == 2) || allowsFreeScroll)) {
                consume = panGestureDetector.onTouchEvent(ev);
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                consume = panGestureDetector.onTouchEvent(ev);
                reset();
            } else if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
                consume = panGestureDetector.onTouchEvent(ev);
                reset();
            }
        }
        return consume;
    }

    private PointF point(MotionEvent ev, int index) {
        MotionEvent.PointerCoords pCords = new MotionEvent.PointerCoords();
        ev.getPointerCoords(index, pCords);
        PointF p1 = new PointF();
        p1.x = pCords.x;
        p1.y = pCords.y;
        return p1;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (state == FTGestureRecognizerState.RECOGNIZED
                || state == FTGestureRecognizerState.FAILED
                || !isEnabled) {
            return false;
        }

        float absDistanceX = abs(distanceX);
        float absDistanceY = abs(distanceY);
        float dist = distance(absDistanceX, absDistanceY);
        if (absDistanceX >= 10 || absDistanceY > 10) {
            if (dist > 100) {
                state = FTGestureRecognizerState.FAILED;
                Log.i("PANTEST", "Failed distance: " + distanceX + " y: " + distanceY + " dist " + dist);
            } else {
                state = FTGestureRecognizerState.RECOGNIZED;
                if (null != delegate) {
                    delegate.didRecognizedGesture();
                }
                Log.i("PANTEST", "Success distance: " + distanceX + " y: " + distanceY + " dist " + dist);
            }
        } else {
            Log.i("PANTEST", "distance: " + distanceX + " y: " + distanceY + " dist " + dist);
        }
        return (state == FTGestureRecognizerState.RECOGNIZED);
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    //temp fix
    public static float distance(float dx, float dy) {
        return (float) sqrt(dx * dx + dy * dy);
    }
}