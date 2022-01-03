package com.fluidtouch.noteshelf.welcome.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTWelcomeScreenSecondFragment extends Fragment {
    @BindView(R.id.firstRow)
    View mFirstRowLayout;
    @BindView(R.id.secondRow)
    View mSecondRowLayout;
    @BindView(R.id.thirdRow)
    View mThirdRowLayout;
    @BindView(R.id.fourthRow)
    View mFourthRowLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        return inflater.inflate(R.layout.welcome_screen2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        FTLog.crashlyticsLog(FTWelcomeScreenSecondFragment.class.getSimpleName());

        resetLayoutParams();
    }

    @Override
    public void onResume() {
        super.onResume();

        mFirstRowLayout.postDelayed(() -> mFirstRowLayout.animate().alpha(1.0f).setDuration(1000).start(), 100);
        mSecondRowLayout.postDelayed(() -> mSecondRowLayout.animate().alpha(1.0f).setDuration(1000).start(), 400);
        mThirdRowLayout.postDelayed(() -> mThirdRowLayout.animate().alpha(1.0f).setDuration(1000).start(), 700);
        mFourthRowLayout.postDelayed(() -> mFourthRowLayout.animate().alpha(1.0f).setDuration(1000).start(), 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        resetLayoutParams();
    }

    private void resetLayoutParams() {
        mFirstRowLayout.clearAnimation();
        mSecondRowLayout.clearAnimation();
        mThirdRowLayout.clearAnimation();
        mFourthRowLayout.clearAnimation();

        mFirstRowLayout.setAlpha(0.0f);
        mSecondRowLayout.setAlpha(0.0f);
        mThirdRowLayout.setAlpha(0.0f);
        mFourthRowLayout.setAlpha(0.0f);
    }
}