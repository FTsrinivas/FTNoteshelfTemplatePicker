package com.fluidtouch.noteshelf.cloud.backup.webdav;

import android.content.Context;

import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTWebDavBackupItem;
import com.fluidtouch.noteshelf.cloud.backup.FTBackupException;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTServicePublisher;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.impl.SardineException;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class FTWebDavServicePublisher implements FTServicePublisher {

    private final FTWebDavCloudHelper cloudHelper;
    private final Context context;

    public FTWebDavServicePublisher(Context context) {
        this.cloudHelper = new FTWebDavCloudHelper(context);
        this.context = context;
    }

    @Override
    public void uploadFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        DavResource davResource = this.cloudHelper.readFile(backupItem.getRelativePath());
        if (davResource == null) {
            this.cloudHelper.uploadFile((FTWebDavBackupItem) backupItem, url, zippedFile, new FTWebDavCloudHelper.Callback() {
                @Override
                public void onSuccess(FTBackupItem backupItem) {
                    taskListener.onFinished(backupItem);
                }

                @Override
                public void onFailure(FTBackupItem backupItem, Exception ex) {
                    handleException(backupItem, ex, taskListener);
                }
            });
        } else {
            this.cloudHelper.updateFile((FTWebDavBackupItem) backupItem, url, zippedFile, new FTWebDavCloudHelper.Callback() {
                @Override
                public void onSuccess(FTBackupItem backupItem) {
                    taskListener.onFinished(backupItem);
                }

                @Override
                public void onFailure(FTBackupItem backupItem, Exception ex) {
                    handleException(backupItem, ex, taskListener);
                }
            });
        }
        //}
    }

    @Override
    public void moveFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener) {
        DavResource davResource = this.cloudHelper.readFile(backupItem.getRelativePath());
        if (davResource == null) {
            this.uploadFile(backupItem, url, documentUUID, zippedFile, taskListener);
        } else {
            this.cloudHelper.moveFile(((FTWebDavBackupItem) backupItem), url, zippedFile, new FTWebDavCloudHelper.Callback() {
                @Override
                public void onSuccess(FTBackupItem backupItem) {
                    taskListener.onFinished(backupItem);
                }

                @Override
                public void onFailure(FTBackupItem backupItem, Exception ex) {
                    handleException(backupItem, ex, taskListener);
                }
            });
        }
        //}
    }

    private void handleException(FTBackupItem backupItem, Exception exception, FTServicePublishManager.TaskListener listener) {
        FTBackupException backupException = new FTBackupException.FTUnknownException(exception);
        if (exception == null)
            backupException = new FTBackupException.FTUnknownException();
        else if (exception instanceof SardineException) {
            int code = ((SardineException) exception).getStatusCode();
            switch (code) {
                case 404:
                    backupException = new FTBackupException.FTFileNotFoundException();
                    break;
                case 403:
                case 408:
                case 503:
                    backupException = new FTBackupException.FTRetryException();
                    break;
                case 405:
                    backupException = new FTBackupException.FTBadRequestException();
                    break;
                case 409:
                    backupException = new FTBackupException.FTForbiddenException(context.getString(R.string.webdav_backup_location_error));
                    break;
                case 429:
                    backupException = new FTBackupException.FTTooManyRequestException();
                    break;
                case 502:
                    backupException = new FTBackupException.FTBackendException();
                    break;
                case 413:
                    backupException = new FTBackupException.FTUnknownException(context.getString(R.string.file_size_is_too_large) + ": " + backupItem.getDisplayName());
                    break;
            }
        } else if (exception instanceof FileNotFoundException) {
            backupException = new FTBackupException.FTFileNotFoundException();
        } else if (exception instanceof ConnectException) {
            backupException = new FTBackupException.FTNoInternetException();
        } else if (exception instanceof SocketTimeoutException) {
            backupException = new FTBackupException.FTUnknownException(context.getString(R.string.file_size_is_too_large) + ": " + backupItem.getDisplayName());
        } else if (exception instanceof IllegalArgumentException) {
            backupException = new FTBackupException.FTUnknownException(new Exception(context.getString(R.string.special_characters_are_not_allowed)));
        }
        listener.onFailure(backupItem, backupException);
    }
}