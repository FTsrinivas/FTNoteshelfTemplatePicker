package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.templatepicker.FTAppConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.offscreenRenderer.FTThumbnailOffScreenRenderer;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtil {

    private BitmapUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Bitmap getBitmap(Uri uri) {
        try {
            if (FTFileManagerUtil.isFileExits(uri.getPath()))
                return BitmapFactory.decodeFile(uri.getPath());
        } catch (Exception exception) {
            Log.i(BitmapUtil.class.getName(), exception.toString());
        }
        return null;
    }

    public static Bitmap getRoundedCornerBitmap(Context context, Bitmap bitmap, float cornerRadiusInDps) {
        float pixels = cornerRadiusInDps * context.getResources().getDisplayMetrics().density;
        if (bitmap == null) {
            return null;
        }
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(rectF, pixels, pixels, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        bitmap = null;

        return output;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean isRecycle) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        if (isRecycle)
            bm.recycle();
        return resizedBitmap;
    }

    public static Bitmap getMergedBitmap(Context context, Bitmap back, Bitmap front, int top) {
        Bitmap bitmapMerged = Bitmap.createBitmap(back.getWidth(), back.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapMerged);
        int widthBack = back.getWidth();
        int widthFront = front.getWidth();

       /* canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, 0f, 0f, null);*/
        Canvas c = new Canvas(bitmapMerged);
        c.drawColor(Color.TRANSPARENT);
        Resources res = context.getResources();

        Drawable drawable1 = new BitmapDrawable(res, back);
        Drawable drawable2 = new BitmapDrawable(res, front);

        top = back.getHeight() / 4;

        drawable1.setBounds(0, 0, back.getWidth(), back.getHeight());
        drawable2.setBounds(0, top, front.getWidth(), back.getHeight() - top);
        drawable1.draw(c);
        drawable2.draw(c);

        FTAppConfig.saveImageInDummy(bitmapMerged, "bitmapMerged");
        return bitmapMerged;
    }

    public static Bitmap getTransparentMergedBitmap(Context context, Bitmap bitmap1, Bitmap bitmap2, int top) {
        Bitmap bitmapMerged = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmapMerged);
        c.drawColor(Color.WHITE);
        Resources res = context.getResources();

        Drawable drawable1 = new BitmapDrawable(res, bitmap1);
        Drawable drawable2 = new BitmapDrawable(res, bitmap2);

        drawable1.setBounds(0, 0, bitmap1.getWidth(), bitmap1.getHeight());
        drawable2.setBounds(0, top, bitmap2.getWidth(), top + bitmap2.getHeight());
        drawable1.draw(c);
        drawable2.draw(c);
        return bitmapMerged;
    }


    public static Bitmap cropBitmap(Bitmap bitmap, RectF rect) {
        if (rect.left < 0) {
            rect.left = 0;
        }
        if (rect.top < 0) {
            rect.top = 0;
        }
        if (rect.bottom > bitmap.getHeight()) {
            rect.bottom = bitmap.getHeight();
        }
        if (rect.right > bitmap.getWidth()) {
            rect.right = bitmap.getWidth();
        }
        return Bitmap.createBitmap(bitmap, (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
    }

    public static Bitmap scaleBitmap(Bitmap bitmapToScale, RectF rect) {
        float newWidth = rect.width();
        float newHeight = rect.height();
        if (rect.left < 0) {
            newWidth = rect.right - 0;
        }

        if (rect.top < 0) {
            newHeight = rect.bottom - 0;
        }
        if (bitmapToScale == null)
            return null;
        //get the original mInitialWidth and mInitialHeight
        int width = bitmapToScale.getWidth();
        int height = bitmapToScale.getHeight();
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(newWidth / width, newHeight / height);

        // recreate the new Bitmap and set it back
        return Bitmap.createBitmap(bitmapToScale, 0, 0, bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix, true);
    }

    public static boolean saveBitmap(Bitmap finalBitmap, String path, String name) {
        File myDir = new File(path);
        if (!myDir.exists())
            myDir.mkdirs();
        File file = new File(myDir, name);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Bitmap decodeByteArray(byte[] src, int w, int h) {
        try {
            // calculate sample size based on w/h
            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(src, 0, src.length, opts);
            if (opts.mCancel || opts.outWidth == -1 || opts.outHeight == -1) {
                return null;
            }
            opts.inSampleSize = Math.min(opts.outWidth / w, opts.outHeight / h);
            opts.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(src, 0, src.length, opts);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Decode an image into a Bitmap, using sub-sampling if the desired dimensions call for it.
     * Also applies a center-crop a la {@link android.widget.ImageView.ScaleType#CENTER_CROP}.
     *
     * @param src an encoded image
     * @param w   desired width in px
     * @param h   desired height in px
     * @return an exactly-sized decoded Bitmap that is center-cropped.
     */
    public static Bitmap decodeByteArrayWithCenterCrop(byte[] src, int w, int h) {
        try {
            final Bitmap decoded = decodeByteArray(src, w, h);
            return centerCrop(decoded, w, h);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Returns a new Bitmap copy with a center-crop effect a la
     * {@link android.widget.ImageView.ScaleType#CENTER_CROP}. May return the input bitmap if no
     * scaling is necessary.
     *
     * @param src original bitmap of any size
     * @param w   desired width in px
     * @param h   desired height in px
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    public static Bitmap centerCrop(final Bitmap src, final int w, final int h) {
        return crop(src, w, h, 0.5f, 0.5f);
    }

    /**
     * Returns a new Bitmap copy with a crop effect depending on the crop anchor given. 0.5f is like
     * {@link android.widget.ImageView.ScaleType#CENTER_CROP}. The crop anchor will be be nudged
     * so the entire cropped bitmap will fit inside the src. May return the input bitmap if no
     * scaling is necessary.
     * <p>
     * <p>
     * Example of changing verticalCenterPercent:
     * _________            _________
     * |         |          |         |
     * |         |          |_________|
     * |         |          |         |/___0.3f
     * |---------|          |_________|\
     * |         |<---0.5f  |         |
     * |---------|          |         |
     * |         |          |         |
     * |         |          |         |
     * |_________|          |_________|
     *
     * @param src                     original bitmap of any size
     * @param w                       desired width in px
     * @param h                       desired height in px
     * @param horizontalCenterPercent determines which part of the src to crop from. Range from 0
     *                                .0f to 1.0f. The value determines which part of the src
     *                                maps to the horizontal center of the resulting bitmap.
     * @param verticalCenterPercent   determines which part of the src to crop from. Range from 0
     *                                .0f to 1.0f. The value determines which part of the src maps
     *                                to the vertical center of the resulting bitmap.
     * @return a copy of src conforming to the given width and height, or src itself if it already
     * matches the given width and height
     */
    public static Bitmap crop(final Bitmap src, final int w, final int h,
                              final float horizontalCenterPercent, final float verticalCenterPercent) {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0
                || verticalCenterPercent > 1) {
            throw new IllegalArgumentException(
                    "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                            + "1.0f, inclusive.");
        }
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src;
        }
        final Matrix m = new Matrix();
        final float scale = Math.max(
                (float) w / srcWidth,
                (float) h / srcHeight);
        m.setScale(scale, scale);
        final int srcCroppedW, srcCroppedH;
        int srcX, srcY;
        srcCroppedW = Math.round(w / scale);
        srcCroppedH = Math.round(h / scale);
        srcX = (int) (srcWidth * horizontalCenterPercent - srcCroppedW / 2);
        srcY = (int) (srcHeight * verticalCenterPercent - srcCroppedH / 2);
        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);
        final Bitmap cropped = Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */);
        return cropped;
    }

    public static String addAlpha(String originalColor, double alpha) {
        long alphaFixed = Math.round(alpha * 255);
        String alphaHex = Long.toHexString(alphaFixed);
        if (alphaHex.length() == 1) {
            alphaHex = "0" + alphaHex;
        }
        originalColor = originalColor.replace("#", "#" + alphaHex);


        return originalColor;
    }

    public static Bitmap getTempThumbnailBitmap(Context context, RectF pdfPageRect) {
        float aspectRatio = pdfPageRect.width() / pdfPageRect.height();
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.thumbnail_temp);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(FTThumbnailOffScreenRenderer.maxTextureSize, (int) (FTThumbnailOffScreenRenderer.maxTextureSize / aspectRatio), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, FTThumbnailOffScreenRenderer.maxTextureSize, (int) (FTThumbnailOffScreenRenderer.maxTextureSize / aspectRatio));
        drawable.draw(canvas);

        return bitmap;
    }
}