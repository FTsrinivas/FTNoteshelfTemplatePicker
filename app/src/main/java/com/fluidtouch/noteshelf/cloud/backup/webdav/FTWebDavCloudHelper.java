package com.fluidtouch.noteshelf.cloud.backup.webdav;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.backup.database.FTWebDavBackupItem;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.cloud.OnTaskCompletedListener;
import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.Executors;

public class FTWebDavCloudHelper {
    private final Context context;
    private String serverAddress;
    private final Observer pathChangeObserver = (o, arg) -> updatePath((FTWebDavCredentials) arg);

    public FTWebDavCloudHelper(Context context) {
        this.context = context;
        updatePath(FTApp.getPref().getWebDavCredentials());
        ObservingService.getInstance().addObserver("pathChangeObserver", pathChangeObserver);
    }

    public DavResource readFile(String remotePath) {
        try {
            if (TextUtils.isEmpty(remotePath)) throw new Exception("remotePath = null.");
            List<DavResource> davResources = FTWebDavClient.getInstance().sardine.getResources(serverAddress + remotePath);
            if (!davResources.isEmpty()) {
                for (DavResource davResource : davResources) {
                    if (davResource.getName().equals(FileUriUtils.getName(remotePath))) {
                        FTLog.debug(FTLog.WEBDAV_BACKUP, "Found remote file at " + remotePath);
                        return davResource;
                    }
                }
            }
        } catch (Exception e) {
            FTLog.error(FTLog.WEBDAV_BACKUP, "Failed to read remote file.\n" + e.getMessage());
        }
        return null;
    }

    public void uploadFile(FTWebDavBackupItem backupItem, String remotePath, File nsaFile, Callback taskListener) {
        try {
            upload(remotePath, nsaFile);
            FTLog.debug(FTLog.WEBDAV_BACKUP, "Uploaded file at " + (serverAddress + remotePath));
            taskListener.onSuccess(backupItem);
        } catch (Exception e) {
            FTLog.error(FTLog.WEBDAV_BACKUP, "Failed to upload file.\n" + e.getMessage());
            taskListener.onFailure(backupItem, e);
        }
    }

    public void moveFile(FTWebDavBackupItem backupItem, String remotePath, File nsaFile, Callback taskListener) {
        try {
            createFolderStructureForPath(remotePath);
            FTWebDavClient.getInstance().sardine.move(serverAddress + backupItem.getRelativePath(), serverAddress + remotePath);
            upload(remotePath, nsaFile);
            FTLog.debug(FTLog.WEBDAV_BACKUP, "Moved file at " + remotePath);
            taskListener.onSuccess(backupItem);
        } catch (Exception e) {
            FTLog.error(FTLog.WEBDAV_BACKUP, "Failed to moved file.\n" + e.getMessage());
            taskListener.onFailure(backupItem, e);
        }
    }

    public void updateFile(FTWebDavBackupItem backupItem, String remotePath, File nsaFile, Callback taskListener) {
        try {
            if (!backupItem.getRelativePath().equals(remotePath)) {
                if (readFile(backupItem.getRelativePath()) != null) {
                    FTWebDavClient.getInstance().sardine.delete(serverAddress + backupItem.getRelativePath());
                }
            }
            upload(remotePath, nsaFile);
            FTLog.debug(FTLog.WEBDAV_BACKUP, "Updated file at " + (serverAddress + remotePath));
            taskListener.onSuccess(backupItem);
        } catch (Exception e) {
            FTLog.error(FTLog.WEBDAV_BACKUP, "Failed to update file.\n" + e.getMessage());
            taskListener.onFailure(backupItem, e);
        }
    }

    public void getBackupFolders(String path, OnTaskCompletedListener listener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (!TextUtils.isEmpty(FTApp.getPref().getWebDavCredentials().getBackupFolder()))
                serverAddress = serverAddress.replace(FTApp.getPref().getWebDavCredentials().getBackupFolder(), "");
            FTLog.debug(FTLog.WEBDAV_BACKUP, "Got list of folders from " + path);
            List<String> folders = new ArrayList<>();
            try {
                List<DavResource> davResources = FTWebDavClient.getInstance().sardine.getResources(serverAddress + path);
                if (!davResources.isEmpty()) {
                    for (int i = 0; i < davResources.size(); i++) {
                        DavResource davResource = davResources.get(i);
                        if (davResource.isDirectory() && i != 0) folders.add(davResource.getName());
                    }
                }
            } catch (Exception e) {
                FTLog.error(FTLog.WEBDAV_BACKUP, "Failed to get list of folders\n" + e.getMessage());
            }
            new Handler(Looper.getMainLooper()).post(() -> listener.OnTaskCompleted(folders));
        });
    }

    public void updatePath(FTWebDavCredentials webDavCredentials) {
        if (webDavCredentials != null) {
            serverAddress = webDavCredentials.getServerAddress() + webDavCredentials.getBackupFolder();
            if (serverAddress.contains(context.getString(R.string.root)))
                serverAddress = serverAddress.replace(context.getString(R.string.root), "");
        }
    }

    private void upload(String remotePath, File nsaFile) throws Exception {
        FTLog.debug(FTLog.WEBDAV_BACKUP, "Checking folder structure...");
        createFolderStructureForPath(remotePath);
        FTLog.debug(FTLog.WEBDAV_BACKUP, "Uploading file...");
        byte[] bytes = new byte[(int) nsaFile.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(nsaFile));
        dis.readFully(bytes);
        dis.close();
        FTWebDavClient.getInstance().sardine.put(serverAddress + remotePath, bytes);
    }

    private void createFolderStructureForPath(String path) throws Exception {
        String[] folders = path.split("/");
        StringBuilder parentFolder = new StringBuilder();
        for (String folder : folders) {
            if (!folder.contains(FTConstants.NSA_EXTENSION) && readFile(parentFolder + folder) == null) {
                FTWebDavClient.getInstance().sardine.createDirectory(serverAddress + parentFolder + folder);
                FTLog.debug(FTLog.WEBDAV_BACKUP, "Created folder " + (serverAddress + parentFolder + folder));
            }
            parentFolder.append(folder).append("/");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ObservingService.getInstance().removeObserver("pathChangeObserver", pathChangeObserver);
    }

    public interface Callback {
        void onSuccess(FTBackupItem backupItem);

        void onFailure(FTBackupItem backupItem, Exception ex);
    }
}