package com.noteshelf.cloud.backup.drive;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupCloudTable;
import com.fluidtouch.noteshelf.backup.database.FTGoogleDriveBackupOperations;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollection;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionLocal;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.noteshelf.cloud.backup.FTRestoreHandlerCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FTGDriveRestoreHandler {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    FTGroupItem ftGroupItem = null;
    FTShelfItemCollection ftShelfItemCollection;
    private Context mContext;
    private Drive googleDriveService;
    private boolean isBooksAvailable = false;
    private FTRestoreHandlerCallback callback;
    private Future taskUnderExecution;
    private FTSmartDialog mSmartDialog;

    public FTGDriveRestoreHandler(Context context) {
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

        taskUnderExecution = executorService.submit(this::runInBackground);
        mSmartDialog.setCancellable(this::cancelRestoring);
    }

    public void cancelRestoring() {
        if (taskUnderExecution != null) {
            taskUnderExecution.cancel(true);
            executorService.shutdown();
        } else {
            executorService.shutdownNow();
        }
    }

    public void runInBackground() {
        createDriveService();
        String noteShelfFileId = checkBackUpData();
        if (noteShelfFileId != null) {
            restoreBooks(noteShelfFileId);
        } else {
            restoreCompleted(new Error(mContext.getString(R.string.error)));
        }
    }

    private void restoreCompleted(Error error) {
        ((Activity) mContext).runOnUiThread(() -> {
            if (mSmartDialog != null && mSmartDialog.isAdded())
                mSmartDialog.dismiss();
            callback.onRestoreCompleted(error);
        });
    }

    public void createDriveService() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(mContext);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(googleAccount.getAccount());
        googleDriveService = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("NoteShelf")
                .build();
    }

    public String checkBackUpData() {
        try {
            String pageToken = null;
            do {
                FileList result = googleDriveService.files().list()
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and name = 'Noteshelf' and trashed=false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name,mimeType)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    return file.getId();
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean restoreBooks(String folderId) {
        try {
            String pageToken = null;
            List<File> resultFolders = new ArrayList<File>();
            do {
                FileList result = googleDriveService.files().list()
                        .setQ("'" + folderId + "' in parents and trashed=false")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name,mimeType)")
                        .setPageToken(pageToken)
                        .execute();
                for (File file : result.getFiles()) {
                    resultFolders.add(file);
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            for (int i = 0; i < resultFolders.size(); i++) {
                if (taskUnderExecution.isCancelled()) break;
                ftGroupItem = null;
                restoreFiles(resultFolders.get(i), "");
            }
            restoreCompleted(null);
        } catch (IOException e) {
            restoreCompleted(new Error(e.getMessage()));
        }

        return isBooksAvailable;
    }

    public void restoreFiles(File parentFile, String parentName) {
        FTShelfCollection localShelfProvider = new FTShelfCollectionLocal();
        if (parentName.isEmpty())
            localShelfProvider.createShelfWithTitle(mContext, parentFile.getName(), new FTShelfCollection.FTItemCollectionAndErrorBlock() {
                @Override
                public void didFinishForShelfItemCollection(FTShelfItemCollection shelf, Error error) {
                    try {
                        ftShelfItemCollection = shelf;
                        String pageToken = null;
                        do {
                            FileList result = googleDriveService.files().list()
                                    .setQ("'" + parentFile.getId() + "' in parents and trashed=false")
                                    .setSpaces("drive")
                                    .setFields("nextPageToken, files(id, name,mimeType)")
                                    .setPageToken(pageToken)
                                    .execute();
                            for (File file : result.getFiles()) {
                                if (taskUnderExecution.isCancelled()) break;
                                if (file.getMimeType().equals("application/vnd.google-apps.folder")) {
                                    ftGroupItem = shelf.createGroupItem(file.getName());
                                    restoreFiles(file, parentFile.getName() + "/");
                                } else {
                                    ftGroupItem = null;
                                    downloadFile(
                                            file.getId(), parentFile.getId(), ftGroupItem, shelf);
                                }
                            }
                            pageToken = result.getNextPageToken();
                        } while (pageToken != null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        else {
            try {
                String pageToken = null;
                do {
                    FileList result = googleDriveService.files().list()
                            .setQ("'" + parentFile.getId() + "' in parents")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name,mimeType)")
                            .setPageToken(pageToken)
                            .execute();
                    for (File file : result.getFiles()) {
                        if (taskUnderExecution.isCancelled()) break;
                        downloadFile(
                                file.getId(), parentFile.getId(), ftGroupItem, ftShelfItemCollection);
                    }
                    pageToken = result.getNextPageToken();
                } while (pageToken != null);
                ftGroupItem = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void downloadFile(String fileId, String parentFileId, FTGroupItem
            groupItem, FTShelfItemCollection shelfItemCollection) {
        try {
            File metadata = googleDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            java.io.File file = new java.io.File(mContext.getCacheDir(), "Restoring");
            if (!file.exists()) {
                file.mkdirs();
            } else {
                FTFileManagerUtil.deleteFilesInsideFolder(file);
            }
            java.io.File fileNSA = new java.io.File(file.getAbsolutePath(), name);
            FileOutputStream out = new FileOutputStream(fileNSA);
            googleDriveService.files().get(fileId).executeMediaAndDownloadTo(out);
            if (fileNSA.getAbsolutePath().toLowerCase().contains("nsa")) {
                isBooksAvailable = true;
                java.io.File zipperDir = new java.io.File(ZipUtil.zipFolderPath());
                FTFileManagerUtil.deleteRecursive(zipperDir);
                ZipUtil.unzip(mContext, fileNSA.getAbsolutePath(), (unZipFile, error) -> {
                    FTUrl unzippedUrl = null;
                    if (error == null) {
                        unzippedUrl = FTUrl.parse(unZipFile.getPath());
                    }

                    shelfItemCollection.addShelfItemForDocument(mContext, FTDocumentUtils.getFileNameWithoutExtension(mContext, unzippedUrl), groupItem, (documentItem, error1) -> {
                        callback.onBookRestored(documentItem, error1);
                        if (documentItem.getDocumentUUID() != null) {
                            FTGoogleDriveBackupCloudTable ftBackupItem = new FTGoogleDriveBackupCloudTable();
                            ftBackupItem.setDocumentUUId(documentItem.getDocumentUUID());
                            ftBackupItem.setRelativePath(FTApp.getRelativePath(documentItem.getFileURL().getPath()));
                            ftBackupItem.setCloudId(fileId);
                            ftBackupItem.setCloudParentId(parentFileId);
                            new FTGoogleDriveBackupOperations().insertItem(ftBackupItem);
                        }
                    }, unzippedUrl);
                });
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }
}
