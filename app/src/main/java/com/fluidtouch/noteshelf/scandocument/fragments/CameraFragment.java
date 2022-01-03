package com.fluidtouch.noteshelf.scandocument.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.PdfUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.scandocument.CameraPreviewsAdapter;
import com.fluidtouch.noteshelf.scandocument.PolygonView;
import com.fluidtouch.noteshelf.scandocument.ScannedImageModel;
import com.fluidtouch.noteshelf.scandocument.util.NativeClass;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf2.R;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 16/05/19
 */
public class CameraFragment extends Fragment implements CameraPreviewsAdapter.CameraPreviewsContainerCallback {
    private static final String TAG = "FTCamera";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    protected CaptureRequest captureRequest;
    @BindView(R.id.fragment_camera_preview_recycler_view)
    protected RecyclerView mPreviewRecyclerView;
    @BindView(R.id.camera_fragment_texture_view)
    protected TextureView mTextureView;
    @BindView(R.id.fragment_camera_polygon_view)
    protected PolygonView mPolygonView;
    private String cameraId;
    private NativeClass nativeClass;
    private Size imageDimension;
    private boolean mFlashSupported;
    private CameraPreviewsAdapter mCameraPreviewsAdapter;

    private boolean mIsFlashOn = false;
    private File file;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private CaptureImageTask captureImageTask;

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Will inform later
        }
    };
    private CropImageFragment.CropImageContainerCallBack mCropImageContainerCallBack = new CropImageFragment.CropImageContainerCallBack() {
        @Override
        public void keepScannedImage(ScannedImageModel imageModel) {
            if (mCameraPreviewsAdapter.getAll().contains(imageModel)) {
                mCameraPreviewsAdapter.notifyDataSetChanged();
            } else {
                mCameraPreviewsAdapter.add(imageModel);
            }
        }

        @Override
        public void removeScannedImage() {
            //Soon
        }

        @Override
        public void openCamera() {
            CameraFragment.this.onResume();
        }
    };
    //endregion
    private ImageReader takePictureReader;
    //endregion
    private boolean allow = true;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (cameraDevice != null) {
                cameraDevice.close();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }
    };
    private CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(getContext(), getString(R.string.saved) + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        nativeClass = new NativeClass();
        mTextureView.setSurfaceTextureListener(textureListener);

        mCameraPreviewsAdapter = new CameraPreviewsAdapter(this);
        mPreviewRecyclerView.setAdapter(mCameraPreviewsAdapter);
    }

    @OnClick(R.id.fragment_camera_import_text_view)
    void importImages(View view) {
        if (mCameraPreviewsAdapter.getAll().isEmpty()) {
            Toast.makeText(getContext(), R.string.no_scanned_items_found, Toast.LENGTH_SHORT).show();
            return;
        }
        view.setClickable(false);
        view.setEnabled(false);
        List<ScannedImageModel> models = mCameraPreviewsAdapter.getAll();
        List<String> filePaths = new ArrayList<>();
        for (int i = 0; i < models.size(); i++) {
            filePaths.add(models.get(i).croppedImagePath);
        }
        String pdfPath = FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/".concat(getString(R.string.scanned_note)) + FTConstants.PDF_EXTENSION;
        PdfUtil.createPdf(filePaths, pdfPath);

        Intent intent = new Intent();
        intent.putExtra(getString(R.string.intent_scanned_doc_path), pdfPath);
        requireActivity().setResult(FTBaseShelfActivity.SCAN_DOCUMENT, intent);
        getActivity().finish();
    }

    @OnClick(R.id.fragment_camera_cancel_text_view)
    void cancelScanning() {
        requireActivity().finish();
    }

    @OnClick(R.id.fragment_camera_flash_image_view)
    void toggleFlash(View view) {
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, mIsFlashOn ? CameraMetadata.FLASH_MODE_OFF : CameraMetadata.FLASH_MODE_TORCH);
        updatePreview();

        ((ImageView) view).setImageResource(mIsFlashOn ? R.drawable.ic_flash_off : R.drawable.ic_flash_on);
        mIsFlashOn = !mIsFlashOn;
    }

    //region Background thread
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @OnClick(R.id.fragment_camera_camera_image_view)
    void takePicture() {
        captureImageTask = new CaptureImageTask();
        captureImageTask.execute();

        closeCamera();
    }

    @Override
    public void cropImage(ScannedImageModel imageModel) {
        if (isAdded())
            getParentFragmentManager().beginTransaction()
                    .add(R.id.scan_camera_container, CropImageFragment.newInstance(imageModel, mCropImageContainerCallBack))
                    .commitAllowingStateLoss();
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
//            ImageReader reader = ImageReader.newInstance(imageDimension.getWidth(), imageDimension.getHeight(), ImageFormat.JPEG, 1);
//            List<Surface> outputSurfaces = new ArrayList<>(2);
//            outputSurfaces.add(reader.getSurface());
//            outputSurfaces.add(surface);
//
//            captureRequestBuilder.addTarget(reader.getSurface());

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
//                    capturePreview();
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getContext(), R.string.configuration_change, Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //region Open/Close camera
    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            transformImage(mTextureView.getWidth(), mTextureView.getHeight());
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void transformImage(int width, int height) {
        Size mPreviewSize = imageDimension;
        if (mPreviewSize == null || mTextureView == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / mPreviewSize.getWidth(), (float) height / mPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private Bitmap transformBitmap(Bitmap bitmap, int width, int height) {
        Size mPreviewSize = imageDimension;
        if (mPreviewSize == null || mTextureView == null) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / mPreviewSize.getWidth(), (float) height / mPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    private void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();
//        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(1));

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    if (allow) {
                        allow = false;
                        new Handler().postDelayed(() -> allow = true, 500);

                        Bitmap tempBitmap = mTextureView.getBitmap();
                        Map<Integer, PointF> points = getEdgePoints(transformBitmap(tempBitmap, mTextureView.getWidth(), mTextureView.getHeight()));
                        if (points == null) {
                            if (mPolygonView.getVisibility() == View.VISIBLE) {
                                requireActivity().runOnUiThread(() -> {
                                    if (isAdded()) mPolygonView.setVisibility(View.INVISIBLE);
                                });
                            }
                            return;
                        }

                        requireActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                mPolygonView.setPoints(points);
                                mPolygonView.setVisibility(View.VISIBLE);
                                int padding = (int) getResources().getDimension(R.dimen.scanPadding);

                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);

                                mPolygonView.setLayoutParams(layoutParams);
                            }
                        });
                    }

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
        Map<Integer, PointF> orderedPoints = mPolygonView.getOrderedPoints(pointFs);
        if (!mPolygonView.isValidShape(orderedPoints)) {
//            orderedPoints = getOutlinePoints(tempBitmap);
            orderedPoints = null;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getContext(), R.string.sorry_please_grant_permission_for_camera, Toast.LENGTH_LONG).show();
                requireActivity().finish();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (captureImageTask != null && captureImageTask.getStatus() == AsyncTask.Status.RUNNING) {
            captureImageTask.cancel(true);
        }
        for (Fragment fragment : getParentFragmentManager().getFragments()) {
            if (fragment instanceof FTSmartDialog) {
                getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
                break;
            }
        }
    }

    private class CaptureImageTask extends AsyncTask<Void, Void, Void> {
        int width = 0;
        int height = 0;
        Bitmap bitmap;
        FTSmartDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new FTSmartDialog();
            dialog.setMessage(getString(R.string.capturing_image));
            dialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
            dialog.show(getChildFragmentManager());

            bitmap = mTextureView.getBitmap();
            width = mTextureView.getWidth();
            height = mTextureView.getHeight();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File tempFolder = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Temp");
            if (!tempFolder.exists()) {
                tempFolder.mkdir();
            }

            Bitmap resultBitmap = transformBitmap(bitmap, width, height);

            file = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/pic_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(file)) {
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            ScannedImageModel imageModel = new ScannedImageModel();
            imageModel.originalImagePath = file.getAbsolutePath();
            imageModel.croppedImagePath = FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/pic_" + System.currentTimeMillis() + ".png";

            cropImage(imageModel);

            if (bitmap != null) {
                bitmap.recycle();
            }

            if (dialog.isAdded()) dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (file != null && file.exists()) {
                file.delete();
            }
            if (dialog.isAdded()) dialog.dismiss();
        }
    }
}