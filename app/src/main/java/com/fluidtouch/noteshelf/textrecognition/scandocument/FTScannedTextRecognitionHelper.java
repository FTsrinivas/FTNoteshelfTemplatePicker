package com.fluidtouch.noteshelf.textrecognition.scandocument;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SizeF;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.noteshelf.cloud.FTCloudServices;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTScannedTextRecognitionHelper {
    private String documentUUID = UUID.randomUUID().toString();
    private boolean isRecognitionInProgress = false;
    private FTNoteshelfDocument currentDocument;
    private Context context;
    private short pagePtr = 0;
    private ExecutorService executor;

    public FTScannedTextRecognitionHelper(Context context, FTNoteshelfDocument document) {
        this.context = context;
        this.currentDocument = document;
        this.documentUUID = document.getDocumentUUID();
    }

    public void startPendingRecognition() {
        if (this.isRecognitionInProgress || !FTCloudServices.INSTANCE.isGooglePlayServicesAvailable(context) || isRecognitionComplete() || FTApp.getPref().get(SystemPref.COUNTRY_CODE, "").equalsIgnoreCase("cn")) {
            return;
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = Executors.newSingleThreadExecutor();

        FTLog.debug(FTLog.VISION_RECOGNITION, "Started pending vision text recognition");
        this.isRecognitionInProgress = true;
        executor.execute(() -> {
            List<FTNoteshelfPage> pages = this.currentDocument.pages(context);
            if (pagePtr >= pages.size()) {
                pagePtr = 0;
                isRecognitionInProgress = false;
                if (!isRecognitionComplete())
                    setRecognitionComplete(true);
                FTLog.debug(FTLog.VISION_RECOGNITION, "Vision Text recognition completed for book");
            } else {
                FTNoteshelfPage eachPage = pages.get(pagePtr);
                if (eachPage != null && eachPage.canRecognizeVisionText() && eachPage.getFTPdfDocumentRef() != null) {
                    Bitmap imageToProcess = eachPage.getFTPdfDocumentRef().pageBackgroundImage(eachPage.associatedPageIndex - 1);
                    if (imageToProcess != null) {
                        FTLog.debug(FTLog.VISION_RECOGNITION, "Found a page for vision text recognition.");
                        long lastUpdatedDate = eachPage.lastUpdated;
                        FTScannedTextRecognitionTask task = new FTScannedTextRecognitionTask();
                        //task.languageCode = FTVisionLanguageMapper.currentISOLanguageCode();
                        task.languageCode = "en_US";
                        task.currentDocument = this.currentDocument;
                        task.viewSize = new SizeF(eachPage.getPageRect().width(), eachPage.getPageRect().height());
                        task.imageToProcess = imageToProcess;
                        task.setListener((info, error) -> {
                            task.imageToProcess.recycle();
                            task.imageToProcess = null;
                            if (info == null || error != null) {
                                //FTLog.error(FTLog.VISION_RECOGNITION, "Vision Error: " + error.getMessage());
                            } else {
                                eachPage.setVisionRecognitionInfo(info);
                                if (lastUpdatedDate != 0) {
                                    info.lastUpdated = lastUpdatedDate;
                                }
                            }
                            pagePtr++;
                            FTLog.debug(FTLog.VISION_RECOGNITION, "Completed recognition for page");
                            isRecognitionInProgress = false;
                            startPendingRecognition();
                        });
                        FTScannedTextRecognitionTaskManager.getInstance().addBackgroundTask(task);
                    }
                } else {
                    pagePtr++;
                    isRecognitionInProgress = false;
                    startPendingRecognition();
                }
            }
        });
    }

    public void wakeUpRecognitionHelperIfNeeded() {
        if (!isRecognitionInProgress) {
            pagePtr = 0;
            startPendingRecognition();
        }
    }

    private boolean isRecognitionComplete() {
        return context.getSharedPreferences(documentUUID, Context.MODE_PRIVATE).getBoolean("isVisionRecog", false);
    }

    public void setRecognitionComplete(boolean status) {
        context.getSharedPreferences(documentUUID, Context.MODE_PRIVATE).edit().putBoolean("isVisionRecog", status).apply();
    }
}