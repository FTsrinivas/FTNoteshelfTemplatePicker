package com.fluidtouch.noteshelf.backup.database;

public interface FTWebDavBackupItem extends FTBackupItem {
    String getCloudId();

    void setCloudId(String cloudId);

    String getCloudParentId();

    void setCloudParentId(String cloudParentId);
}