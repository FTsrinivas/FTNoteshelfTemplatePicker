package com.fluidtouch.noteshelf.document.imageedit.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.fluidtouch.noteshelf2.R;

import java.util.Arrays;
import java.util.Calendar;


public abstract class StickerView extends FrameLayout {

    //region Member Variables
    public static final String TAG = "stickerView";
    // For scalling
    public float parentX = 0, parentY = 0;
    protected ImageView iv_main;
    protected ImageView iv_scale;
    protected RectF boundingRect;
    protected Bitmap bitmap;
    Context mContext;
    float left;
    float top;
    float right;
    float bottom;
    RectF mBitmapRect = new RectF();
    float scale = 1;
    Callbacks parentCallback;
    private BorderView iv_border;
    private ImageView iv_rotate;
    private Matrix transform;
    private float scale_orgX = -1, scale_orgY = -1;
    // For moving
    private float move_orgX = -1, move_orgY = -1;
    private double centerX, centerY;
    private GestureDetector mSingleTapGestureDetector;
    private OnTouchListener mTouchListener = new OnTouchListener() {
        private static final int MAX_CLICK_DURATION = 200;
        private long startClickTime;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (parentCallback != null)
                parentCallback.hideTool();
            mSingleTapGestureDetector.onTouchEvent(event);
            if (view.getTag().equals("DraggableViewGroup")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        move_orgX = event.getRawX();
                        move_orgY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        getBoundaries();
                        float offsetX = event.getRawX() - move_orgX;
                        float offsetY = event.getRawY() - move_orgY;
                        float l = StickerView.this.getX() + offsetX;
                        float t = StickerView.this.getY() + offsetY;
                        float r = l + offsetX + getWidth();
                        float b = t + getHeight();
                        if (t > mBitmapRect.top && l > mBitmapRect.left && r < mBitmapRect.right && b < mBitmapRect.bottom) {
                            StickerView.this.setX(StickerView.this.getX() + offsetX);
                            StickerView.this.setY(StickerView.this.getY() + offsetY);
                            move_orgX = event.getRawX();
                            move_orgY = event.getRawY();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (parentCallback != null)
                            parentCallback.
                                    showTool(getRenderingBoundingRect());
                        break;
                }
            } else if (view.getTag().equals("iv_rotate")) {
//                mSingleTapGestureDetector.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();


                        centerX = StickerView.this.getX() +
                                parentX +
                                (float) StickerView.this.getWidth() / 2;


                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                parentY +
                                statusBarHeight +
                                (float) StickerView.this.getHeight() / 2;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < MAX_CLICK_DURATION) {
                            break;
                        }
                        double angle = Math.atan2(event.getRawY() - centerY, event.getRawX() - centerX) * 180 / Math.PI;
                        setRotation((float) angle - 45);

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        postInvalidate();
                        requestLayout();
                        break;
                    case MotionEvent.ACTION_UP:
                        long clickDurationUp = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDurationUp < MAX_CLICK_DURATION) {
                            if (getRotation() >= 0 && getRotation() < 90)
                                setRotation(90);
                            else if (getRotation() >= 90 && getRotation() < 180)
                                setRotation(180);
                            else if (getRotation() >= 180 && getRotation() < 270)
                                setRotation(270);
                            else if (getRotation() >= -180 && getRotation() < -90)
                                setRotation(270);
                            else if (getRotation() >= 270 && getRotation() < 360)
                                setRotation(0);
                            else if (getRotation() >= -90 && getRotation() < 0)
                                setRotation(0);
                        }
                        transform.setRotate(getRotation(), (float) centerX, (float) centerY);
                        if (parentCallback != null)
                            parentCallback.
                                    showTool(getRenderingBoundingRect());
                        break;
                }
            } else if (view.getTag().equals("iv_border")) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        centerX = StickerView.this.getX() +
                                parentX +
                                (float) StickerView.this.getWidth() / 2;


                        //double statusBarHeight = Math.ceil(25 * getContext().getResources().getDisplayMetrics().density);
                        int result = 0;
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            result = getResources().getDimensionPixelSize(resourceId);
                        }
                        double statusBarHeight = result;
                        centerY = StickerView.this.getY() +
                                parentY +
                                statusBarHeight +
                                (float) StickerView.this.getHeight() / 2;

                        break;
                    case MotionEvent.ACTION_MOVE:

                        double length1 = getLength(centerX, centerY, scale_orgX, scale_orgY);
                        double length2 = getLength(centerX, centerY, event.getRawX(), event.getRawY());

                        if (length2 > length1
                        ) {
                            //scale up
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            int width = StickerView.this.getLayoutParams().width;
                            width += offset;
                            Log.i("width", "width; " + width);
                            if (width > 100) {
                                StickerView.this.getLayoutParams().width = width;
                                StickerView.this.getLayoutParams().height = (int) (width / scale);
                            }

                        } else if (length2 < length1) {
                            //scale down
                            double offsetX = Math.abs(event.getRawX() - scale_orgX);
                            double offsetY = Math.abs(event.getRawY() - scale_orgY);
                            double offset = Math.max(offsetX, offsetY);
                            offset = Math.round(offset);
                            int width = StickerView.this.getLayoutParams().width;
                            width -= offset;
                            Log.i("width", "width; " + width);
                            if (width > 100) {
                                StickerView.this.getLayoutParams().width = width;
                                StickerView.this.getLayoutParams().height = (int) (width / scale);
                            }
                        }

                        scale_orgX = event.getRawX();
                        scale_orgY = event.getRawY();

                        postInvalidate();
                        requestLayout();
                        iv_border.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.v(TAG, "iv_scale action up");
                        if (parentCallback != null)
                            parentCallback.
                                    showTool(getRenderingBoundingRect());
                        setResizeArea(getNewBoundingRect());
                        break;
                }
            }
            return true;
        }
    };

    //region life cycle methods
    public StickerView(Context context) {
        super(context);
        //init(context);
    }
    //endregion

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init(context);
    }

    public void disableRotation() {
        iv_rotate.setVisibility(INVISIBLE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
    //endregion

    //region initialize view
    public void init(Context context, int gravity) {
        mContext = context;
        transform = new Matrix();
        scale = (float) bitmap.getWidth() / bitmap.getHeight();

        this.iv_border = new BorderView(context);
        this.iv_rotate = new ImageView(context);
        this.iv_main = new ImageView(context);
        this.iv_scale = new ImageView(context);

        this.iv_rotate.setImageResource(R.mipmap.sync);

        iv_scale.setTag("DraggableViewGroup");
        this.iv_border.setTag("iv_border");
        this.iv_rotate.setTag("iv_rotate");


        LayoutParams this_params =
                new LayoutParams(
                        (int) boundingRect.width(),
                        (int) boundingRect.height()
                );

        this_params.gravity = gravity;

        LayoutParams iv_main_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        LayoutParams iv_border_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

        LayoutParams iv_rotate_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        iv_rotate_params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        LayoutParams iv_scale_params =
                new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
//        int scalableMargin = getResources().getDimensionPixelSize(R.dimen._30dp);
//        iv_scale_params.setMargins(scalableMargin, scalableMargin, scalableMargin, scalableMargin);

        iv_main.setImageBitmap(bitmap);
        iv_border.setBoundingRect(boundingRect);
        this.setLayoutParams(this_params);
        this.addView(iv_main, iv_main_params);
        this.addView(iv_border, iv_border_params);
        this.addView(iv_scale, iv_scale_params);
        this.addView(iv_rotate, iv_rotate_params);
        this.iv_scale.setOnTouchListener(mTouchListener);
        this.iv_border.setOnTouchListener(mTouchListener);
        this.iv_rotate.setOnTouchListener(mTouchListener);
        mSingleTapGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
//                parentCallback.onLongPress();
            }
        });
        setResizeArea(boundingRect);
    }

    protected abstract Bitmap getBitmap();

    protected abstract RectF getBoundingRect();
    //endregion

    //region TouchListeners

    //region utility methods
    private double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }
    //endregion

    //region data methods
    private void setResizeArea(RectF boundingRect) {
        int scalableMargin = Math.max((int) (boundingRect.width() * .2), (int) (boundingRect.height() * .2));//getResources().getDimensionPixelSize(R.dimen._30dp);
        if (boundingRect.width() > boundingRect.height())
            ((LayoutParams) iv_scale.getLayoutParams()).setMargins(scalableMargin, 0, scalableMargin, 0);
        else
            ((LayoutParams) iv_scale.getLayoutParams()).setMargins(0, scalableMargin, 0, scalableMargin);
    }

    public String getTransformMatrix() {
        float[] v = new float[9];
        transform.getValues(v);
        String transformMatrix = Arrays.toString(v);
        return transformMatrix;
    }
    //endregion

    void getBoundaries() {
        left = -getWidth() * 3 / 4;
        top = -getHeight() * 3 / 4;
        right = ((FrameLayout) getParent()).getWidth() + (getWidth() * 3 / 4);
        bottom = ((FrameLayout) getParent()).getHeight() + (getHeight() * 3 / 4);
        mBitmapRect = new RectF(left, top, right, bottom);
    }

    public RectF getNewBoundingRect() {
        return new RectF(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    }

    public RectF getRenderingBoundingRect() {
        RectF mDisplayRect = new RectF();
        mDisplayRect.set(this.getNewBoundingRect());
        this.getTransMatrix().mapRect(mDisplayRect);
        return mDisplayRect;
    }

    private Matrix getTransMatrix() {
        Matrix matrix = new Matrix();
        matrix.setRotate(this.getRotation(), getNewBoundingRect().centerX(), getNewBoundingRect().centerY());
        return matrix;
    }

    public interface Callbacks {
        void showTool(RectF currentBoundingRect);

        void hideTool();

        void onLongPress();
    }
    //endregion
}
