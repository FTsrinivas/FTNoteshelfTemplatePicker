package com.fluidtouch.noteshelf.document.imageedit.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.fluidtouch.noteshelf2.R;

public class FTImageEraseView extends View implements View.OnTouchListener {

    private final Paint paint = new Paint();
    private final Paint eraserPaint = new Paint();
    Context mContext;
    Canvas bitmapCanvas;
    Bitmap mBitmap;
    Bitmap mPreBitmap;
    Bitmap bitmap;
    Path circlePath;
    Paint circlePaint;
    private float x = 0;
    private float y = 0;
    private boolean isFirst = true;
    private onImageEraseCallbacks mListener;

    public FTImageEraseView(Context context) {
        super(context);
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setOnTouchListener(this);
        // Set background
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    public void setUp() {
        // Set bitmap
        bitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas();
        bitmapCanvas.setBitmap(bitmap);
        bitmapCanvas.drawBitmap(mBitmap, 0, 0, null);

        circlePath = new Path();
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.TRANSPARENT);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        // Set eraser paint properties
        eraserPaint.setAlpha(0);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setStrokeWidth(40);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraserPaint.setAntiAlias(true);

        FrameLayout frameLayout = new FrameLayout(mContext);
        frameLayout.setLayoutParams(new LinearLayout.LayoutParams(mBitmap.getWidth(), mBitmap.getHeight()));
        frameLayout.setBackgroundResource(R.drawable.checks_background);
        mListener.addView(frameLayout);
        setLayoutParams(new FrameLayout.LayoutParams(mBitmap.getWidth(), mBitmap.getHeight()));
        frameLayout.addView(this);
    }

    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawBitmap(bitmap, 0, 0, paint);
//            bitmapCanvas.drawCircle(x, y, 30, eraserPaint);
        //bitmapCanvas.drawLine(x1, y1, x2, y2, eraserPaint);

        canvas.drawPath(circlePath, circlePaint);
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (isFirst) {
            circlePath.reset();
            circlePath.lineTo(x, y);
            x = (int) event.getX();
            y = (int) event.getY();
            isFirst = false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPreBitmap = getErasedBitmap();
        }
        //bitmapCanvas.drawCircle(x, y, 30, eraserPaint);
        bitmapCanvas.drawLine(x, y, event.getX(), event.getY(), eraserPaint);
        circlePath.lineTo(x, y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                //v.invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                invalidate();
                circlePath.reset();
                isFirst = true;
                mListener.onEraseTouch();
                mListener.onEdited();
                return true;

        }
        invalidate();
        return true;
    }

    public void setListener(onImageEraseCallbacks listener) {
        mListener = listener;
    }

    public void setBitmap(Bitmap bm) {
        mBitmap = bm;
    }

    public Bitmap getErasedBitmap() {
        Bitmap bitmapTemp = Bitmap.createBitmap(bitmap.getWidth()/*width*/, bitmap.getHeight()/*height*/, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapTemp);
        draw(canvas);
        return bitmapTemp;
    }

    public Bitmap getUndoBitmap() {
        return Bitmap.createBitmap(mPreBitmap);
    }

    public interface onImageEraseCallbacks {
        void onEraseTouch();

        void onEdited();

        void addView(View view);
    }
}