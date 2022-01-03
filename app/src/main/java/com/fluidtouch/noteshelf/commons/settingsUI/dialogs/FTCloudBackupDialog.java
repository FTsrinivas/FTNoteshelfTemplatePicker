package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.client.android.EvernoteSession;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTWebDavCredentialsDialog;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServiceAccountHandler;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.evernotesync.fragments.FTENPublishDialog;
import com.fluidtouch.noteshelf.evernotesync.fragments.FTENSettingsDialog;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.noteshelf.cloud.FTCloudServices;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.noteshelf.cloud.backup.drive.FTGoogleDriveServiceAccountHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServiceAccountHandler;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServiceAccountHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTCloudBackupDialog extends FTBaseDialog implements FTChooseCloudDialog.ChooseCloudDialogListener, FTAutoBackupDialog.AutoBackupDialogListener, FTENSettingsDialog.ENSettingsDialogListener, FTBackupSettingsDialog.BackupSettingsDialogListener {
    @BindView(R.id.dialog_backup_selected_type_text_view)
    TextView mBackUpTypeTextView;
    @BindView(R.id.dialog_cloud_dropbox_linked_tag)
    TextView dropboxLinkedTag;
    @BindView(R.id.dialog_cloud_google_drive_linked_tag)
    TextView googleDriveLinkedTag;
    @BindView(R.id.dialog_cloud_one_drive_linked_tag)
    TextView oneDriveLinkedTag;
    @BindView(R.id.dialog_cloud_evernote_linked_tag)
    TextView evernoteLinkedTag;
    @BindView(R.id.dialog_cloud_webdav_linked_tag)
    TextView webdavLinkedTag;
    @BindView(R.id.switch_wifi_enable)
    SwitchMaterial wifiEnableSwitch;

    private FTServiceAccountHandler mServiceAccountHandler;

    private final BroadcastReceiver mBackUpSignInResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded()) {
                if (!intent.getBooleanExtra(getString(R.string.intent_is_successful), false)) {
                    if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
                        if (mServiceAccountHandler != null && mServiceAccountHandler instanceof FTWebDavServiceAccountHandler && intent.getIntExtra("responseCode", 0) != 401)
                            showWebDavAlert();
                        else
                            Toast.makeText(getContext(), R.string.unable_to_authenticate, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    updateLinkedAccountUI();
                    if (mServiceAccountHandler != null) {
                        mServiceAccountHandler.handleSignIn(getContext(), intent, backingUpTo -> updateLinkedAccountUI());
                    }
                }
            }
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        context.registerReceiver(mBackUpSignInResultReceiver, new IntentFilter(getString(R.string.intent_sign_in_result)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_cloud_backup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (!FTCloudServices.INSTANCE.isGoogleDriveWorking()) {
            (view.findViewById(R.id.dialog_cloud_google_drive_layout)).setVisibility(View.GONE);
        }

        if (!FTCloudServices.INSTANCE.isDropBoxWorking()) {
            (view.findViewById(R.id.dialog_cloud_dropbox_layout)).setVisibility(View.GONE);
        }

        if (!FTCloudServices.INSTANCE.isOneDriveWorking()) {
            (view.findViewById(R.id.dialog_cloud_one_drive_layout)).setVisibility(View.GONE);
        }

        if (!FTCloudServices.INSTANCE.isEverNoteWorking()) {
            (view.findViewById(R.id.dialog_cloud_evernote_layout)).setVisibility(View.GONE);
        }

        if (BuildConfig.FLAVOR.equals("china")) {
            (view.findViewById(R.id.dialog_cloud_evernote_layout)).setVisibility(View.GONE);
            (view.findViewById(R.id.dialog_cloud_evernote_publish)).setVisibility(View.GONE);
        }

        updateBackupType();
        updateLinkedAccountUI();
        wifiEnableSwitch.setChecked(FTApp.getPref().get(SystemPref.BACKUPTHROUGHWIFI, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null && mServiceAccountHandler instanceof FTDropboxServiceAccountHandler) {
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
        if (getContext() != null)
            try {
                getContext().unregisterReceiver(mBackUpSignInResultReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        super.onDestroy();
    }

    @OnClick(R.id.dialog_backup_selected_type_layout)
    void onAutoBackupClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup");
        if (FTCloudServices.INSTANCE.isGoogleDriveWorking() || FTCloudServices.INSTANCE.isDropBoxWorking() || FTCloudServices.INSTANCE.isOneDriveWorking()) {
            FTServiceAccountHandler serviceAccountHandler = FTServiceAccountHandler.getInstance();
            if (serviceAccountHandler != null) {
                if (serviceAccountHandler.checkSession(getContext())) {
                    openBackupDialog(SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()]);
                    return;
                }
            }
            FTChooseCloudDialog.newInstance().show(getChildFragmentManager());
        }
    }

    @OnClick(R.id.dialog_cloud_evernote_publish)
    void onEvernotePublishClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_EvernotePub");
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            if (FTApp.isForSamsungStore() && !FTApp.getPref().hasAgreedPrivacyPolicy()) {
                FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        ((FTBaseActivity) getActivity()).authenticateEvernoteUser(successful -> {
                            if (successful) {
                                evernoteLinkedTag.setVisibility(View.VISIBLE);
                                FTENPublishDialog.newInstance().show(getChildFragmentManager());
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
                    if (successful) {
                        evernoteLinkedTag.setVisibility(View.VISIBLE);
                        FTENPublishDialog.newInstance().show(getChildFragmentManager());
                    }
                });
            }
        } else {
            FTENPublishDialog.newInstance().show(getChildFragmentManager());
        }
    }

    @OnClick({R.id.dialog_cloud_dropbox_layout, R.id.dialog_cloud_google_drive_layout, R.id.dialog_cloud_one_drive_layout, R.id.dialog_cloud_evernote_layout, R.id.dialog_cloud_webdav_layout})
    void onCloudClicked(View view) {
        switch (view.getId()) {
            case R.id.dialog_cloud_dropbox_layout:
                FTFirebaseAnalytics.logEvent("Settings_CloudBackup_Cloud_Dropbox");
                checkStatus(SystemPref.BackUpType.DROPBOX);
                break;
            case R.id.dialog_cloud_google_drive_layout:
                FTFirebaseAnalytics.logEvent("Settings_CloudBackup_Cloud_GoogleDrive");
                checkStatus(SystemPref.BackUpType.GOOGLE_DRIVE);
                break;
            case R.id.dialog_cloud_one_drive_layout:
                FTFirebaseAnalytics.logEvent("Settings_CloudBackup_Cloud_OneDrive");
                checkStatus(SystemPref.BackUpType.ONE_DRIVE);
                break;
            case R.id.dialog_cloud_webdav_layout:
                FTFirebaseAnalytics.logEvent("settings", "cloud_backup", "webdav");
                checkStatus(SystemPref.BackUpType.WEBDAV);
                break;
            case R.id.dialog_cloud_evernote_layout:
                FTFirebaseAnalytics.logEvent("Settings_CloudBackup_Cloud_Evernote");
                if (!EvernoteSession.getInstance().isLoggedIn()) {
                    if (FTApp.isForSamsungStore() && !FTApp.getPref().hasAgreedPrivacyPolicy()) {
                        FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                            @Override
                            public void onPositiveClick(DialogInterface dialog, int which) {
                                ((FTBaseActivity) getActivity()).authenticateEvernoteUser(successful -> {
                                    if (successful) {
                                        updateLinkedAccountUI();
                                    }
                                });
                            }

                            @Override
                            public void onNegativeClick(DialogInterface dialog, int which) {
                                //Do Nothing
                            }
                        });
                    } else {
                        ((FTBaseActivity) getActivity()).authenticateEvernoteUser(successful -> {
                            if (successful) {
                                updateLinkedAccountUI();
                            }
                        });
                    }
                } else {
                    FTENSettingsDialog.newInstance().show(getChildFragmentManager());
                }
                break;
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

    @OnCheckedChanged(R.id.switch_wifi_enable)
    void onWiFiBackupEnabled(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_Settings_Cloud_Backup_WifiOnly");
        FTApp.getPref().save(SystemPref.BACKUPTHROUGHWIFI, isChecked);
    }

    @Override
    public void updateBackupType() {
        int type = FTApp.getPref().getBackUpType();
        String typeText;
        if (type == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()) {
            typeText = getString(R.string.google_drive);
        } else if (type == SystemPref.BackUpType.DROPBOX.ordinal()) {
            typeText = getString(R.string.dropbox);
        } else if (type == SystemPref.BackUpType.ONE_DRIVE.ordinal()) {
            typeText = getString(R.string.one_drive);
        } else if (type == SystemPref.BackUpType.WEBDAV.ordinal()) {
            typeText = getString(R.string.webdav);
        } else {
            typeText = getString(R.string.do_not_backup);
        }
        mBackUpTypeTextView.setText(typeText);
    }

    @Override
    public void openBackupDialog(SystemPref.BackUpType backUpType) {
        FTAutoBackupDialog.newInstance(backUpType).show(getChildFragmentManager());
    }

    @Override
    public void openChooseCloudDialog() {
        FTChooseCloudDialog.newInstance().show(getChildFragmentManager());
    }

    @Override
    public void onEvernoteSignOut() {
        updateLinkedAccountUI();
    }

    @Override
    public void onSignOut() {
        FTApp.mFTServicePublishManager = null;
        updateBackupType();
        updateLinkedAccountUI();
    }

    private void updateLinkedAccountUI() {
        googleDriveLinkedTag.setVisibility(new FTGoogleDriveServiceAccountHandler().checkSession(getContext()) ? View.VISIBLE : View.GONE);
        dropboxLinkedTag.setVisibility(new FTDropboxServiceAccountHandler().checkSession(getContext()) ? View.VISIBLE : View.GONE);
        oneDriveLinkedTag.setVisibility(new FTOneDriveServiceAccountHandler().checkSession(getContext()) ? View.VISIBLE : View.GONE);
        webdavLinkedTag.setVisibility(new FTWebDavServiceAccountHandler().checkSession(getContext()) ? View.VISIBLE : View.GONE);
        evernoteLinkedTag.setVisibility(EvernoteSession.getInstance().isLoggedIn() ? View.VISIBLE : View.GONE);
    }

    private void checkStatus(SystemPref.BackUpType backUpType) {
        if (backUpType.equals(SystemPref.BackUpType.GOOGLE_DRIVE)) {
            mServiceAccountHandler = new FTGoogleDriveServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.DROPBOX)) {
            mServiceAccountHandler = new FTDropboxServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.ONE_DRIVE)) {
            mServiceAccountHandler = new FTOneDriveServiceAccountHandler();
        } else if (backUpType.equals(SystemPref.BackUpType.WEBDAV)) {
            mServiceAccountHandler = new FTWebDavServiceAccountHandler();
        }
        if (mServiceAccountHandler.checkSession(getContext())) {
            FTBackupSettingsDialog.newInstance(backUpType).show(getChildFragmentManager());
        } else {
            if (!FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
                Toast.makeText(getContext(), R.string.check_your_internet_connection, Toast.LENGTH_SHORT).show();
                return;
            }
            if (FTApp.isForSamsungStore() && !FTApp.getPref().hasAgreedPrivacyPolicy()) {
                FTApp.userConsentDialog(getContext(), new FTDialogFactory.OnAlertDialogShownListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which) {
                        if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler)
                            new FTWebDavCredentialsDialog(mServiceAccountHandler).show(getChildFragmentManager());
                        else
                            mServiceAccountHandler.signIn(getContext());
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which) {
                        //Do Nothing
                    }
                });
            } else {
                if (mServiceAccountHandler instanceof FTWebDavServiceAccountHandler)
                    new FTWebDavCredentialsDialog(mServiceAccountHandler).show(getChildFragmentManager());
                else
                    mServiceAccountHandler.signIn(getContext());
            }
        }
    }

    private void showWebDavAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.webdav_dialog_alert))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
}