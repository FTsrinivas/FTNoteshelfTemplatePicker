package com.fluidtouch.noteshelf.zoomlayout.Gestures;

import android.view.MotionEvent;

public class FTMotionEventHelper {
    static float majorTouchRadius = 30;

    public static boolean hasAnyStylustouch(MotionEvent e) {
        int i = 0;
        int count = e.getPointerCount();

        boolean hasStylusTouch = false;
        for (i = 0; i < count; i++) {
            if (e.getToolType(i) == MotionEvent.TOOL_TYPE_STYLUS) {
                hasStylusTouch = true;
                break;
            }
        }
        return hasStylusTouch;
    }


}
