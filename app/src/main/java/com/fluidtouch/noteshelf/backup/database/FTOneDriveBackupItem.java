package com.fluidtouch.noteshelf.backup.database;

public interface FTOneDriveBackupItem {
    String getCloudId();

    void setCloudId(String cloudId);

    String getCloudParentId();

    void setCloudParentId(String cloudParentId);
}
