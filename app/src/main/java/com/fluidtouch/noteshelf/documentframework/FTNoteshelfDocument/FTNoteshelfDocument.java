package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import static com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants.RECOGNITION_FILES_FOLDER_NAME;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SizeF;

import androidx.fragment.app.DialogFragment;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSString;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.enums.FTPageFooterOption;
import com.fluidtouch.noteshelf.document.enums.FTPageInsertPosition;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItem;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemFactory;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemImage;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPDF;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTPdfDocumentRef;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionCachePlist;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionHelper;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionHelper;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.helpers.recognitioncache.FTRecognitionCache;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecogCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecognitionHelper;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import kr.pe.burt.android.lib.androidoperationqueue.AndroidOperationQueue;

public class FTNoteshelfDocument extends FTDocument implements Parcelable {
    private String documentUUID;

    private ArrayList<FTFileItemPDF> pdfDocumentRefs = new ArrayList<>();
    private FTPdfDocumentRef pdfDocumentRef;

    private AndroidOperationQueue searchOperationQueue = new AndroidOperationQueue("DocumentSearchOperation");

    private FTRecognitionCache recognitionCache;
    private FTHandwritingRecognitionHelper recognitionHelper;
    private FTScannedTextRecognitionHelper visionRecognitionHelper;
    private FTImageRecognitionHelper imageRecognitionHelper;
    private FTRenameDialog renameDialog;
    private boolean isSearchingForKey = false;

    public FTNoteshelfDocument(FTUrl fileURL) {
        super(fileURL);
    }

    private static Boolean isPDFDocumentPasswordProtected(Context context, FTUrl fileURL) {
//        PDDocument document = null;
//        try {
//            document = PDDocument.load(new FileInputStream(new File(fileURL.getPath())), MemoryUsageSetting.setupTempFileOnly());
//            if (document.isEncrypted()) {
//                return true;
//            }
//            document.close();
//        } catch (InvalidPasswordException e) {
//            e.printStackTrace();
//            if(document!=null) {
//                try {
//                    document.close();
//                } catch (IOException ioException) {
//                    ioException.printStackTrace();
//                }
//            }
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return false;
        FTPdfDocumentRef documentRef = new FTPdfDocumentRef(context, fileURL, "");
        return (documentRef.pageDocumentRef() == 1);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopSearching();
        destroyRecognitionManager();
    }

    //region Recognition
    public FTHandwritingRecognitionHelper handwritingRecognitionHelper(Context context) {
        if (null == recognitionHelper) {
            this.recognitionHelper = new FTHandwritingRecognitionHelper(context, this);
        }
        return recognitionHelper;
    }

    public FTScannedTextRecognitionHelper visionRecognitionHelper(Context context) {
        if (null == visionRecognitionHelper) {
            this.visionRecognitionHelper = new FTScannedTextRecognitionHelper(context, this);
        }
        return visionRecognitionHelper;
    }

    public FTImageRecognitionHelper imageRecognitionHelper(Context context) {
        if (null == imageRecognitionHelper) {
            this.imageRecognitionHelper = new FTImageRecognitionHelper(context, this);
        }
        return imageRecognitionHelper;
    }

    public FTRecognitionCache recognitionCache(Context context) {
        if (null == recognitionCache) {
            this.recognitionCache = new FTRecognitionCache(context, this, FTLanguageResourceManager.getInstance().getCurrentLanguageCode());
        }
        return recognitionCache;
    }

    public void destroyRecognitionManager() {
        //ToDo: Release resources
    }
    //endregion

    private FTHandwritingRecognitionCachePlistItem recognitionInfoPlist() {
        if (null == rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem == null) {
            folderItem = new FTFileItem(RECOGNITION_FILES_FOLDER_NAME, true);
            this.rootFileItem.addChildItem(folderItem);
        }
        String plistFileName = FTConstants.RECOGNITION_INFO_FILE_NAME + "_" + FTLanguageResourceManager.getInstance().getCurrentLanguageCode() + FTConstants.PLIST_EXTENSION;
        FTFileItem plistItem = folderItem.childFileItemWithName(plistFileName);
        FTHandwritingRecognitionCachePlistItem recognitionInfoPlist = null;
        if (plistItem instanceof FTHandwritingRecognitionCachePlistItem) {
            recognitionInfoPlist = (FTHandwritingRecognitionCachePlistItem) plistItem;
        }
        if (recognitionInfoPlist == null) {
            recognitionInfoPlist = new FTHandwritingRecognitionCachePlistItem(plistFileName, false);
            folderItem.addChildItem(recognitionInfoPlist);
        }
        return recognitionInfoPlist;
    }

    private FTScannedTextRecogCachePlistItem visionRecognitionInfoPlist() {
        if (null == rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem == null) {
            folderItem = new FTFileItem(RECOGNITION_FILES_FOLDER_NAME, true);
            this.rootFileItem.addChildItem(folderItem);
        }
        //ToDo: Temporarily using en_US as default language; to be replaced with recognized language
        String plistFileName = FTConstants.VISION_RECOGNITION_INFO_FILE_NAME + "_en_US" + FTConstants.PLIST_EXTENSION;
        FTFileItem plistItem = folderItem.childFileItemWithName(plistFileName);
        FTScannedTextRecogCachePlistItem recognitionInfoPlist = null;
        if (plistItem instanceof FTScannedTextRecogCachePlistItem) {
            recognitionInfoPlist = (FTScannedTextRecogCachePlistItem) plistItem;
        }
        if (recognitionInfoPlist == null) {
            recognitionInfoPlist = new FTScannedTextRecogCachePlistItem(plistFileName, false);
            folderItem.addChildItem(recognitionInfoPlist);
        }
        return recognitionInfoPlist;
    }

    private FTImageRecognitionCachePlist imageRecognitionInfoPlist() {
        if (null == rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(RECOGNITION_FILES_FOLDER_NAME);
        if (folderItem == null) {
            folderItem = new FTFileItem(RECOGNITION_FILES_FOLDER_NAME, true);
            this.rootFileItem.addChildItem(folderItem);
        }
        //ToDo: Temporarily using en_US as default language; to be replaced with recognized language
        String plistFileName = FTConstants.IMAGE_RECOGNITION_INFO_FILE_NAME + "_en_US" + FTConstants.PLIST_EXTENSION;
        FTFileItem plistItem = folderItem.childFileItemWithName(plistFileName);
        FTImageRecognitionCachePlist recognitionInfoPlist = null;
        if (plistItem instanceof FTImageRecognitionCachePlist) {
            recognitionInfoPlist = (FTImageRecognitionCachePlist) plistItem;
        }
        if (recognitionInfoPlist == null) {
            recognitionInfoPlist = new FTImageRecognitionCachePlist(plistFileName, false);
            folderItem.addChildItem(recognitionInfoPlist);
        }
        return recognitionInfoPlist;
    }

    public ArrayList<FTNoteshelfPage> pages(Context context) {
        FTNSDocumentInfoPlistItem documentInfoPlist = this.documentInfoPlist();
        if (null != documentInfoPlist) {
            return documentInfoPlist.getPages(context);
        } else {
            return new ArrayList<>();
        }
    }

    public void pages(Context context, OnPagesCreated pagesCreated) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                FTNSDocumentInfoPlistItem documentInfoPlist = FTNoteshelfDocument.this.documentInfoPlist();
                if (null != documentInfoPlist) {
                    return documentInfoPlist.getPages(context);
                } else {
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                pagesCreated.didFinishWithStatus(true, (ArrayList<FTNoteshelfPage>) o);
            }
        }.execute();

    }

    public void unloadAllcontents(Context context) {
        int numPages = pages(context).size();
        for (int i = 0; i < numPages; i++) {
            FTNoteshelfPage page = pages(context).get(i);
            page.unloadContents();
        }
        documentInfoPlist().unloadContentsOfFileItem();
    }

    public Bitmap getShelfImage(Context context) {
        FTFileItemImage imageItem = (FTFileItemImage) this.rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_IMAGE_NAME);
        if (imageItem != null) {
            return imageItem.image(context);
        }
        return null;
    }

    public void setShelfImage(Context context, FTNTheme theme) {
        if (null != this.rootFileItem) {
            Bitmap shelfImage = theme.themeThumbnail(context);
            FTFileItemImage imageItem = (FTFileItemImage) this.rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_IMAGE_NAME);
            if (imageItem == null) {
                imageItem = new FTFileItemImage(FTConstants.COVER_SHELF_IMAGE_NAME, false);
            }
            if (theme.overlayType == 0) {
                FTFileItemImage imageItemOverlay = (FTFileItemImage) this.rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
                if (imageItemOverlay != null) {
                    imageItemOverlay.deleteFileItem();
                }
            } else {
                Bitmap bitmapOverlay = ((FTNCoverTheme) theme).themeOverlay(context);
                setOverlayImage(bitmapOverlay);
            }
            this.rootFileItem.addChildItem(imageItem);
            imageItem.setImage(shelfImage);
        }
    }

    public void setOverlayImage(Bitmap shelfImage) {
        if (null != this.rootFileItem) {
            FTFileItemImage imageItem = (FTFileItemImage) this.rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
            if (imageItem == null) {
                imageItem = new FTFileItemImage(FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME, false);
            }
            this.rootFileItem.addChildItem(imageItem);
            imageItem.setImage(shelfImage);
        }
    }

    //********************************************
    private void createDefaultFileItems(Context context) {

        this.rootFileItem = new FTFileItem(context, this.getFileURL(), true);

        FTFileItem resourceFolderItem = new FTFileItem(FTConstants.RESOURCES_FOLDER_NAME, true);
        this.rootFileItem.addChildItem(resourceFolderItem);

        FTFileItem metadataFolderItem = new FTFileItem(FTConstants.METADATA_FOLDER_NAME, true);
        this.rootFileItem.addChildItem(metadataFolderItem);

        FTFileItem templateFolderItem = new FTFileItem(FTConstants.TEMPLATES_FOLDER_NAME, true);
        this.rootFileItem.addChildItem(templateFolderItem);

        FTFileItem annotationFolderItem = new FTFileItem(FTConstants.ANNOTATIONS_FOLDER_NAME, true);
        this.rootFileItem.addChildItem(annotationFolderItem);

        FTNSDocumentInfoPlistItem documentInfoPlist = new FTNSDocumentInfoPlistItem(FTConstants.DOCUMENT_INFO_FILE_NAME, false);
        this.rootFileItem.addChildItem(documentInfoPlist);

        FTFileItemPlist propertyPlist = new FTFileItemPlist(FTConstants.PROPERTIES_PLIST, false);
        metadataFolderItem.addChildItem(propertyPlist);
    }

    public List<FTAudioAnnotation> getAudioAnnotations(Context context) {
        List<FTAudioAnnotation> audioAnnotations = new ArrayList<>();
        for (FTNoteshelfPage page : pages(context)) {
            audioAnnotations.addAll(page.getAudioAnnotations());
        }
        return audioAnnotations;
    }
    //endregion

    //region Save Notebook
    public synchronized void saveNoteshelfDocument(final Context context, final CompletionBlock onCompletion) {
        new SaveNotesAsyncTask(context, onCompletion).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean writeUpdatesToURL(Context context, FTUrl fileURL) {
        return this.rootFileItem.writeUpdatesToURL(context, fileURL);
    }

    public FTFileItem resourceFolderItem() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(FTConstants.RESOURCES_FOLDER_NAME);
        return folderItem;
    }

    FTFileItem metadataFolderItem() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(FTConstants.METADATA_FOLDER_NAME);
        return folderItem;
    }

    public FTFileItem templateFolderItem() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(FTConstants.TEMPLATES_FOLDER_NAME);
        return folderItem;
    }

    FTFileItem annotationFolderItem() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTFileItem folderItem = this.rootFileItem.childFileItemWithName(FTConstants.ANNOTATIONS_FOLDER_NAME);
        return folderItem;
    }
    //********************************************

    public FTNSDocumentInfoPlistItem documentInfoPlist() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTNSDocumentInfoPlistItem plistItem = (FTNSDocumentInfoPlistItem) this.rootFileItem.childFileItemWithName(FTConstants.DOCUMENT_INFO_FILE_NAME);
        plistItem.parentDocument = this;
        return plistItem;
    }

    FTFileItemPlist propertyInfoPlist() {
        if (null == this.rootFileItem) {
            return null;
        }
        FTFileItemPlist plistItem = (FTFileItemPlist) this.metadataFolderItem().childFileItemWithName(FTConstants.PROPERTIES_PLIST);
        return plistItem;
    }

    public void createDocument(Context context, FTDocumentInputInfo info, final CompletionBlock completion) {
        this.createDefaultFileItems(context);

        String uniqueID = FTDocumentUtils.getUDID();
        this.documentUUID = uniqueID;
        this.setShelfImage(context, info.getCoverTheme());

        this.propertyInfoPlist().setObject(context, FTConstants.DOC_VERSION, FTConstants.DOCUMENT_VERSION_KEY);
        this.propertyInfoPlist().setObject(context, this.documentUUID, FTConstants.DOCUMENT_ID_KEY);
        this.propertyInfoPlist().setObject(context, "Emulator", FTConstants.DEVICE_ID);
        this.propertyInfoPlist().setObject(context, "1.0", FTConstants.APP_VERSION);

        this.insertFileFromInfo(context, info, false, (success, error) -> completion.didFinishWithStatus(success, error));
    }

    public void openDocument(final Context context, final CompletionBlock completion) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
            boolean success = false;
            Error error = null;
            Log.d("TemplatePicker==>"," FTNoteshelfDocument updateCoversForItems getFileURL()::-"+getFileURL());
            if (getFileURL() != null) {
                FTFileItemFactory factory = new FTFileItemFactory(context);
                FTFileItem fileItem = factory.fileItemWithURL(context, getFileURL(), true);
                if (fileItem != null) {
                    rootFileItem = fileItem;
                    if (isValidDocument(context)) {
                        success = true;
                        loadInitialDataForDocument(context);
                    } else {
                        error = new Error(context.getString(R.string.invalid_document));
                        FTLog.saveLog("Opening Noteshelf doc Invalid Document");
                    }
                } else {
                    error = new Error("Root file not found");
                    FTLog.saveLog("Opening Noteshelf doc Root file not found");
                }
            } else {
                error = new Error("Root file not found");
                FTLog.logCrashException(new Exception("openDocument getFileURL() is null"));
                FTLog.saveLog("Opening Noteshelf doc Root file not found");
            }
            completion.didFinishWithStatus(success, error);
        });
    }

    //ToDo: Only being used for Evernote purpose; to be removed eventually
    public void openDocumentWhileInBackground() {
        Context context = FTApp.getInstance().getCurActCtx();
        FTFileItemFactory factory = new FTFileItemFactory(context);
        FTFileItem fileItem = factory.fileItemWithURL(context, getFileURL(), true);
        if (fileItem != null) {
            rootFileItem = fileItem;
            if (isValidDocument(context)) {
                loadInitialDataForDocument(context);
            }
        }
    }

    @Override
    public void loadInitialDataForDocument(Context context) {
        super.loadInitialDataForDocument(context);
        //this.previousFileModeificationDate  = this.getFileModificationDate();

        FTFileItemPlist propertyInfoPlist = this.propertyInfoPlist();
        if (null != propertyInfoPlist) {
            this.documentUUID = propertyInfoPlist.objectForKey(context, FTConstants.DOCUMENT_ID_KEY).toString();
        }

        FTNSDocumentInfoPlistItem documentInfoFileItem = this.documentInfoPlist();
        if (null != documentInfoFileItem) {
            documentInfoFileItem.parentDocument = this;
            NSDictionary documentsListDict = (NSDictionary) documentInfoFileItem.objectForKey(context, FTConstants.DOCUMENTS_KEY);
            if (documentsListDict != null) {
                String[] allKeys = documentsListDict.allKeys();
                for (String key : allKeys) {
                    FTFileItemPDF fileItem = (FTFileItemPDF) this.templateFolderItem().childFileItemWithName(key);
                    if (fileItem != null) {
                        fileItem.documentPassword = this.decryptedPasswordForDocumentName(context, key);
                    }
                }
            }
        }
    }

    private String decryptedPasswordForDocumentName(Context context, String documentName) {
        String password = null;

        NSDictionary templateInfo = this.templateValues(context, documentName);
        if (null != templateInfo) {
            if (templateInfo.objectForKey("password") != null) {
                password = templateInfo.objectForKey("password").toString();
            }
            if (password != null) {
                password = FTDocumentUtils.decryptString(password, false, null);
            }
        }
        return password;

    }

    public void insertFileFromInfo(final Context context, final FTDocumentInputInfo info, boolean isCurrentPage, final CompletionBlock onCompletion) {
        Log.d("Templatepicker==>","insertFileFromInfo info::-"+info+" info::-"+info+" isCurrentPage::-"+isCurrentPage+" onCompletion::-"+onCompletion);
        this.importFileWithInfo(context, info, isCurrentPage, (success, error) -> {
            if (null != error) {
                onCompletion.didFinishWithStatus(success, error);
            } else {
                saveNoteshelfDocument(context, onCompletion);
            }
        });
    }

    //region Import
    private void importFileWithInfo(final Context context, final FTDocumentInputInfo info, boolean isCurrentPage, final CompletionBlock onCompletion) {
        String uniqueID = FTDocumentUtils.getUDID();
        String docName = uniqueID + "." + FTConstants.PDF_FILE_EXT;
        final FTUrl destinationURL = FTUrl.withAppendedPath(this.templateFolderItem().getFileItemURL(), docName);
        File destinationFile = new File(destinationURL.getPath());
        AssetsUtil assetManager = new AssetsUtil();

        if (info.isTemplate && assetManager.isAssetExists(info.inputFileURL.getPath())) {
            //copy
            try {
                assetManager.copyFile(info.inputFileURL.getPath(), destinationFile);
            } catch (Exception e) {
                e.printStackTrace();
                onCompletion.didFinishWithStatus(false, new Error("Copy failed"));
                return;
            }
        } else {
            //Import PDF
            File sourceFile = new File(info.inputFileURL.getPath());
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            Log.d("TemplatePicker==>","importFileWithInfo getPath::-"+info.inputFileURL.getPath());
            try {
                FTDocumentUtils.copyFile(sourceFile, destinationFile);
            } catch (IOException e) {
                e.printStackTrace();
                onCompletion.didFinishWithStatus(false, new Error("Copy failed"));
                return;
            }
        }
        if (FTNoteshelfDocument.isPDFDocumentPasswordProtected(context, destinationURL)) {
            renameDialog = FTRenameDialog.newInstance(FTRenameDialog.RenameType.ASK_PASSWORD, "", -1, new FTRenameDialog.RenameListener() {
                @Override //TODO: SREENU
                /*public void renameShelfItem(String text, int position) {
                    //Log.d("TemplatePicker==>","VMK PasswordProtected FTNoteshelfDocument renameShelfItem::-");*/
                public void renameShelfItem(String text, int position, DialogFragment dialogFragment) {
                    pdfDocumentRef = new FTPdfDocumentRef(context, destinationURL, text);
                    if (pdfDocumentRef.pageDocumentRef() == 0) {
                        renameDialog.dismiss();
                        FTNoteshelfDocument.this.importFileWithInfo(context, info, isCurrentPage, onCompletion);
                    } else if (pdfDocumentRef.pageDocumentRef() == 1) {
                        renameDialog.setErrorMessage();
                    } else {
                        renameDialog.dismiss();
                        FTNoteshelfDocument.this.startImporting(context, destinationURL, text, info, isCurrentPage, onCompletion);
                        //onCompletion.didFinishWithStatus((error == null), error);
                    }
                }

                @Override
                public void dialogActionCancel() {
                    //Log.d("TemplatePicker==>","VMK PasswordProtected FTNoteshelfDocument dialogActionCancel::-");
                    onCompletion.didFinishWithStatus(false, new Error("Import cancelled"));
                }
            });
            renameDialog.show(((FTBaseActivity) (context)).getSupportFragmentManager(), "FTRenameDialog");
        } else {
            this.startImporting(context, destinationURL, "", info, isCurrentPage, onCompletion);
            //onCompletion.didFinishWithStatus((error == null), error);
        }
    }

    private void startImporting(Context context, FTUrl url, String password, FTDocumentInputInfo info, boolean isCurrentPage, CompletionBlock onCompletion) {
        new AsyncTask<Void, Void, Error>() {
            @Override
            protected Error doInBackground(Void... voids) {
                String docName = FTDocumentUtils.getFileName(context, url);

                NSDictionary templateEntries = new NSDictionary();
                if (password != null && !password.equals("")) {
                    String encryptedPassword = FTDocumentUtils.encryptString(password, false, null);
                    NSString passwordToSet = new NSString(encryptedPassword);
                    templateEntries.put("password", passwordToSet);
                }
                templateEntries.put("version", FTConstants.DOC_VERSION);
                FTNoteshelfDocument.this.propertyInfoPlist().setObject(context, FTConstants.DOC_VERSION, FTConstants.DOCUMENT_VERSION_KEY);

                if (info.isImageSource) {
                    templateEntries.put("sourcedFromImage", new NSNumber(info.isImageSource ? 1 : 0));
                }
                templateEntries.put("isTemplate", new NSNumber(info.isTemplate ? 1 : 0));
                templateEntries.put("isTransparentTemplate", new NSNumber(info.isTemplate ? 1 : 0));
                templateEntries.put("footerOption", new NSNumber(info.footerOption == FTPageFooterOption.SHOW ? 1 : 0));
                FTNoteshelfDocument.this.setTemplateValues(context, docName, templateEntries);

                FTFileItemPDF fileItem = (FTFileItemPDF) FTNoteshelfDocument.this.templateFolderItem().childFileItemWithName(docName);

                if (null == fileItem) {
                    fileItem = new FTFileItemPDF(docName, false);
                    fileItem.documentPassword = password;
                    FTNoteshelfDocument.this.templateFolderItem().addChildItem(fileItem);
                }
                pdfDocumentRef = fileItem.pageDocumentRef(context);
                if (null == pdfDocumentRef) {
                    return new Error("Failed to import");

                } else {
                    if (info.isNewBook && info.getCoverTheme() != null && info.getCoverTheme().overlayType == 1) {
                        FTNCoverTheme coverTheme = info.getCoverTheme();
                        Bitmap bitmap = pdfDocumentRef.pageBackgroundImage(0);
                        if (bitmap != null) {
                            Bitmap overlayBitmap = coverTheme.themeOverlay(context);
                            SizeF aspectSize = new SizeF(overlayBitmap.getWidth(), ((float) overlayBitmap.getWidth() / bitmap.getWidth()) * bitmap.getHeight());
                            Bitmap resizedBitmap = BitmapUtil.scaleBitmap(bitmap, new RectF(0, 0, aspectSize.getWidth(), aspectSize.getHeight()));
                            Bitmap shelfImage = BitmapUtil.getTransparentMergedBitmap(context, resizedBitmap, overlayBitmap, 0);
                            coverTheme.bitmap = shelfImage;
                            setShelfImage(context, coverTheme);
                        }
                    }
                    //pdfDocumentRef.copyTextToJson();
                    if (isCurrentPage) {
                        return FTNoteshelfDocument.this.replacePageEntities(context, pdfDocumentRef, url, info.insertAt, info);
                    } else
                        return FTNoteshelfDocument.this.createPageEntities(context, pdfDocumentRef, url, info.insertAt, info);

                }
            }

            @Override
            protected void onPostExecute(Error error) {
                super.onPostExecute(error);
                onCompletion.didFinishWithStatus((error == null), error);
            }
        }.execute();
    }
    //endregion

    public void prepareForImporting(final Context context,
                                    final SuccessErrorBlock onCompletion) {
        this.openDocument(context, (success, error) -> {
            if (success) {
                documentUUID = FTDocumentUtils.getUDID();
                FTFileItemPlist propertyInfoPlist = propertyInfoPlist();
                if (null != propertyInfoPlist) {
                    propertyInfoPlist.setObject(context, FTDocumentUtils.getUDID(), FTConstants.DOCUMENT_ID_KEY);
                    propertyInfoPlist.forceSave = true;
                }
                saveNoteshelfDocument(context, onCompletion::didFinishWithStatus);
            } else {
                onCompletion.didFinishWithStatus(success, error);
            }
        });
    }

    private Error createPageEntities(Context context, FTPdfDocumentRef pdfDocument, FTUrl url, Integer index, FTDocumentInputInfo info) {
        //This should never happen
        if (pages(context).size() < index) {
            return new Error("Page mismatch");
        }
        Integer count = pdfDocument.pageCount();
        if (count == 0) {
            return new Error("Failed to import");
        }

        for (int i = 0; i < count; i++) {
            FTNoteshelfPage page = new FTNoteshelfPage(context);
            page.setParentDocument(this);
            page.associatedPDFKitPageIndex = i + 1;
            page.associatedPageIndex = i + 1;
            page.associatedPDFFileName = FTDocumentUtils.getFileName(context, url);
            page.lineHeight = info.lineHeight;
//            page.pdfPageRect = pdfDocument.getPageRectAtIndex(i);
            page.uuid = FTDocumentUtils.getUDID();
            page.creationDate = FTDeviceUtils.getTimeStamp();
            page.lastUpdated = FTDeviceUtils.getTimeStamp();
            page.deviceModel = FTDeviceUtils.getDeviceName();
//          page.tags =
            this.documentInfoPlist().insertPage(context, page, index + i);
        }

        return null;
    }

    private Error replacePageEntities(Context context, FTPdfDocumentRef pdfDocument, FTUrl url, Integer currentIndex, FTDocumentInputInfo info) {
        //This should never happen
        if (pages(context).size() < currentIndex) {
            return new Error("Page mismatch");
        }

        Integer count = pdfDocument.pageCount();
        if (count == 0) {
            return new Error("Failed to import");
        }

        FTNoteshelfPage pageToDelete = pages(context).get(currentIndex);
        FTNoteshelfPage page = new FTNoteshelfPage(context);
        final int firstPageIndex = 1;
        page.setParentDocument(this);
        page.associatedPDFKitPageIndex = firstPageIndex;
        page.associatedPageIndex = firstPageIndex;
        page.associatedPDFFileName = FTDocumentUtils.getFileName(context, url);
        page.lineHeight = info.lineHeight;
        page.setPdfPageRect(pdfDocument.getPageRectAtIndex(firstPageIndex - 1));
        page.uuid = FTDocumentUtils.getUDID();
        page.creationDate = FTDeviceUtils.getTimeStamp();
        page.lastUpdated = FTDeviceUtils.getTimeStamp();
        page.deviceModel = FTDeviceUtils.getDeviceName();
        page.addAnnotations(pageToDelete.getPageAnnotations());
        page.isBookmarked = pageToDelete.isBookmarked;
        //page.tags =
        page.setPageDirty(true);

        //pageToDelete.willDelete();
        documentInfoPlist().getPages(context).set(currentIndex, page);

        return null;
    }

    private void setTemplateValues(Context context, String tempName, NSDictionary values) {
        FTNSDocumentInfoPlistItem fileItem = this.documentInfoPlist();
        if (null != fileItem) {
            NSDictionary documentsList = (NSDictionary) fileItem.objectForKey(context, FTConstants.DOCUMENTS_KEY);
            if (null == documentsList) {
                documentsList = new NSDictionary();
            }

            NSDictionary dicValue = (NSDictionary) documentsList.objectForKey(tempName);
            if (dicValue == null) {
                dicValue = new NSDictionary();
            }
            for (String key : values.allKeys()) {
                dicValue.put(key, values.objectForKey(key));
            }
            documentsList.put(tempName, dicValue);
            fileItem.setObject(context, documentsList, FTConstants.DOCUMENTS_KEY);
        }
    }

    NSDictionary templateValues(Context context, String tempName) {
        NSDictionary tempInfo = new NSDictionary();

        FTNSDocumentInfoPlistItem fileItem = this.documentInfoPlist();
        if (null != fileItem) {
            NSDictionary documentsList = (NSDictionary) fileItem.objectForKey(context, FTConstants.DOCUMENTS_KEY);
            if (null != documentsList) {
                tempInfo = (NSDictionary) documentsList.objectForKey(tempName);
            }
        }
        return tempInfo;
    }

    private Boolean isValidDocument(Context context) {
        try {
            if (this.pages(context).size() == 0) {
                return false;
            }
            return this.validateFileItemsForDocumentConsistency(context);
        } catch (Exception e) {
            FTLog.logCrashException(e);
            return false;
        }
    }

    private Boolean isDocumentVersionSupported(Context context) {
        String documentVersion = getDocumentVersion(context);
        if (null == documentVersion) {
            return false;
        }
        float value = Float.parseFloat(documentVersion);
        return !(value > FTConstants.APP_SUPPORTED_MAX_DOC_VERSION);
    }

    public String getDocumentVersion(Context context) {
        return this.propertyInfoPlist().objectForKey(context, FTConstants.DOCUMENT_VERSION_KEY).toString();
    }

    private Boolean validateFileItemsForDocumentConsistency(Context context) {
        if (null == this.rootFileItem) {

            return false;
        }
        FTFileItem templateFolderItem = this.rootFileItem.childFileItemWithName(FTConstants.TEMPLATES_FOLDER_NAME);
        if (null == templateFolderItem) {
            FTLog.saveLog("Opening Noteshelf templateFolderItem null");
            return false;
        }

        if (this.templateFolderItem().children.size() == 0) {
            FTLog.saveLog("Opening Noteshelf templateFolderItem().children.size() == 0");
            return false;
        }

        FTFileItem metaDataFolderItem = this.rootFileItem.childFileItemWithName(FTConstants.METADATA_FOLDER_NAME);
        if (null == metaDataFolderItem) {
            FTLog.saveLog("Opening Noteshelf metaDataFolderItem = null");
            return false;
        }

        FTFileItem propertyInfoPlist = this.metadataFolderItem().childFileItemWithName(FTConstants.PROPERTIES_PLIST);
        if (null == propertyInfoPlist) {
            FTLog.saveLog("Opening Noteshelf propertyInfoPlist = null");
            return false;
        }
        if (!this.isDocumentVersionSupported(context)) {
            FTLog.saveLog("Opening Noteshelf version > supported version");
            return false;
        }

        FTFileItem annotateFolderItem = this.rootFileItem.childFileItemWithName(FTConstants.ANNOTATIONS_FOLDER_NAME);
        if (null == annotateFolderItem) {
            annotateFolderItem = new FTFileItem(FTConstants.ANNOTATIONS_FOLDER_NAME, true);
            this.rootFileItem.addChildItem(annotateFolderItem);
        }

        FTFileItem resourceFolderItem = this.rootFileItem.childFileItemWithName(FTConstants.RESOURCES_FOLDER_NAME);
        if (null == resourceFolderItem) {
            resourceFolderItem = new FTFileItem(FTConstants.RESOURCES_FOLDER_NAME, true);
            this.rootFileItem.addChildItem(resourceFolderItem);
        }

        return true;
    }

    //Finder Operations
    public void deletePages(Context context, ArrayList<FTNoteshelfPage> pages, PageDeletionBlock deletionBlock) {
        AsyncTask.execute(() -> {
            FTNSDocumentInfoPlistItem documentInfoPlist = this.documentInfoPlist();
            if (null != documentInfoPlist) {
                for (FTNoteshelfPage eachPage : pages) {
                    FTLog.debug(FTLog.FINDER_OPERATIONS, "Deleting a page");
                    eachPage.willDelete();
                    documentInfoPlist.deletePage(context, eachPage);
                }
                FTLog.debug(FTLog.FINDER_OPERATIONS, "Completed deletion of " + pages.size() + " pages.");
            }
            FTENSyncRecordUtil.removeEvernoteSyncForPages(pages);
            updateCoverImage(context, true);
            deletionBlock.didFinishWithPage(null);
        });
    }

    public void recursivelyCopyPages(Context context, ArrayList<FTNoteshelfPage> pages, boolean withAnnotations, FTPageInsertPosition pageInsertPosition, PageCopyCompletionBlock onCompletion) {
        AsyncTask.execute(() -> {
            ArrayList<FTNoteshelfPage> copiedPages = new ArrayList<>();

            AtomicInteger pageIteratorIndex = new AtomicInteger();
            while (pageIteratorIndex.get() < pages.size()) {
                FTLog.debug(FTLog.FINDER_OPERATIONS, "Duplicating a page.");
                FTNoteshelfPage pageToDuplicate = pages.get(pageIteratorIndex.get());
                pageToDuplicate.deepCopyPage(FTNoteshelfDocument.this, withAnnotations, copiedPage -> {
                    int currentPageIndex = 0;
                    int indexToInsert = 0;

                    ArrayList<FTNoteshelfPage> currentExistingPages = FTNoteshelfDocument.this.pages(context);
                    for (int i = 0; i < currentExistingPages.size(); i++) {
                        FTNoteshelfPage item = currentExistingPages.get(i);
                        if (item.uuid.equals(pageToDuplicate.uuid)) {
                            currentPageIndex = i;
                            break;
                        }
                    }

                    switch (pageInsertPosition) {
                        case PREVIOUS_TO_CURRENT:
                            indexToInsert = Math.max(0, currentPageIndex);
                            break;
                        case NEXT_TO_CURRENT:
                        case AT_CURRENT:
                            indexToInsert = currentPageIndex + 1;
                            break;
                    }

                    FTNoteshelfDocument.this.documentInfoPlist().insertPage(context, copiedPage, indexToInsert);
                    copiedPages.add(copiedPage);
                    pageIteratorIndex.getAndIncrement();
                });
            }

            FTNoteshelfDocument.this.saveNoteshelfDocument(context, (success, error) -> {
                FTLog.debug(FTLog.FINDER_OPERATIONS, "Completed duplication of " + pages.size() + " pages.");
                onCompletion.didFinishWithStatus(success, error, copiedPages);
            });
        });
    }

    public void movePages(ArrayList<FTNoteshelfPage> pages, int toIndex) {
        FTNSDocumentInfoPlistItem documentInfoPlist = this.documentInfoPlist();
        if (null != documentInfoPlist) {
            int index = 0;
            for (FTNoteshelfPage eachItem : pages) {
                documentInfoPlist.movePage(eachItem, toIndex + index);
                index += 1;
            }
        }
    }

    public void movePagesToOtherDocument(Context context, ArrayList<FTNoteshelfPage> pages, FTNoteshelfDocument toDocument, CompletionBlock completionBlock) {
        toDocument.openDocument(context, (success, error) -> {
            if (success) {
                ArrayList<FTNoteshelfPage> pagesToMove = new ArrayList<>(pages);
                toDocument.recursivelyCopyPages(context, pagesToMove, true, FTPageInsertPosition.NEXT_TO_CURRENT, (success1, error1, copiedPages) -> {
                    if (success1) {
                        toDocument.movePages(copiedPages, 0);
                        toDocument.saveNoteshelfDocument(context, new CompletionBlock() {
                            @Override
                            public void didFinishWithStatus(Boolean success1, Error error1) {
                                if (success1) {
                                    FTNoteshelfDocument.this.deletePages(context, pagesToMove, new PageDeletionBlock() {
                                        @Override
                                        public void didFinishWithPage(Error error1) {
                                            if (error1 == null) {
                                                updateCoverImage(context, true);
                                                completionBlock.didFinishWithStatus(true, error1);
                                            } else {
                                                completionBlock.didFinishWithStatus(true, error1);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void insertPageAtIndex(final Context context, int index, boolean isCopied,
                                  final PageInsertionBlock onCompletion) {
        FTNoteshelfPage pageToCopy = null;
        if (index >= this.pages(context).size()) {
            pageToCopy = this.pages(context).get(this.pages(context).size() - 1);
        } else {
            pageToCopy = this.pages(context).get(Math.max(0, index - 1));
        }
        FTNoteshelfPage copiedPage = null;
        boolean isTemplate = pageToCopy.isTemplate();

        if (pageToCopy != null && isCopied) {
            copiedPage = pageToCopy.copyPageAttributes(context);
            copiedPage.uuid = FTDocumentUtils.getUDID();
            copiedPage.setParentDocument(pageToCopy.getParentDocument());
            copiedPage.creationDate = FTDeviceUtils.getTimeStamp();
            copiedPage.lastUpdated = FTDeviceUtils.getTimeStamp();
            copiedPage.deviceModel = FTDeviceUtils.getDeviceName();
            isTemplate = pageToCopy.isTemplate();
//            pageRect = pageCopy.pdfPageRect;
        }

        if (isTemplate && copiedPage != null) {
            this.documentInfoPlist().insertPage(context, copiedPage, index);
            onCompletion.didFinishWithPage(copiedPage, null);
        } else {

            String paperPackName = "";
            /*if (pageToCopy.getPageRect().width() <= pageToCopy.getPageRect().height()) { //If Portrait Page
                paperPackName = "Plain.nsp";
            } else {
                paperPackName = "PlainLand.nsp";
            }*/
            paperPackName = "Plain.nsp";
            final FTNTheme paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
            paperTheme.categoryName = "Basic";
            paperTheme.isBasicTheme = true;
            //paperTheme.thumbnailURLPath = FTConstants.DEFAULT_PAPER_THEME_URL;
            FTTemplateUtil.getInstance().fTTemplateColorsSerializedObject("#F7F7F2-1.0",
                    "Default","#000000-0.15","#000000-0.15");
            FTNoteshelfPage finalCopiedPage1 = copiedPage;
            Log.d("TemplatePicker==>"," Sample Notebook FTNoteShelfDocument");
            FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
            paperTheme.template(context, (documentInfo, generationError) -> {
                documentInfo.insertAt = index;
                final FTNoteshelfPage finalCopiedPage = finalCopiedPage1;

                this.insertFileFromInfo(context, documentInfo, false, (success, error) -> FTNoteshelfDocument.this.saveNoteshelfDocument(context, (success1, error1) -> onCompletion.didFinishWithPage(finalCopiedPage, error1)));
            });
        }
//
//        let IMAGE = FTPDFExportView.snapshot(forPage: copiedPage, size: CGSize(width: 300,height: 400), screenScale: UIScreen.mvp.scale, shouldRenderBackground: true);
//        copiedPage.thumbnail()?.updateThumbnail(IMAGE,updatedDate:Date.init(timeIntervalSinceReferenceDate: copiedPage.lastUpdated.doubleValue));

    }

    //region Search
    public void searchDocumentForKey(final Context context, final String searchKey, final SearchCompletionBlock onCompletion) {
        FTApp.getPref().save(SystemPref.DID_SHW_PREVIOUSLY_SEARCHED, true);
        handwritingRecognitionHelper(context).wakeUpRecognitionHelperIfNeeded();
        stopSearching();
        searchOperationQueue.addOperation((queue, bundle) -> {
            isSearchingForKey = true;
            for (int i = 0; i < pages(context).size(); i++) {
                if (!isSearchingForKey) {
                    break;
                }
                FTNoteshelfPage eachPage = pages(context).get(i);
                isSearchingForKey = true;
                if (TextUtils.isEmpty(searchKey)) {
                    eachPage.clearSearchableItems();
                    ObservingService.getInstance().postNotification("searchObserver_" + getDocumentUUID(), eachPage);
                } else {
                    if (eachPage.searchForKey(searchKey.trim().toLowerCase())) {
                        ObservingService.getInstance().postNotification("searchObserver_" + getDocumentUUID(), eachPage);
                    }
                }
            }
            closePdfDocuments();
            if (onCompletion != null) {
                new Handler(Looper.getMainLooper()).post(() -> onCompletion.didFinishSearchWithStatus(false));
            }
        });
        searchOperationQueue.start();
    }

    public void stopSearching() {
        isSearchingForKey = false;
        if (!searchOperationQueue.isActivated()) searchOperationQueue.stop();
    }
    //endregion

    public boolean hasAnyUnsavedChanges(Context context) {
        FTNSDocumentInfoPlistItem documentInfoPlist = this.documentInfoPlist();
        FTHandwritingRecognitionCachePlistItem recognitionInfoPlist = recognitionInfoPlist();
        FTScannedTextRecogCachePlistItem visionRecognitionInfoPlist = visionRecognitionInfoPlist();
        FTImageRecognitionCachePlist imageRecognitionInfoPlist = imageRecognitionInfoPlist();

        boolean changes = ((null != documentInfoPlist) && (documentInfoPlist.isModified && documentInfoPlist.forceSave))
                || ((null != recognitionInfoPlist) && (recognitionInfoPlist.isModified || recognitionInfoPlist.forceSave)
                || ((null != visionRecognitionInfoPlist) && (visionRecognitionInfoPlist.isModified || visionRecognitionInfoPlist.forceSave))
                || ((null != imageRecognitionInfoPlist) && (imageRecognitionInfoPlist.isModified || imageRecognitionInfoPlist.forceSave)));

        ArrayList<FTNoteshelfPage> allPages = this.pages(context);
        updateCoverImage(context, false);
        if (!changes) {
            for (int p = 0; p < allPages.size(); p++) {
                FTNoteshelfPage eachPage = allPages.get(p);
                if (eachPage.isDirty) {
                    changes = true;
                    break;
                }
            }
        }
        return changes;
    }

    public void addPdfDocumentRef(FTFileItemPDF ftPdfDocumentRef) {
        pdfDocumentRefs.add(ftPdfDocumentRef);
    }

    public void closePdfDocument() {
        if (null != pdfDocumentRef) {
            pdfDocumentRef.closeDocument();
        }
        rootFileItem.unloadContentsOfFileItem();
    }

    public void closePdfDocuments() {
        while (pdfDocumentRefs.size() > 0) {
            try {
                pdfDocumentRefs.get(0).unloadContentsOfFileItem();
                pdfDocumentRefs.remove(0);
            } catch (Exception e) {
                Log.v("PdfFileItem", "Unable to unload");
            }
        }
    }

    public void deleteDocument(Context context) {
        //Terminate search process
        stopSearching();
        destroyRecognitionManager();
        //Remove recognitionPlist files from cache
        recognitionCache(context).deleteRecognitionInfoFromCache();
        //Remove thumbnails for this document
        FTFileManagerUtil.deleteRecursive(new File(FTUrl.thumbnailFolderURL().getPath() + "/" + getDocumentUUID()));
    }

    //region Callback Interfaces
    public interface SuccessErrorBlock {
        void didFinishWithStatus(boolean success, Error error);
    }

    public interface PageCopyCompletionBlock {
        void didFinishWithStatus(boolean success, Error error, ArrayList<FTNoteshelfPage> copiedPages);
    }

    public interface PageInsertionBlock {
        void didFinishWithPage(FTNoteshelfPage insertedPage, Error error);
    }

    public interface SearchCompletionBlock extends Serializable {
        void didFinishSearchWithStatus(boolean cancelled);
    }

    public interface PageDeletionBlock {
        void didFinishWithPage(Error error);
    }

    public interface OnPagesCreated {
        void didFinishWithStatus(boolean success, ArrayList<FTNoteshelfPage> pages);
    }

    private class SaveNotesAsyncTask extends AsyncTask<Object, Void, Error> {
        private boolean success;
        private Context context;
        private CompletionBlock onCompletion;

        SaveNotesAsyncTask(Context context, CompletionBlock onCompletion) {
            this.context = context;
            this.onCompletion = onCompletion;
            FTLog.saveLog("Saving Noteshelf Doc");
            FTLog.debug(FTLog.GLOBAL_SEARCH, "Saving notebook = " + getDisplayTitle(context));
        }

        @Override
        protected Error doInBackground(Object[] objects) {
            Error error = null;
            enableRecognition(context);
            this.success = FTNoteshelfDocument.this.writeUpdatesToURL(context, FTNoteshelfDocument.this.getFileURL());
            if (!this.success) {
                error = new Error("Save failed");
            } else {
                File notebook = new File(getFileURL().getPath());
                notebook.setLastModified(FTDeviceUtils.getTimeStamp() * 1000);
                ArrayList<FTNoteshelfPage> pages = pages(context);
                for (int i = 0; i < pages.size(); i++) {
                    pages.get(i).setPageDirty(false);
                }
            }
            return error;
        }

        @Override
        protected void onPostExecute(Error error) {
            if (this.onCompletion != null)
                this.onCompletion.didFinishWithStatus(this.success, error);
        }
    }

    public void enableRecognition(Context context) {
        //===============Recognition==============
        recognitionCache(context).saveRecognitionInfoToDisk();
        if (recognitionCache != null) {
            FTHandwritingRecognitionCachePlistItem recognitionCachePlist = recognitionCache.recognitionCachePlist();
            if (recognitionCachePlist != null && (recognitionCachePlist.isModified || recognitionCachePlist.forceSave)) {
                Object mutableDict = recognitionCachePlist.getContent(context);
                recognitionInfoPlist().updateContent(mutableDict);
                FTLog.debug(FTLog.HW_RECOGNITION, "Copied handwriting recognition info from cache to notebook folder");
            }
            FTScannedTextRecogCachePlistItem visionRecognitionCachePlist = recognitionCache.visionRecognitionCachePlist();
            if (visionRecognitionCachePlist != null && (visionRecognitionCachePlist.isModified || visionRecognitionCachePlist.forceSave)) {
                Object mutableDict = visionRecognitionCachePlist.getContent(context);
                visionRecognitionInfoPlist().updateContent(mutableDict);
                FTLog.debug(FTLog.VISION_RECOGNITION, "Copied vision recognition info from cache to notebook folder");
            }
            FTImageRecognitionCachePlist imageRecognitionCachePlist = recognitionCache.imageRecognitionCachePlist();
            if (imageRecognitionCachePlist != null && (imageRecognitionCachePlist.isModified || imageRecognitionCachePlist.forceSave)) {
                Object mutableDict = imageRecognitionCachePlist.getContent(context);
                imageRecognitionInfoPlist().updateContent(mutableDict);
                FTLog.debug(FTLog.IMAGE_RECOGNITION, "Copied image annotation recognition info from cache to notebook folder");
            }
        }
        //========================================
    }

    public void updateCoverImage(Context context, boolean isFirstPageMoved) {
        FTFileItem ftFileItem = rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
        if (ftFileItem != null) {
            FTNoteshelfPage noteshelfPage = pages(context).get(0);
            if (noteshelfPage.isPageEdited || isFirstPageMoved) {
                ObservingService.getInstance().addObserver(FTPageThumbnail.strObserver + noteshelfPage.uuid, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        {
                            try {
                                FTFileItemImage imageItem = (FTFileItemImage) rootFileItem.childFileItemWithName(FTConstants.COVER_SHELF_IMAGE_NAME);
                                FTPageThumbnail.FTThumbnail ftThumbnail = (FTPageThumbnail.FTThumbnail) arg;
                                Bitmap image = ftThumbnail.getThumbImage();
                                if (imageItem != null && image != null) {
                                    InputStream overlayInputStream = new FileInputStream(ftFileItem.getFileItemURL().getPath());
                                    Bitmap overlayBitmap = BitmapFactory.decodeStream(overlayInputStream);
                                    SizeF aspectSize = new SizeF(overlayBitmap.getWidth(), ((float) overlayBitmap.getWidth() / image.getWidth()) * image.getHeight());
                                    Bitmap resizedBitmap = BitmapUtil.scaleBitmap(image, new RectF(0, 0, aspectSize.getWidth(), aspectSize.getHeight()));
                                    Bitmap finalBitmap = BitmapUtil.getTransparentMergedBitmap(context, resizedBitmap, overlayBitmap, 0);
                                    imageItem = new FTFileItemImage(FTConstants.COVER_SHELF_IMAGE_NAME, false);
                                    rootFileItem.addChildItem(imageItem);
                                    imageItem.setImage(finalBitmap);
                                    imageItem.saveContentsOfFileItem(context);
                                }
                                ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + ftThumbnail.getPageUUID(), this);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                noteshelfPage.thumbnail().thumbnailImage(context);
            }
        }
    }
    //endregion
}