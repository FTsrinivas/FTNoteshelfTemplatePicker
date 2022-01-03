package com.fluidtouch.noteshelf.document.dialogs.addnew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddNewPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.page)
    void onPageClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_Page");
        FTLog.crashlyticsLog("AddNew: Added new page");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).addNewPage();
        dismiss();
    }

    @OnClick(R.id.page_from_template)
    void onPageFromTemplateClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_PageFromTemplate");
        FTLog.crashlyticsLog("AddNew: New page from template");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).addNewPageFromTemplate();
        dismiss();
    }

    @OnClick(R.id.page_from_image)
    void onPageFromImageClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_PageFromPhoto");
        FTLog.crashlyticsLog("AddNew: New page from image");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).addNewPageFromPhoto();
        dismiss();
    }

    @OnClick(R.id.import_document)
    void onImportDocumentClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_ImportDoc");
        FTLog.crashlyticsLog("AddNew: New page from import");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).importDocument();
        dismiss();
    }

    @OnClick(R.id.scan_document)
    void onScanDocumentClicked() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_ScanDoc");
        FTLog.crashlyticsLog("AddNew: New page from scan");
        if (getActivity() != null) ((AddNewPopupListener) getActivity()).scanDocument();
        dismiss();
    }

    private void dismiss() {
        if (getParentFragment() != null) {
            ((DismissListener) getParentFragment()).dismiss();
        }
    }
}