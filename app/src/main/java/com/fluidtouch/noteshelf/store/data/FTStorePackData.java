package com.fluidtouch.noteshelf.store.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static com.fluidtouch.noteshelf.models.theme.FTNThemeCategory.getThemesPlist;

public class FTStorePackData {

    HashMap<String, Object> mStorePackData = new HashMap<>();

    public FTStorePackData(Context context) {
        super();
        this.copyMetadataIfNeeded(context);

        File plist = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/" + getThemesPlist());
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            mStorePackData = (HashMap<String, Object>) PropertyListParser.parse(inputStream).toJavaObject();
        } catch (Exception e) {
            Log.i(FTStorePackData.class.getName(), e.getMessage());
        }

    }

    public HashMap<String, Object> getStorePackData() {
        return mStorePackData;
    }

    private void copyMetadataIfNeeded(Context context) {
        File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/" + getThemesPlist());
        if (plistFile.exists() && BuildConfig.VERSION_CODE == (int) FTDownloadedStorePackData.getInstance(context).getData("appVersion", 0)) {
            return;
        }
        plistFile.getParentFile().mkdirs();

        AssetManager assetmanager = context.getAssets();
        try {
            InputStream bundleInputStrem = assetmanager.open("" + getThemesPlist());
            File plist = FTFileManagerUtil.createFileFromInputStream(bundleInputStrem, FTConstants.DOCUMENTS_ROOT_PATH + "/" + getThemesPlist());
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            FTDownloadedStorePackData.getInstance(context).setData("appVersion", BuildConfig.VERSION_CODE);
        } catch (Exception e) {
            Log.i(FTStorePackData.class.getName(), e.getMessage());
        }
    }
}
