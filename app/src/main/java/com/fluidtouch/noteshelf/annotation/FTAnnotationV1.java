package com.fluidtouch.noteshelf.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;

//
// Created by sreenu on 13/05/20.
//
// Copyright (c) Fluid Touch PVT LTD. All rights reserved.
//
public class FTAnnotationV1 extends FTAnnotation implements Parcelable {
    protected FTAnnotationV1(Parcel in) {
        super(in);
    }

    public FTAnnotationV1(Context inContext) {
        super(inContext);
    }

    public static final Creator<FTAnnotation> CREATOR = new Creator<FTAnnotation>() {
        public FTAnnotation createFromParcel(Parcel in) {
            int type = in.readInt();
            FTAnnotationType annotationType = FTAnnotationType.initWithRawValue(type);
            if (annotationType == FTAnnotationType.text) {
                return new FTTextAnnotationV1(in);
            } else if (annotationType == FTAnnotationType.image) {
                return new FTImageAnnotationV1(in);
            } else {
                return annotationType == FTAnnotationType.stroke ? new FTStrokeV1(in) : new FTAnnotationV1(in);
            }
        }

        public FTAnnotation[] newArray(int size) {
            return new FTAnnotation[size];
        }
    };

    public static FTAnnotation getImageAnnotation(Context context, FTNoteshelfPage page, Bitmap bitmap) {
        FTImageAnnotationV1 ftImageAnnotation = new FTImageAnnotationV1(context, page);
        ftImageAnnotation.setImage(bitmap);
        ftImageAnnotation.isNew = true;
        return ftImageAnnotation;
    }

    public static FTAnnotation getImageAnnotationForClipart(Context context, FTNoteshelfPage page, Bitmap bitmap, float scale) {
        return getImageAnnotation(context, page, bitmap);
    }

    public static FTAnnotation getTextAnnotation(Context context, RectF boundingRect) {
        FTTextAnnotationV1 ftTextAnnotation = new FTTextAnnotationV1(context);
        ftTextAnnotation.setBoundingRect(boundingRect);
        ftTextAnnotation.isNew = true;
        return ftTextAnnotation;
    }

    public static FTAnnotation getAudioAnnotation(Context context, FTNoteshelfPage page, FTAudioRecording audioRecording) {
        FTAudioAnnotationV1 ftAudioAnnotation = new FTAudioAnnotationV1(context, page);
        ftAudioAnnotation.setAudioRecording(audioRecording);
        return ftAudioAnnotation;
    }

    public FTAnnotation deepCopyAnnotation(FTNoteshelfPage page) {
        return new FTAnnotation(context);
    }

    public interface FTAnnotationProtocol {
        FTAnnotation deepCopyAnnotation(FTNoteshelfPage page);
    }
}
