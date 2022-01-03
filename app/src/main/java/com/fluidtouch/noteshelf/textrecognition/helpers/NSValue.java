package com.fluidtouch.noteshelf.textrecognition.helpers;

import android.graphics.RectF;

public class NSValue {
    public double x = 0.0;
    public double y = 0.0;
    public double width = 0.0;
    public double height = 0.0;

    public NSValue() {
        super();
    }

    public NSValue(RectF rect) {
        super();
        this.x = rect.left;
        this.y = rect.top;

        this.width = rect.right - rect.left;
        this.height = rect.bottom - rect.top;
    }

    public RectF cgRectValue() {
        RectF rect = new RectF();
        rect.left = (float) x;
        rect.top = (float) y;
        rect.right = (float) (x + width);
        rect.bottom = (float) (y + height);
        return rect;
    }
}
