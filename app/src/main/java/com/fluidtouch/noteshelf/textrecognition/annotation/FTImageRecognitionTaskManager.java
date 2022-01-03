package com.fluidtouch.noteshelf.textrecognition.annotation;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskManager;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;

public class FTImageRecognitionTaskManager extends FTBackgroundTaskManager {
    private static FTImageRecognitionTaskManager imageRecognitionTaskManager;

    private FTImageRecognitionTaskManager() {
        super(FTApp.getInstance().getCurActCtx());
    }

    public static FTImageRecognitionTaskManager getInstance() {
        if (imageRecognitionTaskManager == null)
            imageRecognitionTaskManager = new FTImageRecognitionTaskManager();
        return imageRecognitionTaskManager;
    }

    @Override
    public String dispatchQueueID() {
        return "com.fluidtouch.visionTextRecognition";
    }

    @Override
    public FTBackgroundTaskProtocols.FTBackgroundTaskProcessor getTaskProcessor(Context context) {
        return new FTImageRecognitionTaskProcessor();
    }

    @Override
    public boolean canExecuteTask(FTBackgroundTaskProtocols.FTBackgroundTask task) {
        if (task instanceof FTImageRecognitionTask) {
            return ((FTImageRecognitionTask) task).currentDocument != null;
        }
        return false;
    }
}
