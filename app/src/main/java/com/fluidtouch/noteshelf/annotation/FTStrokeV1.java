package com.fluidtouch.noteshelf.annotation;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTSegment;
import com.fluidtouch.renderingengine.annotation.FTStroke;

/**
 * Created by sreenu on 13/05/20.
 */
public class FTStrokeV1 extends FTStroke implements FTAnnotationV1.FTAnnotationProtocol, Parcelable {
    public FTStrokeV1(Context context) {
        super(context);
    }

    protected FTStrokeV1(Parcel in) {
        super(in);
    }

    public static final Creator<FTStrokeV1> CREATOR = new Creator<FTStrokeV1>() {
        @Override
        public FTStrokeV1 createFromParcel(Parcel in) {
            return new FTStrokeV1(in);
        }

        @Override
        public FTStrokeV1[] newArray(int size) {
            return new FTStrokeV1[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public FTStroke deepCopyAnnotation(FTNoteshelfPage toPage) {
        FTStrokeV1 annotation = new FTStrokeV1(this.getContext());
        annotation.uuid = FTDocumentUtils.getUDID();
        annotation.strokeColor = this.strokeColor;
        annotation.strokeWidth = this.strokeWidth;
        annotation.penType = this.penType;
        annotation.hasErasedSegments = this.hasErasedSegments;
        annotation.setBoundingRect(this.getBoundingRect());
        annotation.version = defaultAnnotationVersion();
        annotation.segmentCount = this.segmentCount;
        annotation.setSegmentData(this.segmentData());
//        onCompletion.didFinishWithAnnotation(annotation);
        return annotation;
    }

    public void addSegment(FTSegment newSegment) {
        this.segmentsArray.add(newSegment);
        ++this.segmentCount;
    }

    public float[] getXPoints() {
        if (segmentsArray != null && segmentsArray.size() > 0) {
            int size = segmentsArray.size();
            float[] ptx = new float[size + 1];
            for (int i = 0; i < size; i++) {
                ptx[i] = segmentsArray.get(i).startPoint.x;
                if (i == segmentsArray.size() - 1) {
                    ptx[i + 1] = segmentsArray.get(i).endPoint.x;
                }
            }
            return ptx;
        }
        return null;
    }

    public float[] getYPoints() {
        if (segmentsArray != null && segmentsArray.size() > 0) {
            int size = segmentsArray.size();
            float[] pty = new float[size + 1];
            for (int i = 0; i < size; i++) {
                pty[i] = segmentsArray.get(i).startPoint.y;
                if (i == segmentsArray.size() - 1) {
                    pty[i + 1] = segmentsArray.get(i).endPoint.y;
                }
            }
            return pty;
        }
        return null;
    }
}
