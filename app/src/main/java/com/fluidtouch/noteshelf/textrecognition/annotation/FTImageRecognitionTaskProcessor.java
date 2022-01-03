package com.fluidtouch.noteshelf.textrecognition.annotation;

import android.graphics.Bitmap;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;
import com.noteshelf.vision.AppVisionTextRecognizer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FTImageRecognitionTaskProcessor implements FTBackgroundTaskProtocols.FTBackgroundTaskProcessor {
    private static FTImageRecognitionTaskProcessor imageTextRecognitionProcessor = new FTImageRecognitionTaskProcessor();
    private AppVisionTextRecognizer textRecognizer;
    private boolean canAcceptNewTask = true;

    public static FTImageRecognitionTaskProcessor getInstance() {
        return imageTextRecognitionProcessor;
    }

    @Override
    public void startTask(FTBackgroundTaskProtocols.FTBackgroundTask task, FTBackgroundTaskProtocols.OnCompletion onCompletion) {
        FTImageRecognitionTask currentTask = (FTImageRecognitionTask) task;
        this.canAcceptNewTask = false;
        FTLog.debug(FTLog.IMAGE_RECOGNITION, "Processing image for text");
        recognizeText(currentTask.imageToProcess, (result, error) -> {
            FTLog.debug(FTLog.IMAGE_RECOGNITION, "Done processing with image for text");
            this.canAcceptNewTask = true;
            onCompletion.didFinish();
            currentTask.onCompletion(result, error);
        });
    }

    private synchronized void recognizeText(Bitmap bitmap, ImageTextRecognitionCompletionBlock listener) {
        if (bitmap == null) {
            listener.onRecognitionComplete(null, new Error("Page thumbnail null"));
            return;
        }
        FTImageRecognitionResult result = new FTImageRecognitionResult();
        this.textRecognizer = new AppVisionTextRecognizer();
        this.textRecognizer.setTextRecognizerListener(new AppVisionTextRecognizer.TextRecognizerListener() {
            @Override
            public void OnFailed(@NotNull Exception exception) {
                listener.onRecognitionComplete(result, new Error(exception.getMessage()));
            }

            @Override
            public void OnTextRecognized(@NotNull ArrayList<NSValue> characterRects, @NotNull String recognisedString) {
                result.characterRects = characterRects;
                result.recognisedString = recognisedString;
                result.lastUpdated = FTDeviceUtils.getTimeStamp();
                listener.onRecognitionComplete(result, null);
            }
        });
        this.textRecognizer.getTextData(bitmap);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.textRecognizer.closeRecognizer();
    }

    @Override
    public boolean canAcceptNewTask() {
        return this.canAcceptNewTask;
    }

    private interface ImageTextRecognitionCompletionBlock {
        void onRecognitionComplete(FTImageRecognitionResult result, Error error);
    }
}