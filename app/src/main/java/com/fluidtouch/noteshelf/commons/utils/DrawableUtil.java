package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class DrawableUtil {
    private DrawableUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void setGradientDrawableColor(View view, Object color, int position) {
        try {
            int finalColor = 0;
            if (color instanceof String)
                finalColor = Color.parseColor((String) color);
            else if (color instanceof Integer)
                finalColor = (int) color;
            if (view.getBackground() instanceof GradientDrawable) {
                GradientDrawable bgView = (GradientDrawable) view.getBackground();
                bgView.setColor(finalColor);
            } else if (view.getBackground() instanceof BitmapDrawable) {
                BitmapDrawable bgShape = (BitmapDrawable) view.getBackground();
                bgShape.setColorFilter(finalColor, PorterDuff.Mode.SRC_ATOP);
            } else if (view.getBackground() instanceof LayerDrawable) {
                LayerDrawable bgShape = (LayerDrawable) view.getBackground();
                bgShape.getDrawable(position).setColorFilter(finalColor, PorterDuff.Mode.SRC_ATOP);
            }
        } catch (Exception e) {
            Log.i(DrawableUtil.class.getName(), e.getMessage());
        }
    }

    public static void tintImage(Context context, View view, Object color, int resource) {
        if (context == null)
            return;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resource);
        int finalColor = 0;
        if (color instanceof String)
            finalColor = Color.parseColor((String) color);
        else if (color instanceof Integer)
            finalColor = (int) color;
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(finalColor, PorterDuff.Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (view instanceof ImageView)
            ((ImageView) view).setImageBitmap(bitmapResult);
    }
}
