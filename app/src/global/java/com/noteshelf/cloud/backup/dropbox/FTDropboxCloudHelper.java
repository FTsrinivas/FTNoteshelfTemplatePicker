package com.noteshelf.cloud.backup.dropbox;

import android.util.Log;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.fileproperties.PropertyField;
import com.dropbox.core.v2.fileproperties.PropertyGroup;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationResult;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;
import com.fluidtouch.noteshelf.backup.database.FTDropboxBackupItem;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.noteshelf.cloud.OnTaskCompletedListener;
import com.noteshelf.cloud.backup.FTCloudStorageDetails;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by Sreenu on 20/02/19
 */
public class FTDropboxCloudHelper {
    private final String RELATIVE_PATH = "relativePath";
    private final String UUID = "UUID";

    private DbxClientV2 mDbxClient;

    public FTDropboxCloudHelper(com.noteshelf.cloud.backup.dropbox.FTDropboxServicePublisher ftDropboxServicePublisher) {
        this.mDbxClient = ftDropboxServicePublisher.authenticateService();
    }

    private List<PropertyGroup> getProperties(String relativePath) {
        List<PropertyGroup> groups = new ArrayList<>();
        List<PropertyField> fields = new ArrayList<>();
        fields.add(new PropertyField(RELATIVE_PATH, relativePath));
        groups.add(new PropertyGroup("/Default", fields));
        return groups;
    }

    public Task<FTDropboxBackupItem> readFile(FTDropboxBackupItem backupItem, String relativePath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {

            Log.i("FTNoteshelf", "success");
            Metadata metadata = mDbxClient.files().getMetadata("/" + relativePath);

            return getBackUpItem((FileMetadata) metadata, backupItem);
        });
    }

    public Task<FTDropboxBackupItem> createOrUploadFile(FTDropboxBackupItem backupItem, String relativePath, java.io.File localFile) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            InputStream fileInputStream = new FileInputStream(localFile);

            FileMetadata fileMetadata = mDbxClient.files().uploadBuilder("/" + relativePath)
//                    .withPropertyGroups(getProperties(relativePath))
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(fileInputStream);

            return getBackUpItem(fileMetadata, backupItem);
        });
    }

    public Task<FTDropboxBackupItem> moveFile(FTDropboxBackupItem backupItem, String fromPath, String toPath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            RelocationResult relocationResult = mDbxClient.files().moveV2("/" + fromPath, "/" + toPath);
            return getBackUpItem((FileMetadata) relocationResult.getMetadata(), backupItem);
        });
    }

    public void getStorageDetails(com.noteshelf.cloud.backup.dropbox.FTDropboxServicePublisher ftDropboxServicePublisher, OnTaskCompletedListener taskCompletedListener) {
        DbxClientV2 dbxClient = ftDropboxServicePublisher.authenticateService();
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            SpaceUsage spaceUsage = dbxClient.users().getSpaceUsage();
            FullAccount account = dbxClient.users().getCurrentAccount();
            FTCloudStorageDetails details = new FTCloudStorageDetails();
            details.consumedBytes = spaceUsage.getUsed();
            details.totalBytes = spaceUsage.getAllocation().getIndividualValue().getAllocated();
            details.username = account.getEmail();
            taskCompletedListener.OnTaskCompleted(details);
            return details;
        });
    }

    private FTDropboxBackupItem getBackUpItem(FileMetadata fileMetadata, FTDropboxBackupItem backupItem) {
        backupItem.setCloudId(fileMetadata.getId());
        return backupItem;
    }

    public DbxClientV2 getClient() {
        return mDbxClient;
    }
}
