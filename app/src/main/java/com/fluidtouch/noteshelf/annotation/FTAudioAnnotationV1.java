package com.fluidtouch.noteshelf.annotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fluidtouch.noteshelf.audio.FTAudioPlistItem;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;
import com.fluidtouch.renderingengine.utils.FTGLUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by Sreenu on 13/05/20
 */
public class FTAudioAnnotationV1 extends FTAudioAnnotation implements FTAnnotationV1.FTAnnotationProtocol {
    public FTNoteshelfPage associatedPage;
    private FTAudioRecording audioRecording;

    public FTAudioAnnotationV1(Context inContext, FTNoteshelfPage associatedPage) {
        super(inContext);
        this.associatedPage = associatedPage;
    }

    public List<FTAudioRecording> getAudios() {
        List<FTAudioRecording> recordings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            recordings.add(getAudioRecording());
        }
        return recordings;
    }

    public FTAudioRecording getAudioRecording() {
        FTAudioPlistItem audioPlistItem = audioFileItemPlist();
        if (audioRecording == null && audioPlistItem != null) {
            audioRecording = audioPlistItem.getRecording(getContext());
        }

        return audioRecording;
    }

    public void setAudioRecording(FTAudioRecording audioRecording) {
        this.audioRecording = audioRecording;
        this.audioRecording.setFileName(super.audioPlistFileName());
        this.forceRender = true;

        if (null != this.associatedPage) {
            FTNoteshelfDocument document = this.associatedPage.getParentDocument();
            FTAudioPlistItem audioPlistItem = this.audioFileItemPlist();
            if (null == audioPlistItem) {
                audioPlistItem = new FTAudioPlistItem(this.audioPlistFileName(), false);
                document.resourceFolderItem().addChildItem(audioPlistItem);
            }
            audioPlistItem.setAudioRecording(this.audioRecording);
            audioPlistItem.saveContentsOfFileItem(context);
        }
    }

    private FTAudioPlistItem audioFileItemPlist() {
        FTNoteshelfDocument document = this.associatedPage.getParentDocument();
        return (FTAudioPlistItem) document.resourceFolderItem().childFileItemWithName(this.audioPlistFileName());
    }

    //Need to create for every annotation
    public int textureToRender(float scale, EGLContext context, boolean isForOffline) {
        synchronized (this) {
            this.currentScale = scale;
            if (0 == glTexture || forceRender) {
                forceRender = false;
                Bitmap imageToConsider = this.imageToCreateTexture(scale);
                FTGLUtils.deleteGLTexture(glTexture, false,"Audio annotation");
                glTexture = FTGLUtils.textureFromBitmap(imageToConsider,"Audio annotation");
                eglContext = context;
            }
            return glTexture;
        }
    }

    public void delete() {
        if (this.audioFileItemPlist() != null)
            this.audioFileItemPlist().deleteContent();
    }

    private Bitmap imageToCreateTexture(float scale) {
        return BitmapFactory.decodeResource(getContext().getResources(), R.drawable.recording_2);
    }

    @Override
    public void unloadContents() {
        synchronized (this) {
            audioRecording = null;
            this.audioFileItemPlist().unloadContentsOfFileItem();
            super.unloadContents();
        }
    }

    @Override
    public FTAudioAnnotation deepCopyAnnotation(FTNoteshelfPage page) {
        FTAudioAnnotationV1 annotation = new FTAudioAnnotationV1(this.getContext(), page);
        annotation.uuid = FTDocumentUtils.getUDID();
        annotation.setBoundingRect(this.getBoundingRect());
        annotation.screenScale = this.screenScale;
        annotation.transformScale = this.transformScale;
        annotation.createdTimeInterval = FTDeviceUtils.getTimeStamp();
        annotation.modifiedTimeInterval = FTDeviceUtils.getTimeStamp();
        annotation.version = this.version;
        annotation.setAudioRecording(this.getAudioRecording().deepCopy());

        FTAudioRecording sourceRecording = this.getAudioRecording();

        //copying all the audio tracks
        for (int i = 0; i < annotation.getAudioRecording().getAudioTracks().size(); i++) {
            String srcBasePath = this.associatedPage.getParentDocument().resourceFolderItem().getFileItemURL().getPath() + "/";
            String dstBasePath = page.getParentDocument().resourceFolderItem().getFileItemURL().getPath() + "/";
            File sourceFile = new File(srcBasePath + sourceRecording.getAudioTracks().get(i).audioName);
            File destFile = new File(dstBasePath + annotation.getAudioRecording().getAudioTracks().get(i).audioName);

            try {
                FTDocumentUtils.copyFile(sourceFile, destFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return annotation;
    }
}
