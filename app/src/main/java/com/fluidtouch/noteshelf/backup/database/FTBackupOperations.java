package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;

import java.util.List;

/**
 * Created by Sreenu on 14/02/19
 */
public interface FTBackupOperations {
    static FTBackupOperations getInstance() {
        int backUpType = FTApp.getPref().getBackUpType();
        if (backUpType == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()) {
            return new FTGoogleDriveBackupOperations();
        } else if (backUpType == SystemPref.BackUpType.DROPBOX.ordinal()) {
            return new FTDropboxBackupOperations();
        } else if (backUpType == SystemPref.BackUpType.ONE_DRIVE.ordinal()) {
            return new FTOneDriveBackupOperations();
        } else if (backUpType == SystemPref.BackUpType.WEBDAV.ordinal()) {
            return new FTWebDavBackupOperations();
        } else {
            return null;
        }
    }

    List<? extends FTBackupItem> getList();

    List<? extends FTBackupItem> getList(String documentUUID);

    void insertOrReplace(FTBackupItem backupItem);

    List<? extends FTBackupItem> getErrorList();

    void deleteAll();

    void delete(String documentUUID);
}
