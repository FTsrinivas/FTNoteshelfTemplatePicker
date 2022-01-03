package com.fluidtouch.noteshelf.cloud.backup;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTBackupOperations;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.commons.utils.NetworkType;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTServicePublisher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Sreenu on 30/01/19
 */
public class FTServicePublishManager {

    private final Context context = FTApp.getInstance().getApplicationContext();
    private final HashMap<String, String> fileQueue = new HashMap<>();

    private boolean isPublishingInProgress = false;
    private boolean continuePublishing = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void startPublishing() {
        if (isAllowBackup()) {
            continuePublishing = true;
            addUrls();
            FTApp.getPref().saveBackupError("");
            FTLog.debug(FTLog.BACKUP_PROCESS, "Started backup process for notebooks.");
            if (executorService.isShutdown() || executorService.isTerminated())
                executorService = Executors.newSingleThreadExecutor();
            checkAndPublishItem();
        }
    }

    public void stopPublishing() {
        FTLog.debug(FTLog.BACKUP_PROCESS, "Stopped backup process for notebooks.");
        continuePublishing = false;

        if (TextUtils.isEmpty(FTApp.getPref().getBackupError())) {
            FTApp.getPref().saveLastBackUpAt(System.currentTimeMillis());
            context.sendBroadcast(new Intent(context.getString(R.string.intent_backup_completed)));
            if (!executorService.isShutdown()) executorService.shutdownNow();
        }
        FTLog.debug(FTLog.BACKUP_PROCESS, "Flag set to terminate thread for backing up notebooks.");
    }

    private void checkAndPublishItem() {
        if (continuePublishing && isAllowBackup()) {
            ObservingService.getInstance().postNotification("backup_error", null);
            if (!fileQueue.isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> publishSingleFile(context, fileQueue.values().iterator().next()));
            }
        } else {
            stopPublishing();
        }
    }

    private void publishSingleFile(Context context, String url) {
        if (!continuePublishing || isPublishingInProgress) {
            return;
        }
        isPublishingInProgress = true;

        FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(FTUrl.parse(url));
        FTBackupOperations backupOperations = FTBackupOperations.getInstance();
        if (backupOperations != null && document.getDocumentUUID() != null) {
            List<? extends FTBackupItem> list = backupOperations.getList(document.getDocumentUUID());
            FTBackupItem ftBackupItem;
            if (list.isEmpty()) {
                ftBackupItem = FTBackupItem.getBackupItem();
                if (ftBackupItem != null) {
                    ftBackupItem.setDocumentUUId(document.getDocumentUUID());
                }
            } else {
                ftBackupItem = list.get(0);
            }

            ftBackupItem.setDisplayName(document.getDisplayTitle(context));
            backupOperations.insertOrReplace(ftBackupItem);

            if (new File(url).exists()) {
                String destPath = context.getCacheDir() + "/nsaBackup/";
                File fileExport = new File(destPath);
                if (!fileExport.exists())
                    fileExport.mkdirs();
                else {
                    FTFileManagerUtil.deleteFilesInsideFolder(fileExport);
                }
                ZipUtil.zip(context, url, destPath, FTConstants.NSA_EXTENSION, (zippedFile, error) -> {
                    if (error == null) {
                        try {
                            final String oldRemotePath = ftBackupItem.getRelativePath();
                            final String newRemotePath = FTFileManagerUtil.getExtensionRemovedPath(FTApp.getRelativePath(url)) + FTConstants.NSA_EXTENSION;

                            FTLog.debug(FTLog.BACKUP_PROCESS, "oldRemotePath = " + oldRemotePath + "\nnewRemotePath = " + newRemotePath);

                            if (TextUtils.isEmpty(oldRemotePath) || FTFileManagerUtil.getRelativePath(oldRemotePath).equals(FTFileManagerUtil.getRelativePath(newRemotePath))) {
                                FTServicePublisher.getInstance(context).uploadFile(ftBackupItem, newRemotePath, document.getDocumentUUID(), zippedFile, new TaskListener() {
                                    @Override
                                    public void onFinished(FTBackupItem backupItem) {
                                        FTLog.debug(FTLog.BACKUP_PROCESS, "Completed with " + backupItem.getDisplayName());
                                        backupItem.setError("");
                                        backupItem.setErrorHandlingType(FTBackupError.ErrorHandlingType.NO_ERROR.ordinal());
                                        backupItem.setRelativePath(newRemotePath);
                                        backupItem.setUploadedTime(System.currentTimeMillis());
                                        backupOperations.insertOrReplace(backupItem);
                                        removeNotebookFromQueue(document);
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            isPublishingInProgress = false;
                                            checkAndPublishItem();
                                        });
                                    }

                                    @Override
                                    public void onFailure(FTBackupItem backupItem, FTBackupException exception) {
                                        FTLog.error(FTLog.BACKUP_PROCESS, "Error with " + backupItem.getDisplayName());
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            handleException(document, backupItem, exception, backupOperations);
                                        });
                                    }
                                });
                            } else {
                                FTServicePublisher.getInstance(context).moveFile(ftBackupItem, newRemotePath, document.getDocumentUUID(), zippedFile, new TaskListener() {
                                    @Override
                                    public void onFinished(FTBackupItem backupItem) {
                                        FTLog.debug(FTLog.BACKUP_PROCESS, "Completed with " + backupItem.getDisplayName());
                                        backupItem.setError("");
                                        backupItem.setErrorHandlingType(FTBackupError.ErrorHandlingType.NO_ERROR.ordinal());
                                        backupItem.setRelativePath(newRemotePath);
                                        backupItem.setUploadedTime(System.currentTimeMillis());
                                        backupOperations.insertOrReplace(backupItem);
                                        removeNotebookFromQueue(document);
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            isPublishingInProgress = false;
                                            checkAndPublishItem();
                                        });
                                    }

                                    @Override
                                    public void onFailure(FTBackupItem backupItem, FTBackupException exception) {
                                        FTLog.error(FTLog.BACKUP_PROCESS, "Error with " + backupItem.getDisplayName());
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            handleException(document, backupItem, exception, backupOperations);
                                        });
                                    }
                                });
                            }
                        } catch (Exception e) {
                            FTLog.crashlyticsLog("Exception after zipping file for backup.");
                            FTLog.error(FTLog.BACKUP_PROCESS, "main looper error.");
                            handleException(document, null, new FTBackupException.FTRetryException(), backupOperations);
                        }
                    } else {
                        FTLog.crashlyticsLog("Failed to zip file for backup.");
                        FTLog.error(FTLog.BACKUP_PROCESS, "Error while zipping.");
                        handleException(document, null, new FTBackupException.FTZipFailedException(), backupOperations);
                    }
                });
            } else {
                FTLog.crashlyticsLog("Could not find shelfItem file for backup.");
                FTLog.error(FTLog.BACKUP_PROCESS, "File not found.");
                handleException(document, null, new FTBackupException.FTFileNotFoundException(), backupOperations);
            }
        }
    }

    private void handleException(FTNoteshelfDocument document, FTBackupItem backupItem, FTBackupException exception, FTBackupOperations backupOperations) {
        try {
            isPublishingInProgress = false;
            FTBackupError error;
            if (exception == null || exception.handleException() == null || TextUtils.isEmpty(exception.handleException().message)) {
                error = new FTBackupException.FTUnknownException().handleException();
            } else {
                error = exception.handleException();
            }
            FTApp.getPref().saveBackupError(error.message);
            if (error.canProceedToNext()) {
                if (error.severity != FTBackupError.ErrorHandlingType.NO_ERROR && error.severity != FTBackupError.ErrorHandlingType.IGNORE_CURRENT_AND_CONTINUE) {
                    if (backupItem != null) {
                        backupItem.setErrorHandlingType(error.severity.ordinal());
                        backupItem.setError(error.message);
                        if (!TextUtils.isEmpty(error.message) && error.message.equals(context.getString(R.string.character_limit_exceeded))) {
                            backupItem.setRelativePath("");
                        }
                        backupOperations.insertOrReplace(backupItem);
                    }
                    if (document != null) removeNotebookFromQueue(document);
                }
                checkAndPublishItem();
            } else {
                ObservingService.getInstance().postNotification("backup_error", null);
                stopPublishing();
            }
        } catch (Exception e) {
            String msg = "Unknown error in backup process:- ";
            if (!TextUtils.isEmpty(e.getMessage())) msg += e.getMessage();
            FTLog.crashlyticsLog("Exception while handling backup error:- " + msg);
            stopPublishing();
        }
    }

    private void addUrls() {
        final List<FTShelfItem> notebooks = FTShelfCollectionProvider.getInstance().allLocalShelfItems(context);
        final List<FTBackupItem> backedUpNotebooks = (List<FTBackupItem>) FTBackupOperations.getInstance().getList();
        for (FTShelfItem notebook : notebooks) {
            FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(FTUrl.parse(notebook.getFileURL().getPath()));
            if (backedUpNotebooks.isEmpty()) {
                addNotebookToQueue(document);
            } else if (backedUpNotebooks.stream().noneMatch(item -> item.getDocumentUUId().equals(document.getDocumentUUID()))) {
                addNotebookToQueue(document);
            } else if (backedUpNotebooks.stream().anyMatch(item -> (item.getDocumentUUId().equals(document.getDocumentUUID()) &&
                    (notebook.getFileModificationDate().getTime() > item.getUploadedTime() || !item.getRelativePath().equals(FTFileManagerUtil.getExtensionRemovedPath(FTApp.getRelativePath(document.getFileURL().getPath())) + FTConstants.NSA_EXTENSION))))) {
                addNotebookToQueue(document);
            }
        }
    }

    private void addNotebookToQueue(FTNoteshelfDocument document) {
        if (fileQueue.containsKey(document.getDocumentUUID())) {
            fileQueue.replace(document.getDocumentUUID(), document.getFileURL().getPath());
        } else {
            fileQueue.put(document.getDocumentUUID(), document.getFileURL().getPath());
        }
    }

    private void removeNotebookFromQueue(FTNoteshelfDocument document) {
        if (fileQueue.containsKey(document.getDocumentUUID()) && fileQueue.get(document.getDocumentUUID()).equals(document.getFileURL().getPath())) {
            fileQueue.remove(document.getDocumentUUID());
        }
    }

    private boolean isAllowBackup() {
        if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(context) && !FTApp.getPref().get(SystemPref.BACKUPTHROUGHWIFI, false))
            return true;
        else return FTNetworkConnectionUtil.INSTANCE.getNetworkType(context) == NetworkType.WiFi;
    }

    public interface TaskListener {
        void onFinished(FTBackupItem backupItem);

        void onFailure(FTBackupItem backupItem, FTBackupException exception);
    }

    public interface ServiceRequestCallback {
        void onSuccess();

        void onFailure(Exception exception);
    }

    public interface OnSignOutCallback {
        void onSignOutFinished();
    }
}
