package com.fluidtouch.noteshelf.document.imageedit.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.fluidtouch.noteshelf2.R;

public class FTImageLassoView extends AppCompatImageView {

    private static final float TOLERANCE = 5;
    Bitmap mBitmap;
    Path lassoPath = new Path();
    Region lassoRegion;
    onImageLassoCallbacks mListener;
    private Context mContext;
    private Path mPath;
    private Paint mPaint;
    private float mX;
    private float mY;
    private float mInitX;
    private float mInitY;
    private Region mRegion;
    private boolean mCanProceedForNextAction = true;

    public FTImageLassoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FTImageLassoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FTImageLassoView(Context c) {
        super(c);
        mContext = c;
        mPath = new Path();
        mPaint = getPaint();
    }

    public void setUp() {
        FrameLayout frameLayout = new FrameLayout(mContext);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(mBitmap.getWidth(), mBitmap.getHeight()));
        frameLayout.setBackgroundResource(R.drawable.checks_background);
        mListener.addView(frameLayout);
        setLayoutParams(new FrameLayout.LayoutParams(mBitmap.getWidth(), mBitmap.getHeight()));
        frameLayout.addView(this);
        setImageBitmap(mBitmap);
        setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
    }

    public void setListener(onImageLassoCallbacks listener) {
        mListener = listener;
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
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mRegion != null) {
                    if (!mRegion.contains((int) x, (int) y)) {
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
                    if (mRegion.getBounds().width() > 50 && mRegion.getBounds().height() > 50) {
                        lassoPath = mPath;
                        lassoRegion = mRegion;
                        mListener.onEdited();
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
    }

    public Region getRegion() {
        return mRegion;
    }

    public void setRegion(Region region) {
        mRegion = region;
    }

    public Path getPath() {
        return lassoPath;
    }

    public Bitmap getlasooBitmap() {

        Rect rect = new Rect(0, 0, mBitmap.getWidth(),
                mBitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(mBitmap.getWidth(),
                mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        canvas.drawPath(mPath, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mBitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        rect = new Rect(mRegion.getBounds());
        output = Bitmap.createBitmap(output, Math.max(0, rect.left), Math.max(0, rect.top), Math.min(rect.width(), mBitmap.getWidth() - Math.max(0, rect.left)), Math.min(rect.height(), mBitmap.getHeight() - Math.max(0, rect.top)));
        return output;
    }

    public interface onImageLassoCallbacks {
        void onEdited();

        void addView(View view);
    }
}
