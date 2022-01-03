package com.fluidtouch.noteshelf.welcome.screens;

import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTWelcomeScreenFourthFragment extends Fragment {
    @BindView(R.id.screen4_pdf_image_view)
    ImageView pdfImageView;
    @BindView(R.id.screen4_png_image_view)
    ImageView pngImageView;
    @BindView(R.id.screen4_jpg_image_view)
    ImageView jpgImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        return inflater.inflate(R.layout.welcome_screen4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        FTLog.crashlyticsLog(FTWelcomeScreenFourthFragment.class.getSimpleName());

        resetLayoutParams();
    }

    @Override
    public void onResume() {
        super.onResume();
        float scale = Resources.getSystem().getDisplayMetrics().density;

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            //Animating Pdf ImageView
            ValueAnimator pdfAnimator = ValueAnimator.ofInt(0, (int) (133 * scale));
            pdfAnimator.setDuration(200);
            pdfAnimator.addUpdateListener(animation -> {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pdfImageView.getLayoutParams();
                lp.setMargins(-(Integer) animation.getAnimatedValue(), (int) (23 * scale), 0, 0);
                pdfImageView.setLayoutParams(lp);
            });


            //Animating Png ImageView
            ValueAnimator pngAnimator = ValueAnimator.ofInt(0, (int) (162 * scale));
            pngAnimator.setDuration(200);
            pngAnimator.addUpdateListener(animation -> {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pngImageView.getLayoutParams();
                lp.setMargins(0, 0, -(Integer) animation.getAnimatedValue(), (int) (50 * scale));
                pngImageView.setLayoutParams(lp);
            });

            //Animating jpg ImageView
            ValueAnimator jpgAnimator = ValueAnimator.ofInt(0, (int) (152 * scale));
            jpgAnimator.setDuration(200);
            jpgAnimator.addUpdateListener(animation -> {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) jpgImageView.getLayoutParams();
                lp.setMargins(-(Integer) animation.getAnimatedValue(), 0, 0, (int) (40 * scale));
                jpgImageView.setLayoutParams(lp);
            });


            new Handler().postDelayed(pdfAnimator::start, 100);
            new Handler().postDelayed(pngAnimator::start, 200);
            new Handler().postDelayed(jpgAnimator::start, 300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        resetLayoutParams();
    }

    private void resetLayoutParams() {
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            float scale = Resources.getSystem().getDisplayMetrics().density;

            pdfImageView.clearAnimation();
            jpgImageView.clearAnimation();
            pngImageView.clearAnimation();

            RelativeLayout.LayoutParams pdfImageViewLayoutParams = (RelativeLayout.LayoutParams) pdfImageView.getLayoutParams();
            pdfImageViewLayoutParams.leftMargin = (int) (10 * scale);
            pdfImageView.setLayoutParams(pdfImageViewLayoutParams);

            RelativeLayout.LayoutParams pngImageViewLayoutParams = (RelativeLayout.LayoutParams) jpgImageView.getLayoutParams();
            pngImageViewLayoutParams.leftMargin = (int) (10 * scale);
            jpgImageView.setLayoutParams(pngImageViewLayoutParams);

            RelativeLayout.LayoutParams jpgImageViewLayoutParams = (RelativeLayout.LayoutParams) pngImageView.getLayoutParams();
            jpgImageViewLayoutParams.rightMargin = (int) (10 * scale);
            pngImageView.setLayoutParams(jpgImageViewLayoutParams);
        }
    }
}