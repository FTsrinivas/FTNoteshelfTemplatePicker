package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSettingsDialog extends FTBaseDialog implements FTAppearanceDialog.AppearanceDialogListener, FTStylusDialog.StylusDialogListener {
    @BindView(R.id.settings_dialog_cloud_backup)
    TextView cloudAndBackUpTextView;
    @BindView(R.id.settings_dialog_support)
    TextView supportTextView;

    private SettingsListener mListener;

    public static FTSettingsDialog newInstance() {
        return new FTSettingsDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SettingsListener) context;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!ScreenUtil.isMobile(getContext())) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (FTApp.isForHuawei() || FTApp.isForAppGallery()) {
            cloudAndBackUpTextView.setVisibility(View.GONE);
        }

        if (BuildConfig.FLAVOR.equals("samsungChinese")) {
            supportTextView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.settings_dialog_close_button)
    void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.settings_dialog_appearance)
    void onAppearanceClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Appearance");
        new FTAppearanceDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_handwriting)
    void onHandwritingClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_HandwritingRecognition");
        new FTWritingStyleDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_stylus)
    void onStylusClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Stylus");
        new FTStylusDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_cloud_backup)
    void onCloudBackupClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_CloudAndBackup");
        new FTCloudBackupDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_advanced)
    void onAdvancedClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Advanced");
        new FTAdvancedSettingsDialog().show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_free_templates)
    void onFreeTemplatesClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_NoteshelfClub");
        FTStoreActivity.start(getContext());
    }

    @OnClick(R.id.settings_dialog_support)
    void onSupportClicked() {
        FTLog.crashlyticsLog("UI: Clicked Support");
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Support");
        new FTSupportDialog(() -> mListener.getCollectionProvider()).show(getChildFragmentManager());
    }

    @OnClick(R.id.settings_dialog_about)
    void onAboutClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_AboutNS");
        new FTAboutUsDialog().show(getChildFragmentManager());
    }

    @Override
    public void onShowDateModified(boolean show) {
        mListener.onShowDateModified(show);
    }

    @Override
    public void onStylusEnabled() {
        mListener.onStylusEnabled();
    }

    public interface SettingsListener {
        void onShowDateModified(boolean isChecked);

        void onStylusEnabled();

        FTShelfCollectionProvider getCollectionProvider();
    }
}
