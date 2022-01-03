package com.noteshelf.cloud.backup.onedrive;

import com.fluidtouch.noteshelf.backup.database.FTBackupItem;

import com.noteshelf.cloud.OnTaskCompletedListener;

public class FTOneDriveCloudHelper {

    public FTOneDriveCloudHelper(FTOneDriveServicePublisher oneDriveServicePublisher) {

    }

    public void getStorageDetails(OnTaskCompletedListener listener) {

    }

    public interface Callback {
        void onSuccess(FTBackupItem backupItem);

        void onFailure(FTBackupItem backupItem, Exception ex);
    }
}