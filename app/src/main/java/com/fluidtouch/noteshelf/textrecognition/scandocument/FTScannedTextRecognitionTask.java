package com.fluidtouch.noteshelf.textrecognition.scandocument;

import android.graphics.Bitmap;
import android.util.SizeF;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;

public class FTScannedTextRecognitionTask implements FTBackgroundTaskProtocols.FTBackgroundTask {
    public FTNoteshelfDocument currentDocument;
    public String languageCode;
    public String pageUUID;
    public SizeF viewSize;
    public Bitmap imageToProcess;

    private VisionRecognitionTaskCallback listener;

    @Override
    public void onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus status) {

    }

    public void onCompletion(FTScannedTextRecognitionResult result, Error error) {
        if (listener != null)
            listener.onCompletion(result, error);
    }

    public void setListener(FTScannedTextRecognitionTask.VisionRecognitionTaskCallback listener) {
        this.listener = listener;
    }

    public interface VisionRecognitionTaskCallback {
        void onCompletion(FTScannedTextRecognitionResult result, Error error);
    }
}