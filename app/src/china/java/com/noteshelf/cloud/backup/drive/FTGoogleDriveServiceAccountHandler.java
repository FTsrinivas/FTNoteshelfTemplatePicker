package com.noteshelf.cloud.backup.drive;

import android.content.Context;
import android.content.Intent;

import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

/**
 * Created by Sreenu on 30/01/19
 */
public class FTGoogleDriveServiceAccountHandler implements FTServiceAccountHandler {
    public static final int REQUEST_CODE_SIGN_IN = 1000;

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
    public void handleSignIn(Context context, Intent result, OnSuccessListener<String> onSuccessListener) {

    }
}
