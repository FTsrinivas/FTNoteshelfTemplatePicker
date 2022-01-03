package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.util.SizeF;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols.FTBackgroundTask;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;

import java.util.List;

public class FTHandwritingRecognitionTask implements FTBackgroundTask {
    public FTNoteshelfDocument currentDocument;
    public String languageCode;
    public String pageUUID;
    public List<FTAnnotation> pageAnnotations;
    public SizeF viewSize;

    private RecognitionTaskCallback listener;

    @Override
    public void onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus status) {
    }

    public void onCompletion(FTHandwritingRecognitionResult result, Error error) {
        if (listener != null)
            listener.onCompletion(result, error);
    }

    public void setListener(RecognitionTaskCallback listener) {
        this.listener = listener;
    }

    public interface RecognitionTaskCallback {
        void onCompletion(FTHandwritingRecognitionResult result, Error error);
    }
}