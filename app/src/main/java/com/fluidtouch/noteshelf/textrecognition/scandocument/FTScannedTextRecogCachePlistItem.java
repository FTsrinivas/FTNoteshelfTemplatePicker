package com.fluidtouch.noteshelf.textrecognition.scandocument;

import android.content.Context;

import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;

public class FTScannedTextRecogCachePlistItem extends FTFileItemPlist {
    private FTScannedTextRecognitionResult recognitionResult;

    public FTScannedTextRecogCachePlistItem(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTScannedTextRecogCachePlistItem(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    private String uniqueKeyForPage(FTNoteshelfPage page) {
        return page.associatedPDFFileName.split(".ns_pdf")[0] + "_" + page.associatedPageIndex + ".ns_pdf";
    }

    public FTScannedTextRecognitionResult getRecognitionInfo(Context context, FTNoteshelfPage page) {
        NSDictionary recogInfoDict = (NSDictionary) contentDictionary(context).objectForKey("pageRecognitionInfo");
        FTScannedTextRecognitionResult recognitionInfo = null;
        if (recogInfoDict != null) {
            NSDictionary pageDict = (NSDictionary) recogInfoDict.objectForKey(uniqueKeyForPage(page));
            if (pageDict != null) {
                recognitionInfo = new FTScannedTextRecognitionResult(pageDict);
            }
        } else {
            this.setObject(context, "1.0", "version");
        }
        return recognitionInfo;
    }

    public void setRecognitionInfo(Context context, FTScannedTextRecognitionResult recognitionInfo, FTNoteshelfPage page) {
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
            recogInfoDict.put(uniqueKeyForPage(page), recognitionInfo.dictionaryRepresentation());
            this.setObject(context, recogInfoDict, "pageRecognitionInfo");
        }
    }
}