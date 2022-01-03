package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTUniqueFileName;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfItemCollectionType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTShelfCollectionSystem extends FTShelfCollection {
    public static final String TRASH_FOLDER_NAME = "Trash";

    public static FTUrl systemFolderURL() {
        FTUrl noteshelfURL = FTDocumentUtils.noteshelfDocumentsDirectory();
        FTUrl systemURL = FTUrl.withAppendedPath(noteshelfURL, "System");

        File trashDir = new File(systemURL.getPath());
        if (!trashDir.exists()) {
            if (trashDir.mkdirs()) {
                Log.i("Info", "System dir created");
            }
        }
        return systemURL;
    }

    @Override
    public FTShelfItemCollection collectionWithTitle(Context context, String title) {
        String path = systemFolderURL().getPath();
        final File directory = new File(path);
        File[] files = directory.listFiles();
        if (files.length == 0) {
            return null;
        } else {
            for (File file : files) {
                FTShelfItemCollection collection = newCollectionWithURL(FTUrl.parse(file.getPath()));
                if (collection.getTitle(context).equals(title)) {
                    return collection;
                }
            }
        }
        return null;
    }

    @Override
    public void shelfs(FTShelfItemCollectionBlock onCompletion) {
        String path = systemFolderURL().getPath();
        final File directory = new File(path);
        final ArrayList<FTShelfItemCollection> shelfs = new ArrayList<>();

        List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
        if (files.isEmpty()) {
            createTrashShelfCategory(new FTItemCollectionAndErrorBlock() {
                @Override
                public void didFinishForShelfItemCollection(FTShelfItemCollection shelf, Error error) {
                    if (error == null) {
                        shelfs.add(newCollectionWithURL(shelf.getFileURL()));
                        onCompletion.didFetchCollectionItems(shelfs);
                    }
                }
            });
        } else {
            for (File file : files) {
                shelfs.add(newCollectionWithURL(FTUrl.parse(file.getPath())));
            }
            onCompletion.didFetchCollectionItems(shelfs);
        }
    }

    @Override
    public void createShelfWithTitle(Context context, String title, FTItemCollectionAndErrorBlock block) {
        String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(title + FTConstants.SHELF_EXTENSION, systemFolderURL());

        String path = systemFolderURL().getPath() + "/" + uniqueFileName;
        File directory = new File(path);
        if (directory.mkdirs()) {
            FTShelfItemCollection shelfFolder = newCollectionWithURL(FTUrl.parse(path));
            block.didFinishForShelfItemCollection(shelfFolder, null);
        }
    }

    @Override
    public void deleteShelf(Context context, FTShelfItemCollection shelf, FTItemCollectionAndErrorBlock block) {
        //This method is not currently implemented
    }

    @Override
    public void renameShelf(Context context, String title, FTItemCollectionAndErrorBlock block, FTShelfItemCollection shelf) {
        //This method is not currently implemented
    }

    private FTShelfItemCollection newCollectionWithURL(FTUrl fileURL) {
        FTShelfItemCollectionLocal shelfItemCollectionLocal = new FTShelfItemCollectionLocal(fileURL);
        shelfItemCollectionLocal.setCollectionType(FTShelfItemCollectionType.SYSTEM);
        return new FTShelfItemCollectionLocal(fileURL);
    }

    private void createTrashShelfCategory(FTItemCollectionAndErrorBlock block) {
        String path = systemFolderURL().getPath() + "/" + TRASH_FOLDER_NAME + FTConstants.SHELF_EXTENSION;
        File directory = new File(path);
        if (directory.mkdirs()) {
            FTShelfItemCollection trashFolder = newCollectionWithURL(FTUrl.parse(path));
            block.didFinishForShelfItemCollection(trashFolder, null);
        } else {
            block.didFinishForShelfItemCollection(null, new Error("Failed to create trash category"));
        }
    }
}