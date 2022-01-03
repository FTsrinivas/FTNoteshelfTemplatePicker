package com.fluidtouch.noteshelf.textrecognition.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;

import java.util.List;

public class FTImageRecognitionHelper {
    private boolean isRecognitionInProgress = false;
    private FTNoteshelfDocument currentDocument;
    private Context context;
    private int pagePtr;
    private int imgPtr;
    private boolean startAgain = false;

    public FTImageRecognitionHelper(Context context, FTNoteshelfDocument document) {
        this.context = context;
        this.currentDocument = document;
    }

    private void startPendingRecognition() {
        if (this.isRecognitionInProgress) {
            return;
        }
        this.isRecognitionInProgress = true;
        AsyncTask.execute(() -> {
            List<FTNoteshelfPage> pages = this.currentDocument.pages(context);
            if (pagePtr >= pages.size()) {
                pagePtr = 0;
                imgPtr = 0;
                isRecognitionInProgress = false;
                if (startAgain) {
                    startPendingRecognition();
                }
                this.startAgain = false;
            } else {
                FTNoteshelfPage eachPage = pages.get(pagePtr);
                if (eachPage != null) {
                    List<FTImageAnnotation> imageAnnotations = eachPage.getImageAnnotations();
                    if (imgPtr >= imageAnnotations.size()) {
                        imgPtr = 0;
                        if (imageAnnotations.isEmpty()) {
                            pagePtr = 0;
                        } else {
                            pagePtr++;
                        }
                        isRecognitionInProgress = false;
                    } else {
                        FTImageAnnotationV1 eachImageAnnotation = (FTImageAnnotationV1) imageAnnotations.get(imgPtr);
                        if (eachImageAnnotation != null && eachImageAnnotation.canRecognizeImageText()) {
                            Bitmap imageToProcess = eachImageAnnotation.getImage();
                            if (imageToProcess != null) {
                                FTLog.debug(FTLog.IMAGE_RECOGNITION, "Found imageAnnotation for text recognition");
                                long lastUpdatedDate = (long) eachImageAnnotation.modifiedTimeInterval;
                                FTImageRecognitionTask task = new FTImageRecognitionTask();
                                task.imageToProcess = imageToProcess;
                                task.currentDocument = this.currentDocument;
                                task.imageAnnotationUUID = eachImageAnnotation.uuid;
                                task.pageUUID = eachImageAnnotation.associatedPage.uuid;
                                task.setListener((result, error) -> {
                                    FTLog.debug(FTLog.IMAGE_RECOGNITION, "Completed recognition for an imageAnnotation");
                                    isRecognitionInProgress = false;
                                    if (result == null || error != null) {
                                        //FTLog.error(FTLog.IMAGE_RECOGNITION, "Engine error on  completion while page recognition: " + error);
                                    } else {
                                        eachImageAnnotation.setImageTextRecognitionInfo(result);
                                        eachImageAnnotation.processForText = false;
                                        if (lastUpdatedDate != 0) {
                                            result.lastUpdated = lastUpdatedDate;
                                        }
                                    }
                                    startPendingRecognition();
                                });
                                FTImageRecognitionTaskManager.getInstance().addBackgroundTask(task);
                            }
                        } else {
                            imgPtr++;
                            isRecognitionInProgress = false;
                            startPendingRecognition();
                        }
                    }
                }
            }
        });
    }

    public void wakeUpRecognitionHelperIfNeeded() {
        this.startAgain = true;
        if (!isRecognitionInProgress) {
            FTLog.debug(FTLog.IMAGE_RECOGNITION, "Started pending image annotation text recognition");
            startPendingRecognition();
        }
    }
}