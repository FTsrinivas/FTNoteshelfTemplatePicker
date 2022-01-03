package com.noteshelf.cloud.backup;

import android.content.Context;
import android.content.Intent;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.drive.FTGoogleDriveServiceAccountHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServiceAccountHandler;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServiceAccountHandler;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServiceAccountHandler;

/**
 * Created by Sreenu on 30/01/19
 */
public interface FTServiceAccountHandler {
    static FTServiceAccountHandler getInstance() {
        int backUpType = FTApp.getPref().getBackUpType();
        if (backUpType == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()) {
            return new FTGoogleDriveServiceAccountHandler();
        } else if (backUpType == SystemPref.BackUpType.DROPBOX.ordinal()) {
            return new FTDropboxServiceAccountHandler();
        } else if (backUpType == SystemPref.BackUpType.ONE_DRIVE.ordinal()) {
            return new FTOneDriveServiceAccountHandler();
        } else if (backUpType == SystemPref.BackUpType.WEBDAV.ordinal()) {
            return new FTWebDavServiceAccountHandler();
        } else {
            return null;
        }
    }

    void signIn(Context context);

    void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback);

    boolean checkSession(Context context);

    void handleSignIn(Context context, Intent intent, OnSuccessListener<String> onSuccessListener);
}
