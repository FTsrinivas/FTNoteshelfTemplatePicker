package com.noteshelf.cloud.backup.onedrive;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;

public class FTOneDriveClientApp {
    private final static String[] SCOPES = {"User.Read", "Files.ReadWrite"};
    private final static String AUTHORITY = "https://login.microsoftonline.com/common";

    public static ISingleAccountPublicClientApplication mClientApp;

    public static void createApplication(Context context) {
        int authFileReference = R.raw.auth_config_single_account_dev_beta; //default

        // This works only for ProdRelease not for ProdDebug
        if (BuildConfig.FLAVOR.equals("prod")) {
            authFileReference = R.raw.auth_config_single_account_prod;
        } else if (BuildConfig.FLAVOR.equals("samsung")) {
            authFileReference = R.raw.auth_config_single_account_samsung;
        } else {
            authFileReference = R.raw.auth_config_single_account_dev_beta;
        }
        PublicClientApplication.createSingleAccountPublicClientApplication(context,
                authFileReference, new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                    @Override
                    public void onCreated(ISingleAccountPublicClientApplication application) {
                        FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Created OneDrive single account app.");
                        mClientApp = application;

                        mClientApp.acquireTokenSilentAsync(SCOPES, AUTHORITY, new SilentAuthenticationCallback() {
                            @Override
                            public void onSuccess(IAuthenticationResult authenticationResult) {
                                FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Acquired OneDrive token");
                                FTApp.getPref().saveOneDriveToken(authenticationResult.getAccessToken());
                            }

                            @Override
                            public void onError(MsalException exception) {
                                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error while acquiring token. Cause:\n" + exception.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onError(MsalException exception) {
                        FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Failed create OneDrive single account app. Cause:\n" + exception.getMessage());
                        FTApp.getPref().saveOneDriveToken("");
                    }
                });
    }
}
