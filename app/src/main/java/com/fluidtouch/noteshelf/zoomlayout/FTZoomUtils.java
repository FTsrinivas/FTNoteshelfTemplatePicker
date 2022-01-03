package com.fluidtouch.noteshelf.zoomlayout;

import android.graphics.Rect;
import android.graphics.RectF;

public class FTZoomUtils {
    private FTZoomUtils() {
        // private
    }

    /**
     * Round and set the values on the rectangle
     *
     * @param rect  the rectangle to set
     * @param array the array to read the values from
     */
    public static void setRect(Rect rect, float[] array) {
        setRect(rect, array[0], array[1], array[2], array[3]);
    }

    /**
     * Round and set the values on the rectangle
     *
     * @param rect  the rectangle to set
     * @param array the array to read the values from
     */
    public static void setRect(RectF rect, float[] array) {
        setRect(rect, array[0], array[1], array[2], array[3]);
    }

    /**
     * Round and set the values on the rectangle
     *
     * @param rect the rectangle to set
     * @param l    left
     * @param t    top
     * @param r    right
     * @param b    bottom
     */
    public static void setRect(RectF rect, float l, float t, float r, float b) {
        rect.set(Math.round(l), Math.round(t), Math.round(r), Math.round(b));
    }

    /**
     * Round and set the values on the rectangle
     *
     * @param rect the rectangle to set
     * @param l    left
     * @param t    top
     * @param r    right
     * @param b    bottom
     */
    public static void setRect(Rect rect, float l, float t, float r, float b) {
        rect.set(Math.round(l), Math.round(t), Math.round(r), Math.round(b));
    }

    public static void setArray(float[] array, Rect rect) {
        array[0] = rect.left;
        array[1] = rect.top;
        array[2] = rect.right;
        array[3] = rect.bottom;
    }

    public static void setArray(float[] array, RectF rect) {
        array[0] = rect.left;
        array[1] = rect.top;
        array[2] = rect.right;
        array[3] = rect.bottom;
    }
}
