package com.noteshelf.cloud.backup.dropbox;

import android.content.Context;
import android.content.Intent;

import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

/**
 * Created by Sreenu on 01/02/19
 */
public class FTDropboxServiceAccountHandler implements FTServiceAccountHandler {

    @Override
    public void signIn(Context context) {
    }

    @Override
    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback) {
    }

    @Override
    public boolean checkSession(Context context) {
        return false;
    }

    @Override
    public void handleSignIn(Context context, Intent
            intent, OnSuccessListener<String> onSuccessListener) {
    }

    public static String getOAuth2Token() {
        return null;
    }
}
