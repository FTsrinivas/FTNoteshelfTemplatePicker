package com.fluidtouch.noteshelf.textrecognition.annotation;

import android.content.Context;

import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;

public class FTImageRecognitionCachePlist extends FTFileItemPlist {
    public FTImageRecognitionCachePlist(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTImageRecognitionCachePlist(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public FTImageRecognitionResult getRecognitionInfo(Context context, FTImageAnnotation imageAnnotation) {
        NSDictionary recogInfoDict = (NSDictionary) contentDictionary(context).objectForKey("imageRecognitionInfo");
        FTImageRecognitionResult recognitionInfo = null;
        if (recogInfoDict != null) {
            NSDictionary pageDict = (NSDictionary) recogInfoDict.objectForKey(imageAnnotation.uuid);
            if (pageDict != null) {
                recognitionInfo = new FTImageRecognitionResult(pageDict);
            }
        } else {
            this.setObject(context, "1.0", "version");
        }
        return recognitionInfo;
    }

    public void setRecognitionInfo(Context context, FTImageRecognitionResult recognitionInfo, FTImageAnnotation imageAnnotation) {
        NSDictionary content = contentDictionary(context);
        if (null == content) {
            content = new NSDictionary();
            this.setObject(context, "1.0", "version");
        }
        NSDictionary recogInfoDict = (NSDictionary) content.objectForKey("imageRecognitionInfo");
        if (recogInfoDict == null) {
            recogInfoDict = new NSDictionary();
        }
        if (recognitionInfo != null) {
            recogInfoDict.put(imageAnnotation.uuid, recognitionInfo.dictionaryRepresentation());
            this.setObject(context, recogInfoDict, "imageRecognitionInfo");
        }
    }
}