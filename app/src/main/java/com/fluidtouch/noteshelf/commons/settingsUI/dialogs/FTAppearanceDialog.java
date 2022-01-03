package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.settingsUI.adapters.FTThemeItemAdapter;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.Theme;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTAppearanceDialog extends FTBaseDialog implements FTThemeItemAdapter.ThemeItemAdapterCallback {
    @BindView(R.id.appearance_dialog_themes_recycler_view)
    RecyclerView mThemesRecyclerView;
    @BindView(R.id.appearance_dialog_show_date_switch)
    SwitchMaterial mShowDateSwitch;

    public static FTAppearanceDialog newInstance() {
        return new FTAppearanceDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        FTThemeItemAdapter themeItemAdapter = new FTThemeItemAdapter(this);
        themeItemAdapter.addAll(Theme.getThemes(getContext()));
        mThemesRecyclerView.setAdapter(themeItemAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        mShowDateSwitch.setChecked(FTApp.getPref().get(SystemPref.IS_SHOWING_DATE, FTConstants.DEFAULT_IS_SHOWING_DATE));
    }

    @OnCheckedChanged(R.id.appearance_dialog_show_date_switch)
    void onShowDateChecked(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_ShowDateonShelf");
        FTApp.getPref().save(SystemPref.IS_SHOWING_DATE, isChecked);
        if (getParentFragment() != null)
            ((AppearanceDialogListener) getParentFragment()).onShowDateModified(isChecked);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @Override
    public void onThemeSelected() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_ShowDateonShelf");
        getContext().sendBroadcast(new Intent(getString(R.string.theme)));
    }

    public interface AppearanceDialogListener {
        void onShowDateModified(boolean show);
    }
}