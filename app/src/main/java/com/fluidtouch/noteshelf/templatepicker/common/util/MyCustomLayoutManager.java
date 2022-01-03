package com.fluidtouch.noteshelf.templatepicker.common.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class MyCustomLayoutManager extends LinearLayoutManager {
    //We need mContext to create our LinearSmoothScroller
    private Context mContext;
    private static final float MILLISECONDS_PER_INCH = 50f;

    public MyCustomLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    //Override this method? Check.
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state, int position) {

        //Create your RecyclerView.SmoothScroller instance? Check.
        LinearSmoothScroller smoothScroller =
                new LinearSmoothScroller(mContext) {

                    //Automatically implements this method on instantiation.
                    @Override
                    public PointF computeScrollVectorForPosition
                    (int targetPosition) {
                        return null;
                    }

                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    @Override
                    protected float calculateSpeedPerPixel
                    (DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH/displayMetrics.densityDpi;
                    }
                };

        //Docs do not tell us anything about this,
        //but we need to set the position we want to scroll to.
        smoothScroller.setTargetPosition(position);

        //Call startSmoothScroll(SmoothScroller)? Check.
        startSmoothScroll(smoothScroller);
    }
}
