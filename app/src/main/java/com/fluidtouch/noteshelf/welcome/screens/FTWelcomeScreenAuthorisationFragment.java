package com.fluidtouch.noteshelf.welcome.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenCallback;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu on 30/09/20.
 */
public class FTWelcomeScreenAuthorisationFragment extends Fragment {
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.allow_to_proceed_text_view)
    TextView allowToProceedTextView;

    private FTWelcomeScreenCallback listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.listener = (FTWelcomeScreenCallback) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_screen_authorisation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        FTLog.crashlyticsLog(FTWelcomeScreenAuthorisationFragment.class.getSimpleName());

        if (((FTWelcomeScreenActivity) getActivity()).areTermsAccepted) {
            allowToProceedTextView.setAlpha(0.5f);
            allowToProceedTextView.setText(getString(R.string.accepted));
        } else {
            allowToProceedTextView.setAlpha(1.0f);
            allowToProceedTextView.setText(getString(R.string.allow_to_proceed));
        }
        scrollView.post(() -> {
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    @OnClick(R.id.allow_to_proceed_text_view)
    void allowToProceed() {
        ((FTWelcomeScreenActivity) getActivity()).areTermsAccepted = true;
        allowToProceedTextView.setAlpha(0.5f);
        allowToProceedTextView.setText(getString(R.string.accepted));
        listener.nextScreen();
    }

    @OnClick(R.id.cancel_text_view)
    void cancelAndQuitApp() {
        getActivity().finish();
    }
}
