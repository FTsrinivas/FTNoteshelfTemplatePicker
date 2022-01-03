package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.services.FTZendeskSupportManager;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu on 27/10/20.
 */
public class FTSupportDialog extends FTBaseDialog {
    private SupportParentCallback mParentCallback;

    public FTSupportDialog(SupportParentCallback parentCallback) {
        mParentCallback = parentCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick(R.id.support_get_help_text_view)
    void onGetHelpClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Support_GetHelp");
        if (FTApp.isForHuawei()) {
            FTZendeskSupportManager.showContactActivity(getContext());
        } else {
            FTZendeskSupportManager.showHelpCenter(getContext());
        }
        dismissAll();
    }

    @OnClick(R.id.support_send_logs_text_view)
    void onSendSupportLogClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Support_Contact_Logs");
//        final FTSmartDialog smartDialog = new FTSmartDialog()
//                .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
//                .setMessage(getString(R.string.exporting))
//                .show(getActivity().getSupportFragmentManager());

        final String[] structure = {"Folder Structure\n" + getFolderStructure("", ContextCompat.getDataDir(getContext()))};
//        getProviderStructure(mParentCallback.getCollectionProvider(), structure1 -> {
//            structure[0] += "Provider Structure\n" + structure1;

            FTLog.saveLog(structure[0]);
            FTFirebaseAnalytics.logEvent("shelf", "settings", "support_logs");
            File outFile = new File(FTConstants.SUPPORT_LOG_FILE_PATH);
//            smartDialog.dismissAllowingStateLoss();
            if (outFile.exists()) {
                Uri outFileUri = FileUriUtils.getUriForFile(getContext(), outFile);
                String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(getContext(), outFileUri);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType(mimeType);
                shareIntent.putExtra(Intent.EXTRA_STREAM, outFileUri);
                startActivity(new Intent(shareIntent));
            }
//        });
    }

    private String getFolderStructure(String initialStructure, File file) {
        String structure = initialStructure;
        if (file.isDirectory()) {
            structure = structure + file.getName();
            File[] files = file.listFiles();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                for (File resultFile : files) {
//                    structure += "\n---" + resultFile.getName();
                    structure += "\n" + getFolderStructure(initialStructure + "---", resultFile);
                }
            }
        } else {
            structure = structure + file.getName();
        }

        return structure;
    }

    private void getProviderStructure(FTShelfCollectionProvider collectionProvider, OnStructureCompleteCallback onStructureCompleteCallback) {
        String structure = "";
        String dataPath = ContextCompat.getDataDir(getContext()).getPath();
        collectionProvider.shelfs(shelfs -> {
            getShelfCollectionStructure(structure, 0, shelfs, onStructureCompleteCallback, dataPath);
        });
    }

    private void getShelfCollectionStructure(String structure, int index, List<FTShelfItemCollection> shelfs, OnStructureCompleteCallback onStructureCompleteCallback, String dataPath) {
        final String[] structure1 = {structure};
        if (index >= shelfs.size()) {
            onStructureCompleteCallback.onCompletion(structure);
        } else {
            structure += shelfs.get(index).getFileURL().getPath().split(dataPath + "/Noteshelf.nsdata/")[1] + "\n";
            shelfs.get(index).shelfItems(getContext(), FTShelfSortOrder.BY_NAME, null, "", (notebooks, error) -> {
                for (int j = 0; j < notebooks.size(); j++) {
                    structure1[0] += notebooks.get(j).getFileURL().getPath().split(dataPath + "/Noteshelf.nsdata/")[1] + "\n";
                }
                getShelfCollectionStructure(structure1[0], index + 1, shelfs, onStructureCompleteCallback, dataPath);
            });
        }
    }

    protected interface SupportParentCallback {
        FTShelfCollectionProvider getCollectionProvider();
    }

    private interface OnStructureCompleteCallback {
        void onCompletion(String structure);
    }
}
