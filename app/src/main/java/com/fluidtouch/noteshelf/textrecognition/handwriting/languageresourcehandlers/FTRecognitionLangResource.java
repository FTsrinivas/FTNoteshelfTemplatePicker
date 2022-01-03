package com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.textrecognition.helpers.FTRecognitionUtils;

import java.io.File;

public class FTRecognitionLangResource {
    private String displayNameKey;
    private String languageCode;
    private FTLanguageResourceStatus resourceStatus;

    FTRecognitionLangResource(String displayNameKey, String languageCode) {
        this.displayNameKey = displayNameKey;
        this.languageCode = languageCode;

        if (!languageCode.equals(FTLanguageResourceManager.languageCodeNone)) {
            FTUrl recogFolderUrl = FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getApplicationContext());
            String languagePathURL;
            if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
                languagePathURL = recogFolderUrl.getPath().concat("/hwr_" + this.languageCode + ".dat");
            } else {
                languagePathURL = recogFolderUrl.getPath().concat("/" + "recognition-assets-" + this.languageCode + "/resources/" + this.languageCode);
            }
            File file = new File(languagePathURL);
            if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
                if (file.exists()) {
                    this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                } else {
                    this.resourceStatus = FTLanguageResourceStatus.NONE;
                }
            } else {
                if (file.exists() && file.listFiles().length > 0) {
                    this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                } else {
                    this.resourceStatus = FTLanguageResourceStatus.NONE;
                }
            }
        } else {
            this.resourceStatus = FTLanguageResourceStatus.NONE;
        }
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getDisplayNameKey() {
        return this.displayNameKey;
    }

    public FTLanguageResourceStatus getResourceStatus() {
        return resourceStatus;
    }

    void setResourceStatus(FTLanguageResourceStatus status) {
        this.resourceStatus = status;
    }

    public void downloadResourceOnDemand() {
        if (languageCode.equals(FTLanguageResourceManager.languageCodeNone))
            this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;

        if (this.resourceStatus == FTLanguageResourceStatus.NONE) {
            this.resourceStatus = FTLanguageResourceStatus.DOWNLOADING;
            if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
                if (languageCode.equals("en_US") || languageCode.equals("zh_CN")) {
                    String languagePathURL = FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getCurActCtx()).getPath().concat("/hwr_" + languageCode + ".dat");
                    if (FTFileManagerUtil.isFileExits(languagePathURL)) {
                        this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                    } else {
                        AssetsUtil assetManager = new AssetsUtil();
                        File destinationFile = new File(languagePathURL);
                        try {
                            assetManager.copyLocalAsset("LanguageAssets/samsung/hwr_" + languageCode + ".dat", destinationFile.getPath());
                            this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                            ObservingService.getInstance().postNotification("languageDownloaded", FTRecognitionLangResource.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                            this.resourceStatus = FTLanguageResourceStatus.NONE;
                        }
                    }
                } else {
                    this.resourceStatus = FTLanguageResourceStatus.DOWNLOADING;
                    FTLanguageResourceManager.getInstance().downloadResource(this);
                }
            } else if (languageCode.equals("en_US") || languageCode.equals("zh_CN")) {
                String languagePathURL = FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getCurActCtx()).getPath().concat("/recognition-assets-" + languageCode);
                if (FTFileManagerUtil.isFileExits(languagePathURL)) {
                    this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                } else {
                    this.resourceStatus = FTLanguageResourceStatus.DOWNLOADING;
                    AssetsUtil assetManager = new AssetsUtil();
                    try {
                        File tempFile = new File(FTConstants.TEMP_FOLDER_PATH);
                        if (!tempFile.exists()) {
                            tempFile.mkdir();
                        }
                        assetManager.copyLocalAsset("LanguageAssets/recognition-assets-" + languageCode, FTConstants.TEMP_FOLDER_PATH + "recognition-assets-" + languageCode);
                        ZipUtil.unzip(FTApp.getInstance().getCurActCtx(), FTConstants.TEMP_FOLDER_PATH + "/recognition-assets-" + languageCode,
                                FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getCurActCtx()).getPath(), (file, error) -> {
                                    FTRecognitionLangResource.this.resourceStatus = FTLanguageResourceStatus.DOWNLOADED;
                                    ObservingService.getInstance().postNotification("languageDownloaded", FTRecognitionLangResource.this);
                                });
                    } catch (Exception e) {
                        FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Error copying language from assets.\n" + e.getMessage());
                        this.resourceStatus = FTLanguageResourceStatus.NONE;
                    }
                }
            } else {
                this.resourceStatus = FTLanguageResourceStatus.DOWNLOADING;
                FTLanguageResourceManager.getInstance().downloadResource(this);
            }
        } else if (this.resourceStatus == FTLanguageResourceStatus.DOWNLOADED) {
            ObservingService.getInstance().postNotification("languageDownloaded", FTRecognitionLangResource.this);
        }

    }

    @Override
    public String toString() {
        return displayNameKey;
    }
}