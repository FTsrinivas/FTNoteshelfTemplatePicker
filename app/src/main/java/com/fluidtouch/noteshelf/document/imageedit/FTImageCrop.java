package com.fluidtouch.noteshelf.document.imageedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fluidtouch.noteshelf.document.imageedit.crop.CropImageView;
import com.fluidtouch.noteshelf.document.imageedit.crop.HighlightView;
import com.fluidtouch.noteshelf.document.imageedit.crop.RotateBitmap;
import com.fluidtouch.noteshelf2.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FTImageCrop implements HighlightView.OnCropViewChange {

    private Context mContext;
    private CropImageView imageView;
    private HighlightView cropView;
    private RotateBitmap mBitmap;
    private float mScale = 1;
    private boolean isEdited = false;
    private onImageCropCallbacks mLstener;
    public FTImageCrop(Context context) {
        mContext = context;
    }

    public void setUP() {
        imageView = new CropImageView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = mBitmap.getWidth();
        layoutParams.height = mBitmap.getHeight();
        imageView.setLayoutParams(layoutParams);
        mLstener.addView(imageView);
        imageView.highlightViews.clear();
        imageView.setBackgroundResource(R.drawable.checks_background);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageRotateBitmapResetBase(mBitmap, true);
        new Cropper().crop();
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = new RotateBitmap(bitmap, 0);
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setListener(onImageCropCallbacks listener) {
        mLstener = listener;
    }

    @Override
    public void onCropViewChange() {
        mLstener.onEdited();
    }

    public Bitmap getCropedBitmap() {
        if (cropView == null) {
            return null;
        }

        Bitmap croppedImage;
        Rect r = cropView.getScaledCropRect(1);
        int width = r.width();
        int height = r.height();

        int outWidth = width;
        int outHeight = height;

        try {
            croppedImage = decodeRegionCrop(r, outWidth, outHeight);
        } catch (IllegalArgumentException e) {
            return null;
        }
        isEdited = false;
        return croppedImage;
    }

    private Bitmap decodeRegionCrop(Rect rect, int outWidth, int outHeight) {
        // Release memory now

        Bitmap croppedImage = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(bs, false);
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();


            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
                if (croppedImage != null && (rect.width() > outWidth || rect.height() > outHeight)) {
                    Matrix matrix = new Matrix();
                    matrix.postScale((float) outWidth / rect.width(), (float) outHeight / rect.height());
                    croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix, true);
                }
            } catch (IllegalArgumentException e) {
                // Rethrow with some extra information
                throw new IllegalArgumentException("Rectangle " + rect + " is outside of the image ("
                        + width + "," + height + ", )", e);
            }

        } catch (IOException e) {

        } catch (OutOfMemoryError e) {

        }
        return croppedImage;

    }

    public interface onImageCropCallbacks {
        void addView(View view);

        void onEdited();
    }

    private class Cropper {

        private void makeDefault() {
            if (mBitmap == null) {
                return;
            }
            HighlightView hv = new HighlightView(imageView);
            hv.scale = mScale;
            final int width = mBitmap.getWidth();
            final int height = mBitmap.getHeight();
            Rect imageRect = new Rect(0, 0, width, height);
            RectF cropRect = new RectF(0, 0, width, height);
            hv.setup(imageView.getUnrotatedMatrix(), imageRect, cropRect, false, FTImageCrop.this);
            imageView.add(hv);
        }

        public void crop() {
            new Handler().post(new Runnable() {
                public void run() {
                    makeDefault();
                    imageView.invalidate();
                    if (imageView.highlightViews.size() == 1) {
                        cropView = imageView.highlightViews.get(0);
                        cropView.setFocus(true);
                    }
                }
            });
        }
    }

}
