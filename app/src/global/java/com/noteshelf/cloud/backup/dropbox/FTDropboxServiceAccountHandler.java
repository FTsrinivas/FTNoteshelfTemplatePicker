package com.noteshelf.cloud.backup.dropbox;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;

import java.util.concurrent.Executors;

/**
 * Created by Sreenu on 01/02/19
 */
public class FTDropboxServiceAccountHandler implements FTServiceAccountHandler {
//    private final String APP_KEY = BuildConfig.dropBoxAppKey;

    @Override
    public void signIn(Context context) {
        try {
            Auth.startOAuth2Authentication(context, BuildConfig.dropBoxApiKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback) {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(context.getString(R.string.app_name))
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();

        DbxClientV2 sDbxClient = new DbxClientV2(requestConfig, FTApp.getPref().getDropBoxToken());
        revokeToken(sDbxClient)
                .addOnSuccessListener(o -> {
                    FTApp.getPref().saveDropBoxToken("");
                    onSignOutCallback.onSignOutFinished();
                })
                .addOnFailureListener(e -> {
                    if (e instanceof InvalidAccessTokenException) {
                        FTApp.getPref().saveDropBoxToken("");
                        onSignOutCallback.onSignOutFinished();
                        return;
                    }
                    Toast.makeText(context, R.string.unable_to_sign_out_now_please_try_again_later, Toast.LENGTH_SHORT).show();
                });
    }

    private Task<Object> revokeToken(DbxClientV2 sDbxClient) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            sDbxClient.auth().tokenRevoke();
            return null;
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

        return !FTApp.getPref().getDropBoxToken().equals("");
    }

    @Override
    public void handleSignIn(Context context, Intent
            intent, OnSuccessListener<String> onSuccessListener) {
        int backUpType = FTApp.getPref().getBackUpType();
        if (backUpType == SystemPref.BackUpType.DROPBOX.ordinal()) {
            FTApp.getPref().saveBackupError("");
        }
        FTApp.getPref().saveDropBoxToken(Auth.getOAuth2Token());
        onSuccessListener.onSuccess(SystemPref.BackUpType.DROPBOX.name());
    }

    public static String getOAuth2Token() {
        return Auth.getOAuth2Token();
    }
}
