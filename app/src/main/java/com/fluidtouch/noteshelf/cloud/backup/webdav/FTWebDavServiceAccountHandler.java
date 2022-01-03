package com.fluidtouch.noteshelf.cloud.backup.webdav;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

public class FTWebDavServiceAccountHandler implements FTServiceAccountHandler {

    @Override
    public void signIn(Context context) {

    }

    public void signIn(FTWebDavCredentials webDavCredentials, OkHttpSardine.AuthenticationCallback callback) {
        FTWebDavClient.getInstance().sardine.authenticate(webDavCredentials.getServerAddress(), webDavCredentials.getUsername(), webDavCredentials.getPassword(), callback);
    }

    @Override
    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback
            onSignOutCallback) {
        FTApp.getPref().saveWebDavCredentials(null);
        onSignOutCallback.onSignOutFinished();
    }

    @Override
    public boolean checkSession(Context context) {
        return FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(context) && FTApp.getPref().getWebDavCredentials() != null && !TextUtils.isEmpty(FTApp.getPref().getWebDavCredentials().getServerAddress());
    }

    @Override
    public void handleSignIn(Context context, Intent
            intent, OnSuccessListener<String> onSuccessListener) {
        int backUpType = FTApp.getPref().getBackUpType();
        if (backUpType == SystemPref.BackUpType.WEBDAV.ordinal()) {
            FTApp.getPref().saveBackupError("");
        }
        onSuccessListener.onSuccess(SystemPref.BackUpType.WEBDAV.name());
    }
}