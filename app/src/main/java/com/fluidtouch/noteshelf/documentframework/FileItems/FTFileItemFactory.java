package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;

import com.fluidtouch.noteshelf.audio.FTAudioPlistItem;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNSDocumentInfoPlistItem;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionCachePlist;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecogCachePlistItem;

import java.io.File;

public class FTFileItemFactory {
    private Context mContext;

    public FTFileItemFactory(Context context) {
        this.mContext = context;
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public FTFileItem fileItemWithURL(Context context, FTUrl url, Boolean canLoadSubdirectory) {
        FTFileItem fileItem = null;
        File bookFile = new File(url.getPath());
        Boolean isDir = bookFile.isDirectory();
        if (bookFile.exists()) {
            if (isDir) {
                fileItem = new FTFileItem(context, url, isDir);
                if (canLoadSubdirectory) {
                    File[] dirContents = bookFile.listFiles();
                    for (File childFile : dirContents) {
                        FTFileItem childFileItem = this.fileItemWithURL(context, FTUrl.parse(childFile.getPath()), canLoadSubdirectory);
                        if (childFileItem != null) {
                            childFileItem.parent = fileItem;
                            fileItem.children.add(childFileItem);
                        }
                    }
                }
            } else {
                String pathExtension = getFileExtension(bookFile);
                if (pathExtension.equals("pdf") || pathExtension.equals(FTConstants.PDF_FILE_EXT)) {
                    fileItem = this.pdfFileItemWithURL(url);
                } else if (pathExtension.equals("png")) {
                    fileItem = new FTFileItemImage(context, url, false);
                } else if (pathExtension.equals("plist")) {
                    fileItem = this.plistFileItemWithURL(context, url);
                } else if (pathExtension.equals("sqlite")) {
                    fileItem = new FTFileItemSqlite(context, url, false);
                }
//                else if([url.URLByDeletingLastPathComponent.lastPathComponent isEqualToString:@"Resources"]) //For now considering the items in resources folder as images
//                    {
//                        fileItem = [[FTFileItemImage alloc] initWithURL:url isDirectory:NO ];
//                    }
//                else {
//                        fileItem = [[FTFileItem alloc] initWithURL:url isDirectory:NO];
//                    }
            }
        }
        return fileItem;
    }

    FTFileItem sqliteFileItemWithURL(Context context, FTUrl url) {
        return new FTFileItemSqlite(context, url, false);

    }

    FTFileItem imageFileItemWithURL(FTUrl url) {
        return new FTFileItemImage(getContext(), url, false);

    }

    FTFileItem plistFileItemWithURL(Context context, FTUrl url) {
        if (FTDocumentUtils.getFileName(context, url).equals(FTConstants.DOCUMENT_INFO_FILE_NAME)) {
            FTFileItem fileItem = new FTNSDocumentInfoPlistItem(getContext(), url, false);
            return fileItem;
        }
        if (FTDocumentUtils.getFileName(context, url).contains(FTConstants.RECOGNITION_INFO_FILE_NAME)) {
            FTFileItem fileItem = new FTHandwritingRecognitionCachePlistItem(getContext(), url, false);
            return fileItem;
        } else if (FTDocumentUtils.getFileName(context, url).contains(FTConstants.VISION_RECOGNITION_INFO_FILE_NAME)) {
            FTFileItem fileItem = new FTScannedTextRecogCachePlistItem(getContext(), url, false);
            return fileItem;
        } else if (FTDocumentUtils.getFileName(context, url).contains(FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME)) {
            FTFileItem fileItem = new FTImageRecognitionCachePlist(getContext(), url, false);
            return fileItem;
        } else if (url.getPath().contains(FTConstants.RESOURCES_FOLDER_NAME)) {
            FTFileItem ftFileItem = new FTAudioPlistItem(getContext(), url, false);
            return ftFileItem;
        } else {
            return new FTFileItemPlist(getContext(), url, false);
        }
    }

    FTFileItem pdfFileItemWithURL(FTUrl url) {
        return new FTFileItemPDF(getContext(), url, false);
    }

    private Context getContext() {
        return mContext;
    }

}
