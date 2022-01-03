package com.fluidtouch.noteshelf.welcome;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenAuthorisationFragment;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenFifthFragment;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenFirstFragment;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenFourthFragment;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenSecondFragment;
import com.fluidtouch.noteshelf.welcome.screens.FTWelcomeScreenThirdFragment;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.huawei.android.sdk.drm.Drm;
import com.huawei.android.sdk.drm.DrmCheckCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWelcomeScreenActivity extends AppCompatActivity implements FTWelcomeScreenCallback, ViewPager.OnPageChangeListener {
    @BindView(R.id.welcome_screen_skip_button)
    Button skipButton;
    @BindView(R.id.welcome_screen_next_button)
    Button nextButton;
    @BindView(R.id.welcome_screen_view_pager)
    ViewPager viewPager;

    public boolean areTermsAccepted = false;
    // DRM ID.
    private static final String DRM_ID = "2640091000002209153";
    // DRM key.
    private static final String DRM_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgePddyQ7P7Mp1zT8niJD2ajlNS1r+pQGsKvy04jQs1QDahljd1qe5/4kf4MNRVR68wckRVtabLgYvjy87ONtwWqX5ZyyENryRZ9ftW3FGdjeRCwQBEDLUtV/051SG9KbQxOESDTVDTyMZF0W7F3i/QCgKo9A9xq8pRUo3TgHgUkj46Ab1lUn5WrqJDVwfglaEWjEhd3xRt2Y8+grfbyTuGrULZgTK2kq8mEjsl+vXqhJ+yuxZybCM17bbHVqk5oAQnZH3om+nDIO8lBFUxvP/8VDB46PyljcR4xDLmw97QMk9fQ5D+UZcSG+JujydJC/ojkxXHI4tyuDnQQeuoSGvwIDAQAB";

    private class MyDrmCheckCallback implements DrmCheckCallback {
        @Override
        public void onCheckSuccess() {
            FTFirebaseAnalytics.logEvent("FTWelcomeScreenActivity", "WelcomeScreen", "Valid_Install");
            initUI();
        }

        @Override
        public void onCheckFailed(int errorCode) {
            finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize <= Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_welcome_screen);
        ButterKnife.bind(this);
        FTApp.getInstance().setCurActCtx(this);

        if (BuildConfig.FLAVOR.toLowerCase().contains("china")) {
            Drm.check(this, this.getPackageName(), DRM_ID, DRM_PUBLIC_KEY, new MyDrmCheckCallback());
        } else {
            initUI();
        }
    }

    public void initUI() {
        FTLog.crashlyticsLog(FTWelcomeScreenActivity.class.getSimpleName());
        skipButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        viewPager.addOnPageChangeListener(this);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FTWelcomeScreenFirstFragment());
        fragments.add(new FTWelcomeScreenSecondFragment());
        fragments.add(new FTWelcomeScreenThirdFragment());
        fragments.add(new FTWelcomeScreenFourthFragment());
        if (isForHuawei()) {
            fragments.add(new FTWelcomeScreenAuthorisationFragment());
        }
        fragments.add(new FTWelcomeScreenFifthFragment());
        FTWelcomeScreenAdapter adapter = new FTWelcomeScreenAdapter(fragments, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
    }

    @Override
    public void nextScreen() {
//        animatePagerTransition(true, 1);
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @OnClick(R.id.welcome_screen_skip_button)
    void onSkipClicked() {
//        startNoteTaking();
        if (isForHuawei()) {
            viewPager.setCurrentItem(viewPager.getChildCount() - 2, true);
//            animatePagerTransition(true, viewPager.getChildCount() - 2);
        } else {
            viewPager.setCurrentItem(viewPager.getChildCount() - 1, true);
//            animatePagerTransition(true, viewPager.getChildCount() - 1);
        }
    }

    @OnClick(R.id.welcome_screen_next_button)
    void onNextClicked() {
        nextScreen();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position <= 0 || position >= 4) {
            skipButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
        } else {
            skipButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
        }

        if (isForHuawei() && position == viewPager.getChildCount() - 1) {
            if (!areTermsAccepted) {
                viewPager.setCurrentItem(viewPager.getChildCount() - 2, true);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void animatePagerTransition(final boolean forward, int toPage) {
        ValueAnimator animator = ValueAnimator.ofInt(0, (viewPager.getWidth() - (forward ? viewPager.getPaddingLeft() : viewPager.getPaddingRight())) * toPage - 20);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewPager.endFakeDrag();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                viewPager.endFakeDrag();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private int oldDragPosition = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int dragPosition = (Integer) animation.getAnimatedValue();
                int dragOffset = dragPosition - oldDragPosition;
                oldDragPosition = dragPosition;
                viewPager.fakeDragBy(dragOffset * (forward ? -1 : 1));
            }
        });

        animator.setDuration(1000);
        viewPager.beginFakeDrag();
        animator.start();
    }

    protected boolean isForHuawei() {
//        return (FTApp.isForHuawei());
        return false;
    }
}