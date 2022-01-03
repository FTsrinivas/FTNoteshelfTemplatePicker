package com.fluidtouch.noteshelf.shelf.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTCreateNotebookOptionsPopup extends FTBaseDialog.Popup {

    public CreateNotebookOptionsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            mListener = (CreateNotebookOptionsListener) context;
        } else {
            mListener = (CreateNotebookOptionsListener) getParentFragment();
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
                window.getAttributes().y += ScreenUtil.getStatusBarHeight(getContext());
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_create_notebook_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.tvQuickCreate)
    void onClickQuickCreateNotebook() {
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked Quick Create notebook");
        FTTemplatesInfoSingleton.getInstance().getFTNThemeCategory("onClubDownloadsIConClicked", FTNThemeCategory.FTThemeType.PAPER);
        mListener.createNotebookWithDefaultOptions();
        dismiss();
    }

    @OnClick(R.id.tvQuickCreateSettings)
    void onClickQuickCreateSettings() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_QuickCreateSettings");
        new FTQuickCreateSettingsPopup().show(getChildFragmentManager());
    }

    @OnClick(R.id.tvNewNotebook)
    void onClickNewNotebook() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_NewNotebook");
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked New Notebook");
        mListener.createNewNotebook();
        dismiss();
    }

    @OnClick(R.id.tvNewAudioNote)
    void onClickNewAudioNote() {
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked New Audio Note");
        //listener.createNewAudioNote();
        dismiss();
    }

    @OnClick(R.id.tvImportDocument)
    void onClickImportDocument() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_ImportDoc");
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked Import Document");
        mListener.importDocument();
        dismiss();
    }

    @OnClick(R.id.tvImportPhoto)
    void onClickImportImages() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_ImportPhoto");
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked Import Photo");
        mListener.createNewNotebookFromImages();
        dismiss();
    }

    @OnClick(R.id.tvScanDocument)
    void onClickScanDocument() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_ScanDoc");
        FTLog.crashlyticsLog("QuickAccessPopup: Clicked Scan Document");
        mListener.scanDocument();
        dismiss();
    }

    public interface CreateNotebookOptionsListener extends Serializable {
        void createNotebookWithDefaultOptions();

        void createNewNotebook();

        //void createNewAudioNote();

        void importDocument();

        void createNewNotebookFromImages();

        void scanDocument();
    }
}