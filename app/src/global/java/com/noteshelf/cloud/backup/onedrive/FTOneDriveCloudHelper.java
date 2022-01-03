package com.noteshelf.cloud.backup.onedrive;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemReference;
import com.microsoft.graph.models.extensions.UploadSession;
import com.noteshelf.cloud.OnTaskCompletedListener;
import com.noteshelf.cloud.backup.FTCloudStorageDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class FTOneDriveCloudHelper {

    public IGraphServiceClient mGraphClient;

    public FTOneDriveCloudHelper(FTOneDriveServicePublisher oneDriveServicePublisher) {
        FTOneDriveServiceAccountHandler accountHandler = new FTOneDriveServiceAccountHandler();
        accountHandler.callGraphAPI(FTApp.getPref().getOneDriveToken());
        mGraphClient = accountHandler.mGraphClient;
    }

    public Task<DriveItem> readFile(String relativePath) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try {
                return mGraphClient.me().drive().root().itemWithPath(relativePath).buildRequest().get();
            } catch (Exception e) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Could not read notebook from drive! Cause:\n" + e.getMessage());
            }
            return null;
        });
    }

    public void uploadFile(FTBackupItem backupItem, String url, File zippedFile, Callback taskListener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream fileStream = new FileInputStream(zippedFile);
                long streamSize = zippedFile.length();

                UploadSession uploadSession = mGraphClient
                        .me()
                        .drive()
                        .root()
                        .itemWithPath("/" + url)
                        .createUploadSession(new DriveItemUploadableProperties())
                        .buildRequest()
                        .post();

                int[] customConfig = {320 * 1024};
                ChunkedUploadProvider<DriveItem> chunkedUploadProvider = new ChunkedUploadProvider<>(uploadSession, mGraphClient, fileStream, streamSize, DriveItem.class);
                chunkedUploadProvider.upload(new IProgressCallback<DriveItem>() {
                    @Override
                    public void progress(long current, long max) {

                    }

                    @Override
                    public void success(DriveItem driveItem) {
                        FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "Notebook uploaded successfully.");
                        taskListener.onSuccess(backupItem);
                    }

                    @Override
                    public void failure(ClientException ex) {
                        FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Failed to upload notebook! Cause:\n" + ex.getMessage());
                        FTLog.crashlyticsLog("Failed to upload notebook! Cause:\n" + ex.getMessage());
                        taskListener.onFailure(backupItem, ex);
                    }
                }, customConfig);
            } catch (Exception e) {
                FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Failed to upload notebook! Cause:\n" + e.getMessage());
                FTLog.crashlyticsLog("Failed to upload notebook! Cause:\n" + e.getMessage());
                taskListener.onFailure(backupItem, e);
            }
        });
    }

    public void moveFile(DriveItem notebookDriveItem, FTBackupItem backupItem, String destinationUrl, File zippedFile, Callback taskListener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DriveItem driveItem = new DriveItem();
            driveItem.name = zippedFile.getName();
            driveItem.file = new com.microsoft.graph.models.extensions.File();
            ItemReference itemReference = new ItemReference();
            itemReference.path = "/drive/root:/" + destinationUrl.replace(zippedFile.getName(), "");
            driveItem.parentReference = itemReference;

            mGraphClient.me().drive().items(notebookDriveItem.id).buildRequest().patch(driveItem, new ICallback<DriveItem>() {
                @Override
                public void success(DriveItem driveItem) {
                    FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "File moved successfully.");
                    uploadFile(backupItem, destinationUrl, zippedFile, taskListener);
                    taskListener.onSuccess(backupItem);
                }

                @Override
                public void failure(ClientException ex) {
                    FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error moving file. Cause\n" + ex.getMessage());
                    FTLog.crashlyticsLog("Error moving file. Cause\n" + ex.getMessage());
                    taskListener.onFailure(backupItem, ex);
                }
            });
        });
    }

    public void updateFile(DriveItem notebookDriveItem, FTBackupItem backupItem, String url, File zippedFile, Callback taskListener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            DriveItem driveItem = new DriveItem();
            driveItem.name = zippedFile.getName();

            mGraphClient.me().drive().items(notebookDriveItem.id).buildRequest().patch(driveItem, new ICallback<DriveItem>() {
                @Override
                public void success(DriveItem driveItem) {
                    FTLog.debug(FTLog.ONE_DRIVE_BACKUP, "File updated successfully.");
                    uploadFile(backupItem, url, zippedFile, taskListener);
                }

                @Override
                public void failure(ClientException ex) {
                    FTLog.error(FTLog.ONE_DRIVE_BACKUP, "Error updating file. Cause\n" + ex.getMessage());
                    FTLog.crashlyticsLog("Error updating file. Cause\n" + ex.getMessage());
                    taskListener.onFailure(backupItem, ex);
                }
            });
        });
    }

    public void getStorageDetails(OnTaskCompletedListener listener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (new FTOneDriveServiceAccountHandler().checkSession(FTApp.getInstance().getApplicationContext())) {
                    Drive drive = mGraphClient.me().drive().buildRequest().get();
                    FTCloudStorageDetails storageDetails = new FTCloudStorageDetails();
                    storageDetails.consumedBytes = drive.quota.used;
                    storageDetails.totalBytes = drive.quota.total;
                    storageDetails.username = drive.owner.user.displayName;
                    listener.OnTaskCompleted(storageDetails);
                }
            } catch (Exception e) {
//                FTLog.logCrashException(e);
                if (e.getMessage() != null) FTLog.debug(FTLog.ONE_DRIVE_BACKUP, e.getMessage());
            }
        });
    }

    public interface Callback {
        void onSuccess(FTBackupItem backupItem);

        void onFailure(FTBackupItem backupItem, Exception ex);
    }
}