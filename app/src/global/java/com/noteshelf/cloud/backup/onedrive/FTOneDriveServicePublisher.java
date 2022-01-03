package com.noteshelf.cloud.backup.onedrive;

import android.text.TextUtils;

import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.cloud.backup.FTBackupException;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.common.exception.ClientException;
import com.noteshelf.cloud.OnTaskCompletedListener;
import com.noteshelf.cloud.backup.FTServicePublisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;

public class FTOneDriveServicePublisher implements FTServicePublisher {

    private final FTOneDriveCloudHelper cloudHelper;

    public FTOneDriveServicePublisher() {
        this.cloudHelper = new FTOneDriveCloudHelper(this);
    }

    @Override
    public void uploadFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        this.cloudHelper.readFile(TextUtils.isEmpty(backupItem.getRelativePath()) ? url : backupItem.getRelativePath()).addOnSuccessListener(driveItem -> {
            if (driveItem == null) {
                this.cloudHelper.uploadFile(backupItem, url, zippedFile, new FTOneDriveCloudHelper.Callback() {
                    @Override
                    public void onSuccess(FTBackupItem backupItem1) {
                        taskListener.onFinished(backupItem1);
                    }

                    @Override
                    public void onFailure(FTBackupItem backupItem, Exception exception) {
                        handleException(backupItem, exception, taskListener);
                    }
                });
            } else {
                cloudHelper.updateFile(driveItem, backupItem, url, zippedFile, new FTOneDriveCloudHelper.Callback() {
                    @Override
                    public void onSuccess(FTBackupItem backupItem1) {
                        taskListener.onFinished(backupItem);
                    }

                    @Override
                    public void onFailure(FTBackupItem backupItem1, Exception exception) {
                        handleException(backupItem, exception, taskListener);
                    }
                });
            }
        });
    }

    @Override
    public void moveFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        this.cloudHelper.readFile(TextUtils.isEmpty(backupItem.getRelativePath()) ? url : backupItem.getRelativePath()).addOnSuccessListener(driveItem -> {
            if (driveItem == null) {
                uploadFile(backupItem, url, documentUUID, zippedFile, taskListener);
            } else {
                cloudHelper.moveFile(driveItem, backupItem, url, zippedFile, new FTOneDriveCloudHelper.Callback() {
                    @Override
                    public void onSuccess(FTBackupItem backupItem1) {
                        taskListener.onFinished(backupItem);
                    }

                    @Override
                    public void onFailure(FTBackupItem backupItem1, Exception exception) {
                        handleException(backupItem, exception, taskListener);
                    }
                });
            }
        });
    }

    public void getStorageDetails(OnTaskCompletedListener listener) {
        this.cloudHelper.getStorageDetails(listener);
    }

    private void handleException(FTBackupItem backupItem, Exception exception, FTServicePublishManager.TaskListener listener) {
        FTBackupException backupException = new FTBackupException.FTUnknownException(exception);
        if (exception == null)
            backupException = new FTBackupException.FTUnknownException();
        else if (exception instanceof GraphServiceException) {
            switch (((GraphServiceException) exception).getResponseCode()) {
                case 400:
                    backupException = new FTBackupException.FTBadRequestException();
                    break;
                case 401:
                    backupException = new FTBackupException.FTTokenExpiredException();
                    break;
                case 403:
                    backupException = new FTBackupException.FTForbiddenException("");
                    break;
                case 404:
                    backupException = new FTBackupException.FTFileNotFoundException();
                    break;
                case 408:
                    backupException = new FTBackupException.FTRequestTimeOutException();
                    break;
                case 409:
                    backupException = new FTBackupException.FTConflictException();
                    break;
                case 429:
                    backupException = new FTBackupException.FTTooManyRequestException();
                    break;
            }
        } else if ((exception instanceof MsalClientException) || (exception instanceof ClientException)) {
            backupException = new FTBackupException.FTTokenExpiredException();
        } else if (exception.getCause() != null && exception.getCause() instanceof ConnectException) {
            backupException = new FTBackupException.FTNoInternetException();
        } else if (exception instanceof FileNotFoundException) {
            backupException = new FTBackupException.FTFileNotFoundException();
        }
        listener.onFailure(backupItem, backupException);
    }
}
