package com.fluidtouch.noteshelf.document.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.zoomlayout.Gestures.FTMotionEventHelper;

import java.lang.reflect.Field;

public class FTViewPager extends ViewPager {
    float mStartDragX;
    OnSwipeOutListener mListener;
    private OnPageChangeListener _listener;
    private FTPagerContainerCallback mContainerCallback;

    public FTViewPager(@NonNull Context context) {
        super(context);
        setMyScroller();
    }

    public FTViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMyScroller();
    }

    @Override
    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        super.addOnPageChangeListener(listener);
        _listener = listener;
    }

    @Override
    public void removeOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        super.removeOnPageChangeListener(listener);
        if (listener == _listener) {
            _listener = null;
        }
    }

    @Override
    public void clearOnPageChangeListeners() {
        super.clearOnPageChangeListeners();
        _listener = null;
    }

    public void setCurrentItem(int item, boolean smoothScroll, boolean forcibly) {
        int currentIndex = getCurrentItem();
        super.setCurrentItem(item, smoothScroll);
        if (forcibly && currentIndex == item) {
            _listener.onPageSelected(getCurrentItem());
        }
    }

    public void setCallbacksListener(FTPagerContainerCallback containerCallback) {
        this.mContainerCallback = containerCallback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            if (mContainerCallback != null && !mContainerCallback.isAllowScroll()) {
                return false;
            }
            if (FTMotionEventHelper.hasAnyStylustouch(event) && mContainerCallback.currentMode() != FTToolBarTools.VIEW) {
                return false;
            }
            boolean value = super.onInterceptTouchEvent(event);
            return value;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // pointerIndex out of range
        }
        return false;
    }

    //down one is added for smooth scrolling

    private void setMyScroller() {
        setPageMargin(ScreenUtil.convertDpToPx(getContext(), 10));
        setOffscreenPageLimit(1);
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnSwipeOutListener {
        void onSwipeOutAtStart();

        void onSwipeOutAtEnd();
    }

    public interface FTPagerContainerCallback {
        boolean isAllowScroll();

        FTToolBarTools currentMode();
    }

    public class MyScroller extends Scroller {
        public MyScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, 350 /*1 secs*/);
        }
    }
}
