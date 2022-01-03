package com.fluidtouch.noteshelf.backup.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Sreenu on 04/02/19
 */
@Entity
public class FTDropboxBackupCloudTable implements FTBackupItem, FTDropboxBackupItem {
    @Id
    private String documentUUId;
    private String displayName;
    private String cloudId;
    private String relativePath;
    private long uploadedTime = 0;
    private int errorHandlingType;
    private String error;

    @Generated(hash = 1346752328)
    public FTDropboxBackupCloudTable(String documentUUId, String displayName, String cloudId,
                                     String relativePath, long uploadedTime, int errorHandlingType, String error) {
        this.documentUUId = documentUUId;
        this.displayName = displayName;
        this.cloudId = cloudId;
        this.relativePath = relativePath;
        this.uploadedTime = uploadedTime;
        this.errorHandlingType = errorHandlingType;
        this.error = error;
    }

    @Generated(hash = 345591011)
    public FTDropboxBackupCloudTable() {
    }

    public String getDocumentUUId() {
        return this.documentUUId;
    }

    public void setDocumentUUId(String documentUUId) {
        this.documentUUId = documentUUId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCloudId() {
        return this.cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public long getUploadedTime() {
        return this.uploadedTime;
    }

    public void setUploadedTime(long uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    public int getErrorHandlingType() {
        return this.errorHandlingType;
    }

    public void setErrorHandlingType(int errorHandlingType) {
        this.errorHandlingType = errorHandlingType;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
