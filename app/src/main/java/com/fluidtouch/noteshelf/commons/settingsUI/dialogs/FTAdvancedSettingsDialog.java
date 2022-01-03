package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTAdvancedSettingsDialog extends FTBaseDialog {
    @BindView(R.id.settings_dialog_quick_access_gesture_switch)
    SwitchMaterial mQuickAccessGestureSwitch;
    @BindView(R.id.settings_dialog_thumbnails_gesture_switch)
    SwitchMaterial mThumbnailsGestureSwitch;
    @BindView(R.id.settings_dialog_disable_hyperlinks_switch)
    SwitchMaterial mDisableHyperlinksSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_advanced_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mQuickAccessGestureSwitch.setChecked(FTApp.getPref().isQuickAccessPanelEnabled());
        mThumbnailsGestureSwitch.setChecked(FTApp.getPref().isFinderEnabled());
    }

    @Override
    public void onResume() {
        super.onResume();
        mDisableHyperlinksSwitch.setChecked(FTApp.getPref().get(SystemPref.HYPERLINKS_DISABLED, false));
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnCheckedChanged(R.id.settings_dialog_quick_access_gesture_switch)
    void onQuickAccessGestureChanged(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Advanced_QuickAccGesture");
        FTApp.getPref().saveQuickAccessPanelEnabled(isChecked);
        ObservingService.getInstance().postNotification("EdgeSwipeGestureState", isChecked);
    }

    @OnCheckedChanged(R.id.settings_dialog_thumbnails_gesture_switch)
    void onThumbnailsGestureChanged(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Advanced_ThumbnailGesture");
        FTApp.getPref().saveFinderEnabled(isChecked);
        ObservingService.getInstance().postNotification("EdgeSwipeGestureState", isChecked);
    }

    @OnCheckedChanged(R.id.settings_dialog_disable_hyperlinks_switch)
    void onDisableHyperlinksChanged(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Advanced_DisableHyperlink");
        FTApp.getPref().save(SystemPref.HYPERLINKS_DISABLED, isChecked);
    }
}
