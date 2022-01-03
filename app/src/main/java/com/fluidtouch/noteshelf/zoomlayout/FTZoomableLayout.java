package com.fluidtouch.noteshelf.zoomlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.SPenSupport;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTGestureRecognizerDelegate;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTGestureRecognizerState;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTMotionEventHelper;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTPanGestureRecognizer;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTPinchGestureDetector;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class FTZoomableLayout extends FrameLayout {
    public static final String TAG = FTZoomableLayout.class.getSimpleName();
    private static final int DEF_ZOOM_DURATION = 50;
    public boolean DEBUG = false;
    protected FTZoomLayout.FTZoomLayoutContainerCallback mContainerCallback;
    protected boolean isZoomLocked = false;
    protected int currentTooltype = MotionEvent.TOOL_TYPE_FINGER;
    boolean mAllowScale = false;
    RectF mDrawRect = new RectF();
    RectF mViewPortRect = new RectF();
    // allow parent views to intercept any touch events that we do not consume
    boolean mAllowParentInterceptOnEdge = true;
    // allow parent views to intercept any touch events that we do not consume even if we are in a scaled state
    boolean mAllowParentInterceptOnScaled = false;
    boolean isScaleEnd = true;
    private boolean allowsZoomAnimation = true;
    private GestureListener mGestureListener;

    private MotionEvent event;

    private SimpleOnGlobalLayoutChangedListener mSimpleOnGlobalLayoutChangedListener;
    private Matrix mScaleMatrix = new Matrix();
    private Matrix mScaleMatrixInverse = new Matrix();
    private Matrix mTranslateMatrix = new Matrix();
    private Matrix mTranslateMatrixInverse = new Matrix();
    // helper array to save heap
    private float[] mMatrixValues = new float[9];
    private float mFocusY;
    private float mFocusX;
    // Helper array to save heap
    private float[] mArray = new float[6];
    // for set scale
    private boolean mAllowOverScale = true;
    private FlingRunnable mFlingRunnable;
    private AnimatedZoomRunnable mAnimatedZoomRunnable;
    private Interpolator mAnimationInterpolator = new DecelerateInterpolator();
    private int mZoomDuration = DEF_ZOOM_DURATION;
    // minimum scale of the content
    private float mMinScale = 1.0f;
    // maximum scale of the content
    private float mMaxScale = 4.0f;
    public float currentScale = 1f;
    private boolean mAllowZoom = true;
    // Listeners
    private ZoomDispatcher mZoomDispatcher = new ZoomDispatcher();
    private PanDispatcher mPanDispatcher = new PanDispatcher();
    private List<OnZoomListener> mOnZoomListeners;
    private List<OnPanListener> mOnPanListeners;
    private long scaleGestureStartTime = 0;
    private Handler scaleHandler = new Handler();
    private Handler cancellGestureHander;
    public String documentUid = "";
    Runnable scaleEndTask = new Runnable() {
        @Override
        public void run() {
            if (!isScaleEnd) {
                mGestureListener.onScaleEnd(null);
                isScaleEnd = true;
            }
        }
    };

    public void enableAllGesture(boolean enable) {
        mGestureListener.setIsEnabled(enable);
    }

    public FTZoomableLayout(Context context) {
        super(context);
        init(context, null);
    }

    public FTZoomableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FTZoomableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public FTZoomableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public static void removeGlobal(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        ViewTreeObserver obs = v.getViewTreeObserver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            obs.removeOnGlobalLayoutListener(listener);
        } else {
            obs.removeGlobalOnLayoutListener(listener);
        }
    }

    public boolean allowsFreeScroll(MotionEvent event) {
        return true;
    }

    private void init(Context context, AttributeSet attrs) {
        mGestureListener = new GestureListener(context);
        mSimpleOnGlobalLayoutChangedListener = new SimpleOnGlobalLayoutChangedListener();
        getViewTreeObserver().addOnGlobalLayoutListener(mSimpleOnGlobalLayoutChangedListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        FTZoomableLayout.removeGlobal(this, mSimpleOnGlobalLayoutChangedListener);
        super.onDetachedFromWindow();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(-getPosX(), -getPosY());
        float scale = getScale();
        canvas.scale(scale, scale, mFocusX, mFocusY);
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            // Few issues here, ignore for now.
            // NullPointerException
            // StackOverflowError when drawing childViews
        }
        canvas.restore();
    }

    /**
     * Although the docs say that you shouldn't override this, I decided to do
     * so because it offers me an easy way to change the invalidated area to my
     * likening.
     */
    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        scaledPointsToScreenPoints(dirty);
        float scale = getScale();
        location[0] *= scale;
        location[1] *= scale;
        return super.invalidateChildInParent(location, dirty);
    }

    private void scaledPointsToScreenPoints(Rect rect) {
        FTZoomUtils.setArray(mArray, rect);
        mArray = scaledPointsToScreenPoints(mArray);
        FTZoomUtils.setRect(rect, mArray);
    }

    private void scaledPointsToScreenPoints(RectF rect) {
        FTZoomUtils.setArray(mArray, rect);
        mArray = scaledPointsToScreenPoints(mArray);
        FTZoomUtils.setRect(rect, mArray);
    }

    private float[] scaledPointsToScreenPoints(float[] a) {
        mScaleMatrix.mapPoints(a);
        mTranslateMatrix.mapPoints(a);
        return a;
    }

    private float[] screenPointsToScaledPoints(float[] a) {
        mTranslateMatrixInverse.mapPoints(a);
        mScaleMatrixInverse.mapPoints(a);
        return a;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ev.setAction(SPenSupport.convertSPenEventAction(ev.getActionMasked()));
        mArray[0] = ev.getX();
        mArray[1] = ev.getY();
        screenPointsToScaledPoints(mArray);
        ev.setLocation(mArray[0], mArray[1]);
        currentTooltype = ev.getToolType(0);
        if (mContainerCallback.currentMode() == FTToolBarTools.LASSO) {
            if (FTMotionEventHelper.hasAnyStylustouch(ev))
                mAllowZoom = false;
            else if (FTApp.getPref().isStylusEnabled())
                mAllowZoom = true;
            else
                mAllowZoom = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mAllowZoom)
            return mAllowZoom;
        if (isAllowZoom() && ev.getAction() == MotionEvent.ACTION_DOWN && mContainerCallback.isInsideAudio(ev)) {
            setAllowZoom(false);
            mContainerCallback.setAudioMode(true);
        }
        return mAllowZoom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mArray[0] = ev.getX();
        mArray[1] = ev.getY();
        scaledPointsToScreenPoints(mArray);
        ev.setLocation(mArray[0], mArray[1]);
        if (!mAllowZoom) {
            return false;
        }
        event = ev;
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        boolean consumed = mGestureListener.onTouchEvent(ev);
        if (action == MotionEvent.ACTION_UP) {
            // manually call up
            consumed = mGestureListener.onUp(ev) || consumed;
        }
        if (action == MotionEvent.ACTION_CANCEL && isTranslating()) {
            consumed = mGestureListener.onUp(ev) || consumed;
        }
        return consumed;
    }

    public void resetToNormal() {
        mScaleMatrix = new Matrix();
        mScaleMatrixInverse = new Matrix();
        mTranslateMatrix = new Matrix();
        mTranslateMatrixInverse = new Matrix();

        mDrawRect = new RectF();
        mViewPortRect = new RectF();

        matrixUpdated();
    }

    public void setContentOffset(float x, float y) {
        PointF p = getClosestValidTranslationPoint();

        View child = getChildAt(0);
        RectF childFrame = new RectF(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        if (child.isLayoutRequested()) {
            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) child.getLayoutParams();
            childFrame = new RectF(lParams.leftMargin, lParams.topMargin, lParams.width, lParams.height);
        }

        float width = childFrame.width();
        float height = childFrame.height();

        if (width > getWidth()) {
            p.x = x;
        }
        if (height > getHeight()) {
            p.y = y;
        }


        float maxX = p.x + Math.min(getWidth(), width);
        if (maxX > width) {
            p.x -= maxX - width;
        }
        float maxY = p.y + Math.min(getHeight(), height);
        ;
        if (maxY > height) {
            p.y -= maxY - height;
        }
        internalMove(p.x, p.y, false);
    }

    /**
     * When setting a new focus point, the translations on scale-matrix will change,
     * to counter that we'll first read old translation values, then apply the new focus-point
     * (with the old scale), then read the new translation values. Lastly we'll ensureTranslation
     * out ensureTranslation-matrix by the delta given by the scale-matrix translations.
     *
     * @param focusX focus-focusX in screen coordinate
     * @param focusY focus-focusY in screen coordinate
     */
    private void fixFocusPoint(float focusX, float focusY) {
        mArray[0] = focusX;
        mArray[1] = focusY;
        screenPointsToScaledPoints(mArray);
        // The first scale event translates the content, so we'll counter that ensureTranslation
        float x1 = getMatrixValue(mScaleMatrix, Matrix.MTRANS_X);
        float y1 = getMatrixValue(mScaleMatrix, Matrix.MTRANS_Y);
        internalScale(getScale(), mArray[0], mArray[1]);
        float dX = getMatrixValue(mScaleMatrix, Matrix.MTRANS_X) - x1;
        float dY = getMatrixValue(mScaleMatrix, Matrix.MTRANS_Y) - y1;
        internalMove(dX + getPosX(), dY + getPosY(), false);
    }

    private void cancelFling() {
        if (mFlingRunnable != null) {
            mFlingRunnable.cancelFling();
            mFlingRunnable = null;
        }
    }

    private void cancelZoom() {
        if (mAnimatedZoomRunnable != null) {
            mAnimatedZoomRunnable.cancel();
            mAnimatedZoomRunnable = null;
        }
    }

    /**
     * The rectangle representing the location of the view inside the ZoomView. including scale and translations.
     */
    public RectF getDrawRect() {
        return new RectF(mDrawRect);
    }

    public boolean isAllowOverScale() {
        return mAllowOverScale;
    }

    public void setAllowOverScale(boolean allowOverScale) {
        mAllowOverScale = allowOverScale;
    }

    public boolean isAllowParentInterceptOnEdge() {
        return mAllowParentInterceptOnEdge;
    }

    public void setAllowParentInterceptOnEdge(boolean allowParentInterceptOnEdge) {
        mAllowParentInterceptOnEdge = allowParentInterceptOnEdge;
    }

    public boolean isAllowParentInterceptOnScaled() {
        return mAllowParentInterceptOnScaled;
    }

    public void setAllowParentInterceptOnScaled(boolean allowParentInterceptOnScaled) {
        mAllowParentInterceptOnScaled = allowParentInterceptOnScaled;
    }

    public int getZoomDuration() {
        return mZoomDuration;
    }

    public void setZoomDuration(int zoomDuration) {
        mZoomDuration = zoomDuration < 0 ? DEF_ZOOM_DURATION : zoomDuration;
    }

    public void setZoomInterpolator(Interpolator zoomAnimationInterpolator) {
        mAnimationInterpolator = zoomAnimationInterpolator;
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    public void setMaxScale(float maxScale) {
        mMaxScale = maxScale;
        if (mMaxScale < mMinScale) {
            setMinScale(maxScale);
        }
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
        if (mMinScale > mMaxScale) {
            setMaxScale(mMinScale);
        }
    }

    public boolean isAllowZoom() {
        return mAllowZoom;
    }

    public void setAllowZoom(boolean allowZoom) {
        mAllowZoom = allowZoom;
    }

    public float getScale() {
        return getMatrixValue(mScaleMatrix, Matrix.MSCALE_X);
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
//        final View c = getChildAt(0);
        setScale(scale, getRight() / 2, getBottom() / 2, animate);
    }

    public boolean isTranslating() {
        return mGestureListener.mScrolling;
    }

    public boolean isScaling() {
        return mGestureListener.isInProgress();
    }

    public boolean isScaled() {
        return !NumberUtils.isEqual(getScale(), 1.0f, 0.05f);
    }

    public void setScale(float scale, float focusX, float focusY, boolean animate) {
        if (!mAllowZoom) {
            return;
        }
        fixFocusPoint(focusX, focusY);
        if (!mAllowOverScale) {
            scale = NumberUtils.clamp(mMinScale, scale, mMaxScale);
        }
//        if (animate) {
//            mAnimatedZoomRunnable = new AnimatedZoomRunnable();
//            mAnimatedZoomRunnable.scale(getScale(), scale, mFocusX, mFocusY, true);
//            ViewCompat.postOnAnimation(this, mAnimatedZoomRunnable);
//        } else {
        mZoomDispatcher.onZoomBegin(getScale());
        internalScale(scale, mFocusX, mFocusY);
        mZoomDispatcher.onZoom(scale);
        mZoomDispatcher.onZoomEnd(scale);
//        }
    }

    public boolean moveBy(float dX, float dY) {
        return moveTo(dX + getPosX(), dY + getPosY());
    }

    public boolean moveTo(float posX, float posY) {
        mPanDispatcher.onPanBegin();
        if (internalMove(posX, posY, true)) {
            mPanDispatcher.onPan();
        }
        mPanDispatcher.onPanEnd();
        return true;
    }

    private boolean internalMoveBy(float dx, float dy, boolean clamp) {
        float tdx = dx;
        float tdy = dy;
        if (clamp) {
            RectF bounds = getTranslateDeltaBounds();
            tdx = NumberUtils.clamp(bounds.left, dx, bounds.right);
            tdy = NumberUtils.clamp(bounds.top, dy, bounds.bottom);
        }
//        L.d(TAG, String.format(Locale.US, "clamp: x[ %.2f -> %.2f ], y[ %.2f -> %.2f ]", dx, tdx, dy, tdy));
        float posX = tdx + getPosX();
        float posY = tdy + getPosY();
        if (!NumberUtils.isEqual(posX, getPosX()) ||
                !NumberUtils.isEqual(posY, getPosY())) {
            mTranslateMatrix.setTranslate(-posX, -posY);
            matrixUpdated();
            invalidate();
            return true;
        }
        return false;
    }

    private boolean internalMove(float posX, float posY, boolean clamp) {
//        L.d(TAG, String.format(Locale.US, "internalMove: x[ %.2f -> %.2f ], y[ %.2f -> %.2f ]", getPosX(), posX, getPosY(), posY));
        return internalMoveBy(posX - getPosX(), posY - getPosY(), clamp);
    }

    private RectF getTranslateDeltaBounds() {
        RectF r = new RectF();
        float maxDeltaX = mDrawRect.width() - mViewPortRect.width();
        if (maxDeltaX < 0) {
            float leftEdge = Math.round((mViewPortRect.width() - mDrawRect.width()) / 2);
            if (leftEdge > mDrawRect.left) {
                r.left = 0;
                r.right = leftEdge - mDrawRect.left;
            } else {
                r.left = leftEdge - mDrawRect.left;
                r.right = 0;
            }
        } else {
            r.left = mDrawRect.left - mViewPortRect.left;
            r.right = r.left + maxDeltaX;
        }

        float maxDeltaY = mDrawRect.height() - mViewPortRect.height();
        if (maxDeltaY < 0) {
            float topEdge = Math.round((mViewPortRect.height() - mDrawRect.height()) / 2f);
            if (topEdge > mDrawRect.top) {
                r.top = mDrawRect.top - topEdge;
                r.bottom = 0;
            } else {
                r.top = topEdge - mDrawRect.top;
                r.bottom = 0;
            }
        } else {
            r.top = mDrawRect.top - mViewPortRect.top;
            r.bottom = r.top + maxDeltaY;
        }

        return r;
    }

    /**
     * Gets the closest valid translation point, to the current {@link #getPosX() x} and
     * {@link #getPosY() y} coordinates.
     *
     * @return the closest point
     */
    private PointF getClosestValidTranslationPoint() {
        PointF p = new PointF(getPosX(), getPosY());
        if (mDrawRect.width() < mViewPortRect.width()) {
            p.x += mDrawRect.centerX() - mViewPortRect.centerX();
        } else if (mDrawRect.right < mViewPortRect.right) {
            p.x += mDrawRect.right - mViewPortRect.right;
        } else if (mDrawRect.left > mViewPortRect.left) {
            p.x += mDrawRect.left - mViewPortRect.left;
        }
        if (mDrawRect.height() < mViewPortRect.height()) {
            p.y += mDrawRect.centerY() - mViewPortRect.centerY();
        } else if (mDrawRect.bottom < mViewPortRect.bottom) {
            p.y += mDrawRect.bottom - mViewPortRect.bottom;
        } else if (mDrawRect.top > mViewPortRect.top) {
            p.y += mDrawRect.top - mViewPortRect.top;
        }
        return p;
    }

    private void internalScale(float scale, float focusX, float focusY) {
        mFocusX = focusX;
        mFocusY = focusY;
        mScaleMatrix.setScale(scale, scale, mFocusX, mFocusY);
        matrixUpdated();
        requestLayout();
        invalidate();
    }

    /**
     * Update all variables that rely on the Matrix'es.
     */
    private void matrixUpdated() {
        // First inverse matrixes
        mScaleMatrix.invert(mScaleMatrixInverse);
        mTranslateMatrix.invert(mTranslateMatrixInverse);
        // Update DrawRect - maybe this should be viewPort.left instead of 0?
        FTZoomUtils.setRect(mViewPortRect, 0, 0, getWidth(), getHeight());

        final View child = getChildAt(0);
        if (child != null) {
            RectF childFrame = new RectF(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if (child.isLayoutRequested()) {
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) child.getLayoutParams();
                childFrame = new RectF(lParams.leftMargin, lParams.topMargin, lParams.width, lParams.height);
            }
            FTZoomUtils.setRect(mDrawRect, childFrame.left, childFrame.top, childFrame.right, childFrame.bottom);
            scaledPointsToScreenPoints(mDrawRect);
        } else {
            // If no child is added, then center the drawrect, and let it be empty
            float x = mViewPortRect.centerX();
            float y = mViewPortRect.centerY();
            mDrawRect.set(x, y, x, y);
        }
    }

    /**
     * Get the current x-translation
     */
    public float getPosX() {
        return -getMatrixValue(mTranslateMatrix, Matrix.MTRANS_X);
    }

    /**
     * Get the current y-translation
     */
    public float getPosY() {
        return -getMatrixValue(mTranslateMatrix, Matrix.MTRANS_Y);
    }

    /**
     * Read a specific value from a given matrix
     *
     * @param matrix The Matrix to read a value from
     * @param value  The value-position to read
     * @return The value at a given position
     */
    private float getMatrixValue(Matrix matrix, int value) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[value];
    }

    public void addOnZoomListener(OnZoomListener l) {
        if (mOnZoomListeners == null) {
            mOnZoomListeners = new ArrayList<>();
        }
        mOnZoomListeners.add(l);
    }

    public void removeOnZoomListener(OnZoomListener listener) {
        if (mOnZoomListeners != null) {
            mOnZoomListeners.remove(listener);
        }
    }

    public void clearOnZoomListeners() {
        if (mOnZoomListeners != null) {
            mOnZoomListeners.clear();
        }
    }

    public void addOnPanListener(OnPanListener l) {
        if (mOnPanListeners == null) {
            mOnPanListeners = new ArrayList<>();
        }
        mOnPanListeners.add(l);
    }

    public void removeOnPanListener(OnPanListener listener) {
        if (mOnPanListeners != null) {
            mOnPanListeners.remove(listener);
        }
    }

    public void clearOnPanListeners() {
        if (mOnPanListeners != null) {
            mOnPanListeners.clear();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new IllegalStateException("Cannot set OnClickListener, please use OnTapListener.");
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        throw new IllegalStateException("Cannot set OnLongClickListener, please use OnLongTabListener.");
    }

    @Override
    public void setOnTouchListener(View.OnTouchListener l) {
        throw new IllegalStateException("Cannot set OnTouchListener.");
    }

    public void updatePositions(float offsetX, float offsetY) {
        mOnPanListeners.get(0).onPanBegin(this, event);
        internalMoveBy(offsetX, offsetY, true);
        mOnPanListeners.get(0).onPanEnd(this, event);
    }

    public void setOnMouseWheelScroll(MotionEvent event) {
        if (isScaleEnd) {
            isScaleEnd = false;
            mAllowScale = false;
            mZoomDispatcher.onZoomBegin(getScale());
            fixFocusPoint(event.getX(), event.getY());
        }
        float scaleFactor = event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0 ? 1.1f : 0.9f;
        float prevScale = getScale();
        float scale = prevScale * scaleFactor;
        float differenceScale = abs(prevScale - scale);
        Log.d("PALM", "Scale Check" + differenceScale);
        if (differenceScale > 0.3 && !mAllowScale) {
            mAllowScale = true;
        }
        if (mAllowScale) {
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return;
            internalScale(scale, event.getX(), event.getY());
            mZoomDispatcher.onZoom(scale);
            if (scaleHandler != null)
                scaleHandler.removeCallbacks(scaleEndTask);
            scaleHandler.postDelayed(scaleEndTask, 500);
        }

    }

    public void setOnSpenKeyEvent(float scaleFactor) {
        if (isScaleEnd) {
            isScaleEnd = false;
            mAllowScale = false;
            mZoomDispatcher.onZoomBegin(getScale());
            fixFocusPoint(getDrawRect().width() / 2, getDrawRect().height() / 2);
        }
//        float scaleFactor = event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0 ? 1.1f : 0.9f;
        float prevScale = getScale();
        float scale = prevScale * scaleFactor;
        float differenceScale = abs(prevScale - scale);
        Log.d("PALM", "Scale Check" + differenceScale);
        if (!mAllowScale) {
            mAllowScale = true;
        }
        if (mAllowScale) {
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return;
            internalScale(scale, getDrawRect().width() / 2, getDrawRect().height() / 2);
            mZoomDispatcher.onZoom(scale);
            if (scaleHandler != null)
                scaleHandler.removeCallbacks(scaleEndTask);
            scaleHandler.postDelayed(scaleEndTask, 500);
        }

    }

    public void setZoomLocked(boolean isZoomLocked) {
        this.isZoomLocked = isZoomLocked;
    }

    interface AnimationCompleteListener {
        void onFinsh();
    }

    public interface OnZoomListener {
        void onZoomBegin(FTZoomableLayout view, float scale, MotionEvent ev);

        void onZoom(FTZoomableLayout view, float scale, MotionEvent ev);

        void onZoomEnd(FTZoomableLayout view, float scale, MotionEvent ev);
    }

    public interface OnPanListener {
        void onPanBegin(FTZoomableLayout view, MotionEvent ev);

        void onPan(FTZoomableLayout view, MotionEvent ev);

        void onPanEnd(FTZoomableLayout view, MotionEvent ev);
    }

    class GestureListener implements ScaleGestureDetector.OnScaleGestureListener,
            GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        private ScaleGestureDetector mScaleDetector;
        private GestureDetector mGestureDetector;

        private FTPanGestureRecognizer panGestureRecognizerToFail;
        private FTPinchGestureDetector pinchGestureRecognizerToFail;

        private boolean mScrolling = false;
        private boolean mIsFling = false;

        private MotionEvent event;

        private boolean isEnabled = true;

        GestureListener(Context context) {
            mScaleDetector = new ScaleGestureDetector(context, this);
            mScaleDetector.setStylusScaleEnabled(true);
            mScaleDetector.setQuickScaleEnabled(true);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                // Quick scale doesn't play nice with the current implementation
                mScaleDetector.setQuickScaleEnabled(false);
            }

            mGestureDetector = new GestureDetector(context, this);
            mGestureDetector.setIsLongpressEnabled(false);

            panGestureRecognizerToFail = new FTPanGestureRecognizer(context, null);

            pinchGestureRecognizerToFail = new FTPinchGestureDetector(context, new FTGestureRecognizerDelegate() {
                @Override
                public boolean shouldReceiveTouch() {
                    if (isZoomLocked || currentTooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                        return false;
                    }
                    return true;
                }

                @Override
                public void didRecognizedGesture() {
                    panGestureRecognizerToFail.state = FTGestureRecognizerState.RECOGNIZED;
                    Log.i("PINCH", "Pinch Recognized");
                }
            });
        }

        void setIsEnabled(boolean enable) {
            isEnabled = enable;
            enableAlLGesture(enable);
        }

        boolean getIsEnabled() {
            return isEnabled;
        }

        void enabledZoom(boolean enable) {
            pinchGestureRecognizerToFail.setEnabled(enable);
        }

        void enabledPan(boolean enable) {
            panGestureRecognizerToFail.setEnabled(enable);
        }

        void enableAlLGesture(boolean enable) {
            isEnabled = enable;
            enabledZoom(enable);
            enabledPan(enable);
        }

        boolean isInProgress() {
            return mScaleDetector.isInProgress();
        }

        boolean onTouchEvent(MotionEvent ev) {
            boolean consume = false;
            if (isEnabled) {
                pinchGestureRecognizerToFail.onTouchEvent(ev);
                panGestureRecognizerToFail.onTouchEvent(ev, allowsFreeScroll(ev));
                consume = mScaleDetector.onTouchEvent(ev);
                consume = mGestureDetector.onTouchEvent(ev) || consume;
            } else {
                Log.i("test", "test");
            }
            return consume;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("long", "enter");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            String deviceMan = android.os.Build.MANUFACTURER;
            if (isScaling() && !deviceMan.equalsIgnoreCase("samsung")) {
                return false;
            }
            if (panGestureRecognizerToFail.state != FTGestureRecognizerState.RECOGNIZED) {
                return true;
            }
            boolean consumed = false;
            if (!mScaleDetector.isInProgress() || deviceMan.equalsIgnoreCase("samsung")) {
                // only drag if we have one pointer and aren't already scaling
                if (currentScale <= mContainerCallback.getOriginalScale()) {
                    requestDisallowInterceptTouchEvent(false);
                    return consumed;
                }
                consumed = internalMoveBy(distanceX, distanceY, true);
                if (consumed) {
                    if (!mScrolling) {
                        mPanDispatcher.onPanBegin();
                        enabledZoom(false);
                        mScrolling = true;
                    }
                    mPanDispatcher.onPan();
                }
                if (mAllowParentInterceptOnEdge && !consumed && (!isScaled() || mAllowParentInterceptOnScaled)) {
                    requestDisallowInterceptTouchEvent(false);
                }
            }
            return consumed;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isScaling() || isZoomLocked) {
                return false;
            }
            if (!allowsFreeScroll(e2) || FTMotionEventHelper.hasAnyStylustouch(e2)) {
                return false;
            }

            float scale = getScale();
            float newScale = NumberUtils.clamp(mMinScale, scale, mMaxScale);
            if (NumberUtils.isEqual(newScale, scale)) {
                // only fling if no scale is needed - scale will happen on ACTION_UP
                mIsFling = true;
                mFlingRunnable = new FlingRunnable(getContext());
                mFlingRunnable.fling((int) velocityX, (int) velocityY);
                ViewCompat.postOnAnimation(FTZoomableLayout.this, mFlingRunnable);
                return true;
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onDown(MotionEvent e) {
            requestDisallowInterceptTouchEvent(true);
            cancelFling();
            cancelZoom();
            return false;
        }

        boolean onUp(MotionEvent e) {
            boolean consumed = false;
            if (mScrolling) {
                mPanDispatcher.onPanEnd();
                mIsFling = false;
                mScrolling = false;
                consumed = true;
                enableAlLGesture(true);
            }
            return consumed;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            boolean canAccept = false;
            ObservingService.getInstance().postNotification("isZoomLocked_" + documentUid, null);
            if (pinchGestureRecognizerToFail.state == FTGestureRecognizerState.RECOGNIZED && !mAllowScale) {
                if (isZoomLocked || currentTooltype == MotionEvent.TOOL_TYPE_STYLUS) {
                    return false;
                }
                isScaleEnd = false;
                mAllowScale = true;
                scaleGestureStartTime = detector.getEventTime();
                mZoomDispatcher.onZoomBegin(getScale());
                fixFocusPoint(detector.getFocusX(), detector.getFocusY());
                canAccept = true;
                enabledPan(false);
                Log.d("PALM", "Scale Began " + detector.getScaleFactor());
            }
            return canAccept;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                return false;
            }
            float prevScale = getScale();
            float scale = prevScale * scaleFactor;
            if (mAllowScale) {
                internalScale(scale, mFocusX, mFocusY);
                mZoomDispatcher.onZoom(scale);
                if (scaleHandler != null) {
                    scaleHandler.removeCallbacks(scaleEndTask);
                }
                scaleHandler.postDelayed(scaleEndTask, 300);
                return true;
            }
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaleEnd = true;
            if (scaleHandler != null)
                scaleHandler.removeCallbacks(scaleEndTask);
            boolean shouldCall = true;
            if (allowsZoomAnimation) {
                mAnimatedZoomRunnable = new AnimatedZoomRunnable();
                mAnimatedZoomRunnable.animListenr = new AnimationCompleteListener() {
                    @Override
                    public void onFinsh() {
                        mZoomDispatcher.onZoomEnd(getScale());
                        mAnimatedZoomRunnable = null;
                    }
                };
                shouldCall = !mAnimatedZoomRunnable.runValidation();
            }

            if (shouldCall) {
                mZoomDispatcher.onZoomEnd(getScale());
            }
            mAllowScale = false;
            enableAlLGesture(true);
        }
    }

    private class AnimatedZoomRunnable implements Runnable {

        boolean mCancelled = false;
        boolean mFinished = false;
        AnimationCompleteListener animListenr;

        private long mStartTime;
        private float mZoomStart, mZoomEnd, mFocalX, mFocalY;
        private float mStartX, mStartY, mTargetX, mTargetY;

        AnimatedZoomRunnable() {
            mStartTime = System.currentTimeMillis();
        }

        boolean doScale() {
            return !NumberUtils.isEqual(mZoomStart, mZoomEnd);
        }

        boolean doTranslate() {
            return !NumberUtils.isEqual(mStartX, mTargetX) || !NumberUtils.isEqual(mStartY, mTargetY);
        }

        boolean runValidation() {
            float scale = getScale();
            float newScale = NumberUtils.clamp(mMinScale, scale, mMaxScale);
            scale(scale, newScale, mFocusX, mFocusY, true);
            if (mAnimatedZoomRunnable.doScale() || mAnimatedZoomRunnable.doTranslate()) {
                ViewCompat.postOnAnimation(FTZoomableLayout.this, mAnimatedZoomRunnable);
                return true;
            }
            return false;
        }

        AnimatedZoomRunnable scale(float currentZoom, float targetZoom, float focalX, float focalY, boolean ensureTranslations) {

            mFocalX = focalX;
            mFocalY = focalY;
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
            if (doScale()) {
//                log(String.format("AnimatedZoomRunnable.Scale: %s -> %s", mZoomStart, mZoomEnd));
                mZoomDispatcher.onZoomBegin(getScale());
            }
            if (ensureTranslations) {
                mStartX = getPosX();
                mStartY = getPosY();
                boolean scale = doScale();
                if (scale) {
                    mScaleMatrix.setScale(mZoomEnd, mZoomEnd, mFocalX, mFocalY);
                    matrixUpdated();
                }
                PointF p = getClosestValidTranslationPoint();
                mTargetX = p.x;
                mTargetY = p.y;
                if (scale) {
                    mScaleMatrix.setScale(mZoomStart, mZoomStart, FTZoomableLayout.this.mFocusX, FTZoomableLayout.this.mFocusY);
                    matrixUpdated();
                }
                if (doTranslate()) {
//                    log(String.format(Locale.US, "AnimatedZoomRunnable.ensureTranslation x[%.0f -> %.0f], y[%.0f -> %.0f]", mStartX, mTargetX, mStartY, mTargetY));
                    mPanDispatcher.onPanBegin();
                }
            }
            return this;
        }

        void cancel() {
            mCancelled = true;
            finish();
        }

        private void finish() {
            if (!mFinished) {
                if (null != animListenr) {
                    animListenr.onFinsh();
                } else {
                    if (doScale()) {
                        mZoomDispatcher.onZoomEnd(getScale());
                    }
                    if (doTranslate()) {
                        mPanDispatcher.onPanEnd();
                    }
                }
            }
            mFinished = true;
        }

        @Override
        public void run() {

            if (mCancelled || (!doScale() && !doTranslate())) {
                return;
            }

            float t = interpolate();
            if (doScale()) {
                float newScale = mZoomStart + t * (mZoomEnd - mZoomStart);
//                log(String.format(Locale.US, "AnimatedZoomRunnable.run.scale %.2f", newScale));
                internalScale(newScale, mFocalX, mFocalY);
                mZoomDispatcher.onZoom(newScale);
            }
            if (doTranslate()) {
                float x = mStartX + t * (mTargetX - mStartX);
                float y = mStartY + t * (mTargetY - mStartY);
//                log(String.format(Locale.US, "AnimatedZoomRunnable.run.translate x:%.0f, y:%.0f", x, y));
                internalMove(x, y, false);
                mPanDispatcher.onPan();
            }

            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                ViewCompat.postOnAnimation(FTZoomableLayout.this, this);
            } else {
                finish();
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            return mAnimationInterpolator.getInterpolation(t);
        }

    }

    private class FlingRunnable implements Runnable {

        private final FTZoomScrollerCompat mScroller;
        private int mCurrentX, mCurrentY;
        private boolean mFinished = false;

        FlingRunnable(Context context) {
            mScroller = FTZoomScrollerCompat.getScroller(context);
        }

        void fling(int velocityX, int velocityY) {

            final int startX = Math.round(mViewPortRect.left);
            final int minX, maxX;
            if (mViewPortRect.width() < mDrawRect.width()) {
                minX = Math.round(mDrawRect.left);
                maxX = Math.round(mDrawRect.width() - mViewPortRect.width());
            } else {
                minX = maxX = startX;
            }

            final int startY = Math.round(mViewPortRect.top);
            final int minY, maxY;
            if (mViewPortRect.height() < mDrawRect.height()) {
                minY = Math.round(mDrawRect.top);
                maxY = Math.round(mDrawRect.bottom - mViewPortRect.bottom);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

//            log(String.format("fling. x[ %s - %s ], y[ %s - %s ]", minX, maxX, minY, maxY));
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
                mPanDispatcher.onPanBegin();
            } else {
                mFinished = true;
            }

        }

        void cancelFling() {
            mScroller.forceFinished(true);
            finish();
        }

        private void finish() {
            if (!mFinished) {
                mPanDispatcher.onPanEnd();
            }
            mFinished = true;
        }

        public boolean isFinished() {
            return mScroller.isFinished();
        }

        @Override
        public void run() {
            if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {

                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();

//                log(String.format("mCurrentX:%s, newX:%s, mCurrentY:%s, newY:%s", mCurrentX, newX, mCurrentY, newY));
                if (internalMoveBy(mCurrentX - newX, mCurrentY - newY, true)) {
                    mPanDispatcher.onPan();
                } else {
                    mScroller.forceFinished(true);
                }

                mCurrentX = newX;
                mCurrentY = newY;

                // Post On animation
                ViewCompat.postOnAnimation(FTZoomableLayout.this, FlingRunnable.this);
            } else {
                finish();
            }
        }
    }

    private class ZoomDispatcher {

        int mCount = 0;

        void onZoomBegin(float scale) {
            if (mOnZoomListeners != null) {
                for (int i = 0, z = mOnZoomListeners.size(); i < z; i++) {
                    OnZoomListener listener = mOnZoomListeners.get(i);
                    if (listener != null) {
                        listener.onZoomBegin(FTZoomableLayout.this, scale, event);
                    }
                }
            }
        }

        void onZoom(float scale) {
            if (mOnZoomListeners != null) {
                for (int i = 0, z = mOnZoomListeners.size(); i < z; i++) {
                    OnZoomListener listener = mOnZoomListeners.get(i);
                    if (listener != null) {
                        listener.onZoom(FTZoomableLayout.this, scale, event);
                    }
                }
            }
        }

        void onZoomEnd(float scale) {
            if (mOnZoomListeners != null) {
                for (int i = 0, z = mOnZoomListeners.size(); i < z; i++) {
                    OnZoomListener listener = mOnZoomListeners.get(i);
                    if (listener != null) {
                        listener.onZoomEnd(FTZoomableLayout.this, scale, event);
                    }
                }
            }
        }
    }

    private class PanDispatcher {

        int mCount = 0;

        void onPanBegin() {
            if (mOnPanListeners != null) {
                for (int i = 0, z = mOnPanListeners.size(); i < z; i++) {
                    OnPanListener listener = mOnPanListeners.get(i);
                    if (listener != null) {
                        listener.onPanBegin(FTZoomableLayout.this, event);
                    }
                }
            }
        }

        void onPan() {
            if (mOnPanListeners != null) {
                for (int i = 0, z = mOnPanListeners.size(); i < z; i++) {
                    OnPanListener listener = mOnPanListeners.get(i);
                    if (listener != null) {
                        listener.onPan(FTZoomableLayout.this, event);
                    }
                }
            }
        }

        void onPanEnd() {
            if (mOnPanListeners != null) {
                for (int i = 0, z = mOnPanListeners.size(); i < z; i++) {
                    OnPanListener listener = mOnPanListeners.get(i);
                    if (listener != null) {
                        listener.onPanEnd(FTZoomableLayout.this, event);
                    }
                }
            }
        }
    }

    private class SimpleOnGlobalLayoutChangedListener implements ViewTreeObserver.OnGlobalLayoutListener {

        private int mLeft, mTop, mRight, mBottom;

        @Override
        public void onGlobalLayout() {
            int oldL = mLeft;
            int oldT = mTop;
            int oldR = mRight;
            int oldB = mBottom;
            mLeft = getLeft();
            mTop = getTop();
            mRight = getRight();
            mBottom = getBottom();
            boolean changed = oldL != mLeft || oldT != mTop || oldR != mRight || oldB != mBottom;
            if (changed) {
                matrixUpdated();
                PointF p = getClosestValidTranslationPoint();
                //mOnPanListeners.get(0).onPanBegin(FTZoomableLayout.this);
                internalMove(p.x, p.y, false);
                mOnPanListeners.get(0).onPanEnd(FTZoomableLayout.this, event);
            }
        }
    }
}