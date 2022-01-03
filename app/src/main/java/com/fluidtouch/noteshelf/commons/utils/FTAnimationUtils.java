package com.fluidtouch.noteshelf.commons.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fluidtouch.noteshelf2.R;

public class FTAnimationUtils extends AnimationUtils {

    public static void showStartPanelAnimation(Context context, View view, boolean isForwardNavigated, final AnimationListener listener) {
        Animation animation;
        if (isForwardNavigated) {
            animation = loadAnimation(context, R.anim.slide_start_left_to_right);
        } else {
            animation = loadAnimation(context, R.anim.slide_start_right_to_left);
        }

        if (listener != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listener.onAnimationEnd();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (view != null) {
            view.startAnimation(animation);
        }
    }

    public static void showEndPanelAnimation(Context context, View view, boolean isForwardNavigated, final AnimationListener listener) {
        Animation animation;
        if (isForwardNavigated) {
            animation = loadAnimation(context, R.anim.slide_end_right_to_left);
        } else {
            animation = loadAnimation(context, R.anim.slide_bottom);
        }

        if (listener != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listener.onAnimationEnd();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        if (view != null) {
            view.startAnimation(animation);
        }
    }

    public static void valueAnimatorTop(final View view, int start, int end) {
        valueAnimatorTop(view, start, end, 200);
    }

    public static void valueAnimatorTop(final View view, int start, int end, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void valueAnimatorBottom(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(0, 0, 0, (Integer) animation.getAnimatedValue());
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void valueAnimatorLeft(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins((Integer) animation.getAnimatedValue(), 0, 0, 0);
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public static void valueAnimatorRight(final View view, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
            lp.setMargins(0, 0, (Integer) animation.getAnimatedValue(), 0);
            view.setLayoutParams(lp);
        });
        animator.start();
    }

    public interface AnimationListener {
        void onAnimationEnd();
    }
}