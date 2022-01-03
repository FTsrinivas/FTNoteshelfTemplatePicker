package com.fluidtouch.noteshelf.scandocument.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.scandocument.PolygonView;
import com.fluidtouch.noteshelf.scandocument.ScannedImageModel;
import com.fluidtouch.noteshelf.scandocument.util.NativeClass;
import com.fluidtouch.noteshelf2.R;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 22/05/19
 */
public class CropImageFragment extends Fragment {
    @BindView(R.id.fragment_crop_image_cropping_image_view)
    protected ImageView imageView;
    @BindView(R.id.fragment_crop_image_cropping_image_holder)
    protected FrameLayout holderImageCrop;
    @BindView(R.id.fragment_crop_image_polygon_view)
    protected PolygonView polygonView;

    private Bitmap mSelectedImageBitmap;
    private NativeClass nativeClass;
    private CropImageContainerCallBack mContainerCallback;
    private ScannedImageModel mImageModel;

    public static CropImageFragment newInstance(ScannedImageModel imageModel, CropImageContainerCallBack containerCallback) {
        CropImageFragment fragment = new CropImageFragment();
        fragment.mImageModel = imageModel;
        fragment.mContainerCallback = containerCallback;
        return fragment;
    }

    public static Bitmap createContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.red(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.red(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static Bitmap enhanceImage(Bitmap mBitmap, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap mEnhancedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap
                .getConfig());
        Canvas canvas = new Canvas(mEnhancedBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(mBitmap, 0, 0, paint);
        return mEnhancedBitmap;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        initializeElement();
    }

    @OnClick(R.id.fragment_crop_image_cancel_text_view)
    void cancelEditing() {
        assert getFragmentManager() != null;
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @OnClick(R.id.fragment_crop_image_keep_scan_text_view)
    void saveCroppedImage(View view) {
        if (!polygonView.isValidShape(polygonView.getPoints())) {
            Toast.makeText(getContext(), R.string.cropped_image_not_valid_shape, Toast.LENGTH_SHORT).show();
            return;
        }
        view.setClickable(false);
        view.setEnabled(false);
        FTSmartDialog dialog = new FTSmartDialog();
        dialog.setMessage(getString(R.string.cropping_image));
        dialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
        dialog.show(getFragmentManager());

        try (FileOutputStream out = new FileOutputStream(mImageModel.croppedImagePath)) {
            enhanceImage(getCroppedImage(), (float) 1.1, 50).compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mContainerCallback.keepScannedImage(mImageModel);
        getFragmentManager().beginTransaction().remove(this).commit();
        dialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContainerCallback != null) mContainerCallback.openCamera();
    }

    /**
     * For testing
     */
    private void saveImages() {
        float contrast = (float) 1.0;
        for (int i = 0; i < 10; i++) {
            contrast = contrast + (float) 0.1;
            try (FileOutputStream out = new FileOutputStream(FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/pic_" + contrast + ".png")) {
                enhanceImage(BitmapFactory.decodeFile(mImageModel.croppedImagePath), contrast, 50).compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fragment_crop_image_reload_image_view)
    void reloadImage() {
        cancelEditing();
    }

    private void initializeElement() {
        nativeClass = new NativeClass();

        holderImageCrop.post(this::initializeCropping);
    }

    private void initializeCropping() {
        if (mImageModel == null)
            return;
        mSelectedImageBitmap = BitmapFactory.decodeFile(mImageModel.originalImagePath);

        Bitmap scaledBitmap = scaledBitmap(mSelectedImageBitmap, holderImageCrop.getWidth(), holderImageCrop.getHeight());
        imageView.setImageBitmap(scaledBitmap);

        Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        if (mImageModel.croppingPoints.size() == 0) {
            mImageModel.croppingPoints = getEdgePoints(scaledBitmap);
        }

        polygonView.setPoints(mImageModel.croppingPoints);
        polygonView.setVisibility(View.VISIBLE);

        int padding = (int) getResources().getDimension(R.dimen.scanPadding);


//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(scaledBitmap.getWidth(), scaledBitmap.getHeight());
//        layoutParams.gravity = Gravity.CENTER;
//
//        polygonView.setLayoutParams(layoutParams);

        ((ImageView) getView().findViewById(R.id.crop_image_dummy_image_view)).setImageBitmap(tempBitmap);
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Log.v("CropImage", width + " " + height);
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        return orderedValidEdgePoints(tempBitmap, pointFs);
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        MatOfPoint2f point2f = nativeClass.getPoint(tempBitmap);
        List<PointF> result = new ArrayList<>();
        if (point2f == null) {
            Map<Integer, PointF> points = getOutlinePoints(tempBitmap);
            for (int i = 0; i < 4; i++) {
                result.add(points.get(i));
            }

        } else {
            List<Point> points = Arrays.asList(point2f.toArray());
            for (int i = 0; i < points.size(); i++) {
                result.add(new PointF(((float) points.get(i).x), ((float) points.get(i).y)));
            }
        }

        return result;
    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        int width = 200;
        outlinePoints.put(0, new PointF(width, width));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth() - width, width));
        outlinePoints.put(2, new PointF(width, tempBitmap.getHeight() - width));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth() - width, tempBitmap.getHeight() - width));
        return outlinePoints;
    }

    private Bitmap getCroppedImage() {
        mImageModel.croppingPoints = polygonView.getPoints();
        Map<Integer, PointF> points = polygonView.getPoints();

        float xRatio = (float) mSelectedImageBitmap.getWidth() / imageView.getWidth();
        float yRatio = (float) mSelectedImageBitmap.getHeight() / imageView.getHeight();

        float x1 = (points.get(0).x) * xRatio + 20;
        float x2 = (points.get(1).x) * xRatio + 20;
        float x3 = (points.get(2).x) * xRatio + 20;
        float x4 = (points.get(3).x) * xRatio + 20;
        float y1 = (points.get(0).y) * yRatio + 20;
        float y2 = (points.get(1).y) * yRatio + 20;
        float y3 = (points.get(2).y) * yRatio + 20;
        float y4 = (points.get(3).y) * yRatio + 20;

        return nativeClass.getScannedBitmap(mSelectedImageBitmap, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public interface CropImageContainerCallBack {
        void keepScannedImage(ScannedImageModel imageModel);

        void removeScannedImage();

        void openCamera();
    }
}
