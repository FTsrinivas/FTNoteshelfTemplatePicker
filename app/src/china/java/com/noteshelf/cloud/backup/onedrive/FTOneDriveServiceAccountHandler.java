package com.noteshelf.cloud.backup.onedrive;

import android.content.Context;
import android.content.Intent;

import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

public class FTOneDriveServiceAccountHandler implements FTServiceAccountHandler {

    public void signIn(Context context) {

    }

    private void loadAccount() {

    }

    private void signIn() {

    }

    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback) {
    }

    public boolean checkSession(Context context) {
        return false;
    }

    public void handleSignIn(Context context, Intent intent, OnSuccessListener<String> onSuccessListener) {

    }
}