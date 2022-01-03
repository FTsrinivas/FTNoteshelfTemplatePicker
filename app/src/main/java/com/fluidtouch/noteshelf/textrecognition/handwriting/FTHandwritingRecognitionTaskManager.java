package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskManager;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;

import java.util.Observer;

public class FTHandwritingRecognitionTaskManager extends FTBackgroundTaskManager {
    private static FTHandwritingRecognitionTaskManager recognitionTaskManager;

    private Observer languageChangeObserver;

    private FTHandwritingRecognitionTaskManager() {
        super(FTApp.getInstance().getCurActCtx());
        languageChangeObserver = (o, arg) -> handleLanguageChange();
        ObservingService.getInstance().addObserver("languageChange", languageChangeObserver);
    }

    public static FTHandwritingRecognitionTaskManager getInstance() {
        if (recognitionTaskManager == null)
            recognitionTaskManager = new FTHandwritingRecognitionTaskManager();
        return recognitionTaskManager;
    }

    private void handleLanguageChange() {
        synchronized (this) {
            super.taskList.clear();
        }
    }

    @Override
    public String dispatchQueueID() {
        return "com.fluidtouch.handWritingRecognition";
    }

    @Override
    public FTBackgroundTaskProtocols.FTBackgroundTaskProcessor getTaskProcessor(Context context) {
        return new FTHandwritingRecognitionTaskProcessor(context, FTLanguageResourceManager.getInstance().getCurrentLanguageCode());
    }

    @Override
    public boolean canExecuteTask(FTBackgroundTaskProtocols.FTBackgroundTask task) {
        if (task instanceof FTHandwritingRecognitionTask) {
            return ((FTHandwritingRecognitionTask) task).currentDocument != null;
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ObservingService.getInstance().removeObserver("languageChange", languageChangeObserver);
    }
}