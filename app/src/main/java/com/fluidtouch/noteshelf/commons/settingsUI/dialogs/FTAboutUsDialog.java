package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTPrivacyPolicyDialog;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf.whatsnew.FTWhatsNewDialog;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAboutUsDialog extends FTBaseDialog {
    @BindView(R.id.about_dialog_version)
    TextView versionTextView;
    @BindView(R.id.about_dialog_welcome_tour)
    TextView welcomeTour;
    @BindView(R.id.about_dialog_social_networks)
    LinearLayout socialNetworksLayout;
    @BindView(R.id.about_dialog_whats_new)
    TextView whatsNew;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_about_us, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        versionTextView.setText(getString(R.string.set_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        if (BuildConfig.FLAVOR.equals("beta") || BuildConfig.FLAVOR.equals("dev")) {
            welcomeTour.setVisibility(View.VISIBLE);
        } else {
            welcomeTour.setVisibility(View.GONE);
        }

        if (FTApp.isForHuawei()) {
            socialNetworksLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick(R.id.about_dialog_facebook)
    void onFacebookClicked() {
        FTLog.crashlyticsLog("UI: Clicked Facebook");
        FTFirebaseAnalytics.logEvent("Shelf_Settings_AboutNS_Facebook");
        if (getActivity() != null) {
            try {
                getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/213196118806391")));
            } catch (Exception e) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/213196118806391")));
            }
        }
    }

    @OnClick(R.id.about_dialog_instagram)
    void onInstagramClicked() {
        FTLog.crashlyticsLog("UI: Clicked Instagram");
        FTFirebaseAnalytics.logEvent("Shelf_Settings_AboutNS_Instagram");
        if (getActivity() != null) {
            try {
                getActivity().getPackageManager().getPackageInfo("com.instagram.android", 0);
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=noteshelfapp")));
            } catch (Exception e) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/noteshelfapp/")));
            }
        }
    }

    @OnClick(R.id.about_dialog_twitter)
    void onTwitterClicked() {
        FTLog.crashlyticsLog("UI: Clicked Twitter");
        FTFirebaseAnalytics.logEvent("Shelf_Settings_AboutNS_Twitter");
        if (getActivity() != null) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=noteshelf")));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/noteshelf")));
            }
        }
    }

    @OnClick(R.id.about_dialog_welcome_tour)
    void onWelcomeTourClicked() {
        startActivity(new Intent(getActivity(), FTWelcomeScreenActivity.class));
    }

    @OnClick(R.id.about_dialog_privacy_policy)
    void onPrivacyPolicyClicked() {
        new FTPrivacyPolicyDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.about_dialog_whats_new)
    void onWhatsNewClicked() {
        new FTWhatsNewDialog().show(getChildFragmentManager(), FTWhatsNewDialog.class.getName());
    }
}