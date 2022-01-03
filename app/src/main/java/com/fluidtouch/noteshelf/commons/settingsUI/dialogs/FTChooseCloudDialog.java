package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTSelectBackupLocationDialog;
import com.fluidtouch.noteshelf.backup.database.FTWebDavCredentialsDialog;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.noteshelf.cloud.backup.drive.FTGoogleDriveServiceAccountHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServiceAccountHandler;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServiceAccountHandler;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServiceAccountHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 05/02/19
 */

public class FTChooseCloudDialog extends FTBaseDialog {
    @BindView(R.id.check_dropbox)
    ImageView dropboxCheck;
    @BindView(R.id.check_google_drive)
    ImageView googleDriveCheck;
    @BindView(R.id.check_onedrive)
    ImageView oneDriveCheck;
    @BindView(R.id.check_webdav)
    ImageView webdavCheck;
    @BindView(R.id.check_do_not_backup)
    ImageView notBackupCheck;

    private FTServiceAccountHandler mServiceAccountHandler;
    private FTServicePublishManager mFTServicePublishManager;
    private boolean isAgreed = true;

    private final BroadcastReceiver mBackUpSignInResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                if (!intent.getBooleanExtra(getString(R.string.intent_is_successful), false)) {
                    updateSelectedUI(SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()]);
                    if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
                        if (mServiceAccountHandler != null && mServiceAccountHandler instanceof FTWebDavServiceAccountHandler && intent.getIntExtra("responseCode", 0) != 401)
                            showWebDavAlert();
                        else
                            Toast.makeText(context, R.string.unexpected_error_occurred_please_try_again, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mServiceAccountHandler != null) {
                        mServiceAccountHandler.handleSignIn(context, intent, backingUpTo -> {
                            updateSelectedUI(SystemPref.BackUpType.valueOf(backingUpTo));
                            if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler
                                    && FTApp.getPref().getWebDavCredentials() != null && TextUtils.isEmpty(FTApp.getPref().getWebDavCredentials().getBackupFolder()) && getParentFragment() != null) {
                                new FTSelectBackupLocationDialog("").show(getParentFragmentManager());
                            } else {
                                openBackupDialog(SystemPref.BackUpType.valueOf(backingUpTo));
                            }
                        });
                    }
                }
            }
        }
    };


    public static FTChooseCloudDialog newInstance() {
        return new FTChooseCloudDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_choose_cloud, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context.registerReceiver(mBackUpSignInResultReceiver, new IntentFilter(getString(R.string.intent_sign_in_result)));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (FTApp.isForHuawei()) {
            view.findViewById(R.id.radio_google_drive).setVisibility(View.GONE);
        }

        googleDriveCheck.setVisibility(View.GONE);
        dropboxCheck.setVisibility(View.GONE);
        oneDriveCheck.setVisibility(View.GONE);
        webdavCheck.setVisibility(View.GONE);
        notBackupCheck.setVisibility(View.GONE);

        mFTServicePublishManager = FTApp.getServicePublishManager();

        updateSelectedUI(SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()]);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && mServiceAccountHandler instanceof FTDropboxServiceAccountHandler && isAgreed) {
            String mOAuth2Token = FTDropboxServiceAccountHandler.getOAuth2Token();
            Intent intent = new Intent();
            intent.putExtra(getString(R.string.intent_is_successful), mOAuth2Token != null);
            if (mOAuth2Token != null) {
                FTApp.getPref().saveDropBoxToken(mOAuth2Token);
            }
            mBackUpSignInResultReceiver.onReceive(getContext(), intent);
        }
    }

    @Override
    public void onDestroy() {
        if (getContext() != null) {
            try {
                getContext().unregisterReceiver(mBackUpSignInResultReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick({R.id.radio_dropbox, R.id.radio_google_drive, R.id.radio_onedrive, R.id.radio_webdav, R.id.radio_do_not_backup})
    void onBackupTypeSelected(View view) {
        switch (view.getId()) {
            case R.id.radio_google_drive:
                FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_Google");
                checkStatus(SystemPref.BackUpType.GOOGLE_DRIVE);
                break;
            case R.id.radio_dropbox:
                FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_Dropbox");
                checkStatus(SystemPref.BackUpType.DROPBOX);
                break;
            case R.id.radio_onedrive:
                FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_OneDrive");
                checkStatus(SystemPref.BackUpType.ONE_DRIVE);
                break;
            case R.id.radio_webdav:
                FTFirebaseAnalytics.logEvent("settings", "choose_cloud", "webdav");
                checkStatus(SystemPref.BackUpType.WEBDAV);
                break;
            default:
                FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_NoBackp");
                FTApp.getPref().saveBackUpType(SystemPref.BackUpType.NONE);
                if (FTApp.getServicePublishManager() != null) {
                    FTApp.getServicePublishManager().stopPublishing();
                    FTApp.mFTServicePublishManager = null;
                }
                updateSelectedUI(SystemPref.BackUpType.NONE);
                break;
        }
    }

    @Override
    public void dismiss() {
        if (getParentFragment() != null) {
            ((ChooseCloudDialogListener) getParentFragment()).updateBackupType();
        }
        super.dismiss();
    }

    private void checkStatus(SystemPref.BackUpType backUpType) {
        isAgreed = true;
        if (backUpType.equals(SystemPref.BackUpType.GOOGLE_DRIVE)) {
            mServiceAccountHandler = new FTGoogleDriveServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.DROPBOX)) {
            mServiceAccountHandler = new FTDropboxServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.ONE_DRIVE)) {
            mServiceAccountHandler = new FTOneDriveServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.WEBDAV)) {
            mServiceAccountHandler = new FTWebDavServiceAccountHandler();
        } else {
            dismiss();
            return;
        }
        if (mServiceAccountHandler.checkSession(getContext())) {
            updateSelectedUI(backUpType);
            if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler
                    && FTApp.getPref().getWebDavCredentials() != null && TextUtils.isEmpty(FTApp.getPref().getWebDavCredentials().getBackupFolder())) {
                new FTSelectBackupLocationDialog("").show(getParentFragmentManager());
            } else {
                openBackupDialog(backUpType);
            }
        } else {
            if (FTApp.isForSamsungStore()) {
                isAgreed = false;
                FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        isAgreed = true;
                        if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler)
                            new FTWebDavCredentialsDialog(mServiceAccountHandler).show(getParentFragmentManager());
                        else
                            mServiceAccountHandler.signIn(getContext());
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which) {
                        updateSelectedUI(SystemPref.BackUpType.NONE);
                    }
                });
            } else {
                if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler)
                    new FTWebDavCredentialsDialog(mServiceAccountHandler).show(getParentFragmentManager());
                else
                    mServiceAccountHandler.signIn(getContext());
            }
        }
    }

    private void updateSelectedUI(SystemPref.BackUpType backupType) {
        googleDriveCheck.setVisibility(View.GONE);
        dropboxCheck.setVisibility(View.GONE);
        oneDriveCheck.setVisibility(View.GONE);
        webdavCheck.setVisibility(View.GONE);
        notBackupCheck.setVisibility(View.GONE);
        if (backupType == SystemPref.BackUpType.GOOGLE_DRIVE) {
            googleDriveCheck.setVisibility(View.VISIBLE);
        } else if (backupType == SystemPref.BackUpType.DROPBOX) {
            dropboxCheck.setVisibility(View.VISIBLE);
        } else if (backupType == SystemPref.BackUpType.ONE_DRIVE) {
            oneDriveCheck.setVisibility(View.VISIBLE);
        } else if (backupType == SystemPref.BackUpType.WEBDAV) {
            webdavCheck.setVisibility(View.VISIBLE);
        } else {
            notBackupCheck.setVisibility(View.VISIBLE);
        }
    }

    private void openBackupDialog(SystemPref.BackUpType backUpType) {
        if (getParentFragment() != null) {
            ((ChooseCloudDialogListener) getParentFragment()).openBackupDialog(backUpType);
        }
        dismiss();
    }

    private void showWebDavAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.webdav_dialog_alert))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    interface ChooseCloudDialogListener {
        void updateBackupType();

        void openBackupDialog(SystemPref.BackUpType backUpType);
    }
}