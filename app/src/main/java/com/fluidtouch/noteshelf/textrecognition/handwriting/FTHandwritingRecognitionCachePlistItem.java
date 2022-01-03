package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.content.Context;

import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;

public class FTHandwritingRecognitionCachePlistItem extends FTFileItemPlist {
    String currentVersion = "1.0";
    private FTHandwritingRecognitionResult recognitionResult;

    public FTHandwritingRecognitionCachePlistItem(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTHandwritingRecognitionCachePlistItem(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public FTHandwritingRecognitionResult getRecognitionInfo(Context context, FTNoteshelfPage page) {
        NSDictionary recogInfoDict = (NSDictionary) contentDictionary(context).objectForKey("pageRecognitionInfo");
        FTHandwritingRecognitionResult recognitionInfo = null;
        if (recogInfoDict != null) {
            NSDictionary pageDict = (NSDictionary) recogInfoDict.objectForKey(page.uuid);
            if (pageDict != null) {
                recognitionInfo = new FTHandwritingRecognitionResult(pageDict);
            }
        } else {
            this.setObject(context, "1.0", "version");
        }
        return recognitionInfo;
    }

    public void setRecognitionInfo(Context context, String pageID, FTHandwritingRecognitionResult recognitionInfo) {
        NSDictionary content = contentDictionary(context);
        if (null == content) {
            content = new NSDictionary();
            this.setObject(context, "1.0", "version");
        }
        NSDictionary recogInfoDict = (NSDictionary) content.objectForKey("pageRecognitionInfo");
        if (recogInfoDict == null) {
            recogInfoDict = new NSDictionary();
        }
        if (recognitionInfo != null) {
            recogInfoDict.put(pageID, recognitionInfo.dictionaryRepresentation());
            this.setObject(context, recogInfoDict, "pageRecognitionInfo");
        }
    }

    public void deleteRecognitionInfo(Context context, String pageID) {
        NSDictionary recogInfoDict = (NSDictionary) contentDictionary(context).objectForKey("pageRecognitionInfo");
        if (recogInfoDict != null) {
            recogInfoDict.remove(pageID);
            this.setObject(context, recogInfoDict, "pageRecognitionInfo");
        }
    }
}