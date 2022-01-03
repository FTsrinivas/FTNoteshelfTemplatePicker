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
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTDeveloperOptionsDialog extends FTBaseDialog {

    @BindView(R.id.prediction_switch)
    SwitchMaterial mPredictionSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_developer_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mPredictionSwitch.setChecked(FTApp.getPref().isPredictionEnabled());
    }

    @OnCheckedChanged(R.id.prediction_switch)
    void onDeveloperOptionsGestureChanged(CompoundButton buttonView, boolean isChecked) {
        FTApp.getPref().savePredictionEnabled(isChecked);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }
}
