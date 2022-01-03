package com.fluidtouch.noteshelf.textrecognition.annotation;

import android.graphics.Bitmap;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;

public class FTImageRecognitionTask implements FTBackgroundTaskProtocols.FTBackgroundTask {
    public FTNoteshelfDocument currentDocument;
    public String languageCode;
    public String pageUUID;
    public String imageAnnotationUUID;
    public Bitmap imageToProcess;

    private ImageRecognitionTaskCallback listener;

    @Override
    public void onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus status) {

    }

    public void onCompletion(FTImageRecognitionResult result, Error error) {
        if (listener != null)
            listener.onCompletion(result, error);
    }

    public void setListener(ImageRecognitionTaskCallback listener) {
        this.listener = listener;
    }

    public interface ImageRecognitionTaskCallback {
        void onCompletion(FTImageRecognitionResult result, Error error);
    }
}
