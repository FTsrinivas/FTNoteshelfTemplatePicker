package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Optional;

public class FTNotebookOptionsPopup extends FTBaseDialog.Popup implements FTBackupNotebookOptionsPopup.Listener,
        FTGotoPageDialog.Listener,
        FTStylusPopup.Listener,
        CompoundButton.OnCheckedChangeListener {
    public FTNotebookOptionsPopup ftNotebookOptionsPopup;
    @BindView(R.id.scrollView)
    ScrollView mScrollView;
    @BindView(R.id.switch_StylusEnable)
    SwitchMaterial mStylusSwitch;
    @BindView(R.id.tvPressureSensitivity)
    TextView mPressureSensitivity;
    @BindView(R.id.stylus_switch_layout)
    LinearLayout mStylusSwitchLayout;
    @BindView(R.id.tvShare)
    TextView tvShare;
    @BindView(R.id.prediction_switch)
    SwitchMaterial mPredictionSwitch;
    @Nullable
    @BindView(R.id.settings_dialog_developer_options)
    View mDeveloperOptionsLayout;

    private NotebookOptionsPopupListener mListener;
    private FTNoteshelfPage mCurrentPage;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getActivity() != null) {
            mListener = (NotebookOptionsPopupListener) getActivity();
        }
    }

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
        return inflater.inflate(R.layout.popup_notebook_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        tvShare.setVisibility(isMobile() ? View.VISIBLE : View.GONE);

        mStylusSwitch.setChecked(FTApp.getPref().isStylusEnabled());
        boolean pressureEnabled = false;
        if (FTApp.getPref().isStylusEnabled())
            pressureEnabled = FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false);
        mPressureSensitivity.setText(getString(R.string.pressure_sensitivity) + ": " + getString(pressureEnabled ? R.string.on : R.string.off));

        mStylusSwitch.setOnCheckedChangeListener(this);

        mDeveloperOptionsLayout.setVisibility(FTApp.isProduction() ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.tvChangePageTemplate)
    void onChangePageTemplateClicked() {
        FTFirebaseAnalytics.logEvent("NB_Options_ChangeTemplate");
        if (getFragmentManager() != null) {
            //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.PAPER).show(getFragmentManager(), FTChooseCoverPaperDialog.class.getName());
            //dismissAllowingStateLoss();

            FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
            _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
            _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(FTTemplateMode.ChangePage);
            _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.PAPER);
            _ftTemplatepickerInputInfo.set_notebookTitle(null);
             FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getFragmentManager(), FTChooseCoverPaperDialog.class.getName());

        }
    }

    @OnClick(R.id.tvRotatePage)
    void onRotatePageClicked(View view) {
        new FTRotatePagePopup().show(getChildFragmentManager());
    }

    @OnClick(R.id.tvGotoPage)
    void onGotoPageClicked(View view) {
        FTFirebaseAnalytics.logEvent("NB_Options_GoToPage");
        FTLog.crashlyticsLog("NBOptions: GoToPage");
        new FTGotoPageDialog().show(getChildFragmentManager(), FTGotoPageDialog.class.getName());
    }

    @OnClick(R.id.stylus_switch_layout)
    void onStylusEnabled(View view) {
        FTFirebaseAnalytics.logEvent("NB_Options_TapStylus");
        FTLog.crashlyticsLog("NBOptions: TapStylus");
        new FTStylusPopup().show(getChildFragmentManager());
    }

    @OnClick(R.id.tvGetInfo)
    void onGetInfoClicked(View view) {
        FTFirebaseAnalytics.logEvent("NB_Options_GetInfo");
        FTLog.crashlyticsLog("NBOptions: AllPages");
        new FTGetInfoPopup().show(getChildFragmentManager());
    }

    @OnClick(R.id.tvBackup)
    void onBackupClicked(View view) {
        FTFirebaseAnalytics.logEvent("NB_Options_AutoBackup");
        new FTBackupNotebookOptionsPopup().show(getChildFragmentManager());
    }

    @OnClick(R.id.tvShare)
    void onShareClicked(View view) {
        FTFirebaseAnalytics.logEvent("NB_Share_AllPages");
        FTLog.crashlyticsLog("NBOptions: Share_AllPages");
        mListener.share(view);
        dismiss();
    }

    @Override
    public FTNoteshelfPage getCurrentPage() {
        if (mCurrentPage == null && getActivity() != null) {
            mCurrentPage = ((FTDocumentActivity) getActivity()).getCurrentPage();
        }
        return mCurrentPage;
    }

    @Override
    public void onPageNumberSelected(int pageNumber) {
        mListener.scrollToPageAtIndex(pageNumber);
        dismissAllowingStateLoss();
    }

    @Override
    public void onStylusEnabled(boolean enable) {
        mStylusSwitch.setChecked(enable);
        mListener.onStylusEnabled();
        boolean pressureEnabled = false;
        if (enable)
            pressureEnabled = FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false);
        mPressureSensitivity.setText(getString(R.string.pressure_sensitivity) + ": " + getString(pressureEnabled ? R.string.on : R.string.off));
    }

    @Override
    public void onStylusPressureEnabled(boolean enable) {
        mPressureSensitivity.setText(getString(R.string.pressure_sensitivity) + ": " + getString(enable ? R.string.on : R.string.off));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        FTApp.getPref().save(SystemPref.STYLUS_ENABLED, isChecked);
        mListener.onStylusEnabled();
        boolean pressureEnabled = false;
        if (isChecked)
            pressureEnabled = FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false);
        mPressureSensitivity.setText(getString(R.string.pressure_sensitivity) + ": " + getString(pressureEnabled ? R.string.on : R.string.off));
    }


   /* @OnCheckedChanged(R.id.prediction_switch)
    void onDeveloperOptionsGestureChanged(CompoundButton buttonView, boolean isChecked) {
        FTApp.getPref().savePredictionEnabled(isChecked);
    }*/

    public interface NotebookOptionsPopupListener {
        void onStylusEnabled();

        void scrollToPageAtIndex(int indexToShow);

        void share(View view);
    }
}