package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.content.Context;
import android.util.SizeF;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;

import java.util.List;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTHandwritingRecognitionHelper {
    private FTNoteshelfDocument currentDocument;
    private Context context;
    private boolean isRecognitionInProgress = false;
    private Observer languageChangeObserver;
    private boolean hasPendingRecognitionPages = true;

    private int pagePtr = 0;
    private ExecutorService executor;

    private Listener listener;

    public FTHandwritingRecognitionHelper(Context context, FTNoteshelfDocument document) {
        this.context = context;
        this.currentDocument = document;
        languageChangeObserver = (o, arg) -> FTHandwritingRecognitionHelper.this.handleLanguageChange((String) arg);
        ObservingService.getInstance().addObserver("languageChange", languageChangeObserver);
    }

    private static boolean shouldProceedWithRecognition() {
        return !FTLanguageResourceManager.getInstance().getCurrentLanguageCode().equals(FTLanguageResourceManager.languageCodeNone);
    }

    private void handleLanguageChange(String selectedLanguage) {
        this.currentDocument.recognitionCache(context).updateLanguage(selectedLanguage);
        if (shouldProceedWithRecognition()) {
            this.wakeUpRecognitionHelperIfNeeded();
        }
    }

    public void startPendingRecognition() {
        if (!shouldProceedWithRecognition())
            return;
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            if (FTApp.getEngine(context) == null) {
                return;
            }
        } else if (FTApp.getEngine() == null) {
            return;
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        executor = Executors.newSingleThreadExecutor();
        this.isRecognitionInProgress = true;
        try {
            executor.execute(() -> {
                List<FTNoteshelfPage> pages = this.currentDocument.pages(context);
                if (pagePtr >= pages.size()) {  //Completed recognition for all pages
                    ObservingService.getInstance().postNotification("pageIndexing", null);
                    pagePtr = 0;
                    isRecognitionInProgress = false;
                    if (hasPendingRecognitionPages) { //Restart recognition on latest changes
                        startPendingRecognition();
                        hasPendingRecognitionPages = false;
                    }
                } else {
                    FTNoteshelfPage pageToProcess = pages.get(pagePtr);
                    if (pageToProcess != null && pageToProcess.canRecognizeHandwriting()) {
                        FTLog.debug(FTLog.HW_RECOGNITION, "Found a page for recognition at index = " + (pageToProcess.pageIndex() + 1));

                        //Notify finder to show indexing dialog
                        ObservingService.getInstance().postNotification("pageIndexing", pageToProcess);

                        long lastUpdatedDate = pageToProcess.lastUpdated;
                        FTHandwritingRecognitionTask task = new FTHandwritingRecognitionTask();
                        task.languageCode = FTLanguageResourceManager.getInstance().getCurrentLanguageCode();
                        task.currentDocument = this.currentDocument;
                        task.pageAnnotations = pageToProcess.getPageAnnotations();
                        task.viewSize = new SizeF(pageToProcess.getPageRect().width(), pageToProcess.getPageRect().height());
                        task.setListener((info, error) -> {
                            isRecognitionInProgress = false;
                            if (error != null) {
                                FTLog.error(FTLog.HW_RECOGNITION, error.getMessage());
                            } else if (info == null) {
                                FTLog.error(FTLog.HW_RECOGNITION, "RecognitionInfo is null for page at index = " + (pageToProcess.pageIndex() + 1));
                            } else {
                                FTLog.debug(FTLog.HW_RECOGNITION, "Recognition completed for a page at index = " + (pageToProcess.pageIndex() + 1));
                                pageToProcess.setRecognitionInfo(context, info);
                                if (lastUpdatedDate != 0) {
                                    info.lastUpdated = lastUpdatedDate;
                                }
                            }
                            if (listener != null) listener.onEachPageCompleted(pageToProcess);
                            pagePtr++;
                            //Added this to avoid multiple Threads at the same time.
                            startPendingRecognition();
                        });
                        FTHandwritingRecognitionTaskManager.getInstance().addBackgroundTask(task);
                    } else {
                        pagePtr++;
                        //Added this to avoid multiple Threads at the same time.
                        startPendingRecognition();
                    }
                }
            });
        } catch (Exception e) {
            startPendingRecognition();
            FTLog.logCrashException(e);
        }
    }

    public void wakeUpRecognitionHelperIfNeeded() {
        hasPendingRecognitionPages = true;
        if (!isRecognitionInProgress) {
            FTLog.debug(FTLog.HW_RECOGNITION, "Started pending handwriting recognition.");
            startPendingRecognition();
        }
    }

    public void setPageListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onEachPageCompleted(FTNoteshelfPage page);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (languageChangeObserver != null) {
            ObservingService.getInstance().removeObserver("languageChange", languageChangeObserver);
        }
    }
}