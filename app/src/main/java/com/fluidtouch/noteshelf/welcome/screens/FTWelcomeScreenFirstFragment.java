package com.fluidtouch.noteshelf.welcome.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenCallback;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWelcomeScreenFirstFragment extends Fragment {
    @BindView(R.id.welcome_screen1_image1)
    ImageView image1View;
    @BindView(R.id.welcome_screen1_image2_left)
    ImageView image2LeftView;
    @BindView(R.id.welcome_screen1_image2_right)
    ImageView image2RightView;
    @BindView(R.id.welcome_screen1_image3_left)
    ImageView image3LeftView;
    @BindView(R.id.welcome_screen1_image3_right)
    ImageView image3RightView;

    private FTWelcomeScreenCallback listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.listener = (FTWelcomeScreenCallback) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.welcome_screen1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FTLog.crashlyticsLog(FTWelcomeScreenFirstFragment.class.getSimpleName());
        ButterKnife.bind(this, view);
        resetLayoutParams();
        FTApp.getPref().save(SystemPref.FIRST_TIME_INSTALLED_VERSION, BuildConfig.VERSION_NAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        int image2Margin = (int) getResources().getDimension(R.dimen.new_198dp);
        int image3Margin = (int) getResources().getDimension(R.dimen.new_396dp);
        if (getResources().getConfiguration().smallestScreenWidthDp <= 720) {
            image2Margin = (int) (getResources().getDimension(R.dimen.new_198dp) / 2);
            image3Margin = (int) (getResources().getDimension(R.dimen.new_396dp) / 2);
        }
        FTAnimationUtils.valueAnimatorRight(image2LeftView, 0, image2Margin);
        FTAnimationUtils.valueAnimatorLeft(image2RightView, 0, image2Margin);
        FTAnimationUtils.valueAnimatorRight(image3LeftView, 0, image3Margin);
        FTAnimationUtils.valueAnimatorLeft(image3RightView, 0, image3Margin);
    }

    @Override
    public void onPause() {
        super.onPause();
        resetLayoutParams();
    }

    @OnClick(R.id.welcome_screen1_continue_button)
    void onContinueClicked() {
        listener.nextScreen();
    }

    private void resetLayoutParams() {
        image2LeftView.clearAnimation();
        image2RightView.clearAnimation();
        image3LeftView.clearAnimation();
        image3RightView.clearAnimation();

        RelativeLayout.LayoutParams image2LeftViewLayoutParams = (RelativeLayout.LayoutParams) image2LeftView.getLayoutParams();
        image2LeftViewLayoutParams.rightMargin = 0;
        image2LeftView.setLayoutParams(image2LeftViewLayoutParams);

        RelativeLayout.LayoutParams image2RightViewLayoutParams = (RelativeLayout.LayoutParams) image2RightView.getLayoutParams();
        image2RightViewLayoutParams.leftMargin = 0;
        image2RightView.setLayoutParams(image2RightViewLayoutParams);

        RelativeLayout.LayoutParams image3LeftViewLayoutParams = (RelativeLayout.LayoutParams) image3LeftView.getLayoutParams();
        image3LeftViewLayoutParams.rightMargin = 0;
        image3LeftView.setLayoutParams(image3LeftViewLayoutParams);

        RelativeLayout.LayoutParams image3RightViewLayoutParams = (RelativeLayout.LayoutParams) image3RightView.getLayoutParams();
        image3RightViewLayoutParams.leftMargin = 0;
        image3RightView.setLayoutParams(image3RightViewLayoutParams);
    }
}
