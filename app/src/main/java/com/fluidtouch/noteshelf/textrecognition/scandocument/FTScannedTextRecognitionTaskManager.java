package com.fluidtouch.noteshelf.textrecognition.scandocument;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskManager;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;

public class FTScannedTextRecognitionTaskManager extends FTBackgroundTaskManager {
    private static FTScannedTextRecognitionTaskManager visionRecognitionTaskManager;

    private FTScannedTextRecognitionTaskManager() {
        super(FTApp.getInstance().getCurActCtx());
    }

    public static FTScannedTextRecognitionTaskManager getInstance() {
        if (visionRecognitionTaskManager == null)
            visionRecognitionTaskManager = new FTScannedTextRecognitionTaskManager();
        return visionRecognitionTaskManager;
    }

    @Override
    public String dispatchQueueID() {
        return "com.fluidtouch.visionTextRecognition";
    }

    @Override
    public FTBackgroundTaskProtocols.FTBackgroundTaskProcessor getTaskProcessor(Context context) {
        return new FTScannedTextRecognitionTaskProcessor();
    }

    @Override
    public boolean canExecuteTask(FTBackgroundTaskProtocols.FTBackgroundTask task) {
        if (task instanceof FTScannedTextRecognitionTask) {
            return ((FTScannedTextRecognitionTask) task).currentDocument != null;
        }
        return false;
    }
}