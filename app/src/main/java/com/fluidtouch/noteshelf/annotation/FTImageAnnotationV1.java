package com.fluidtouch.noteshelf.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemImage;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionCachePlist;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionResult;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.utils.FTGLUtils;

import java.io.File;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by sreenu on 13/05/20.
 */
public class FTImageAnnotationV1 extends FTImageAnnotation implements FTAnnotationV1.FTAnnotationProtocol, Parcelable {
    public FTNoteshelfPage associatedPage;
    private int imageLockStatus;

    public int getImageLockStatus() {
        return imageLockStatus;
    }

    public void setImageLockStatus(int imageLockStatus) {
        this.imageLockStatus = imageLockStatus;
    }

    public FTImageAnnotationV1(Context inContext, FTNoteshelfPage page) {
        super(inContext);
        this.associatedPage = page;
    }

    FTImageAnnotationV1(Parcel in) {
        super(in);
    }

    public static final Creator<FTImageAnnotationV1> CREATOR = new Creator<FTImageAnnotationV1>() {
        @Override
        public FTImageAnnotationV1 createFromParcel(Parcel in) {
            return new FTImageAnnotationV1(in);
        }

        @Override
        public FTImageAnnotationV1[] newArray(int size) {
            return new FTImageAnnotationV1[size];
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
    public void setImage(Bitmap bitmap) {
        super.setImage(bitmap);

        if (null != this.associatedPage) {
            FTNoteshelfDocument document = this.associatedPage.getParentDocument();
            FTFileItemImage imageFileItem = this.imageContentFileItem();
            if (null == imageFileItem) {
                imageFileItem = new FTFileItemImage(this.imageContentFileName(), false);
                document.resourceFolderItem().addChildItem(imageFileItem);
            }
            imageFileItem.setImage(bitmap);
            imageFileItem.saveContentsOfFileItem(context);
        }
    }

    @Override
    public Bitmap getImage() {
        if (null == super.image) {
            FTFileItemImage ftFileItemImage = this.imageContentFileItem();
            if (ftFileItemImage == null) {
                FTLog.logCrashException(new Exception("Image file item missing"));
                return null;
            }
            super.image = ftFileItemImage.image(getContext());
        }
        return super.getImage();
    }

    public void delete() {
        if (this.imageContentFileItem() != null)
            this.imageContentFileItem().deleteContent();
    }

    private FTFileItemImage imageContentFileItem() {
        FTNoteshelfDocument document = this.associatedPage.getParentDocument();
        FTFileItemImage imageFileItem = (FTFileItemImage) document.resourceFolderItem().childFileItemWithName(this.imageContentFileName());
        return imageFileItem;
    }

    @Override
    public void unloadContents() {
        synchronized (this) {
            super.image = null;
            try {
                if (imageContentFileItem() != null)
                    this.imageContentFileItem().unloadContentsOfFileItem();
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.unloadContents();
        }
    }

    @Override
    public FTImageAnnotation deepCopyAnnotation(FTNoteshelfPage toPage) {
        FTImageAnnotationV1 annotation = new FTImageAnnotationV1(this.getContext(), toPage);
        annotation.uuid = FTDocumentUtils.getUDID();
        annotation.setBoundingRect(this.getBoundingRect());
        annotation.version = this.version;
        annotation.setImage(this.getImage());

        FTFileItemImage sourceFileItem = this.imageContentFileItem();

        if (null == sourceFileItem) {
            return null;
        }

        FTNoteshelfDocument document = toPage.getParentDocument();
        FTFileItemImage copiedFileItem = new FTFileItemImage(annotation.imageContentFileName(), false);
        document.resourceFolderItem().addChildItem(copiedFileItem);
        try {
            FTDocumentUtils.copyFile(new File(sourceFileItem.getFileItemURL().getPath()), new File(copiedFileItem.getFileItemURL().getPath()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return annotation;
    }

    public FTImageRecognitionResult getImageTextRecognitionInfo() {
        FTNoteshelfDocument recognitionDocument = this.associatedPage.getParentDocument();
        FTImageRecognitionCachePlist fileItem = recognitionDocument.recognitionCache(getContext()).imageRecognitionCachePlist();
        if (fileItem != null) {
            return fileItem.getRecognitionInfo(getContext(), this);
        }
        return null;
    }

        public void setImageTextRecognitionInfo(FTImageRecognitionResult recognitionInfo) {
        FTNoteshelfDocument recognitionDocument = this.associatedPage.getParentDocument();
        FTImageRecognitionCachePlist fileItem = recognitionDocument.recognitionCache(getContext()).imageRecognitionCachePlist();
        fileItem.setRecognitionInfo(getContext(), recognitionInfo, this);
        recognitionDocument.recognitionCache(getContext()).saveRecognitionInfoToDisk();
    }

    public boolean canRecognizeImageText() {
        boolean canRecognize = false;
        FTImageRecognitionResult recognitionInfo = getImageTextRecognitionInfo();
        if (null == recognitionInfo) {
            canRecognize = true;
        } else if (processForText) {
            canRecognize = true;
        } else {
            if (recognitionInfo.lastUpdated < super.modifiedTimeInterval) {
                canRecognize = true;
            }
        }
        return canRecognize;
    }

    public FTUrl getImageUrl() {
        return this.imageContentFileItem().getFileItemURL();
    }

    public Bitmap getRotatedImage() {
        if (null == image || image.isRecycled()) {
            FTFileItemImage ftFileItemImage = this.imageContentFileItem();
            if (ftFileItemImage == null) {
                FTLog.logCrashException(new Exception("Image file item missing"));
                return null;
            }
            image = this.imageContentFileItem().image(getContext());
        }
        Bitmap rotated = null;
        //rotated = Bitmap.createScaledBitmap(image, (int) image.getWidth(), (int) image.getHeight(), true);
        if (image != null && image.getWidth() > 0 && image.getHeight() > 0)
            rotated = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), getTransMatrix(), true);

        return rotated == null ? image : rotated;
    }

    private Bitmap imageToCreateTexture(float scale) {
        return this.getRotatedImage();
    }

    public synchronized int textureToRender(float scale, EGLContext context, boolean isForOffline) {
        scale = this.currentScale = scale;
        if (0 == glTexture || forceRender) {
            forceRender = false;
            Bitmap imageToConsider = this.imageToCreateTexture(scale);
            if (imageToConsider == null)
                return -1;
            FTGLUtils.logInfo("PreviousTextureId: " + glTexture + "");
            FTGLUtils.deleteGLTexture(glTexture, false,"Image annotation");
            glTexture = FTGLUtils.textureFromBitmap(imageToConsider, "Image annotation");
            eglContext = context;
        }
        return glTexture;
    }
}
