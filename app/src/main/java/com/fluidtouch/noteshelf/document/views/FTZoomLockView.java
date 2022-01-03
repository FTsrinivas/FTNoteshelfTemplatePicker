package com.fluidtouch.noteshelf.document.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf2.R;

import org.opencv.core.Point;
import org.opencv.core.Size;

public class FTZoomLockView extends FrameLayout {
    private ZoomLockCallbacks mZoomLockCallbacks;
    private GestureDetector mSingleTapGestureDetector;

    public FTZoomLockView(@NonNull Context context) {
        super(context);
        init();
    }

    public FTZoomLockView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FTZoomLockView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FTZoomLockView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.zoom_lock_dialog_layout, null);
        addView(view);
        (view.findViewById(R.id.zoom_lock_root_layout)).setOnTouchListener(mParentOnTouchListener);
        mSingleTapGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                mZoomLockCallbacks.onClick(FTZoomLockView.this);
                return true;
            }
        });
    }

    public void setmZoomLockCallbacks(ZoomLockCallbacks zoomLockCallbacks) {
        mZoomLockCallbacks = zoomLockCallbacks;
    }

    public void setPosition(Point position) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.leftMargin = (int) position.x;
        params.topMargin = (int) position.y;
        setLayoutParams(params);
    }

    private View.OnTouchListener mParentOnTouchListener = new View.OnTouchListener() {
        private int xDelta;
        private int yDelta;

        private float xParentInitial;
        private float yParentInitial;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            mSingleTapGestureDetector.onTouchEvent(event);
            mZoomLockCallbacks.isDragMode(true);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) getLayoutParams();

                    xParentInitial = x;
                    yParentInitial = y;

                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    return true;

                case MotionEvent.ACTION_UP:
                    mZoomLockCallbacks.isDragMode(false);
                    if (x == xParentInitial && y == yParentInitial) {
                        return false;
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                    int bundX = x - xDelta;
                    int bundY = y - yDelta;
                    if (bundX >= 0 && (bundX + getWidth()) < mZoomLockCallbacks.getContainerSize().width) {
                        layoutParams.leftMargin = x - xDelta;
                    }
                    if (bundY >= 0 && (bundY + getHeight()) < mZoomLockCallbacks.getContainerSize().height) {
                        layoutParams.topMargin = y - yDelta;
                    }

                    layoutParams.rightMargin = 0;
                    layoutParams.bottomMargin = 0;
                    mZoomLockCallbacks.onPositionChange(new Point(layoutParams.leftMargin, layoutParams.topMargin));
                    setLayoutParams(layoutParams);

                    return true;

                default:
                    break;
            }

            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                mZoomLockCallbacks.isDragMode(false);
            }
            return false;
        }
    };

    public interface ZoomLockCallbacks {
        Size getContainerSize();

        void onPositionChange(Point point);

        void isDragMode(boolean isInDrag);

        void onClick(View view);
    }
}
