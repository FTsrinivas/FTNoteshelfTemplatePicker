package com.noteshelf.cloud.backup.drive;

import android.text.TextUtils;
import android.util.Log;

import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupItem;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.io.Files;
import com.noteshelf.cloud.OnTaskCompletedListener;
import com.noteshelf.cloud.backup.FTCloudStorageDetails;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by Sreenu on 14/02/19
 */
public class FTGoogleDriveCloudHelper {
    //App properties constants
    private final String RELATIVE_PATH = "relativePath";
    private final String UUID = "UUID";
    private final String CREATED_BY = "Created by";
    private final String APP_NAME = "Noteshelf";

    private Drive mDriveService;

    public FTGoogleDriveCloudHelper(com.noteshelf.cloud.backup.drive.FTGoogleDriverServicePublisher ftGoogleDriverServicePublisher) {
        this.mDriveService = ftGoogleDriverServicePublisher.authenticateService();
    }

    private byte[] readBytes(java.io.File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            try {
                byteBuffer.write(buffer, 0, len);
            } catch (Exception e) {
//                FTLog.logCrashException(e);
                return new ByteArrayOutputStream().toByteArray();
            }

        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public Task<FTGoogleDriveBackupItem> readFile(FTGoogleDriveBackupItem backupItem, String name, String relativePath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {

            File file = null;
            String query = "name = '" + name + "' and appProperties has { key='relativePath' and value='" + relativePath + "' } and trashed = false";
            FileList fileList = mDriveService.files().list().setQ(query)
                    .setFields("files(id, name, parents, modifiedTime, createdTime, appProperties)")
                    .execute();
            for (int i = 0; i < fileList.getFiles().size(); i++) {
                String relPath = fileList.getFiles().get(i).getAppProperties().get("relativePath");
                if (!TextUtils.isEmpty(relPath) && relPath.equals(relativePath)) {
                    file = fileList.getFiles().get(i);
                    break;
                }
            }

            return file == null ? null : getBackUpItem(file, backupItem);
        });
    }


    protected Task<List<File>> queryFiles(String nextPageToken) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            List<File> files = new ArrayList<>();
            do {
                FileList result = mDriveService.files().list()
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(nextPageToken)
                        .execute();

                files.addAll(result.getFiles());
            } while (nextPageToken != null);

            return files;
        });
    }

    Task<FTGoogleDriveBackupItem> createFile(FTGoogleDriveBackupItem backupItem, java.io.File localFile, String parentIds, String documentUUID, String relativePath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            long currentMillis = System.currentTimeMillis();
            File metadataFile = new File();
            metadataFile.setName(localFile.getName());
            metadataFile.setCreatedTime(new DateTime(currentMillis));
            metadataFile.setModifiedTime(new DateTime(currentMillis));

            HashMap<String, String> properties = new HashMap<>();
            properties.put(UUID, documentUUID);
            properties.put(RELATIVE_PATH, relativePath);
//            properties.put(CREATED_BY, APP_NAME);
            metadataFile.setAppProperties(properties);
            if (!parentIds.equals(""))
                metadataFile.setParents(new ArrayList<>(Arrays.asList(parentIds.split(","))));

            //ByteArrayContent byteArrayContent = new ByteArrayContent("[*/*]", readBytes(localFile));
            ByteArrayContent byteArrayContent = new ByteArrayContent("[*/*]", Files.toByteArray(localFile));
            File file = mDriveService.files().create(metadataFile, byteArrayContent)
                    .setFields("id, name, parents, modifiedTime, createdTime, appProperties")
                    .execute();

            return getBackUpItem(file, backupItem);
        });
    }

    Task<FTGoogleDriveBackupItem> updateFile(FTGoogleDriveBackupItem backupItem, String relativePath, String fieldId, java.io.File localUpdatedFile) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            File metadataFile = new File();
            metadataFile.setName(localUpdatedFile.getName());
            metadataFile.setModifiedTime(new DateTime(System.currentTimeMillis()));

            HashMap<String, String> properties = new HashMap<>();
            properties.put(RELATIVE_PATH, relativePath);
            metadataFile.setAppProperties(properties);

//            ByteArrayContent byteArrayContent = new ByteArrayContent("[*/*]", readBytes(localUpdatedFile));
            ByteArrayContent byteArrayContent = new ByteArrayContent("[*/*]", Files.toByteArray(localUpdatedFile));
            File file = mDriveService.files().update(fieldId, metadataFile, byteArrayContent)
                    .setFields("id, name, parents, modifiedTime, createdTime, appProperties")
                    .execute();
            return getBackUpItem(file, backupItem);
        });
    }

    Task<FTGoogleDriveBackupItem> createFolder(FTGoogleDriveBackupItem backupItem, String folderName, String parentId, String relativePath) {
        Log.i("FTNoteShelf", "folder name" + folderName);
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            if (!parentId.equals(""))
                fileMetadata.setParents(new ArrayList<>(Arrays.asList(parentId.split(","))));

            //Should set this mime-type for folder creation
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            HashMap<String, String> properties = new HashMap<>();
            properties.put(RELATIVE_PATH, relativePath);
//            properties.put(CREATED_BY, APP_NAME);
            fileMetadata.setAppProperties(properties);

            File file = mDriveService.files().create(fileMetadata)
                    //Setting which fields should be returned after creation of folder.
                    .setFields("id, name, parents, modifiedTime, createdTime, appProperties")
                    .execute();
            return getBackUpItem(file, backupItem);
        });
    }

    Task<FTGoogleDriveBackupItem> moveFile(FTGoogleDriveBackupItem backupItem, String fileId, String toFolderId) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            File file = mDriveService.files().get(fileId)
                    .setFields("parents")
                    .execute();
            mDriveService.files().list().setQ("name = 'hello'").execute();

            //Getting current parents ids for removal
            StringBuilder currentParents = new StringBuilder();
            for (String parent : file.getParents()) {
                currentParents.append(parent);
                currentParents.append(',');
            }

            file = mDriveService.files().update(fileId, null)
                    .setAddParents(toFolderId)
                    .setRemoveParents(currentParents.toString())
                    .setFields("id, name, parents, modifiedTime, createdTime, appProperties")
                    .execute();
            return getBackUpItem(file, backupItem);
        });
    }

    public Task<Void> downloadFile(String fileId) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    Task<FTGoogleDriveBackupItem> readFolder(FTGoogleDriveBackupItem backupItem, String name, String relativePath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            File file = null;
            String query = "name = '" + name + "' and appProperties has { key='relativePath' and value='" + relativePath + "' } and trashed = false";
            FileList fileList = mDriveService.files().list().setQ(query)
                    .setFields("files(id, name, parents, modifiedTime, createdTime, appProperties)")
                    .execute();
            for (int i = 0; i < fileList.getFiles().size(); i++) {
                String relPath = fileList.getFiles().get(i).getAppProperties().get("relativePath");
                if (!TextUtils.isEmpty(relPath) && relPath.equals(relativePath)) {
                    file = fileList.getFiles().get(i);
                    break;
                }
            }

            return file == null ? null : getBackUpItem(file, backupItem);
        });
    }

    public void getStorageDetails(com.noteshelf.cloud.backup.drive.FTGoogleDriverServicePublisher ftGoogleDriverServicePublisher, OnTaskCompletedListener taskCompletedListener) {
        Drive driveService = ftGoogleDriverServicePublisher.authenticateService();
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try {
                About aboutDetails = driveService.about().get().setFields("user,storageQuota").execute();
                FTCloudStorageDetails details = new FTCloudStorageDetails();
                details.totalBytes = aboutDetails.getStorageQuota().getLimit();
                details.consumedBytes = aboutDetails.getStorageQuota().getUsage();
                details.username = aboutDetails.getUser().getEmailAddress();
                taskCompletedListener.OnTaskCompleted(details);
                return details;
            } catch (Exception e) {
                e.printStackTrace();
//                FTLog.logCrashException(e);
                taskCompletedListener.OnTaskCompleted(null);
                return null;
            }
        });
    }

    private FTGoogleDriveBackupItem getBackUpItem(File file, FTGoogleDriveBackupItem backupItem) {
        backupItem.setCloudId(file.getId());
        backupItem.setCloudParentId(file.getParents().get(0));
        return backupItem;
    }

}
