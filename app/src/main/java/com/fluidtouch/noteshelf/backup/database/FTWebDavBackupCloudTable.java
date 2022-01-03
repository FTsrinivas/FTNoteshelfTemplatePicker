package com.fluidtouch.noteshelf.backup.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FTWebDavBackupCloudTable implements FTBackupItem, FTWebDavBackupItem {
    @Id
    private String documentUUId;
    private String displayName;
    private String cloudId;
    private String cloudParentId;
    private String relativePath = "";
    private long uploadedTime = 0;
    private int errorHandlingType;
    private String error;

    @Keep
    public FTWebDavBackupCloudTable(String documentUUId, String displayName, String cloudId,
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

    public FTWebDavBackupCloudTable() {
    }

    @Override
    public String getDocumentUUId() {
        return documentUUId;
    }

    @Override
    public void setDocumentUUId(String documentUUId) {
        this.documentUUId = documentUUId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getCloudId() {
        return cloudId;
    }

    @Override
    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    @Override
    public String getCloudParentId() {
        return cloudParentId;
    }

    @Override
    public void setCloudParentId(String cloudParentId) {
        this.cloudParentId = cloudParentId;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public long getUploadedTime() {
        return uploadedTime;
    }

    @Override
    public void setUploadedTime(long uploadedTime) {
        this.uploadedTime = uploadedTime;
    }

    @Override
    public int getErrorHandlingType() {
        return errorHandlingType;
    }

    @Override
    public void setErrorHandlingType(int errorHandlingType) {
        this.errorHandlingType = errorHandlingType;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }
}
