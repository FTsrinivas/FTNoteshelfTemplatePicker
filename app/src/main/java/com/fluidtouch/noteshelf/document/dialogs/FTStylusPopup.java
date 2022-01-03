package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ScrollView;

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
import butterknife.OnClick;

public class FTStylusPopup extends FTBaseDialog.Popup implements CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.stylus_dialog_enable_switch)
    SwitchMaterial stylusEnabledSwitch;
    @BindView(R.id.stylus_dialog_pressure_sensitivity)
    SwitchMaterial pressureSensitivitySwitch;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.END);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_stylus_notebook_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        stylusEnabledSwitch.setChecked(FTApp.getPref().isStylusEnabled());
        if (!FTApp.getPref().isStylusEnabled()) {
            pressureSensitivitySwitch.setChecked(false);
            pressureSensitivitySwitch.setClickable(false);
            pressureSensitivitySwitch.setEnabled(false);
            pressureSensitivitySwitch.setAlpha(0.5f);
            FTApp.getPref().save(SystemPref.STYLUS_PRESSURE_ENABLED, false);
        } else {
            pressureSensitivitySwitch.setClickable(true);
            pressureSensitivitySwitch.setEnabled(true);
            pressureSensitivitySwitch.setAlpha(1.0f);
            pressureSensitivitySwitch.setChecked(FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false));
        }


        stylusEnabledSwitch.setOnCheckedChangeListener(this);
        pressureSensitivitySwitch.setOnCheckedChangeListener(this);
    }

    @OnClick(R.id.dialog_title)
    void onBackClicked() {
        dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.stylus_dialog_enable_switch:
                FTFirebaseAnalytics.logEvent("document", "notebook_options", "stylus");
                FTLog.logCrashCustomKey("Stylus", "" + isChecked);

                pressureSensitivitySwitch.setAlpha(isChecked ? 1.0f : 0.5f);
                pressureSensitivitySwitch.setClickable(isChecked);
                pressureSensitivitySwitch.setEnabled(isChecked);

                if (!isChecked) {
                    pressureSensitivitySwitch.setChecked(isChecked);
                    FTApp.getPref().save(SystemPref.STYLUS_PRESSURE_ENABLED, isChecked);
                }

                FTApp.getPref().save(SystemPref.STYLUS_ENABLED, isChecked);
                if (getParentFragment() != null)
                    ((FTStylusPopup.Listener) getParentFragment()).onStylusEnabled(isChecked);
                break;
            case R.id.stylus_dialog_pressure_sensitivity:
                FTFirebaseAnalytics.logEvent("document", "notebook_options", "pressure_sensitivity");
                FTApp.getPref().save(SystemPref.STYLUS_PRESSURE_ENABLED, isChecked);
                if (getParentFragment() != null)
                    ((FTStylusPopup.Listener) getParentFragment()).onStylusPressureEnabled(isChecked);
                break;
        }
    }

    public interface Listener {
        void onStylusEnabled(boolean enable);

        void onStylusPressureEnabled(boolean enable);
    }
}