package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTStylusDialog extends FTBaseDialog {
    @BindView(R.id.stylus_dialog_enable_switch)
    SwitchMaterial stylusEnabledSwitch;
    @BindView(R.id.stylus_dialog_pressure_sensitivity)
    SwitchMaterial pressureSensitivitySwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_stylus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        stylusEnabledSwitch.setChecked(FTApp.getPref().isStylusEnabled());
        if (FTApp.getPref().isStylusEnabled()) {
            pressureSensitivitySwitch.setChecked(FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false));
            pressureSensitivitySwitch.setAlpha(1.0f);
            pressureSensitivitySwitch.setClickable(true);
        } else {
            pressureSensitivitySwitch.setAlpha(0.5f);
            pressureSensitivitySwitch.setClickable(false);
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

    @OnCheckedChanged(R.id.stylus_dialog_enable_switch)
    void onStylusChecked(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Stylus_Enable");
        FTLog.logCrashCustomKey("Stylus", "" + isChecked);
        FTApp.getPref().saveStylusEnabled(isChecked);
        pressureSensitivitySwitch.setAlpha(isChecked ? 1.0f : 0.5f);
        pressureSensitivitySwitch.setClickable(isChecked);

        if (!isChecked) {
            pressureSensitivitySwitch.setChecked(isChecked);
            FTApp.getPref().save(SystemPref.STYLUS_PRESSURE_ENABLED, isChecked);
        }

        if (getParentFragment() != null)
            ((StylusDialogListener) getParentFragment()).onStylusEnabled();
    }

    @OnCheckedChanged(R.id.stylus_dialog_pressure_sensitivity)
    void onPressureSensitivityChecked(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Stylus_PressureSens");
        if (FTApp.getPref().isStylusEnabled())
            FTApp.getPref().save(SystemPref.STYLUS_PRESSURE_ENABLED, isChecked);
    }

    public interface StylusDialogListener {
        void onStylusEnabled();
    }
}
