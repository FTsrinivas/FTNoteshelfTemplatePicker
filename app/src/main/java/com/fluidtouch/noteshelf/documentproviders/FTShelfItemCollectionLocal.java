package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTUniqueFileName;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FTShelfItemCollectionLocal extends FTShelfItemCollection {

    public FTShelfItemCollectionLocal(FTUrl fileURL) {
        super(fileURL);
    }

    @Override
    public synchronized void shelfItems(Context context, FTShelfSortOrder sortOrder, FTGroupItem parent, String searchKey, ShelfNotebookItemsAndErrorBlock onCompletion) {
        if (getChildren().isEmpty()) {
            String filePath = this.getFileURL().getPath();
            File shelf = new File(filePath);
            File[] files = shelf.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    FTShelfItem notebook = null;
                    String fileName = file.getName();
                    if (fileName.contains(FTConstants.GROUP_EXTENSION)) {
                        notebook = new FTGroupItem(FTUrl.parse(file.getPath()));
                        notebook.setType(RKShelfItemType.GROUP);
                        ((FTGroupItem) notebook).setChildren(this.getChildrenInGroup((FTGroupItem) notebook));
                    } else if (fileName.contains(FTConstants.NS_EXTENSION)) {
                        notebook = new FTShelfItem(FTUrl.parse(file.getPath()));
                        notebook.setParent(parent);
                    } else {
                        continue;
                    }
                    notebook.setShelfCollection(this);
                    addChild(notebook);
                }
            }
        }

        if (parent != null) {
            parent.setType(RKShelfItemType.GROUP);
            parent.setShelfCollection(this);
            parent.setChildren(getChildrenInGroup(parent));
        }

        if (searchKey != null && !searchKey.equals("") && searchKey.length() > 0) {
            this.searchWithKey(context, searchKey, parent, onCompletion);
            return;
        }

        onCompletion.didFinishWithNotebookItems((parent != null) ? parent.getChildren() : getChildren(), null);
    }

    private void searchWithKey(Context context, String searchKey, FTGroupItem parent, ShelfNotebookItemsAndErrorBlock onCompletion) {
        final ArrayList<FTShelfItem> notebooks = new ArrayList<>();
        if (parent != null) {
            for (FTShelfItem shelfItem : parent.getChildren()) {
                if (shelfItem.getDisplayTitle(context).toLowerCase().contains(searchKey.toLowerCase())) {
                    notebooks.add(shelfItem);
                }
            }
        } else {
            for (FTShelfItem shelfItem : getChildren()) {
                if (shelfItem.getType() == RKShelfItemType.GROUP) {
                    for (FTShelfItem groupNotebook : ((FTGroupItem) shelfItem).getChildren()) {
                        if (groupNotebook.getDisplayTitle(context).toLowerCase().contains(searchKey.toLowerCase())) {
                            notebooks.add(shelfItem);
                        }
                    }
                } else if (shelfItem.getDisplayTitle(context).toLowerCase().contains(searchKey.toLowerCase())) {
                    notebooks.add(shelfItem);
                }
            }
        }

        onCompletion.didFinishWithNotebookItems(notebooks, null);
    }

    private List<FTShelfItem> getChildrenInGroup(FTGroupItem group) {
        ArrayList<FTShelfItem> children = new ArrayList<>();

        String groupPath = group.getFileURL().getPath();
        File shelf = new File(groupPath);
        File[] files = shelf.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                FTShelfItem notebook = new FTShelfItem(FTUrl.parse(files[i].getPath()));
                notebook.setShelfCollection(this);
                notebook.setParent(group);
                children.add(notebook);
            }
        }
        return children;
    }

    @Override
    public void createGroupItem(Context context, List<FTShelfItem> shelfItemsToGroup, FTGroupCreationBlock onCompletion, String groupName) {

        String groupFileName = groupName + FTConstants.GROUP_EXTENSION;
        String uniqueGroupName = FTUniqueFileName.uniqueFileNameFor(groupFileName, this.getFileURL());
        String path = this.getFileURL().getPath() + "/" + uniqueGroupName;

        File directory = new File(path);
        FTGroupItem groupFolder = null;
        Error error = null;

        if (directory.mkdirs()) {
            groupFolder = new FTGroupItem(FTUrl.parse(path));
            groupFolder.setType(RKShelfItemType.GROUP);

            for (int i = 0; i < shelfItemsToGroup.size(); i++) {
                this.moveDocumentItem(context, shelfItemsToGroup.get(i), groupFolder, this, null);
            }
            groupFolder.setChildren(shelfItemsToGroup);
            groupFolder.setShelfCollection(this);
            this.addChild(groupFolder);
        } else {
            error = new Error();
        }
        onCompletion.didCreateGroup(groupFolder, error);
    }

    @Override
    public FTGroupItem createGroupItem(String groupName) {

        String groupFileName = groupName + FTConstants.GROUP_EXTENSION;
        String uniqueGroupName = FTUniqueFileName.uniqueFileNameFor(groupFileName, this.getFileURL());
        String path = this.getFileURL().getPath() + "/" + uniqueGroupName;

        File directory = new File(path);
        FTGroupItem groupFolder = null;
        Error error = null;

        if (directory.mkdirs()) {
            groupFolder = new FTGroupItem(FTUrl.parse(path));
            groupFolder.setType(RKShelfItemType.GROUP);
            groupFolder.setShelfCollection(this);
            this.addChild(groupFolder);
        } else {
            error = new Error();
        }
        return groupFolder;
    }

    private void moveDocumentItem(Context context, FTShelfItem notebook, FTGroupItem toGroup, FTShelfItemCollection collection, @Nullable ShelfNotebookAndErrorBlock onCompletion) {
        String fileName = FTDocumentUtils.getFileName(context, notebook.getFileURL());
        String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(fileName, toGroup.getFileURL());
        FTUrl destURL = this.documentURLWithFileName(context, uniqueFileName, toGroup, this);
        File sourceFile = new File(notebook.getFileURL().getPath());
        File destFile = new File(destURL.getPath());
        if (sourceFile.renameTo(destFile)) {
            destFile.setLastModified(System.currentTimeMillis());

            FTGroupItem groupItem = notebook.getParent();
            if (groupItem != null) {
                groupItem.removeChild(notebook);
                groupItem.deleteGroupIfEmpty();
            } else if (notebook.getShelfCollection() != null) {
                notebook.getShelfCollection().removeChild(notebook);
            }

            removeChild(notebook);
            notebook.setFileURL(FTUrl.parse(destFile.getPath()));
            notebook.setShelfCollection(this);
            notebook.setParent(toGroup);
            toGroup.addChild(notebook);

            FTShelfCollectionProvider.getInstance().pinnedShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());
            FTShelfCollectionProvider.getInstance().recentShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());

            // Evernote
            //----------------------------------
            FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(notebook.getFileURL());
            if (document != null) {
                document.openDocumentWhileInBackground();
                if (FTENSyncRecordUtil.isSyncEnabledForNotebook(document.getDocumentUUID())) {
                    FTENSyncRecordUtil.enableEvernoteSyncForNotebook(context, document);
                }
            }
            //----------------------------------
            if (onCompletion != null) {
                onCompletion.didFinishWithNotebookItem(toGroup, null);
            }
        }
    }

    @Override
    public void addShelfItemForDocument(Context context, String toTitle, FTGroupItem toGroup, FTDocumentItemAndErrorBlock onCompletion, FTUrl fileURL) {
        String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(toTitle + FTConstants.NS_EXTENSION, toGroup == null ? this.getFileURL() : toGroup.getFileURL());
        FTUrl destURL = this.documentURLWithFileName(context, uniqueFileName, toGroup, this);
        File sourceFile = new File(fileURL.getPath());
        File destFile = new File(destURL.getPath());

        FTFileManagerUtil.copyRecursively(sourceFile, destFile);

        Error error = null;
        FTDocumentItem newItem = null;
        if (!destFile.exists()) error = new Error();
        else {
            newItem = new FTDocumentItem(destURL);
            newItem.setParent(toGroup);
            newItem.setShelfCollection(this);

            if (toGroup != null) {
                toGroup.setShelfCollection(this);
                toGroup.addChild(newItem);
            } else {
                this.addChild(newItem);
            }
        }

        if (onCompletion != null) {
            onCompletion.didFinishAddingItem(newItem, error);
        }
    }

    @Override
    public synchronized void renameShelfItem(Context context, String toTitle, ShelfNotebookAndErrorBlock onCompletion, FTShelfItem shelfItem) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Error error = null;
            if (shelfItem.getDisplayTitle(context).equals(toTitle)) {
                onCompletion.didFinishWithNotebookItem(shelfItem, error);
                return;
            }

            String shelfItemName = toTitle + (shelfItem.getType() == RKShelfItemType.GROUP ? FTConstants.GROUP_EXTENSION : FTConstants.NS_EXTENSION);
            String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(shelfItemName, shelfItem.parent != null ? shelfItem.parent.getFileURL() : this.getFileURL());
            FTUrl destURL = this.documentURLWithFileName(context, uniqueFileName, shelfItem.parent, this);
            File sourceFile = new File(shelfItem.getFileURL().getPath());
            File destFile = new File(destURL.getPath());

            if (sourceFile.renameTo(destFile)) {
                shelfItem.setFileURL(destURL);
                if (shelfItem instanceof FTGroupItem) {
                    for (FTShelfItem item : ((FTGroupItem) shelfItem).getChildren()) {
                        item.setFileURL(FTUrl.parse(shelfItem.getFileURL().getPath() + "/" + FTDocumentUtils.getFileName(context, item.getFileURL())));
                    }
                }
            } else {
                error = new Error();
            }

            FTShelfCollectionProvider.getInstance().pinnedShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());
            FTShelfCollectionProvider.getInstance().recentShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());

            // Evernote
            //----------------------------------
            FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(shelfItem.getFileURL());
            if (document != null && shelfItem.getType() != RKShelfItemType.GROUP) {
                document.openDocumentWhileInBackground();
                if (FTENSyncRecordUtil.isSyncEnabledForNotebook(document.getDocumentUUID())) {
                    FTENSyncRecordUtil.enableEvernoteSyncForNotebook(context, document);
                }
            }
            //----------------------------------

            if (onCompletion != null) {
                Error finalError = error;
                new Handler(Looper.getMainLooper()).post(() -> onCompletion.didFinishWithNotebookItem(shelfItem, finalError));
            }
        });
    }

    @Override
    public void removeShelfItems(Context context, ShelfNotebookItemsAndErrorBlock onCompletion, List<FTShelfItem> shelfItems) {
        Executors.newSingleThreadExecutor().execute(() -> {
            final List<FTShelfItem> notebooks = new ArrayList<>(shelfItems);
            for (FTShelfItem notebook : notebooks) {
                //If current Category is Trash removing notebooks permanently
                if (isTrash(context)) {
                    removeChild(notebook);
                    FTFileManagerUtil.deleteRecursive(notebook.getFileURL().getPath());
                }
                //Moving notebooks from current Category to Trash
                else {
                    if (notebook.getType().equals(RKShelfItemType.GROUP)) {
                        FTGroupItem groupItem = (FTGroupItem) notebook;
                        List<FTShelfItem> groupChildren = new ArrayList<>(groupItem.getChildren());
                        for (FTShelfItem shelfItem : groupChildren) {
                            moveNotebookToTrash(context, shelfItem);
                        }
                        removeChild(groupItem);
                        File groupFolder = new File(groupItem.getFileURL().getPath());
                        FTFileManagerUtil.deleteRecursive(groupFolder);
                    } else {
                        moveNotebookToTrash(context, notebook);
                    }
                }
            }
            ((AppCompatActivity) context).runOnUiThread(() -> onCompletion.didFinishWithNotebookItems(shelfItems, null));
        });
    }

    private void moveNotebookToTrash(Context context, FTShelfItem notebook) {
        FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(notebook.getFileURL());
        document.deleteDocument(context);
        File sourceFile = new File(notebook.getFileURL().getPath());
        String trashFolderPath = FTShelfCollectionSystem.systemFolderURL().getPath() + "/" + FTShelfCollectionSystem.TRASH_FOLDER_NAME + FTConstants.SHELF_EXTENSION;
        String notebookName = FTDocumentUtils.getFileName(context, notebook.getFileURL());
        String uniqueNotebookName = FTUniqueFileName.uniqueFileNameFor(notebookName, FTUrl.parse(trashFolderPath));
        File destinationFile = new File(trashFolderPath + "/" + uniqueNotebookName);

        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.removePinned(sourceFile.getPath());
        FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(sourceFile.getPath());

        //Remove from backup info.
        /*new FTGoogleDriveBackupOperations().delete(document.getDocumentUUID());
        new FTDropboxBackupOperations().delete(document.getDocumentUUID());
        new FTOneDriveBackupOperations().delete(document.getDocumentUUID());
        new FTWebDavBackupOperations().delete(document.getDocumentUUID());*/

        if (sourceFile.renameTo(destinationFile)) {
            FTGroupItem groupItem = notebook.getParent();
            if (groupItem != null) {
                groupItem.removeChild(notebook);
                groupItem.deleteGroupIfEmpty();
            }
            if (notebook.getShelfCollection() != null)
                notebook.getShelfCollection().removeChild(notebook);
            notebook.setParent(null);
            notebook.setShelfCollection(null);
            notebook.setFileURL(FTUrl.parse(destinationFile.getPath()));
        }
    }

    @Override
    public void moveShelfItem(FTShelfItem notebook, FTGroupItem toGroup, FTMoveShelfItemBlock onCompletion, Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String fileName = FTDocumentUtils.getFileName(context, notebook.getFileURL());
            String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(fileName, toGroup != null ? toGroup.getFileURL() : this.getFileURL());
            FTUrl destURL = this.documentURLWithFileName(context, uniqueFileName, toGroup, this);
            File sourceFile = new File(notebook.getFileURL().getPath());
            File destFile = new File(destURL.getPath());

            Error error = null;
            if (sourceFile.renameTo(destFile)) {
                FTGroupItem groupItem = notebook.getParent();
                if (groupItem != null) {
                    groupItem.removeChild(notebook);
                    groupItem.deleteGroupIfEmpty();
                } else if (notebook.getShelfCollection() != null) {
                    notebook.getShelfCollection().removeChild(notebook);
                }

                notebook.setFileURL(destURL);
                notebook.setShelfCollection(this);
                if (toGroup != null) {
                    toGroup.addChild(notebook);
                    notebook.setParent(toGroup);
                } else {
                    this.addChild(notebook);
                }
                destFile.setLastModified(System.currentTimeMillis());
            } else {
                error = new Error();
            }

            FTShelfCollectionProvider.getInstance().pinnedShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());
            FTShelfCollectionProvider.getInstance().recentShelfProvider.updatePath(sourceFile.getPath(), destFile.getPath());

            FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(notebook.getFileURL());
            FTENNotebook enNotebook = FTENSyncRecordUtil.getEnNotebook(document.getDocumentUUID());
            if (document != null && enNotebook != null && enNotebook.getSyncEnabled()) {
                FTENSyncRecordUtil.enableEvernoteSyncForNotebook(context, document);
            }

            Error finalError = error;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (onCompletion != null) {
                    onCompletion.didMoveToGroup(toGroup, finalError);
                }
            });
        });
    }

    private FTUrl documentURLWithFileName(Context context, String fileName, FTGroupItem toGroup, FTShelfItemCollection collection) {
        FTUrl docURL = collection.getFileURL();
        if (toGroup != null) {
            docURL = FTUrl.withAppendedPath(docURL, toGroup.getDisplayTitle(context) + FTConstants.GROUP_EXTENSION);
        }
        docURL = FTUrl.withAppendedPath(docURL, fileName);
        return docURL;
    }
}
