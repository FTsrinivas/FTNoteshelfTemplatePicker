package com.fluidtouch.noteshelf.backup.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FTOneDriveBackupCloudTable implements FTBackupItem, FTOneDriveBackupItem {
    @Id
    private String documentUUId;
    private String displayName;
    private String cloudId;
    private String cloudParentId;
    private String relativePath = "";
    private long uploadedTime = 0;
    private int errorHandlingType;
    private String error;

    public FTOneDriveBackupCloudTable() {
    }

    @Generated(hash = 692899904)
    public FTOneDriveBackupCloudTable(String documentUUId, String displayName, String cloudId,
            String cloudParentId, String relativePath, long uploadedTime,
            int errorHandlingType, String error) {
        this.documentUUId = documentUUId;
        this.displayName = displayName;
        this.cloudId = cloudId;
        this.cloudParentId = cloudParentId;
        this.relativePath = relativePath;
        this.uploadedTime = uploadedTime;
        this.errorHandlingType = errorHandlingType;
        this.error = error;
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

    public String getCloudParentId() {
        return this.cloudParentId;
    }

    public void setCloudParentId(String cloudParentId) {
        this.cloudParentId = cloudParentId;
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
