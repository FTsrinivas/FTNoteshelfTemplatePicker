package com.fluidtouch.noteshelf.textrecognition.helpers;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.preferences.SystemPref;

import java.io.File;

public class FTRecognitionUtils {

    public static String configurationPathForLanguage(Context context, String languageCode) {
        String configurationPath = FTRecognitionUtils.recognitionResourcesFolderURL(context).getPath().concat("/recognition-assets-" + languageCode + "/conf");
        File resourceFile = new File(configurationPath);
        if (resourceFile.exists()) {
            return configurationPath;
        }
        return null;
    }

    public static FTUrl recognitionResourcesFolderURL(Context context) {
        String bundlePath = ContextCompat.getDataDir(context).getPath();
        String libraryPath = bundlePath + "/Library";
        String recognitionResourcesPath = libraryPath + "/RecognitionResources";
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            recognitionResourcesPath = recognitionResourcesPath.concat("/samsung");
        }
        File file = new File(recognitionResourcesPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FTUrl recognitionResourcesFolder = new FTUrl(recognitionResourcesPath);
        return recognitionResourcesFolder;
    }
}
