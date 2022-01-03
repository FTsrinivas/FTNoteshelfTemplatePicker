package com.fluidtouch.noteshelf.document.search;

import android.graphics.RectF;

/**
 * Test class for calculating Rect
 **/
public class FTRectCalculator {
    public static RectF getActualRect(RectF textRect, RectF cropBox, RectF mediaBox) {
        float diffLeft = Math.abs(cropBox.left - mediaBox.left);
        float diffTop = Math.abs(cropBox.top - mediaBox.top);
        float diffRight = Math.abs(cropBox.right - mediaBox.right);
        float diffBottom = Math.abs(cropBox.bottom - mediaBox.bottom);

        RectF rectF = new RectF();
        rectF.left = Math.abs(textRect.left - diffLeft);
        rectF.top = Math.abs(textRect.top - diffBottom);
        rectF.right = Math.abs(textRect.right - diffRight);
        rectF.bottom = Math.abs(textRect.bottom - diffBottom);
        return rectF;
    }

    public static RectF union(RectF r1, RectF r2) {
        RectF out = new RectF();
        out.left = Math.min(r1.left, r2.left);
        out.top = Math.max(r1.top, r2.top);
        out.right = Math.max(r1.right, r2.right);
        out.bottom = Math.min(r1.bottom, r2.bottom);
        return out;
    }
}