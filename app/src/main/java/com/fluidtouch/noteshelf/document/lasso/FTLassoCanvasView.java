package com.fluidtouch.noteshelf.document.lasso;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf2.BuildConfig;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Sreenu on 22/03/19
 */
public class FTLassoCanvasView extends AppCompatImageView {
    private static final float TOLERANCE = 5;
    private Path mPath;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private float mX;
    private float mY;
    private float mInitX;
    private float mInitY;
    private Region mRegion;
    private boolean mCanProceedForNextAction = true;
    private LassoCanvasContainerCallback mLassoCanvasContainerCallback;
    private GestureDetector longPressDetector;

    public FTLassoCanvasView(Context context) {
        super(context);
    }

    public FTLassoCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FTLassoCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FTLassoCanvasView(Context c, LassoCanvasContainerCallback lassoCanvasContainerCallback) {
        super(c);
        mLassoCanvasContainerCallback = lassoCanvasContainerCallback;
        mPath = new Path();
        mPaint = getPaint();

        longPressDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent event) {
                mCanProceedForNextAction = false;
                outsideTouch();
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                if (null != clipboardManager) {
                    try {
                        ClipData data = clipboardManager.getPrimaryClip();
                        if (null != data && data.getDescription().getLabel().equals(BuildConfig.APPLICATION_ID)) {
                            lassoCanvasContainerCallback.showLassoLongPressOptions(event);
                        }
                    } catch (Exception e) {
                        Log.i(this.getClass().getName(), e.getMessage());
                    }
                }

            }
        });
    }

    public Paint getPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);
        return paint;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    private void startDrawingLasso(float x, float y) {
        mCanProceedForNextAction = true;
        mPath = new Path();
        mPaint = getPaint();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mInitX = x;
        mInitY = y;
    }

    private void continueDrawingLasso(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void finishDrawingLasso() {
        mCanProceedForNextAction = false;
        mPath.lineTo(mInitX, mInitY);

        RectF rectF = new RectF();
        mPath.computeBounds(rectF, true);
        mRegion = new Region();
        mRegion.setPath(mPath, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        mPaint.setPathEffect(getDashPathEffect());
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    public DashPathEffect getDashPathEffect() {
        float[] intervals = new float[]{30.0f, 10.0f};
        float phase = 0;
        return new DashPathEffect(intervals, phase);
    }

    //override the onTouchEvent
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        boolean hasStylusTouch = false;
//        int touchesCount = event.getPointerCount();
//        for (int i = 0; i < touchesCount; i++) {
//            if (!(event.getToolType(i) != TOOL_TYPE_STYLUS || event.getToolType(i) != TOOL_TYPE_FINGER)) {
//                return true;
//            }
//            if (event.getToolType(i) == TOOL_TYPE_STYLUS)
//                hasStylusTouch = true;
//        }
//        if (!hasStylusTouch) {
//            return false;
//        }
        float x = event.getX();
        float y = event.getY();
        longPressDetector.onTouchEvent(event);
        mLassoCanvasContainerCallback.processEventForAudio(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mRegion != null) {
                    if (!mRegion.contains((int) x, (int) y) || mLassoCanvasContainerCallback.isInResizeMode()) {
                        outsideTouch();
                    }
                    return true;
                }
                startDrawingLasso(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCanProceedForNextAction) {
                    continueDrawingLasso(x, y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCanProceedForNextAction) {
                    finishDrawingLasso();
                    invalidate();
                    if (mRegion.getBounds().width() > 0 && mRegion.getBounds().height() > 0) {
                        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawPath(mPath, mPaint);
                        RectF rectF = new RectF();
                        mPath.computeBounds(rectF, true);
                        int offset = 3;
                        RectF finalRectF = new RectF((float) Math.floor(rectF.left) - offset, (float) Math.floor(rectF.top) - offset,
                                (float) Math.ceil(rectF.right) + offset, (float) Math.ceil(rectF.bottom) + offset);
                        mLassoCanvasContainerCallback.addLassoSelectionView(BitmapUtil.cropBitmap(bitmap, finalRectF), finalRectF, mRegion);
                        clearCanvas();
                    }
                }
                break;

            default:
                break;
        }
        return true;
    }

    public void outsideTouch() {
        mRegion = null;
        clearCanvas();
        mLassoCanvasContainerCallback.onOutsideTouch();
    }

    public void setRegion(Region region) {
        mRegion = region;
    }

    public interface LassoCanvasContainerCallback {

        void onOutsideTouch();

        void addLassoSelectionView(Bitmap bitmap, RectF rectF, Region mRegion);

        void showLassoLongPressOptions(MotionEvent event);

        void processEventForAudio(MotionEvent motionEvent);

        boolean isInResizeMode();
    }
}
