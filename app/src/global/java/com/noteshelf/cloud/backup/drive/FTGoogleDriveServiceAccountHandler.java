package com.noteshelf.cloud.backup.drive;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.noteshelf.cloud.OnSuccessListener;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

/**
 * Created by Sreenu on 30/01/19
 */
public class FTGoogleDriveServiceAccountHandler implements FTServiceAccountHandler {
    public static final int REQUEST_CODE_SIGN_IN = 1000;

    private GoogleSignInClient getSignInClient(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken("47590175345-k6eb0hfunrr41qq5sj4h704s3rhf26sr.apps.googleusercontent.com")
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }

    @Override
    public void signIn(Context context) {
        ((FragmentActivity) context).startActivityForResult(getSignInClient(context).getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void signOut(Context context, FTServicePublishManager.OnSignOutCallback onSignOutCallback) {
        getSignInClient(context).signOut()
                .addOnSuccessListener(aVoid -> {
                    onSignOutCallback.onSignOutFinished();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, R.string.unable_to_sign_out_now_please_try_again_later, Toast.LENGTH_SHORT).show();
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

        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }

    @Override
    public void handleSignIn(Context context, Intent result, OnSuccessListener<String> onSuccessListener) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    int backUpType = FTApp.getPref().getBackUpType();
                    if (backUpType == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()) {
                        FTApp.getPref().saveBackupError("");
                    }
                    onSuccessListener.onSuccess(SystemPref.BackUpType.GOOGLE_DRIVE.name());
                })
                .addOnFailureListener(exception -> {
                    //Display some error message.
                });
    }
}
