package com.fluidtouch.noteshelf.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.document.textedit.FTStyledTextBitmapGenerator;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;
import com.fluidtouch.renderingengine.utils.FTGLUtils;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.google.gson.Gson;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by sreenu on 13/05/20.
 */
public class FTTextAnnotationV1 extends FTTextAnnotation implements FTAnnotationV1.FTAnnotationProtocol, Parcelable {
    public boolean isNew = false;
    private FTStyledText inputText = new FTStyledText();

    public FTTextAnnotationV1(Context inContext) {
        super(inContext);
    }

    protected FTTextAnnotationV1(Parcel in) {
        super(in);
        setJsonText(in.readString());
    }

    public static final Creator<FTTextAnnotationV1> CREATOR = new Creator<FTTextAnnotationV1>() {
        @Override
        public FTTextAnnotationV1 createFromParcel(Parcel in) {
            return new FTTextAnnotationV1(in);
        }

        @Override
        public FTTextAnnotationV1[] newArray(int size) {
            return new FTTextAnnotationV1[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(getJsonText());
    }

    @Override
    public FTTextAnnotation deepCopyAnnotation(FTNoteshelfPage toPage) {
        FTTextAnnotationV1 annotation = new FTTextAnnotationV1(this.getContext());
        annotation.uuid = FTDocumentUtils.getUDID();
        annotation.setBoundingRect(this.getBoundingRect());
        annotation.screenScale = this.screenScale;
        annotation.transformScale = this.transformScale;
        annotation.createdTimeInterval = FTDeviceUtils.getTimeStamp();
        annotation.modifiedTimeInterval = FTDeviceUtils.getTimeStamp();
        annotation.version = this.version;
        annotation.setJsonText(this.getJsonText());
        return annotation;
    }

    public String getNonAttributedString() {
        return inputText.getPlainText();
    }

    private String getJsonText() {
        return new Gson().toJson(inputText);
    }

    private void setJsonText(String jsonText) {
        if (jsonText != null) {
            setInputTextWithInfo(new Gson().fromJson(jsonText, FTStyledText.class));
        }
    }

    public void setInputTextWithInfo(FTStyledText inputText) {
        this.inputText = inputText;
    }

    public FTStyledText getTextInputInfo() {
        return inputText;
    }

    public void setColor(int color) {
        getTextInputInfo().setColor(color);
        forceRender = true;
    }

    public int textureToRender(float scale, EGLContext context, boolean isForOffline) {
        synchronized (this) {
            RectF boundingRect = getBoundingRect();
            if (boundingRect.width() <= 0 || boundingRect.height() <= 0)
                return -1;
            if (scale != this.currentScale) {
                forceRender = true;
            }
            this.currentScale = scale;
            Log.i("offline current", scale + "");
            if (0 == glTexture || forceRender) {
                forceRender = false;
                Bitmap imageToConsider = this.imageToCreateTexture(scale);
                if (imageToConsider == null)
                    return -1;
                FTGLUtils.deleteGLTexture(glTexture, false,"Text annotation");
                glTexture = FTGLUtils.textureFromBitmap(imageToConsider, "Text annotation");
                eglContext = context;
            }
            return glTexture;
        }
    }

    private Bitmap imageToCreateTexture(float scale) {
        RectF boudningRect = FTGeometryUtils.scaleRect(getBoundingRect(), scale);
        return new FTStyledTextBitmapGenerator().getBitmap(getContext(), boudningRect.width(), boudningRect.height(), this.getTextInputInfo(), currentScale);
    }
}
