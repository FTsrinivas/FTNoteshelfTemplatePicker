package com.fluidtouch.noteshelf.textrecognition.scandocument;

import android.graphics.Bitmap;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;
import com.noteshelf.vision.AppVisionTextRecognizer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FTScannedTextRecognitionTaskProcessor implements FTBackgroundTaskProtocols.FTBackgroundTaskProcessor {

    private boolean canAcceptNewTask = true;
    private AppVisionTextRecognizer textRecognizer;

    @Override
    public boolean canAcceptNewTask() {
        return this.canAcceptNewTask;
    }

    @Override
    public void startTask(FTBackgroundTaskProtocols.FTBackgroundTask task, FTBackgroundTaskProtocols.OnCompletion onCompletion) {
        FTLog.debug(FTLog.VISION_RECOGNITION, "Started background recognition task");
        FTScannedTextRecognitionTask currentTask = (FTScannedTextRecognitionTask) task;
        this.canAcceptNewTask = false;
        recognizeText(currentTask.imageToProcess, (result, error) -> {
            FTLog.debug(FTLog.VISION_RECOGNITION, "Completed background recognition task");
            this.canAcceptNewTask = true;
            onCompletion.didFinish();
            currentTask.onCompletion(result, error);
        });
    }

    private void recognizeText(Bitmap bitmap, RecognitionCompletionBlock listener) {
        if (bitmap == null) {
            listener.onRecognitionCompleted(null, new Error("Page thumbnail null"));
            return;
        }

        FTScannedTextRecognitionResult result = new FTScannedTextRecognitionResult();
        this.textRecognizer = new AppVisionTextRecognizer();
        this.textRecognizer.setTextRecognizerListener(new AppVisionTextRecognizer.TextRecognizerListener() {
            @Override
            public void OnFailed(@NotNull Exception exception) {
                listener.onRecognitionCompleted(result, new Error(exception.getMessage()));
            }

            @Override
            public void OnTextRecognized(@NotNull ArrayList<NSValue> characterRects, @NotNull String recognisedString) {
                result.characterRects = characterRects;
                result.recognisedString = recognisedString;
                result.lastUpdated = FTDeviceUtils.getTimeStamp();
                listener.onRecognitionCompleted(result, null);
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.textRecognizer.closeRecognizer();
    }

    private interface RecognitionCompletionBlock {
        void onRecognitionCompleted(FTScannedTextRecognitionResult result, Error error);
    }
}
