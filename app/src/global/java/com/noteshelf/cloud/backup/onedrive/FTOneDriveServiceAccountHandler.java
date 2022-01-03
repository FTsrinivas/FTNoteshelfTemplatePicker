package com.noteshelf.cloud.backup.onedrive;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

public class FTOneDriveServiceAccountHandler implements FTServiceAccountHandler {
    private final static String[] SCOPES = {"User.Read", "Files.ReadWrite"};
    private final static String AUTHORITY = "https://login.microsoftonline.com/common";

    private final ISingleAccountPublicClientApplication mClientApp = FTOneDriveClientApp.mClientApp;

    public IGraphServiceClient mGraphClient;

    @Override
    public void signIn(Context context) {
        if (mClientApp == null) return;
        loadAccount();
    }

    private void loadAccount() {
        mClientApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(@Nullable IAccount activeAccount) {
                if (activeAccount == null) {
                    signIn();
                } else {
                    FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "OneDrive account loaded");
                    acquireToken();
                }
            }

            @Override
            public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
                signIn();
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error while loading account. Cause:\n" + exception.getMessage());
                signIn();
            }
        });
    }

    private void signIn() {
        mClientApp.signIn((AppCompatActivity) FTApp.getInstance().getCurActCtx(), null, SCOPES, new AuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Successfully authenticated OneDrive account");
                FTApp.getPref().saveOneDriveToken(authenticationResult.getAccessToken());
                callGraphAPI(authenticationResult.getAccessToken());
                sendAuthBroadcast();
            }

            @Override
            public void onError(MsalException exception) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Authentication failed for OneDrive. Cause:" + exception.getMessage());
                FTLog.crashlyticsLog("Authentication failed for OneDrive. Cause:" + exception.getMessage());
                FTApp.getPref().saveOneDriveToken("");
            }

            @Override
            public void onCancel() {
                FTApp.getPref().saveOneDriveToken("");
            }
        });
    }

    @Override
    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback) {
        if (FTApp.getPref().getOneDriveToken().equals("")) {
            onSignOutCallback.onSignOutFinished();
            return;
        }
        mClientApp.signOut(new ISingleAccountPublicClientApplication.SignOutCallback() {
            @Override
            public void onSignOut() {
                FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Successfully signed out of OneDrive");
                FTApp.getPref().saveOneDriveToken("");
                onSignOutCallback.onSignOutFinished();
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error while signing out of OneDrive. Cause:" + exception.getMessage());
                FTLog.crashlyticsLog("Error while signing out of OneDrive. Cause:" + exception.getMessage());
                FTApp.getPref().saveOneDriveToken("");
                onSignOutCallback.onSignOutFinished();
            }
        });
    }

    @Override
    public boolean checkSession(Context context) {
//        String cloudError = FTApp.getPref().getBackupError();
//        if (!cloudError.equals("")) {
//            if (cloudError.equals("Your session expired, please login again.")) {
//                return false;
//            }
//        }
        return !FTApp.getPref().getOneDriveToken().equals("");
    }

    @Override
    public void handleSignIn(Context context, Intent intent, OnSuccessListener<String> onSuccessListener) {
        int backUpType = FTApp.getPref().getBackUpType();
        if (backUpType == SystemPref.BackUpType.ONE_DRIVE.ordinal()) {
            FTApp.getPref().saveBackupError("");
        }
        onSuccessListener.onSuccess(SystemPref.BackUpType.ONE_DRIVE.name());
    }

    private void acquireToken() {
        mClientApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, new SilentAuthenticationCallback() {
            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Acquired OneDrive token");
                FTApp.getPref().saveOneDriveToken(authenticationResult.getAccessToken());
                callGraphAPI(authenticationResult.getAccessToken());
                sendAuthBroadcast();
            }

            @Override
            public void onError(MsalException exception) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error while acquiring token. Cause:\n" + exception.getMessage());
                FTLog.crashlyticsLog("Error while acquiring token. Cause:\n" + exception.getMessage());
                FTApp.getPref().saveOneDriveToken("");
                signIn(FTApp.getInstance().getCurActCtx());
            }
        });
    }

    private void sendAuthBroadcast() {
        Context context = FTApp.getInstance().getApplicationContext();
        Intent intent = new Intent();
        intent.putExtra(context.getString(R.string.intent_is_successful), true);
        intent.setAction(context.getString(R.string.intent_sign_in_result));
        context.sendBroadcast(intent);
    }

    public void callGraphAPI(String accessToken) {
        if (mGraphClient == null)
            mGraphClient = GraphServiceClient.builder().authenticationProvider(request -> request.addHeader("Authorization", "Bearer " + accessToken)).buildClient();
    }
}