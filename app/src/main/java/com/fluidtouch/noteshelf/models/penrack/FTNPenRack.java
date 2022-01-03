package com.fluidtouch.noteshelf.models.penrack;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FTNPenRack {
    private static FTNPenRack mInstance;
    private NSDictionary rootDictionary;

    private final String CURRENT_COLORS = "currentColors";
    private final String DEFAULT_COLORS = "defaultColors";
    private final String penRackPlistPath = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/FTPenRack_v1.plist";

    public static synchronized FTNPenRack getInstance() {
        if (mInstance == null) {
            mInstance = new FTNPenRack();
        }

        return mInstance;
    }

    private FTNPenRack() {
        this.copyMetadataIfNeeded(FTApp.getInstance().getApplicationContext());

        File plist = new File(penRackPlistPath);
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            this.rootDictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Log.i(FTNPenRack.class.getName(), e.getMessage());
        }
    }

    public HashMap<String, Object> getPenRackData() {
        return (HashMap<String, Object>) rootDictionary.toJavaObject();
    }

    public HashMap<String, Object> getDefaultRack(String rackName) {
        return (HashMap<String, Object>) getPenRackData().get(rackName);
    }

    public void updateColors(String forRackType, List<String> newColors) {
        NSDictionary penRack = (NSDictionary) rootDictionary.objectForKey(forRackType);
        penRack.remove(CURRENT_COLORS);
        NSArray array = new NSArray(newColors.size());
        for (int i = 0; i < newColors.size(); i++) {
            String color = newColors.get(i);
            array.setValue(i, color.contains("#") ? color.split("#") : color);
        }
        penRack.put(CURRENT_COLORS, array);
        savePlistWithChanges();
    }

    public void resetColors(String forRackType) {
        NSDictionary penRack = (NSDictionary) rootDictionary.objectForKey(forRackType);
        penRack.remove(CURRENT_COLORS);
        NSArray defaultColors = (NSArray) penRack.get(DEFAULT_COLORS);
        penRack.put(CURRENT_COLORS, defaultColors);
        savePlistWithChanges();
    }

    private void savePlistWithChanges() {
        try {
            File file = new File(penRackPlistPath);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(rootDictionary.toXMLPropertyList().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyMetadataIfNeeded(Context context) {
        File plistFile = new File(penRackPlistPath);
        if (plistFile.exists()) {
            return;
        }
        plistFile.getParentFile().mkdirs();

        AssetManager assetmanager = context.getAssets();
        try {
            InputStream bundleInputStrem = assetmanager.open("FTPenRack_v1.plist");
            File plist = FTFileManagerUtil.createFileFromInputStream(bundleInputStrem, FTConstants.DOCUMENTS_ROOT_PATH + "/Library/FTPenRack_v1.plist");
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
        } catch (Exception e) {
            Log.i(FTNPenRack.class.getName(), e.getMessage());
        }
    }

    public List<String> getStringList(Object[] objects) {
        List<String> strings = new ArrayList<>();
        if (objects != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                strings = Arrays.stream(objects).map(Objects::toString).collect(Collectors.toList());
                for (Object object : objects) {
                    String color = (String) object;
                }
            } else {
                for (Object object : objects) {
                    String color = (String) object;
                    strings.add(color);
                }
            }
        }
        return strings;
    }
}
