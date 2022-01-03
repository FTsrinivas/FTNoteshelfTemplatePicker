package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupOperations;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.commons.utils.NumberUtils;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTCloudStorageDetails;
import com.noteshelf.cloud.backup.drive.FTGoogleDriveCloudHelper;
import com.noteshelf.cloud.backup.drive.FTGoogleDriveServiceAccountHandler;
import com.noteshelf.cloud.backup.drive.FTGoogleDriverServicePublisher;
import com.noteshelf.cloud.backup.dropbox.FTDropboxCloudHelper;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServiceAccountHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServicePublisher;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveCloudHelper;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServiceAccountHandler;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServicePublisher;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavCredentials;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServiceAccountHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 05/02/19
 */

public class FTBackupSettingsDialog extends FTBaseDialog implements FTServicePublishManager.OnSignOutCallback {
    @BindView(R.id.backup_settings_dialog_title)
    TextView titleTextView;
    @BindView(R.id.backup_settings_dialog_progress)
    ProgressBar backupProgress;
    @BindView(R.id.backup_settings_dialog_data_used_text_view)
    TextView dataUsageTextView;
    @BindView(R.id.backup_settings_dialog_username)
    TextView usernameTextView;

    private SystemPref.BackUpType backingUpTo = SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()];
    private FTCloudStorageDetails mCloudStorageDetails;

    public static FTBackupSettingsDialog newInstance(SystemPref.BackUpType backingUpTo) {
        FTBackupSettingsDialog backupSettingsDialog = new FTBackupSettingsDialog();
        backupSettingsDialog.backingUpTo = backingUpTo;
        return backupSettingsDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_backup_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (backingUpTo.equals(SystemPref.BackUpType.GOOGLE_DRIVE) && FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
            titleTextView.setText(R.string.google_drive);
            FTGoogleDriverServicePublisher googleDriverServicePublisher = new FTGoogleDriverServicePublisher(getContext());
            FTGoogleDriveCloudHelper googleDriveCloudHelper = new FTGoogleDriveCloudHelper(googleDriverServicePublisher);
            googleDriveCloudHelper.getStorageDetails(googleDriverServicePublisher, response -> {
                if (response instanceof FTCloudStorageDetails)
                    mCloudStorageDetails = (FTCloudStorageDetails) response;
                if (isAdded()) view.post(this::setStorageDetailsUI);
            });
        } else if (backingUpTo.equals(SystemPref.BackUpType.DROPBOX) && FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
            titleTextView.setText(R.string.dropbox);
            FTDropboxServicePublisher dropboxServicePublisher = new FTDropboxServicePublisher(getContext());
            FTDropboxCloudHelper dropboxCloudHelper = new FTDropboxCloudHelper(dropboxServicePublisher);
            dropboxCloudHelper.getStorageDetails(dropboxServicePublisher, response -> {
                if (response instanceof FTCloudStorageDetails)
                    mCloudStorageDetails = (FTCloudStorageDetails) response;
                if (isAdded()) view.post(this::setStorageDetailsUI);
            });
        } else if (backingUpTo.equals(SystemPref.BackUpType.ONE_DRIVE) && FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
            titleTextView.setText(R.string.one_drive);
            FTOneDriveServicePublisher oneDriveServicePublisher = new FTOneDriveServicePublisher();
            FTOneDriveCloudHelper oneDriveCloudHelper = new FTOneDriveCloudHelper(oneDriveServicePublisher);
            oneDriveCloudHelper.getStorageDetails(response -> {
                if (response instanceof FTCloudStorageDetails) {
                    mCloudStorageDetails = (FTCloudStorageDetails) response;
                    if (isAdded()) view.post(FTBackupSettingsDialog.this::setStorageDetailsUI);
                }
            });
        } else if (backingUpTo.equals(SystemPref.BackUpType.WEBDAV) && FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
            titleTextView.setText(R.string.webdav);
            setStorageDetailsUI();
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCloudStorageDetails = (FTCloudStorageDetails) savedInstanceState.getSerializable(FTCloudStorageDetails.class.getName());
        }
        setStorageDetailsUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(FTCloudStorageDetails.class.getName(), mCloudStorageDetails);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick(R.id.backup_settings_dialog_sign_out_button)
    void onSignOutClicked() {
        if (backingUpTo.ordinal() == FTApp.getPref().getBackUpType()) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
            alertBuilder.setTitle(R.string.sign_out);
            alertBuilder.setMessage(getString(R.string.this_action_will_turn_off_auto_backup_for_type, backingUpTo.name()));
            alertBuilder.setPositiveButton(getString(R.string.sign_out), (dialog, which) -> {
                signOutTheUser();
                dialog.dismiss();
            });
            alertBuilder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            AlertDialog alert = alertBuilder.create();
            alert.show();
        } else {
            signOutTheUser();
        }
    }

    private void signOutTheUser() {
        if (backingUpTo == SystemPref.BackUpType.DROPBOX) {
            FTDropboxServiceAccountHandler dropboxServiceAccountHandler = new FTDropboxServiceAccountHandler();
            dropboxServiceAccountHandler.signOut(getContext(), FTBackupSettingsDialog.this);
        } else if (backingUpTo == SystemPref.BackUpType.GOOGLE_DRIVE) {
            FTGoogleDriveServiceAccountHandler googleDriveServiceAccountHandler = new FTGoogleDriveServiceAccountHandler();
            googleDriveServiceAccountHandler.signOut(getContext(), FTBackupSettingsDialog.this);
        } else if (backingUpTo == SystemPref.BackUpType.ONE_DRIVE) {
            FTOneDriveServiceAccountHandler oneDriveServiceAccountHandler = new FTOneDriveServiceAccountHandler();
            oneDriveServiceAccountHandler.signOut(getContext(), FTBackupSettingsDialog.this);
        } else if (backingUpTo == SystemPref.BackUpType.WEBDAV) {
            FTWebDavServiceAccountHandler webDavServiceAccountHandler = new FTWebDavServiceAccountHandler();
            webDavServiceAccountHandler.signOut(getContext(), FTBackupSettingsDialog.this);
        }
    }

    @Override
    public void onSignOutFinished() {
        if (FTBackupOperations.getInstance() != null) {
            FTBackupOperations.getInstance().deleteAll();
        }
        if (backingUpTo == SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()]) {
            FTApp.getPref().saveBackUpType(SystemPref.BackUpType.NONE);
        }
        FTLog.crashlyticsLog("Sign-out button clicked");
        if (getParentFragment() != null)
            ((BackupSettingsDialogListener) getParentFragment()).onSignOut();
        dismiss();
    }

    private void setStorageDetailsUI() {
        if (backingUpTo.equals(SystemPref.BackUpType.WEBDAV)) {
            FTWebDavCredentials webDavCredentials = FTApp.getPref().getWebDavCredentials();
            if (webDavCredentials != null) {
                backupProgress.setVisibility(View.GONE);
                dataUsageTextView.setText(Html.fromHtml(getString(R.string.logged_in_as, webDavCredentials.getUsername()), Html.FROM_HTML_MODE_COMPACT));
                dataUsageTextView.setAlpha(1.0f);
                usernameTextView.setText(getString(R.string.connect_through, webDavCredentials.getServerAddress()));
                usernameTextView.setAlpha(0.6f);
            }
        } else {
            if (mCloudStorageDetails == null) {
                usernameTextView.setText(getString(R.string.loading));
                dataUsageTextView.setText("");
                backupProgress.setVisibility(View.GONE);
                backupProgress.setProgress(0);
            } else {
                usernameTextView.setText(mCloudStorageDetails.username);
                dataUsageTextView.setText(mCloudStorageDetails.getStatus());
                backupProgress.setProgress(NumberUtils.calculatePercentage(mCloudStorageDetails.consumedBytes, mCloudStorageDetails.totalBytes));
                backupProgress.setVisibility(View.VISIBLE);
            }
        }
    }


    public interface BackupSettingsDialogListener {
        void onSignOut();
    }
}