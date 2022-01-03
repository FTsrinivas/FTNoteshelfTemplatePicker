package com.fluidtouch.noteshelf.documentframework;

import android.net.Uri;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class FTUrl implements Serializable {

    private String urlPath = "";

    public FTUrl(String path) {
        this.urlPath = path;
    }

    public static FTUrl parse(String path) {
        return new FTUrl(path);
    }

    public FTUrl withAppendedPath(String fileName) {
        Uri fileUri = Uri.parse(this.getPath());
        Uri appenedUri = Uri.withAppendedPath(fileUri, fileName);
        return new FTUrl(appenedUri.getPath());
    }

    public static FTUrl withAppendedPath(FTUrl baseUrl, String fileName) {
        Uri fileUri = Uri.parse(baseUrl.getPath());
        Uri appenedUri = Uri.withAppendedPath(fileUri, fileName);
        return new FTUrl(appenedUri.getPath());
    }

    public static FTUrl fromFile(File file) {
        return new FTUrl(Uri.fromFile(file).getPath());
    }

    public static FTUrl thumbnailFolderURL() {
        String libraryPath = FTConstants.DOCUMENTS_ROOT_PATH + "/Library";
        String thumbnailsPath = libraryPath + "/Thumbnails";
        FTUrl thumbnailFolder = new FTUrl(thumbnailsPath);
        return thumbnailFolder;
    }

    public static String noteshelfRootDirectory() {
        return FTDocumentUtils.noteshelfDocumentsDirectory().getPath();
    }

    public String getPath() {
        return this.urlPath;
    }

    public String getScheme() {
        Uri url = Uri.parse(this.getPath());
        return url.getScheme();
    }

    @Override
    public boolean equals(Object obj) {
        return getPath().equals(((FTUrl) obj).getPath());
    }

    public String relativePathWRTCollection() {
        List<String> components = (Uri.parse(this.getPath())).getPathSegments();
        StringBuilder relativePath = new StringBuilder();
        boolean pathBegan = false;

        for (String component : components) {
            if (pathBegan) {
                if (relativePath.length() > 0) {
                    relativePath.append("/").append(component);
                } else {
                    relativePath.append(component);
                }
            }
            if (component.equals(FTDocumentUtils.ROOT_DIR_NAME)) {
                pathBegan = true;
            }
        }
        return relativePath.toString();
    }

}

