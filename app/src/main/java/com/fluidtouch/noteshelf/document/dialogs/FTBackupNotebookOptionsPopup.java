package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTBackupNotebookOptionsPopup extends FTBaseDialog.Popup {
    @BindView(R.id.switch_EvernoteSync)
    SwitchMaterial enSyncSwitch;

    private Listener mListener;

    public static FTBackupNotebookOptionsPopup newInstance() {
        return new FTBackupNotebookOptionsPopup();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            mListener = (Listener) getParentFragment();
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
        return inflater.inflate(R.layout.popup_backup_notebook_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (mListener != null) {
            enSyncSwitch.setChecked(FTENSyncRecordUtil.isSyncEnabledForNotebook(mListener.getCurrentPage().getParentDocument().getDocumentUUID()));
        }
    }

    @OnCheckedChanged(R.id.switch_AutoBackup)
    void onAutoBackupChecked(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("document_activity", "advanced_notebook_options", "auto_backup_switch");
    }

    @OnCheckedChanged(R.id.switch_EvernoteSync)
    void onEvernoteSyncCheck(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("document_activity", "advanced_notebook_options", "evernote_sync_switch");
        if (isChecked && !EvernoteSession.getInstance().isLoggedIn()) {
            if (FTApp.isForSamsungStore()) {
                FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        ((FTBaseActivity) getActivity()).authenticateEvernoteUser(successful -> {
                            if (successful) {
                                enableEvernotePublish(true);
                            }
                        });
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
            } else {
                ((FTBaseActivity) getActivity()).authenticateEvernoteUser(successful -> {
                    enableEvernotePublish(true);
                });
            }
        } else {
            enableEvernotePublish(isChecked);
        }
    }

    private void enableEvernotePublish(boolean enable) {
        if (mListener != null) {
            FTNoteshelfDocument currentDocument = mListener.getCurrentPage().getParentDocument();
            if (enable && !FTENSyncRecordUtil.isSyncEnabledForNotebook(currentDocument.getDocumentUUID())) {
                FTLog.crashlyticsLog("EvernoteSync: Evernote Sync enabled for notebook");
                FTENSyncRecordUtil.enableEvernoteSyncForNotebook(getContext(), currentDocument);
            } else if (!enable && FTENSyncRecordUtil.isSyncEnabledForNotebook(currentDocument.getDocumentUUID())) {
                FTLog.crashlyticsLog("EvernoteSync: Evernote Sync disabled for notebook");
                FTENSyncRecordUtil.disableSyncForNotebook(currentDocument.getDocumentUUID());
            }
        }
    }

    @OnClick(R.id.dialog_title)
    void onCloseClicked() {
        dismiss();
    }

    public interface Listener {
        FTNoteshelfPage getCurrentPage();
    }
}