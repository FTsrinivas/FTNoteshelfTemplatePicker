package com.fluidtouch.noteshelf.document.lasso;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.annotation.FTAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTStrokeV1;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.imageedit.view.StickerImageView;
import com.fluidtouch.noteshelf.document.penracks.FTCustomColorPickerFragment;
import com.fluidtouch.noteshelf.document.penracks.FTEditColorsFragment;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.models.penrack.FTNPenRack;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels.FTRecognitionResult;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTSegment;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;
import com.fluidtouch.renderingengine.renderer.FTOffscreenBitmap;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LassoFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FTLassoFragment extends Fragment implements FTLassoConvertToText.ConvertToTextCallbacks, FTCustomColorPickerFragment.ColorPickerContainerCallback {
    //region Member Variables
    private ImageView mLassoSelectionView;
    private ImageView mAnnotationsSnapshotView;
    private FTLassoCanvasView mLassoCanvasView;
    private StickerImageView mResizeImageView;

    private PointF mLassoSelectionStartPoint = new PointF();
    private Point mMovementOffsetPoint = new Point(0, 0);
    private RectF initialBoundingRect;

    private GestureDetector mSingleTapGestureDetector;
    private PopupWindow mOptionsPopupWindow;
    private PopupWindow mColorPopupWindow;
    private ClipboardManager mClipboardManager;

    private ArrayList<FTAnnotation> mSelectedAnnotations = new ArrayList<>();
    private ArrayList<FTAnnotation> mLockedImageAnnotations = new ArrayList<>();
    private ArrayList<FTAnnotation> mOldAnnotations = new ArrayList<>();
    private ArrayList<FTAnnotation> mUpdatedAnnotations = new ArrayList<>();
    private Region mRegion = new Region();
    private boolean selectionContainsStroke = false;
    //endregion

    //region Listener Definition
    private LassoFragmentInteractionListener mFragmentListener;
    private LassoFragmentScreenshotListener mLassoFragmentScreenshotListener;
    private RectF annotationsRect = new RectF(0, 0, 0, 0);
    private final Handler handler = new Handler(Looper.getMainLooper());

    //endregion
    //region Touch Listener
    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        private float xDelta;
        private float yDelta;

        private int mInitialMarginX;
        private int mInitialMarginY;

        private int mInitialAnnotationsParentOffsetX;
        private int mInitialAnnotationsParentOffsetY;

        private boolean canProceedForNextTouch = false;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final float x = event.getRawX();
            final float y = event.getRawY();
            mSingleTapGestureDetector.onTouchEvent(event);

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    if (!isInsideTheRegion(event)) {
                        canProceedForNextTouch = false;
                        return true;
                    } else {
                        canProceedForNextTouch = true;
                    }
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    mInitialMarginX = lParams.leftMargin;
                    mInitialMarginY = lParams.topMargin;
                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;

                    if (mAnnotationsSnapshotView != null) {
                        mInitialAnnotationsParentOffsetX = v.getLeft() - mAnnotationsSnapshotView.getLeft();
                        mInitialAnnotationsParentOffsetY = v.getTop() - mAnnotationsSnapshotView.getTop();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (canProceedForNextTouch) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                        mMovementOffsetPoint.x = mMovementOffsetPoint.x + params.leftMargin - mInitialMarginX;
                        mMovementOffsetPoint.y = mMovementOffsetPoint.y + params.topMargin - mInitialMarginY;
                        addOffset(params.leftMargin - mInitialMarginX, params.topMargin - mInitialMarginY, mSelectedAnnotations, 1 / mFragmentListener.getContainerScale());
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (canProceedForNextTouch) {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) v.getLayoutParams();

                        mRegion.translate((int) (x - xDelta - layoutParams.leftMargin), (int) (y - yDelta - layoutParams.topMargin));
                        layoutParams.leftMargin = (int) (x - xDelta);
                        layoutParams.topMargin = (int) (y - yDelta);
                        v.setLayoutParams(layoutParams);


                        if (mAnnotationsSnapshotView != null) {
                            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) mAnnotationsSnapshotView.getLayoutParams();
                            layoutParams1.leftMargin = (int) (x - xDelta - mInitialAnnotationsParentOffsetX);
                            layoutParams1.topMargin = (int) (y - yDelta - mInitialAnnotationsParentOffsetY);
                            mAnnotationsSnapshotView.setLayoutParams(layoutParams1);
                        }
                    }
                    return true;

                default:
                    break;
            }

            return true;
        }
    };

    public static FTLassoFragment newInstance(Context context, LassoFragmentInteractionListener listener) {
        FTLassoFragment lassoFragment = new FTLassoFragment();
        lassoFragment.mFragmentListener = listener;
        lassoFragment.mClipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        return lassoFragment;
    }
    //endregion
    //endregion

    //region Lifecycle

    private static byte[] marshall(List<FTAnnotation> parcelable) {
        Parcel parcel = Parcel.obtain();
        parcel.writeTypedList(parcelable);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    private static Parcel unMarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new FrameLayout(requireContext());
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mLassoCanvasView = new FTLassoCanvasView(getContext(), new FTLassoCanvasView.LassoCanvasContainerCallback() {
            @Override
            public void onOutsideTouch() {
                if (null != getView() && ((FrameLayout) getView()).getChildCount() < 4) {
                    lassoOutsideTouch();
                } else if (mResizeImageView != null) {
                    RectF newBoundingRect = mResizeImageView.getNewBoundingRect();
                    float scale = newBoundingRect.width() / (initialBoundingRect.width());
                    initialBoundingRect = FTGeometryUtils.scaleRect(initialBoundingRect, scale);
                    for (int i = 0; i < mSelectedAnnotations.size(); i++) {
                        FTAnnotation annotation = mSelectedAnnotations.get(i);

                        RectF actualBoundingRect = annotation.getBoundingRect();
                        actualBoundingRect = FTGeometryUtils.scaleRect(actualBoundingRect, mFragmentListener.getContainerScale() * scale);
                        float offsetX = newBoundingRect.left - initialBoundingRect.left;
                        float offsetY = newBoundingRect.top - initialBoundingRect.top;
                        actualBoundingRect.offset(offsetX, offsetY);
                        annotation.setBoundingRect(FTGeometryUtils.scaleRect(actualBoundingRect, 1 / mFragmentListener.getContainerScale()));

                        if (annotation.annotationType() == FTAnnotationType.stroke) {
                            for (int j = 0; j < ((FTStroke) annotation).segmentCount; j++) {
                                FTSegment segment = ((FTStroke) annotation).getSegmentAtIndex(j);
                                segment.thickness *= scale;

                                segment.boundingRect.set(FTGeometryUtils.scaleRect(segment.boundingRect, mFragmentListener.getContainerScale() * scale));
                                segment.startPoint.set(FTGeometryUtils.scalePointF(segment.startPoint, mFragmentListener.getContainerScale() * scale));
                                segment.endPoint.set(FTGeometryUtils.scalePointF(segment.endPoint, mFragmentListener.getContainerScale() * scale));

                                segment.boundingRect.offset(offsetX, offsetY);
                                segment.startPoint.offset(offsetX, offsetY);
                                segment.endPoint.offset(offsetX, offsetY);

                                segment.boundingRect.set(FTGeometryUtils.scaleRect(segment.boundingRect, 1 / mFragmentListener.getContainerScale()));
                                segment.startPoint.set(FTGeometryUtils.scalePointF(segment.startPoint, 1 / mFragmentListener.getContainerScale()));
                                segment.endPoint.set(FTGeometryUtils.scalePointF(segment.endPoint, 1 / mFragmentListener.getContainerScale()));
                            }
                        } else if (annotation.annotationType() == FTAnnotationType.text) {
                            FTStyledText text = ((FTTextAnnotationV1) annotation).getTextInputInfo();
                            text.setSize((int) (text.getSize() * scale));
                            text.setPadding((int) (text.getPadding() * scale));
                        }
                    }
                    lassoOutsideTouch();
                }
            }

            @Override
            public void addLassoSelectionView(Bitmap bitmap, RectF rectF, Region region) {
                FTLassoFragment.this.addLassoSelectionView(bitmap, region, rectF);
                setSelectedAnnotations(getAnnotationsInRegion(mRegion, rectF));
                addAnnotationsSnapshot(getContext(), rectF);
            }

            @Override
            public void showLassoLongPressOptions(MotionEvent event) {
                FTLassoFragment.this.showLassoLongPressOptions(getView(), event);
            }

            @Override
            public void processEventForAudio(MotionEvent motionEvent) {
                mFragmentListener.processEventForAudio(motionEvent);
            }

            @Override
            public boolean isInResizeMode() {
                return ((FrameLayout) requireView()).getChildCount() > 3;
            }
        });
        ((FrameLayout) requireView()).addView(mLassoCanvasView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOptionsPopupWindow != null && mOptionsPopupWindow.isShowing()) {
            mOptionsPopupWindow.dismiss();
        }
        if (mColorPopupWindow != null && mColorPopupWindow.isShowing()) {
            mColorPopupWindow.dismiss();
        }
    }

    private void setSelectedAnnotations(ArrayList<FTAnnotation> annotations) {
        mLockedImageAnnotations.clear();
        for (int i = 0; i < annotations.size(); i++) {
            FTAnnotation annotation = annotations.get(i);
            if (annotation.annotationType() == FTAnnotationType.image) {
                FTImageAnnotationV1 imgAnt = (FTImageAnnotationV1) annotations.get(i);
                if (imgAnt.getImageLockStatus() == 1) {
                    mLockedImageAnnotations.add(annotations.get(i));
                    annotations.remove(annotations.get(i));
                }
            }
        }

        this.mSelectedAnnotations = annotations;

        setAnnotationsBackUp(mOldAnnotations, mSelectedAnnotations);
    }

    private void setAnnotationsBackUp(ArrayList<FTAnnotation> toAnnotations, ArrayList<FTAnnotation> fromAnnotations) {
        toAnnotations.clear();
        for (int i = 0; i < fromAnnotations.size(); i++) {
            FTAnnotation annotation = fromAnnotations.get(i);
            if (annotation.annotationType() == FTAnnotationType.stroke) {
                toAnnotations.add(setStroke((FTStroke) annotation));
            } else if (annotation.annotationType() == FTAnnotationType.image) {
                toAnnotations.add(setImage((FTImageAnnotationV1) annotation));
            } else if (annotation.annotationType() == FTAnnotationType.text) {
                toAnnotations.add(setText((FTTextAnnotationV1) annotation));
            }
        }
    }

    private FTStroke setStroke(FTStroke editingStroke) {
        FTStrokeV1 stroke = new FTStrokeV1(getContext());
        stroke.setBoundingRect(new RectF(editingStroke.getBoundingRect()));
        stroke.strokeColor = editingStroke.strokeColor;
        stroke.strokeWidth = editingStroke.strokeWidth;

        for (int j = 0; j < editingStroke.segmentCount; j++) {
            FTSegment segment = editingStroke.getSegmentAtIndex(j);
            FTSegment newSegment = new FTSegment(new PointF(segment.startPoint.x, segment.startPoint.y), new PointF(segment.endPoint.x, segment.endPoint.y),
                    segment.thickness, new RectF(segment.boundingRect), segment.opacity);
            newSegment.setSegmentAsErased(segment.isSegmentErased());
            stroke.addSegment(newSegment);
        }
        return stroke;
    }

    private FTImageAnnotation setImage(FTImageAnnotationV1 editingImage) {
        FTImageAnnotation image = new FTImageAnnotationV1(getContext(), editingImage.associatedPage);
        image.setBoundingRect(new RectF(editingImage.getBoundingRect()));
        image.setBitmap(editingImage.getImage());
        image.setImgAngel(editingImage.getImgAngel());
        image.setImgTxMatrix(editingImage.getImgTxMatrix());
        return image;
    }

    //endregion


    //region Lasso Selection

    private FTTextAnnotation setText(FTTextAnnotationV1 editingText) {
        FTTextAnnotationV1 text = new FTTextAnnotationV1(getContext());
        text.setBoundingRect(new RectF(editingText.getBoundingRect()));
        FTStyledText styledText = FTStyledText.instance(editingText.getTextInputInfo());
        text.setInputTextWithInfo(styledText);
        text.getTextInputInfo().setPlainText(editingText.getTextInputInfo().getPlainText());
        return text;
    }
    //endregion

    //region Options Menus

    private void addLassoSelectionView(Bitmap bitmap, Region region, RectF rectF) {
        mRegion = region;
        mLassoSelectionView = new ImageView(getContext());
        mLassoSelectionView.setLayoutParams(new FrameLayout.LayoutParams((int) rectF.width(), (int) rectF.height()));
        mLassoSelectionView.setImageBitmap(bitmap);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mLassoSelectionView.getLayoutParams();

        layoutParams.leftMargin = (int) rectF.left;
        layoutParams.topMargin = (int) rectF.top;
        mLassoSelectionView.setLayoutParams(layoutParams);

        mSingleTapGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isInsideTheRegion(e)) {
                    if (mLassoSelectionView != null)
                        showLassoSingleTapOptions(mLassoSelectionView);
                } else {
                    mLassoCanvasView.outsideTouch();
                }
                return true;
            }
        });

        mLassoSelectionView.setOnTouchListener(mViewTouchListener);
        ((FrameLayout) requireView()).addView(mLassoSelectionView);

        mLassoSelectionView.post(() -> {
            if (mLassoSelectionView != null)
                showLassoSingleTapOptions(mLassoSelectionView);
        });
    }

    public void showLassoOptions(View view) {
        if (mLassoSelectionView != null & view.getId() != R.id.doc_toolbar_lasso_image_view) {
            showLassoSingleTapOptions(view);
        } else if (null != getContext()) {
            if (mLassoCanvasView != null)
                mLassoCanvasView.outsideTouch();
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            if (null != clipboardManager) {
                try {
                    ClipData data = clipboardManager.getPrimaryClip();
                    if (null != data && data.getDescription().getLabel().equals(BuildConfig.APPLICATION_ID)) {
                        showLassoLongPressOptions(view != null ? view : getView(), null);
                    }
                } catch (Exception e) {
                    Log.i(this.getClass().getName(), e.getMessage());
                }
            }
        }
    }

    private void showLassoLongPressOptions(View parentView, final MotionEvent event) {
        mOptionsPopupWindow = new PopupWindow(getContext());

        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.lasso_long_tap_options, null);

        mOptionsPopupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setContentView(popUpView);
        mOptionsPopupWindow.setFocusable(true);
        mOptionsPopupWindow.setBackgroundDrawable(null);

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_long_tap_options_cut_text_view).setOnClickListener(v -> {
            v.getBackground().setAlpha(180);
            handler.postDelayed(() -> {
                v.getBackground().setAlpha(255);
                mOptionsPopupWindow.dismiss();
            }, 150);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_long_tap_options_paste_text_view).setOnClickListener(v -> {
            v.getBackground().setAlpha(180);
            handler.postDelayed(() -> {
                pasteLassoData(event);
                v.getBackground().setAlpha(255);
                mOptionsPopupWindow.dismiss();
            }, 150);
        });
        if (event != null) {
            mOptionsPopupWindow.showAtLocation(getView(), Gravity.NO_GRAVITY, (int) event.getRawX(), (int) event.getRawY() - ScreenUtil.getStatusBarHeight(getContext()));
        } else {
            int[] parentLocation = new int[2];
            parentView.getLocationInWindow(parentLocation);
            mOptionsPopupWindow.showAsDropDown((View) (parentView.getParent().getParent()), parentLocation[0], 0);
        }
    }

    private void showLassoSingleTapOptions(View view) {
        final int delay = 300;
        final int onClickingAlpha = 180;
        final int finalAlpha = 255;
        mOptionsPopupWindow = new PopupWindow(getContext());

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.lasso_options, null);

        mOptionsPopupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        mOptionsPopupWindow.setContentView(popUpView);
        mOptionsPopupWindow.setFocusable(true);
        mOptionsPopupWindow.setBackgroundDrawable(null);
        //mOptionsPopupWindow = FTPopupFactory.create(getContext(), view, R.layout.lasso_options, 0, 0);
        //mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_screenshot_text_view).setVisibility(View.GONE);
        if (!selectionContainsStroke) {
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_convert_to_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.view_convert_to_text).setVisibility(View.GONE);
        }

        if (mSelectedAnnotations.size() == 0) {
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_cut_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_cut_view).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_copy_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_copy_view).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_delete_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_delete_view).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_color_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_color_view).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_convert_to_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.view_convert_to_text).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_resize_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_resize_view).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_bring_to_front_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.view_bring_to_front).setVisibility(View.GONE);

            mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_send_to_back_text_view).setVisibility(View.GONE);
            mOptionsPopupWindow.getContentView().findViewById(R.id.view_send_to_back).setVisibility(View.GONE);
        }

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_screenshot_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Screenshot");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                v.getBackground().setAlpha(finalAlpha);
                screenShotLogic();
                mOptionsPopupWindow.dismiss();
            }, delay);


        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_cut_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Cut");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                cutToClipboard();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_delete_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Delete");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                deleteSelectedAnnotations();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_copy_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Copy");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                copyToClipboard();
                lassoCanvasOutsideTouch();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_color_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Color");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
                showColorPicker(view);
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_resize_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Resize");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
                resizeImage();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_convert_to_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_ConvertToText");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                //lassoCanvasOutsideTouch();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
                convertToText();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_bring_to_front_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Front");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                lassoCanvasOutsideTouch();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
                bringTOFront();
            }, delay);
        });

        mOptionsPopupWindow.getContentView().findViewById(R.id.lasso_options_send_to_back_text_view).setOnClickListener(v -> {
            FTFirebaseAnalytics.logEvent("Lasso_Back");
            v.getBackground().setAlpha(onClickingAlpha);
            handler.postDelayed(() -> {
                lassoCanvasOutsideTouch();
                v.getBackground().setAlpha(finalAlpha);
                mOptionsPopupWindow.dismiss();
                sendTOBack();
            }, delay);
        });

        int[] location = new int[2];
        view.getLocationInWindow(location);
        mOptionsPopupWindow.showAtLocation(getView(), Gravity.NO_GRAVITY, Math.max(0, location[0] + (int) mFragmentListener.getVisibleRect().left), Math.max(0, (location[1] - getResources().getDimensionPixelOffset(R.dimen._38dp)) + (int) mFragmentListener.getVisibleRect().top));
    }

    //endregion

    private void screenShotLogic() {
        lassoCanvasOutsideTouch();
        RectF rectF = getSelectedRect();
        Bitmap bitmap = null;
        ArrayList<FTAnnotation> combinedAnnotations = new ArrayList<>();
        combinedAnnotations.addAll(mSelectedAnnotations);
        combinedAnnotations.addAll(mLockedImageAnnotations);
        try {
            bitmap = getBitmapOfAnnotations(getContext(), combinedAnnotations, rectF, false).image;
        } catch (Exception e) {
            FTLog.crashlyticsLog("lasso screenShot" + annotationsRect.toShortString() + "croprect" + rectF.toString());
            FTLog.logCrashException(e);
        }
        annotationsRect = new RectF(0, 0, 0, 0);
        if (getActivity() != null)
            ((LassoFragmentScreenshotListener) getActivity()).screenshotCaptured(bitmap);
    }
    //region Cut / Copy / Paste

    private void resizeImage() {
        if (mAnnotationsSnapshotView != null) {
            initialBoundingRect = new RectF(mAnnotationsSnapshotView.getLeft(), mAnnotationsSnapshotView.getTop(),
                    mAnnotationsSnapshotView.getRight(), mAnnotationsSnapshotView.getBottom());
            mResizeImageView = new StickerImageView(getActivity());
            mResizeImageView.setImageBitmap(((BitmapDrawable) mAnnotationsSnapshotView.getDrawable()).getBitmap());
            mResizeImageView.setBoundingRect(new RectF(initialBoundingRect));
            ((FrameLayout) requireView()).addView(mResizeImageView);
            mAnnotationsSnapshotView.setVisibility(View.GONE);
            if (mLassoSelectionView != null) mLassoSelectionView.setVisibility(View.GONE);

            mResizeImageView.init(getContext(), Gravity.CENTER);
            int[] location = new int[2];
            getView().getLocationOnScreen(location);
            mResizeImageView.parentX = location[0] + mFragmentListener.getVisibleRect().left;
            mResizeImageView.parentY = location[0] + mFragmentListener.getVisibleRect().top;
            mResizeImageView.disableRotation();
            mResizeImageView.setVisibility(View.INVISIBLE);

            handler.postDelayed(() -> {
                mResizeImageView.setX(initialBoundingRect.left);
                mResizeImageView.setY(initialBoundingRect.top);
                mResizeImageView.setVisibility(View.VISIBLE);
            }, 50);
        }
    }

    private void cutToClipboard() {
        boolean isAbleToCopy = copyToClipboard();
        if (isAbleToCopy) {
            deleteSelectedAnnotations();
        }
    }

    private boolean copyToClipboard() {
        try {
            float scale = mFragmentListener.getContainerScale();
            Intent intent = new Intent(getContext(), FTApp.class);
            intent.putExtra(requireContext().getString(R.string.intent_annotations), marshall(this.mSelectedAnnotations));
            intent.putExtra(getContext().getString(R.string.intent_lasso_width), mLassoSelectionView.getWidth() / scale);
            intent.putExtra(getContext().getString(R.string.intent_lasso_height), mLassoSelectionView.getHeight() / scale);
            intent.putExtra(getContext().getString(R.string.intent_lasso_old_x), mLassoSelectionView.getX() / scale);
            intent.putExtra(getContext().getString(R.string.intent_lasso_old_y), mLassoSelectionView.getY() / scale);
            intent.putExtra(getContext().getString(R.string.intent_lasso_region), scaleRegion(mRegion, 1 / scale));
            ClipData clipdata = ClipData.newIntent(BuildConfig.APPLICATION_ID, intent);
            mClipboardManager.setPrimaryClip(clipdata);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.cannot_support_heavy_selection, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void convertToText() {
        new FTLassoConvertToText().show(getChildFragmentManager(), FTLassoConvertToText.class.getName());
    }

    private void bringTOFront() {
        mFragmentListener.selectedAnnotationsBringTOFront(getSelectedAnnotations());
    }

    private void sendTOBack() {
        mFragmentListener.selectedAnnotationsSendTOBack(getSelectedAnnotations());
    }

    private void pasteLassoData(MotionEvent event) {
        ClipboardManager clipboardManager = (ClipboardManager) Objects.requireNonNull(getContext()).getSystemService(CLIPBOARD_SERVICE);
        Intent intent = Objects.requireNonNull(clipboardManager.getPrimaryClip()).getItemAt(0).getIntent();
        float width = intent.getFloatExtra(getString(R.string.intent_lasso_width), 0f);
        float height = intent.getFloatExtra(getString(R.string.intent_lasso_height), 0f);
        float oldX = intent.getFloatExtra(getString(R.string.intent_lasso_old_x), 0f);
        float oldY = intent.getFloatExtra(getString(R.string.intent_lasso_old_y), 0f);
        if (event == null) {
            event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_OUTSIDE, (oldX + (width * 0.5f)) * mFragmentListener.getContainerScale(), (oldY + (height * 0.5f)) * mFragmentListener.getContainerScale(), -1);
        }

        Region region = scaleRegion(intent.getParcelableExtra(getString(R.string.intent_lasso_region)), mFragmentListener.getContainerScale());
        int leftMargin = (int) (event.getX() - (width * 0.5f));
        int topMargin = (int) (event.getY() - (height * 0.5f));

        region.translate((int) (event.getX() - (oldX + (width * 0.5f)) * mFragmentListener.getContainerScale()), (int) (event.getY() - (oldY + (height * 0.5f)) * mFragmentListener.getContainerScale()));
        addLassoSelectionView(getBitmap(region), region, new RectF(region.getBounds()));
        pasteAnnotations(getContext(), event, intent, width, height);
        addAnnotationsSnapshot(getContext(), (new RectF(leftMargin, topMargin, leftMargin + width, topMargin + height)));
        mLassoCanvasView.setRegion(region);
    }

    //region Marshall / Un-marshall

    private void pasteAnnotations(Context context, MotionEvent event, Intent intent, float width, float height) {
        ArrayList<FTAnnotation> ftAnnotations = new ArrayList<>();
        byte[] bytes = intent.getByteArrayExtra(context.getString(R.string.intent_annotations));
        unMarshall(bytes).readTypedList(ftAnnotations, FTAnnotationV1.CREATOR);

        for (FTAnnotation annotation : ftAnnotations) {
            if (annotation instanceof FTImageAnnotation) {
                ((FTImageAnnotationV1) annotation).associatedPage = mFragmentListener.currentPage();
                //The file item need to be created
                ((FTImageAnnotation) annotation).setImage(((FTImageAnnotation) annotation).getImage());
                String newUUID = FTDocumentUtils.getUDID();
                String originalUrl = ((FTImageAnnotationV1) annotation).getImageUrl().getPath();
                FTFileManagerUtil.copyRecursively(new File(originalUrl), new File(originalUrl.split(annotation.uuid)[0] + newUUID + ".png"));
                annotation.uuid = newUUID;
            }
            annotation.context = context;
        }

        //Add Offset to the pasted data
        float scale = mFragmentListener.getContainerScale();
        RectF rect = getUnionOfRects(ftAnnotations, scale);
        RectF finalRect = new RectF(rect);
        float offsetX = event.getX() - width * 0.5f * scale - intent.getFloatExtra(context.getString(R.string.intent_lasso_old_x), 0f) * scale;
        float offsetY = event.getY() - height * 0.5f * scale - intent.getFloatExtra(context.getString(R.string.intent_lasso_old_y), 0f) * scale;
        finalRect.offset(offsetX, offsetY);
        addOffset(offsetX, offsetY, ftAnnotations, 1 / scale);

        //Assigning for moving of the VIEW
        setSelectedAnnotations(ftAnnotations);
        mFragmentListener.addAnnotations(ftAnnotations, true);
    }

    public void lassoCanvasOutsideTouch() {
        if (mLassoCanvasView != null) {
            selectionContainsStroke = false;
            mLassoCanvasView.outsideTouch();
        }
    }
    //endregion
    //endregion

    public void lassoOutsideTouch() {
        Log.i("LASSO", "outside touch Lasso");
        selectionContainsStroke = false;
        if (mLassoSelectionView != null) {
            configureAnnotationsUnderRect(false);
            handler.postDelayed(() -> {
                try {
                    ((FrameLayout) requireView()).removeViews(1, ((FrameLayout) getView()).getChildCount() - 1);
                    mLassoSelectionView = null;
                    mMovementOffsetPoint.set(0, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 100);
        }

        if (mOptionsPopupWindow != null && mOptionsPopupWindow.isShowing()) {
            mOptionsPopupWindow.dismiss();
        }

        if (mColorPopupWindow != null && mColorPopupWindow.isShowing()) {
            mColorPopupWindow.dismiss();
        }
    }

    private void addAnnotationsSnapshot(Context context, RectF parentRect) {
        if (!mSelectedAnnotations.isEmpty()) {
            RectF rectF = getUnionOfRects(this.mSelectedAnnotations, mFragmentListener.getContainerScale());
//            rectF.union(parentRect);

            int x = (int) rectF.left;
            int y = (int) rectF.top;
            RectF scaledPageRect = FTGeometryUtils.scaleRect(mFragmentListener.getContainerRect(), mFragmentListener.getContainerScale());
            rectF.bottom = y + (scaledPageRect.height() < y + rectF.height() ? scaledPageRect.height() : (int) rectF.height());
            rectF.right = x + (scaledPageRect.width() < x + rectF.width() ? scaledPageRect.width() : (int) rectF.width());
            if (rectF.width() > 1 && rectF.height() > 1) {
                FTLog.crashlyticsLog(rectF.toString());
                FTOffscreenBitmap bitmapInfo = getBitmapOfAnnotations(context, rectF, true);
                Bitmap bitmap = bitmapInfo.image;
                annotationsRect = rectF;

                mAnnotationsSnapshotView = new ImageView(context);
                mAnnotationsSnapshotView.setTag("Annotations Thumbnail");
                mAnnotationsSnapshotView.setLayoutParams(new FrameLayout.LayoutParams(Objects.requireNonNull(bitmap).getWidth(), bitmap.getHeight()));
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAnnotationsSnapshotView.getLayoutParams();
                layoutParams.leftMargin = (int) (rectF.left);
                layoutParams.topMargin = (int) (rectF.top);
                mAnnotationsSnapshotView.setLayoutParams(layoutParams);
                mAnnotationsSnapshotView.setImageBitmap(bitmap);
                ((FrameLayout) Objects.requireNonNull(getView())).addView(mAnnotationsSnapshotView, 1);
            }
        }
        handler.postDelayed(() -> {
            configureAnnotationsUnderRect(true);
        }, 50);
    }

    //region Annotations Snapshot Creation
    private FTOffscreenBitmap getBitmapOfAnnotations(Context context, RectF rect, boolean hideBackground) {
        return getBitmapOfAnnotations(context, this.mSelectedAnnotations, rect, hideBackground);
    }

    private FTOffscreenBitmap getBitmapOfAnnotations(Context context, ArrayList<FTAnnotation> mSelectedAnnotations, RectF rect, boolean hideBackground) {
        FTRenderManager renderManager = new FTRenderManager(context, FTRenderMode.offScreen);

        Size aspectFitSize = new Size((int) rect.width(), (int) rect.height());

        int tileLength = FTRenderManager.TILE_LENGTH;
        Bitmap combinedBitmap = Bitmap.createBitmap(aspectFitSize.getWidth(), aspectFitSize.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(combinedBitmap);

        int minRow = (int) (rect.left/tileLength);
        int maxRow = (int) Math.ceil(rect.right/tileLength);

        int minCol = (int) (rect.top/tileLength);
        int maxCol = (int) Math.ceil(rect.bottom/tileLength);

        for(int i = minCol; i < maxCol; i++) {
            for(int j = minRow; j < maxRow; j++) {
                RectF rectF = new RectF(j * tileLength, i * tileLength, (j + 1) * tileLength, (i + 1) * tileLength);
                FTOffscreenBitmap bitmapInfo = renderManager.generateTileImage(mSelectedAnnotations,
                        mFragmentListener.bgTexture(),
                        hideBackground,
                        rectF,
                        mFragmentListener.getContainerScale(),
                        mFragmentListener.getRenderingPageRect(),
                        "Lasso image generation");
                Paint paint = new Paint();
                float left = (j * tileLength) - rect.left;
                float top = (i * tileLength) - rect.top;
                canvas.drawBitmap(bitmapInfo.image, left, top, paint);
                bitmapInfo.image.recycle();
            }
        }
        renderManager.destroy();
        FTOffscreenBitmap bitmapInfo = new FTOffscreenBitmap();
        bitmapInfo.image = combinedBitmap;
        bitmapInfo.scale = mFragmentListener.getContainerScale();
        return bitmapInfo;
    }

    //region Color Change
    FTLassoColorPickerAdapter adapter;

    private void showColorPicker(View view) {
        FTEditColorsFragment.newInstance("FTDefaultPenRack", this::changeColor).show(view, getChildFragmentManager());
    }
    //endregion

    private void changeColor(String color) {
        ArrayList<FTAnnotation> annotations = mSelectedAnnotations;
        for (int i = 0; i < annotations.size(); i++) {
            if (annotations.get(i).annotationType() == FTAnnotationType.stroke) {
                FTStroke stroke = (FTStroke) annotations.get(i);
                stroke.setStrokeColor(Color.parseColor(color));
            } else if (annotations.get(i).annotationType() == FTAnnotationType.text) {
                FTTextAnnotationV1 textAnnotation = (FTTextAnnotationV1) annotations.get(i);
                textAnnotation.setColor(Color.parseColor(color));
            }
        }

        if (mAnnotationsSnapshotView != null) {
            RectF rectF = new RectF();
            rectF.left = mAnnotationsSnapshotView.getLeft();
            rectF.right = mAnnotationsSnapshotView.getRight();
            rectF.top = mAnnotationsSnapshotView.getTop();
            rectF.bottom = mAnnotationsSnapshotView.getBottom();
            setUpAnnotations(false);
            FTOffscreenBitmap bitmapInfo = getBitmapOfAnnotations(getContext(), rectF, true);
            mAnnotationsSnapshotView.setImageBitmap(BitmapUtil.scaleBitmap(bitmapInfo.image, rectF));
            setUpAnnotations(true);
        }
    }

    List<String> customColors;
    FTNPenRack penRack;

    private List<String> getColors() {
        try {
            penRack = FTNPenRack.getInstance();
            HashMap<String, Object> penRackData = (HashMap<String, Object>) penRack.getPenRackData().get("FTDefaultPenRack");
            if (penRackData == null)
                return new ArrayList<>();
            customColors = penRack.getStringList((Object[]) penRackData.get("currentColors"));
            return customColors;
        } catch (Exception e) {
            Log.i(this.getClass().getName(), e.getMessage());
        }

        return new ArrayList<>();
    }

    private boolean areAnnotationsDeleted = false;

    private void deleteSelectedAnnotations() {
        mFragmentListener.removeAnnotations(this.mSelectedAnnotations, true);
        areAnnotationsDeleted = true;
        lassoCanvasOutsideTouch();
        mOptionsPopupWindow.dismiss();
    }
    //endregion

    //region Helper methods

    private void setUpAnnotations(boolean isHidden) {
        if (this.mSelectedAnnotations.isEmpty()) {
            return;
        }
        ArrayList<FTAnnotation> annotations = this.mSelectedAnnotations;
        for (int i = 0; i < annotations.size(); i++) {
            annotations.get(i).hidden = isHidden;
        }
    }

    private void configureAnnotationsUnderRect(boolean isHidden) {
        setUpAnnotations(isHidden);
        if (areAnnotationsDeleted) {
            areAnnotationsDeleted = false;
        } else {
            if (!isHidden) {
                mFragmentListener.updateAnnotations(this.mSelectedAnnotations, getHelperAnnotations(), true);
            } else {
                mFragmentListener.reloadAnnotationsInRect(getUnionOfRects(this.mSelectedAnnotations, mFragmentListener.getContainerScale()));
            }
        }
    }

    private ArrayList<FTAnnotation> getHelperAnnotations() {
        setAnnotationsBackUp(mUpdatedAnnotations, mSelectedAnnotations);

        for (int i = 0; i < mSelectedAnnotations.size(); i++) {
            FTAnnotation annotation = mOldAnnotations.get(i);
            if (annotation.annotationType() == FTAnnotationType.stroke) {
                FTStroke stroke = (FTStroke) mSelectedAnnotations.get(i);
                stroke.setBoundingRect(new RectF(annotation.getBoundingRect()));
                stroke.setStrokeColor(((FTStroke) annotation).strokeColor);
                stroke.strokeWidth = ((FTStroke) annotation).strokeWidth;

                for (int j = 0; j < ((FTStroke) annotation).segmentCount; j++) {
                    FTSegment segment = ((FTStroke) annotation).getSegmentAtIndex(j);
                    FTSegment newSegment = new FTSegment(new PointF(segment.startPoint.x, segment.startPoint.y), new PointF(segment.endPoint.x, segment.endPoint.y),
                            segment.thickness, new RectF(segment.boundingRect), segment.opacity);
                    newSegment.setSegmentAsErased(segment.isSegmentErased());
                    stroke.setSegmentAtIndex(j, newSegment);
                }

            } else if (annotation.annotationType() == FTAnnotationType.image) {
                FTImageAnnotation image = (FTImageAnnotation) mSelectedAnnotations.get(i);
                image.setBoundingRect(new RectF(annotation.getBoundingRect()));
                image.setImgAngel(((FTImageAnnotation) annotation).getImgAngel());
                image.setImgTxMatrix(((FTImageAnnotation) annotation).getImgTxMatrix());
                image.setBitmap(((FTImageAnnotation) annotation).getImage());
            } else if (annotation.annotationType() == FTAnnotationType.text) {
                FTTextAnnotationV1 text = (FTTextAnnotationV1) mSelectedAnnotations.get(i);
                text.setBoundingRect(new RectF(annotation.getBoundingRect()));
                text.setInputTextWithInfo(((FTTextAnnotationV1) annotation).getTextInputInfo());
                text.getTextInputInfo().setPlainText(((FTTextAnnotationV1) annotation).getTextInputInfo().getPlainText());
            }
        }

        return mUpdatedAnnotations;
    }

    private RectF getUnionOfRects(ArrayList<FTAnnotation> annotations, float scale) {
        RectF rect = new RectF();
        for (int i = 0; i < annotations.size(); i++) {
            FTAnnotation annotation = annotations.get(i);
            if (!selectionContainsStroke && annotation instanceof FTStroke) {
                FTPenType penType = ((FTStroke) annotation).penType;
                if (penType != FTPenType.highlighter && penType != FTPenType.flatHighlighter) {
                    selectionContainsStroke = true;
                }
            }
            if (annotation.annotationType() == FTAnnotationType.image)
                rect.union(((FTImageAnnotation) annotation).getRenderingRect());
            else
                rect.union(annotation.getBoundingRect());
        }
        return FTGeometryUtils.scaleRect(rect, scale);
    }

    private void addOffset(float offsetX, float offsetY, List<FTAnnotation> annotations, float scale) {
        for (int i = 0; i < annotations.size(); i++) {
            if (annotations.get(i).annotationType() == FTAnnotationType.stroke) {
                ((FTStroke) annotations.get(i)).applyOffset(offsetX * scale, offsetY * scale);
            }
            annotations.get(i).getBoundingRect().offset(offsetX * scale, offsetY * scale);
        }
    }

    private boolean isInsideTheRegion(MotionEvent event) {
        if (mLassoCanvasView == null || mLassoSelectionView == null) {
            return false;
        }
        return mRegion.contains((int) (event.getX() + mLassoSelectionView.getLeft()), (int) (event.getY() + mLassoSelectionView.getTop()));
    }

    private Bitmap getBitmap(Region region) {
        try {
            RectF rect = mFragmentListener.getContainerRect();
            Bitmap bitmap = Bitmap.createBitmap((int) (rect.width() * mFragmentListener.getContainerScale()),
                    (int) (rect.height() * mFragmentListener.getContainerScale()), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawPath(region.getBoundaryPath(), getPaint());

            int offset = 3;
            RectF rectF = new RectF();
            region.getBoundaryPath().computeBounds(rectF, true);
            RectF finalRectF = new RectF((float) Math.floor(rectF.left) - offset, (float) Math.floor(rectF.top) - offset,
                    (float) Math.ceil(rectF.right) + offset, (float) Math.ceil(rectF.bottom) + offset);

            return BitmapUtil.cropBitmap(bitmap, finalRectF);
        } catch (Exception e) {
            return null;
        }
    }

    private Paint getPaint() {
        Paint paint = mLassoCanvasView.getPaint();
        paint.setPathEffect(mLassoCanvasView.getDashPathEffect());
        return paint;
    }

    private Path scalePath(Region region, float scale) {
        Path path = region.getBoundaryPath();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        path.transform(matrix);
        return path;
    }

    private Region scaleRegion(Region region, float scale) {
        Path path = scalePath(region, scale);
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        Region newRegion = new Region();
        newRegion.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return newRegion;
    }

    private ArrayList<FTAnnotation> getAnnotationsInRegion(Region region, RectF rectF) {
        RectF unScaledSelectedRect = FTGeometryUtils.scaleRect(rectF, 1 / mFragmentListener.getContainerScale());
        ArrayList<FTAnnotation> newPageAnnotations = new ArrayList<>();
        ArrayList<FTAnnotation> pageAnnotations = mFragmentListener.currentPage().getPageAnnotations();
        int count = pageAnnotations.size();

        ArrayList<FTAnnotationType> annotationTypes = new ArrayList<>();
        annotationTypes.add(FTAnnotationType.text);
        annotationTypes.add(FTAnnotationType.image);
        annotationTypes.add(FTAnnotationType.stroke);

        int i = count;
        while (i > 0) {
            FTAnnotation annotation = pageAnnotations.get(i - 1);
            FTAnnotationType annotationType = annotation.annotationType();
            if (annotationTypes.contains(annotationType)) {
                if (annotationType == FTAnnotationType.stroke) {
                    if (((FTStroke) annotation).intersectsRect(unScaledSelectedRect, region, mFragmentListener.getContainerScale())) {
                        newPageAnnotations.add(annotation);
                    }
                } else {
                    if (RectF.intersects(annotation.getBoundingRect(), unScaledSelectedRect)) {
                        newPageAnnotations.add(annotation);
                    }
                }
            }
            i--;
        }
        Collections.reverse(newPageAnnotations);
        return newPageAnnotations;
    }

    @NotNull
    @Override
    public ArrayList<FTAnnotation> getSelectedAnnotations() {
        return mSelectedAnnotations;
    }

    @NotNull
    @Override
    public RectF getSelectedRect() {
        RectF rectF = new RectF();
        mRegion.getBoundaryPath().computeBounds(rectF, true);
        return rectF;
    }

    @Override
    public void onConvertToTextBoxClicked(@NotNull String text) {
        mFragmentListener.addTextBoxFromHW(text, mSelectedAnnotations);
    }

    @Override
    public void addColorToRack(String color, int position) {
        if (color.length() > 0 && !customColors.contains(color.substring(1))) {
            customColors.add(color.substring(1));
            if (adapter != null) {
                adapter.add(adapter.getItemCount() - 1, color.substring(1, color.length()));
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean isColorExistsInRack(String color) {
        return false;
    }

    @Override
    public void onBackClicked() {
        penRack.updateColors("FTDefaultPenRack", customColors);
    }

    public interface LassoFragmentInteractionListener {
        void reloadAnnotationsInRect(RectF boundingRect);

        void addAnnotations(List<FTAnnotation> annotations, boolean refreshView);

        void addTextBoxFromHW(@NotNull String text, List<FTAnnotation> annotations);

        void updateAnnotations(ArrayList<FTAnnotation> oldAnnotations, ArrayList<FTAnnotation> helperAnnotations, boolean refreshView);

        void removeAnnotations(List<FTAnnotation> annotations, boolean refreshView);

        RectF getContainerRect();

        RectF getRenderingPageRect();

        RectF getVisibleRect();

        float getContainerScale();

        FTNoteshelfPage currentPage();

        void processEventForAudio(MotionEvent motionEvent);

        void selectedAnnotationsBringTOFront(ArrayList<FTAnnotation> selectedAnnotations);

        void selectedAnnotationsSendTOBack(ArrayList<FTAnnotation> selectedAnnotations);

        int bgTexture();
    }
    //endregion

    public interface LassoFragmentScreenshotListener {
        void screenshotCaptured(Bitmap bitmap);
    }
}
