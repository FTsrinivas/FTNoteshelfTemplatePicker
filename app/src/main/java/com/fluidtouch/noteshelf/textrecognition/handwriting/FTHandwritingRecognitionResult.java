package com.fluidtouch.noteshelf.textrecognition.handwriting;

import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.textrecognition.helpers.NSDataValueConverter;
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue;

import java.util.ArrayList;

public class FTHandwritingRecognitionResult {
    public ArrayList<NSValue> characterRects = new ArrayList<>();
    public String recognisedString = "";
    public String languageCode = "";
    public long lastUpdated = FTDeviceUtils.getTimeStamp();

    public FTHandwritingRecognitionResult() {
        super();
    }

    public FTHandwritingRecognitionResult(NSDictionary dict) {
        super();
        if (dict.get("recognisedText") != null) {
            String fullString = dict.objectForKey("recognisedText").toString();
            this.recognisedString = fullString;
        }

        if (dict.get("characterRects") != null) {
            NSData characterRectsData = (NSData) dict.get("characterRects");
            ArrayList<NSValue> characterRectValues = NSDataValueConverter.rectValuesArrayFromData(characterRectsData);
            this.characterRects = characterRectValues;
        }
        if (dict.get("lastUpdated") != null) {
            this.lastUpdated = Long.valueOf(dict.objectForKey("lastUpdated").toJavaObject().toString());
        }
        if (dict.get("language") != null) {
            String languageCode = dict.objectForKey("language").toString();
            this.languageCode = languageCode;
        }
    }

    private NSData characterRectData() {
        NSData rectData = NSDataValueConverter.dataWithRectValuesArray(characterRects);
        return rectData;
    }

    public NSDictionary dictionaryRepresentation() {
        NSDictionary dictRep = new NSDictionary();
        dictRep.put("recognisedText", this.recognisedString);
        dictRep.put("characterRects", this.characterRectData());
        dictRep.put("lastUpdated", this.lastUpdated);
        dictRep.put("language", this.languageCode);
        return dictRep;
    }
}
