package com.fluidtouch.noteshelf.backup.database;

/**
 * Created by Sreenu on 18/02/19
 */
public interface FTGoogleDriveBackupItem {
    String getCloudId();

    void setCloudId(String cloudId);

    String getCloudParentId();

    void setCloudParentId(String cloudParentId);
}
