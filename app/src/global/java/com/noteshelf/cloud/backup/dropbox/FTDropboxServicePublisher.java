package com.noteshelf.cloud.backup.dropbox;

import android.content.Context;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.RetryException;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.RelocationError;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTDropboxBackupItem;
import com.fluidtouch.noteshelf.cloud.backup.FTBackupException;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTServicePublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Sreenu on 04/02/19
 */
public class FTDropboxServicePublisher implements FTServicePublisher {
    public FTDropboxCloudHelper cloudHelper;
    private FTServicePublishManager.TaskListener mTaskListener;
    private Context context;

    public DbxClientV2 authenticateService() {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(context.getString(R.string.app_name))
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();

        return new DbxClientV2(requestConfig, FTApp.getPref().getDropBoxToken());
    }

    public FTDropboxServicePublisher(Context context) {
        this.context = context;
        cloudHelper = new FTDropboxCloudHelper(this);
    }

    @Override
    public void uploadFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        mTaskListener = taskListener;
        cloudHelper.readFile((FTDropboxBackupItem) backupItem, backupItem.getRelativePath())
                .addOnSuccessListener(dropboxBackupItem -> moveFile((FTBackupItem) dropboxBackupItem, url, documentUUID, zippedFile, taskListener))
                .addOnFailureListener(dropboxBackupItem -> createOrUpdateFile((FTDropboxBackupItem) backupItem, url, zippedFile));
    }

    @Override
    public void moveFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        mTaskListener = taskListener;
        FTDropboxBackupItem ftDropboxBackupItem = (FTDropboxBackupItem) backupItem;
        moveFile(ftDropboxBackupItem, url, zippedFile);
    }

    private void createOrUpdateFile(FTDropboxBackupItem backupItem, String url, File localFile) {
        cloudHelper.createOrUploadFile(backupItem, url, localFile)
                .addOnSuccessListener(backUpItem -> mTaskListener.onFinished((FTBackupItem) backUpItem))
                .addOnFailureListener(exception -> onFailure(backupItem, exception));
    }

    private void moveFile(FTDropboxBackupItem backupItem, String url, File zippedFile) {
        if (cloudHelper != null) {
            cloudHelper.readFile(backupItem, url)
                    .addOnSuccessListener(backupItem1 -> createOrUpdateFile(backupItem, url, zippedFile))
                    .addOnFailureListener(e -> {
                        if (e instanceof GetMetadataErrorException
                                && ((GetMetadataErrorException) e).errorValue.isPath()
                                && ((GetMetadataErrorException) e).errorValue.getPathValue().toString().contains("not_found")) {
                            cloudHelper.moveFile(backupItem, backupItem.getRelativePath(), url)
                                    .addOnSuccessListener(backUpItem -> {
                                        createOrUpdateFile(backupItem, url, zippedFile);
                                    })
                                    .addOnFailureListener(exception -> {
                                        onFailure(backupItem, exception);
                                    });
                        } else {
                            onFailure(backupItem, e);
                        }
                    });
        }
    }

    private void onFailure(FTDropboxBackupItem backupItem, Exception exception) {
        FTBackupException backupException = new FTBackupException.FTUnknownException(exception);
        if (exception == null)
            backupException = new FTBackupException.FTUnknownException();
        else if (exception instanceof UnknownHostException) {
            backupException = new FTBackupException.FTCouldNotFindHostException();
        } else if (exception instanceof SocketTimeoutException) {
            backupException = new FTBackupException.FTSocketTimeOutException();
        } else if (exception instanceof RetryException) {
            backupException = new FTBackupException.FTRetryException();
        } else if (exception instanceof RelocationErrorException) {
            RelocationError relocationError = ((RelocationErrorException) exception).errorValue;
            String errorMessage = "";
            if (relocationError.isFromLookup()) {
                errorMessage = relocationError.getFromLookupValue().toStringMultiline();
            } else if (relocationError.isFromWrite()) {
                errorMessage = relocationError.getFromWriteValue().toStringMultiline();
            }
            errorMessage = errorMessage.contains("_") ? "Unable to move file" : errorMessage;
            backupException = new FTBackupException.FTRelocationException(errorMessage);
        } else if (exception instanceof InvalidAccessTokenException) {
            backupException = new FTBackupException.FTTokenExpiredException();
        } else if (exception instanceof DbxException) {
            //In future we may need to try for invalidTokenException also if required.
            backupException = new FTBackupException.FTRetryException();
        } else if (exception instanceof GetMetadataErrorException) {
            backupException = new FTBackupException.FTMalformedException(context.getString(R.string.special_characters_are_not_allowed));
        } else if (exception instanceof FileNotFoundException) {
            backupException = new FTBackupException.FTFileNotFoundException();
        }

        mTaskListener.onFailure((FTBackupItem) backupItem, backupException);
    }
}
