package com.fluidtouch.noteshelf.backup.database;

/**
 * Created by Sreenu on 18/02/19
 */
public interface FTDropboxBackupItem {
    String getCloudId();

    void setCloudId(String cloudId);

    String getRelativePath();

    void setRelativePath(String relativePath);
}
