package com.noteshelf.cloud.backup;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.noteshelf.cloud.backup.drive.FTGoogleDriverServicePublisher;
import com.noteshelf.cloud.backup.dropbox.FTDropboxServicePublisher;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveServicePublisher;
import com.fluidtouch.noteshelf.cloud.backup.webdav.FTWebDavServicePublisher;

import java.io.File;

/**
 * Created by Sreenu on 30/01/19
 */
public interface FTServicePublisher {
    public static FTServicePublisher getInstance(Context context) {
        int type = FTApp.getPref().getBackUpType();
        if (type == SystemPref.BackUpType.WEBDAV.ordinal()) {
            return new FTWebDavServicePublisher(context);
        } else {
            return null;
        }
    }

    void uploadFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener);

    void moveFile(FTBackupItem backupItem, String url, String documentUUID, File zippedFile, FTServicePublishManager.TaskListener taskListener);
}
