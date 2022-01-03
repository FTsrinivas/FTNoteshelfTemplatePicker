package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;

/**
 * Created by Sreenu on 04/02/19
 */
public interface FTBackupItem {
    static FTBackupItem getBackupItem() {
        if (FTApp.getPref().getBackUpType() == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()) {
            return new FTGoogleDriveBackupCloudTable();
        } else if (FTApp.getPref().getBackUpType() == SystemPref.BackUpType.DROPBOX.ordinal()) {
            return new FTDropboxBackupCloudTable();
        } else if (FTApp.getPref().getBackUpType() == SystemPref.BackUpType.ONE_DRIVE.ordinal()) {
            return new FTOneDriveBackupCloudTable();
        } else if (FTApp.getPref().getBackUpType() == SystemPref.BackUpType.WEBDAV.ordinal()) {
            return new FTWebDavBackupCloudTable();
        } else {
            return null;
        }
    }

    String getDocumentUUId();

    void setDocumentUUId(String documentUUId);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getRelativePath();

    void setRelativePath(String relativePath);

    long getUploadedTime();

    void setUploadedTime(long uploadedTime);

    String getError();

    void setError(String error);

    int getErrorHandlingType();

    void setErrorHandlingType(int errorHandlingType);

}
