package com.noteshelf.cloud.backup.dropbox;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTDropboxBackupCloudTable;
import com.fluidtouch.noteshelf.backup.database.FTDropboxBackupOperations;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollection;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionLocal;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.backup.FTRestoreHandlerCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTDropboxRestoreHandler {
    private final Context mContext;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DbxClientV2 dropboxService;
    private FTGroupItem groupItem = null;
    private FTShelfItemCollection shelfItemCollection;
    private FTRestoreHandlerCallback callback;
    private FTSmartDialog mSmartDialog;

    public FTDropboxRestoreHandler(@NonNull Context context) {
        mContext = context;
    }

    public void setListener(FTRestoreHandlerCallback callback) {
        this.callback = callback;
    }

    public void startRestoring() {
        mSmartDialog = new FTSmartDialog()
                .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                .setMessage(mContext.getString(R.string.restoring))
                .show(((AppCompatActivity) mContext).getSupportFragmentManager());

        FTDropboxServicePublisher dropboxServicePublisher = new FTDropboxServicePublisher(mContext);
        dropboxService = dropboxServicePublisher.cloudHelper.getClient();
        executorService.execute(this::restoreBooks);

        mSmartDialog.setCancellable(this::cancelRestoring);
    }

    public void cancelRestoring() {
        executorService.shutdownNow();
    }

    private void restoreCompleted(Error error) {
        ((Activity) mContext).runOnUiThread(() -> {
            if (mSmartDialog != null && mSmartDialog.isAdded())
                mSmartDialog.dismiss();
            callback.onRestoreCompleted(error);
        });
    }

    public void restoreBooks() {
        try {
            List<Metadata> resultFolders = new ArrayList<>();

            ListFolderResult result = dropboxService.files().listFolderBuilder("/noteshelf")
                    .withIncludeDeleted(false)
                    .start();
            for (Metadata folder : result.getEntries()) {
                if (folder instanceof FolderMetadata) {
                    resultFolders.add(folder);
                }
            }

            for (int i = 0; i < resultFolders.size(); i++) {
                groupItem = null;
                restoreFiles(resultFolders.get(i), "");
            }
            restoreCompleted(null);
        } catch (Exception e) {
            restoreCompleted(new Error(mContext.getString(R.string.error)));
        }
    }

    private void restoreFiles(Metadata parentFile, String parentName) {
        FTShelfCollection localShelfProvider = new FTShelfCollectionLocal();
        if (parentName.isEmpty())
            localShelfProvider.createShelfWithTitle(mContext, parentFile.getName(), (shelf, error) -> {
                try {
                    ListFolderResult result = dropboxService.files().listFolderBuilder(parentFile.getPathLower())
                            .withIncludeDeleted(false)
                            .start();
                    shelfItemCollection = shelf;
                    for (Metadata file : result.getEntries()) {
                        if (file instanceof FolderMetadata) {
                            groupItem = shelf.createGroupItem(file.getName());
                            restoreFiles(file, parentFile.getName() + "/");
                        } else {
                            groupItem = null;
                            if (file.getName().contains(FTConstants.NSA_EXTENSION)) {
                                downloadFile((FileMetadata) file);
                            }
                        }
                    }
                } catch (Exception e) {
                    FTLog.error(FTLog.DROPBOX_RESTORE, e.getMessage());
                }
            });
        else
            try {
                ListFolderResult result = dropboxService.files().listFolderBuilder(parentFile.getPathLower())
                        .withIncludeDeleted(false)
                        .start();
                for (Metadata file : result.getEntries()) {
                    if (file instanceof FileMetadata) {
                        downloadFile((FileMetadata) file);
                    }
                }
                groupItem = null;
            } catch (Exception e) {
                FTLog.error(FTLog.DROPBOX_RESTORE, e.getMessage());
            }
    }

    private void downloadFile(FileMetadata fileToDownload) {
        try {
            File file = new File(mContext.getCacheDir(), "Restoring");
            if (!file.exists()) {
                file.mkdirs();
            } else {
                FTFileManagerUtil.deleteFilesInsideFolder(file);
            }
            java.io.File fileNSA = new java.io.File(file.getAbsolutePath(), file.getName());
            FileOutputStream out = new FileOutputStream(fileNSA);
            dropboxService.files().downloadBuilder(fileToDownload.getPathLower()).download(out);
            out.close();

            java.io.File zipperDir = new java.io.File(ZipUtil.zipFolderPath());
            FTFileManagerUtil.deleteRecursive(zipperDir);
            ZipUtil.unzip(mContext, fileNSA.getAbsolutePath(), (unZipFile, error) -> {
                FTUrl unzippedUrl = null;
                if (error == null) {
                    unzippedUrl = FTUrl.parse(unZipFile.getPath());
                }

                shelfItemCollection.addShelfItemForDocument(mContext, FTDocumentUtils.getFileNameWithoutExtension(mContext, unzippedUrl), this.groupItem, (documentItem, error1) -> {
                    callback.onBookRestored(documentItem, error1);
                    if (documentItem.getDocumentUUID() != null) {
                        FTDropboxBackupCloudTable ftBackupItem = new FTDropboxBackupCloudTable();
                        ftBackupItem.setDocumentUUId(documentItem.getDocumentUUID());
                        ftBackupItem.setRelativePath(FTApp.getRelativePath(documentItem.getFileURL().getPath()));
                        ftBackupItem.setCloudId(fileToDownload.getId());
                        new FTDropboxBackupOperations().insertItem(ftBackupItem);
                    }
                }, unzippedUrl);
            });
        } catch (Exception e) {
            FTLog.error(FTLog.DROPBOX_RESTORE, e.getMessage());
        }
    }
}