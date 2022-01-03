package com.fluidtouch.noteshelf.documentframework.Utilities;

import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.io.File;

public class FTUniqueFileName {

    public static String uniqueFileNameFor(String documentName, FTUrl inFolderURL) {
        String fullFileName = documentName;//.replaceAll("[-+.^:,]","");
        String extension = fullFileName.substring(fullFileName.lastIndexOf("."));
        String fileName = fullFileName.substring(0, fullFileName.lastIndexOf("."));
        fileName = validateFileNameFor(fileName);

        String newDocName = "";

        int count = 0;
        Boolean nameExists = true;
        while (nameExists) {
            if (count == 0) {
                newDocName = fileName + extension;
            } else {
                newDocName = fileName + " " + count + extension;
            }
            nameExists = FTUniqueFileName.fileExistsWithName(newDocName, inFolderURL);
            if (!nameExists) {
                break;
            } else {
                count += 1;
            }
        }

        return newDocName;
    }

    private static Boolean fileExistsWithName(String fullFileName, FTUrl inFolderURL) {
        Boolean fileExists = false;
        File file = new File(inFolderURL.getPath() + "/" + fullFileName);
        fileExists = file.exists();
        return fileExists;
    }

    private static String validateFileNameFor(String fileName) {
        String docName = fileName.replaceAll("[-+?%#.^:,]", "");
        docName = docName.replace("/", "-");
        docName = docName.replace(".", "");
        if (docName.length() > 240) {
            docName = docName.substring(0, 240);
        }
        if (docName.length() == 0) {
            docName = "Untitled";
        }
        return docName;
    }
}
