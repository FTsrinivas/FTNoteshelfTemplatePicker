package com.fluidtouch.noteshelf.document.imageedit;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.SizeF;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.NumberUtils;
import com.fluidtouch.noteshelf.document.FTAnnotationFragment;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.imageedit.view.StickerImageView;
import com.fluidtouch.noteshelf.document.imageedit.view.StickerView;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTImageEditFragment extends FTAnnotationFragment implements
        StickerView.Callbacks {


    //region Member Variables
    View view;
    @BindView(R.id.layMain)
    FrameLayout layMain;
    StickerImageView mImageView;
    RectF visibleRect;
    private Callbacks mImageViewListener;
    private Bitmap bitmap;
    private RectF mBoundingRect;
    private FTImageAnnotationV1 ftAnnotation;
    private List<String> popupMenuItemList = new ArrayList<>();
    private FTPopupView popupList;
    private boolean isBitmapEdited = false;

    //region Factory Method
    public static FTImageEditFragment newInstance(FTAnnotation ftAnnotation, Callbacks mImageViewListener) {
        FTImageEditFragment f = new FTImageEditFragment();
        f.ftAnnotation = (FTImageAnnotationV1) ftAnnotation;
        f.mImageViewListener = mImageViewListener;

        return f;
    }
    //endregion

    @Override
    public void outsideClick() {
        if (layMain != null)
            layMain.performClick();
    }
    //endregion

    //region Lifecycle Events
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.edit_imageview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        visibleRect = mImageViewListener.getVisibleRect();
        bitmap = ftAnnotation.getImage();
        mBoundingRect = ftAnnotation.getBoundingRect() != null ? FTGeometryUtils.scaleRect(ftAnnotation.getBoundingRect(), mImageViewListener.getContainerScale()) : null;

        configureViews(bitmap, mBoundingRect);
        popupMenuItemList.add(getResources().getString(R.string.delete));
        popupMenuItemList.add(getResources().getString(R.string.edit));
        popupMenuItemList.add(getResources().getString(R.string.lock));
        popupMenuItemList.add(getResources().getString(R.string.cut));
        popupMenuItemList.add(getResources().getString(R.string.copy));
        popupMenuItemList.add(getResources().getString(R.string.bring_to_front));
        popupMenuItemList.add(getResources().getString(R.string.send_to_back));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String filename = data.getStringExtra("image");
            try {
                FileInputStream is = getActivity().openFileInput(filename);
                bitmap = BitmapFactory.decodeStream(is);
                ftAnnotation.setImage(bitmap);
                ftAnnotation.processForText = true;
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                //ftAnnotation.setImage(bitmap);
                RectF rectF = new RectF();
                rectF.set(mImageView.getNewBoundingRect());
                rectF.bottom = rectF.top + (rectF.width() / (float) ((float) bitmap.getWidth() / (float) bitmap.getHeight()));
                configureViews(bitmap, rectF);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (layMain != null)
            layMain.removeAllViews();
    }

    //endregion

    //region Configure Views

    private void configureViews(final Bitmap bitmap, final RectF boundedRect) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null && getActivity() != null) {
                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    int x;
                    int y;
                    final int gcd = NumberUtils.gcd(bitmapWidth, bitmapHeight);
                    x = bitmapWidth / gcd;
                    y = bitmapHeight / gcd;
                    if (null == boundedRect || boundedRect.width() <= 0) {
                        RectF rectF = FTGeometryUtils.scaleRect(visibleRect, mImageViewListener.getOriginalScale() / mImageViewListener.getContainerScale());
                        float left = Math.max((rectF.width() - bitmapWidth) / 2, 0);
                        float top = Math.max((rectF.height() - bitmapHeight) / 2, 0);
                        float right = 0;
                        float bottom = 0;
                        if (bitmapWidth > rectF.width()) {
                            right = rectF.width();
                            bottom = rectF.width() / ((float) x / (float) y);
                        } else if (bitmapHeight > rectF.height()) {
                            right = rectF.height() * ((float) x / (float) y);
                            bottom = rectF.height();
                        } else {
                            right = bitmapWidth;
                            bottom = bitmapHeight;
                        }
                        right = Math.min(right + left, rectF.width());
                        bottom = Math.min(bottom + top, rectF.height());
                        mBoundingRect = new RectF(left, top, right, bottom);
                        if (left == 0 && top == 0) {
                            left = (rectF.width() - mBoundingRect.width()) / 2;
                            top = (rectF.height() - mBoundingRect.height()) / 2;
                            mBoundingRect = new RectF(left, top, left + mBoundingRect.width(), top + mBoundingRect.height());
                        } else if (left == 0) {
                            left = (rectF.width() - mBoundingRect.width()) / 2;
                            mBoundingRect = new RectF(left, top, left + mBoundingRect.width(), bottom);
                        } else if (top == 0) {
                            top = (rectF.height() - mBoundingRect.height()) / 2;
                            mBoundingRect = new RectF(left, top, right, top + mBoundingRect.height());
                        }
                        if (mImageViewListener.getOriginalScale() != mImageViewListener.getContainerScale()) {
                            float scale = 1 / mImageViewListener.getOriginalScale();
                            RectF boundingRect = FTGeometryUtils.scaleRect(mBoundingRect, scale);
                            mBoundingRect = FTGeometryUtils.scaleRect(boundingRect, mImageViewListener.getContainerScale());
                        }
                    } else {

                        float actualWidth = mBoundingRect.width() / 2;
                        float actualHeight = mBoundingRect.height() / 2;
                        SizeF finalSize = FTGeometryUtils.aspectSize(new SizeF(boundedRect.width(), boundedRect.height()), new SizeF(mBoundingRect.width(), mBoundingRect.height()));
                        float croppedWidth = finalSize.getWidth() / 2;
                        float croppedHeight = finalSize.getHeight() / 2;

                        mBoundingRect.left = mBoundingRect.left + actualWidth - croppedWidth;
                        mBoundingRect.right = mBoundingRect.right - actualWidth + croppedWidth;
                        mBoundingRect.top = mBoundingRect.top + actualHeight - croppedHeight;
                        mBoundingRect.bottom = mBoundingRect.bottom - actualHeight + croppedHeight;
                    }
                    mImageView = new StickerImageView(getActivity());
                    mImageView.setImageBitmap(bitmap);
                    mImageView.setBoundingRect(mBoundingRect);
                    mImageView.setOnRotationCallback(FTImageEditFragment.this);
                    layMain.removeAllViews();
                    layMain.addView(mImageView);
                    mImageView.init(getContext(), Gravity.CENTER);
                    int[] location = new int[2];
                    layMain.getLocationOnScreen(location);
                    mImageView.parentX = location[0] + visibleRect.left;
                    mImageView.parentY = location[1] + visibleRect.top;
                    mImageView.setVisibility(View.INVISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mImageView.setRotation(ftAnnotation.getImgAngel());
                            mImageView.setX(mBoundingRect.left);
                            mImageView.setY(mBoundingRect.top);
                            mImageView.setVisibility(View.VISIBLE);
                        }
                    }, 50);

                    layMain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideTool();
                            onFinish(bitmap, mImageView.getNewBoundingRect());
                        }
                    });
                }
            }
        }, 50);

    }

    //endregion

    //region Listener Callbacks

    @Override
    public void showTool(RectF currentBoundingRect) {

        float xCord = Math.max(0, Math.min(getView().getWidth(), currentBoundingRect.left + (currentBoundingRect.right - currentBoundingRect.left) / 2)) + visibleRect.left;
        float yCord = Math.max(0, currentBoundingRect.top + visibleRect.top);

        popupList = new FTPopupView(view.getContext());
        popupList.showPopupListWindow(view, 0, xCord, yCord, popupMenuItemList, new FTPopupView.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                if (position == 0) {
                    FTFirebaseAnalytics.logEvent("Media_Delete");
                    if (ftAnnotation != null) {
                        ftAnnotation.hidden = false;
                        ftAnnotation.delete();
                        mImageViewListener.removeAnnotation(ftAnnotation);
                    }

                    if (mImageViewListener != null)
                        mImageViewListener.onAnnotationEditFinish();
                } else if (position == 1) {
                    FTFirebaseAnalytics.logEvent("Media_Edit");
                    try {

                        ftAnnotation.setImgAngel(mImageView.getRotation());
                        ftAnnotation.setImgTxMatrix(mImageView.getTransformMatrix());

                        String filename = "bitmap.png";
                        FileOutputStream stream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                        ftAnnotation.getImage().compress(Bitmap.CompressFormat.PNG, 100, stream);

                        //Cleanup
                        stream.close();
                        FTDocumentActivity.isAnnotationOpen = 1;
                        FTImageAdvanceEditingAcitivity.start(getActivity(), filename);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (position == 2) {
                    FTFirebaseAnalytics.logEvent("Media_Lock");
                    ftAnnotation.setImageLockStatus(1);
                    hideTool();
                    outsideClick();
                }

            }
        });

    }

    @Override
    public void hideTool() {
        if (popupList != null)
            popupList.hidePopupListWindow();
    }

    @Override
    public void onLongPress() {
        enableDrag();
    }

    private void onFinish(Bitmap bitmap, RectF bounedingRect) {
        RectF boundingRect = FTGeometryUtils.scaleRect(bounedingRect, 1 / mImageViewListener.getContainerScale());
        if (ftAnnotation.isNew) {
            ftAnnotation.setImage(bitmap);
            ftAnnotation.setBoundingRect(boundingRect);
            ftAnnotation.setImgAngel(mImageView.getRotation());
            ftAnnotation.setImgTxMatrix(mImageView.getTransformMatrix());
            ftAnnotation.hidden = false;
            ftAnnotation.isNew = false;
            mImageViewListener.addAnnotation(ftAnnotation);
            if (ftAnnotation != null)
                ftAnnotation.associatedPage.getParentDocument().imageRecognitionHelper(getContext()).wakeUpRecognitionHelperIfNeeded();
        } else {
            FTImageAnnotationV1 helperAnnotation = new FTImageAnnotationV1(getContext(), ftAnnotation.associatedPage);
            helperAnnotation.setBoundingRect(boundingRect);
            helperAnnotation.setImgAngel(mImageView.getRotation());
            helperAnnotation.setImgTxMatrix(mImageView.getTransformMatrix());
            helperAnnotation.setBitmap(bitmap);
            mImageViewListener.updateAnnotation(ftAnnotation, helperAnnotation);

            if (isBitmapEdited && ftAnnotation != null) {
                ftAnnotation.associatedPage.getParentDocument().imageRecognitionHelper(getContext()).wakeUpRecognitionHelperIfNeeded();
            }
        }

        if (mImageViewListener != null)
            mImageViewListener.onAnnotationEditFinish();
    }

    private void enableDrag() {
        final Uri uri = FileUriUtils.getUriForFile(getContext(), new File(ftAnnotation.getImageUrl().getPath()));
        ClipDescription NOTE_STREAM_TYPES = new ClipDescription(null,
                new String[]{ClipDescription.MIMETYPE_TEXT_URILIST});
        ClipData clipData = new ClipData(NOTE_STREAM_TYPES, new ClipData.Item(uri));
        // Must include DRAG_FLAG_GLOBAL to allow for dragging data
        // between apps. This example provides read-only access
        // to the data.
        // Instantiates the drag shadow builder.
        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(mImageView);
        int flags = View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ;
        mImageView.startDragAndDrop(clipData, myShadow, null, flags);
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            // Creates a draggable image that will fill the Canvas provided by the system.
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() / 2;

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() / 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }
}
