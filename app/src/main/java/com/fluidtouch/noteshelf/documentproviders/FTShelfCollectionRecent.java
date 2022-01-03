package com.fluidtouch.noteshelf.documentproviders;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sreenu on 04/08/20.
 */
public class FTShelfCollectionRecent {
    private NSDictionary rootDirectory = new NSDictionary();
    private List<String> paths = new ArrayList<>();

    public void shelfs(FTShelfItemCollectionBlock onCompletion) {
        final ArrayList<FTShelfItemCollection> shelfs = new ArrayList<>();
        paths = getRecentFilesPaths(recentsPlistPath().getPath());
        for (int i = 0; i < paths.size(); i++) {
            shelfs.add(new FTShelfItemCollectionLocal(FTUrl.parse(paths.get(i))));
        }

        onCompletion.didFetchCollectionItems(shelfs);
    }

    private List<String> getRecentFilesPaths(String plistPath) {
        FileInputStream inputStream = null;
        List<String> paths = new ArrayList<>();
        try {
            inputStream = new FileInputStream(plistPath);
            rootDirectory = (NSDictionary) PropertyListParser.parse(inputStream);

            if (rootDirectory != null && rootDirectory.containsKey("paths")) {
                NSArray pathsArray = (NSArray) rootDirectory.objectForKey("paths");
                for (int i = 0; i < pathsArray.count(); i++) {
                    String filePath = pathsArray.objectAtIndex(i).toString();
                    if (new File(filePath).exists()) {
                        paths.add(filePath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    private FTUrl recentsPlistPath() {
        FTUrl noteshelfURL = FTDocumentUtils.noteshelfDocumentsDirectory();
        FTUrl recentPlistURL = FTUrl.withAppendedPath(noteshelfURL, FTConstants.RECENT_PLIST);

        File recentFile = new File(recentPlistURL.getPath());
        if (!recentFile.exists()) {
            saveContent(recentFile.getPath());
        }
        return recentPlistURL;
    }

    private void saveContent(String path) {
        NSArray array = new NSArray(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            array.setValue(i, paths.get(i));
        }
        if (rootDirectory != null) {
            rootDirectory.put("paths", paths);
        }

        try {
            File file = new File(path);
            FileOutputStream outputStream = null;
            outputStream = new FileOutputStream(file);
            outputStream.write(rootDirectory.toXMLPropertyList().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRecent(String path) {
        if (!checkIfPresent(path)) {
            paths.add(0, path);
            if (paths.size() > 10) {
                paths.remove(10);
            }

            saveContent(recentsPlistPath().getPath());
        } else {
            saveContent(recentsPlistPath().getPath());
        }
    }

    private boolean checkIfPresent(String path) {
        for (int i = 0; i < paths.size(); i++) {
            if (path.equals(paths.get(i))) {
                paths.remove(i);
                paths.add(0, path);
                return true;
            }
        }
        return false;
    }

    public void removeRecent(String path) {
        for (int i = 0; i < paths.size(); i++) {
            if (path.equals(paths.get(i))) {
                paths.remove(i);
                break;
            }
        }

        saveContent(recentsPlistPath().getPath());
    }

    public void updatePath(String oldPath, String updatedPath) {
        for (int i = 0; i < paths.size(); i++) {
            if (oldPath.equals(paths.get(i))) {
                paths.set(i, updatedPath);
                break;
            }
        }
        saveContent(recentsPlistPath().getPath());
    }
}
