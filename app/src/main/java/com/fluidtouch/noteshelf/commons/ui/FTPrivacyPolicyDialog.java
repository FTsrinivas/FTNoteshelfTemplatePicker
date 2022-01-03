package com.fluidtouch.noteshelf.commons.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTPrivacyPolicyDialog extends FTBaseDialog {
    @BindView(R.id.webView)
    WebView mWebView;
    @BindView(R.id.dialog_back_button)
    ImageView mBackButton;
    @BindView(R.id.acceptButton)
    Button mAcceptButton;
    @BindView(R.id.due_to_xxxx)
    TextView mDueToTextView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            if (isChildFragment()) {
                dialog.setCanceledOnTouchOutside(false);
            } else {
                window.setDimAmount(0.0f);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mBackButton.setVisibility(isChildFragment() ? View.VISIBLE : View.INVISIBLE);
        mAcceptButton.setVisibility(isChildFragment() ? View.GONE : View.VISIBLE);
        mDueToTextView.setVisibility(isChildFragment() ? View.GONE : View.VISIBLE);

        try {
            String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();

            if (FTApp.isChineseBuild()) {
                lang = "china";
            }

            if (!Arrays.asList(getResources().getAssets().list("privacyPolicy")).contains("policy_" + lang + ".html")) {
                lang = "en";
            }

            mWebView.loadUrl("file:///android_asset/privacyPolicy/policy_" + lang + ".html");
        } catch (Exception e) {
            FTLog.error(getClass().getName(), "Unable to load privacy policy");
        }
    }

    @OnClick(R.id.dialog_back_button)
    void onBackButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.acceptButton)
    void onAcceptButtonClicked() {
        String policyVersion = AssetsUtil.getNewPolicyVersion(getContext());
        FTApp.getPref().save(SystemPref.PRIVACY_POLICY_VERSION, policyVersion);
        if (getParentFragment() != null && getParentFragment() instanceof Listener) {
            ((Listener) getParentFragment()).onUserAccepted();
        }
        dismiss();
    }

    public interface Listener {
        void onUserAccepted();
    }
}