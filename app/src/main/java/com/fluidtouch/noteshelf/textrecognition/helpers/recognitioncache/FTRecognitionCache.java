package com.fluidtouch.noteshelf.textrecognition.helpers.recognitioncache;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItem;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionCachePlist;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecogCachePlistItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FTRecognitionCache {
    private Context context;
    private FTNoteshelfDocument currentDocument;
    private FTHandwritingRecognitionCachePlistItem cachePlistItem;
    private FTScannedTextRecogCachePlistItem visionCachePlistItem;
    private FTImageRecognitionCachePlist imageCachePlistItem;
    private FTFileItem recognitionCacheRoot;
    private FTFileItem documentIDFolder;
    private String languageCode;
    private long lastSavedTime = 0;

    public FTRecognitionCache(@NotNull Context context, FTNoteshelfDocument document, String language) {
        this.context = context;
        this.currentDocument = document;
        this.languageCode = language;
        recognitionCacheRoot = new FTFileItem(context, FTRecognitionCache.recognitionCacheDirectory(), true);
    }

    private static FTUrl recognitionCacheDirectory() {
        File cacheDirectory = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/RecognitionCache");
        if (!cacheDirectory.exists())
            cacheDirectory.mkdirs();
        return FTUrl.fromFile(cacheDirectory);
    }

    public FTHandwritingRecognitionCachePlistItem recognitionCachePlist() {
        if (this.languageCode == null || this.languageCode.equals(FTLanguageResourceManager.languageCodeNone)) {
            return null;
        }
        if (this.documentIDFolder == null) {
            this.documentIDFolder = this.recognitionCacheRoot.childFileItemWithName(currentDocument.getDocumentUUID());
            if (this.documentIDFolder == null) {
                this.documentIDFolder = new FTFileItem(currentDocument.getDocumentUUID(), true);
                this.recognitionCacheRoot.addChildItem(this.documentIDFolder);
            }
        }
        String plistFileName = FTConstants.RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
        if (cachePlistItem != null && cachePlistItem.fileName.equals(plistFileName)) {
            return this.cachePlistItem;
        }

        FTHandwritingRecognitionCachePlistItem recognitionCachePlist = (FTHandwritingRecognitionCachePlistItem) this.documentIDFolder.childFileItemWithName(plistFileName);
        if (recognitionCachePlist == null) {
            recognitionCachePlist = new FTHandwritingRecognitionCachePlistItem(plistFileName, false);
            this.documentIDFolder.addChildItem(recognitionCachePlist);
        }

        FTUrl cachePlistURL = FTUrl.withAppendedPath(this.documentIDFolder.getFileItemURL(), plistFileName);

        if (FTFileManagerUtil.isFileExits(cachePlistURL.getPath())) {
            copyPackageRecognitionInfoToCache();
        } else {
            File cachedFile = new File(cachePlistURL.getPath());
            long cachedFileModifiedDate = cachedFile.lastModified();
            if (this.currentDocument.rootFileItem != null) {
                FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
                if (folderItem != null) {
                    plistFileName = FTConstants.RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                    FTUrl packagePlistURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);
                    if (FTFileManagerUtil.isFileExits(packagePlistURL.getPath())) {
                        File packageFile = new File(packagePlistURL.getPath());
                        long packageFileModifiedDate = packageFile.lastModified();
                        if (packageFileModifiedDate > cachedFileModifiedDate) {
                            copyPackageRecognitionInfoToCache();
                        }
                    }
                }
            }
        }
        this.cachePlistItem = recognitionCachePlist;
        return this.cachePlistItem;
    }

    private void copyPackageRecognitionInfoToCache() {
        if (this.currentDocument.rootFileItem == null) {
            return;
        }

        FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem != null) {
            String plistFileName = FTConstants.RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
            FTUrl recognitionInfoURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);

            if (!FTFileManagerUtil.isFileExits(recognitionInfoURL.getPath())) {
                return;
            }
            if (this.documentIDFolder != null) {
                this.recognitionCacheRoot.writeUpdatesToURL(context, this.recognitionCacheRoot.getFileItemURL());
                String cachePlistFileName = FTConstants.RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                FTUrl recognitionCacheURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), cachePlistFileName);
                if (FTFileManagerUtil.isFileExits(recognitionCacheURL.getPath())) {
                    new File(recognitionCacheURL.getPath()).renameTo(new File(recognitionCacheURL.getPath()));
                }
            }
        }
    }

    public FTScannedTextRecogCachePlistItem visionRecognitionCachePlist() {
        if (this.languageCode.isEmpty() || this.languageCode.equals(FTLanguageResourceManager.languageCodeNone)) {
            return null;
        }
        if (this.documentIDFolder == null) {
            this.documentIDFolder = this.recognitionCacheRoot.childFileItemWithName(currentDocument.getDocumentUUID());
            if (this.documentIDFolder == null) {
                this.documentIDFolder = new FTFileItem(currentDocument.getDocumentUUID(), true);
                this.recognitionCacheRoot.addChildItem(this.documentIDFolder);
            }
        }
        String plistFileName = FTConstants.VISION_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
        if (visionCachePlistItem != null && visionCachePlistItem.fileName.equals(plistFileName)) {
            return this.visionCachePlistItem;
        }

        FTScannedTextRecogCachePlistItem recognitionCachePlist = (FTScannedTextRecogCachePlistItem) this.documentIDFolder.childFileItemWithName(plistFileName);
        if (recognitionCachePlist == null) {
            recognitionCachePlist = new FTScannedTextRecogCachePlistItem(plistFileName, false);
            this.documentIDFolder.addChildItem(recognitionCachePlist);
        }

        FTUrl cachePlistURL = FTUrl.withAppendedPath(this.documentIDFolder.getFileItemURL(), plistFileName);

        if (!FTFileManagerUtil.isFileExits(cachePlistURL.getPath())) {
            copyPackageVisionRecognitionInfoToCache();
        } else {
            File cachedFile = new File(cachePlistURL.getPath());
            long cachedFileModifiedDate = cachedFile.lastModified();
            FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
            if (folderItem != null) {
                plistFileName = FTConstants.VISION_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                FTUrl packagePlistURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);
                if (FTFileManagerUtil.isFileExits(packagePlistURL.getPath())) {
                    File packageFile = new File(packagePlistURL.getPath());
                    long packageFileModifiedDate = packageFile.lastModified();
                    if (packageFileModifiedDate > cachedFileModifiedDate) {
                        copyPackageVisionRecognitionInfoToCache();
                    }
                }
            }
        }
        this.visionCachePlistItem = recognitionCachePlist;
        return this.visionCachePlistItem;
    }

    private void copyPackageVisionRecognitionInfoToCache() {
        if (this.currentDocument.rootFileItem == null) {
            return;
        }

        FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem != null) {
            String plistFileName = FTConstants.VISION_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
            FTUrl recognitionInfoURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);

            if (!FTFileManagerUtil.isFileExits(recognitionInfoURL.getPath())) {
                return;
            }
            if (this.documentIDFolder != null) {
                this.recognitionCacheRoot.writeUpdatesToURL(context, this.recognitionCacheRoot.getFileItemURL());
                String cachePlistFileName = FTConstants.VISION_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                FTUrl recognitionCacheURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), cachePlistFileName);
                if (FTFileManagerUtil.isFileExits(recognitionCacheURL.getPath())) {
                    new File(recognitionCacheURL.getPath()).renameTo(new File(recognitionCacheURL.getPath()));
                }
            }
        }
    }

    public FTImageRecognitionCachePlist imageRecognitionCachePlist() {
        if (this.documentIDFolder == null) {
            this.documentIDFolder = this.recognitionCacheRoot.childFileItemWithName(currentDocument.getDocumentUUID());
            if (this.documentIDFolder == null) {
                this.documentIDFolder = new FTFileItem(currentDocument.getDocumentUUID(), true);
                this.recognitionCacheRoot.addChildItem(this.documentIDFolder);
            }
        }
        String plistFileName = FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
        if (imageCachePlistItem != null && imageCachePlistItem.fileName.equals(plistFileName)) {
            return this.imageCachePlistItem;
        }

        FTImageRecognitionCachePlist recognitionCachePlist = (FTImageRecognitionCachePlist) this.documentIDFolder.childFileItemWithName(plistFileName);
        if (recognitionCachePlist == null) {
            recognitionCachePlist = new FTImageRecognitionCachePlist(plistFileName, false);
            this.documentIDFolder.addChildItem(recognitionCachePlist);
        }

        FTUrl cachePlistURL = FTUrl.withAppendedPath(this.documentIDFolder.getFileItemURL(), plistFileName);

        if (!FTFileManagerUtil.isFileExits(cachePlistURL.getPath())) {
            copyPackageImageRecognitionInfoToCache();
        } else {
            File cachedFile = new File(cachePlistURL.getPath());
            long cachedFileModifiedDate = cachedFile.lastModified();
            FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
            if (folderItem != null) {
                plistFileName = FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                FTUrl packagePlistURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);
                if (FTFileManagerUtil.isFileExits(packagePlistURL.getPath())) {
                    File packageFile = new File(packagePlistURL.getPath());
                    long packageFileModifiedDate = packageFile.lastModified();
                    if (packageFileModifiedDate > cachedFileModifiedDate) {
                        copyPackageImageRecognitionInfoToCache();
                    }
                }
            }
        }
        this.imageCachePlistItem = recognitionCachePlist;
        return this.imageCachePlistItem;

    }

    private void copyPackageImageRecognitionInfoToCache() {
        if (this.currentDocument.rootFileItem == null) {
            return;
        }

        FTFileItem folderItem = this.currentDocument.rootFileItem.childFileItemWithName(FTConstants.RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem != null) {
            String plistFileName = FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
            FTUrl recognitionInfoURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), plistFileName);

            if (!FTFileManagerUtil.isFileExits(recognitionInfoURL.getPath())) {
                return;
            }
            if (this.documentIDFolder != null) {
                this.recognitionCacheRoot.writeUpdatesToURL(context, this.recognitionCacheRoot.getFileItemURL());
                String cachePlistFileName = FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME + "_" + this.languageCode + FTConstants.PLIST_EXTENSION;
                FTUrl recognitionCacheURL = FTUrl.withAppendedPath(folderItem.getFileItemURL(), cachePlistFileName);
                if (FTFileManagerUtil.isFileExits(recognitionCacheURL.getPath())) {
                    new File(recognitionCacheURL.getPath()).renameTo(new File(recognitionCacheURL.getPath()));
                }
            }
        }
    }

    public void deleteRecognitionInfoFromCache() {
        File documentFolder = new File(recognitionCacheRoot.getFileItemURL().getPath() + "/" + currentDocument.getDocumentUUID());
        if (documentFolder.exists()) {
            FTFileManagerUtil.deleteRecursive(documentFolder);
        }
    }

    public void updateLanguage(String language) {
        this.languageCode = language;
    }

    public void saveRecognitionInfoToDisk() {
        this.lastSavedTime = FTDeviceUtils.getTimeStamp();
        this.recognitionCacheRoot.writeUpdatesToURL(context, recognitionCacheRoot.getFileItemURL());
        Log.d("LOG_RECOGNITION", "Saved recognitionInfo to Cache Disk");
    }
}