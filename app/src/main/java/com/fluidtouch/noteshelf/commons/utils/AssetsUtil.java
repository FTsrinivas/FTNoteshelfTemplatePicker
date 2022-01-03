package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class AssetsUtil {
    public static final String ASSETS_PATH1 = "/android_asset/";
    public static final String ASSETS_PATH = "file:///android_asset/";

    public static FTUrl getUri(String fromResource, String withExtension) {
        return getUri(fromResource + withExtension);
    }

    public static FTUrl getUri(String fromResouceWithExtension) {
        return FTUrl.fromFile(new File(ASSETS_PATH + fromResouceWithExtension));
    }

    public static boolean isAssetExists(String filePath) {
        String filename = StringUtil.getFileName(filePath);
        AssetManager assetManager = FTApp.getInstance().getCurActCtx().getAssets();
        try {
            return Arrays.asList(assetManager.list(filePath.split("/" + filename)[0])).contains(filename);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAssetFileExits(Context context, String assetPath) {
        AssetManager mg = context.getResources().getAssets();
        InputStream is = null;
        try {
            is = mg.open(assetPath);
            is.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Close the inputStream after using it.
    public static InputStream getInputStream(String filePath) {
        AssetManager assetManager = FTApp.getInstance().getResources().getAssets();
        try {
            return assetManager.open(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    public void copyDirectory(String sourcePath, File target) throws IOException {
        Context context = FTApp.getInstance();
        if (!target.exists()) {
            target.mkdir();
        }
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(sourcePath);
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        if (files != null) {
            for (String filename : files) {
                File targetFile = new File(target.getPath() + "/" + filename);
                copyFile(sourcePath + "/" + filename, targetFile);
            }
        }
    }

    public void copyFile(String sourcePath, File target) throws IOException {
        Context context = FTApp.getInstance();
        target.getParentFile().mkdirs();

        try (
                InputStream in = context.getAssets().open(sourcePath);
                //InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)
        ) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

    public void copyLocalAsset(String relativePath, String destinationPath) {
        Context context = FTApp.getInstance();
        AssetManager assetManager = context.getAssets();
        String assets[] = null;

        try {
            assets = assetManager.list(relativePath);
            if (assets.length == 0) {
                Log.i("FILE_LIST:: ", relativePath);
                copyFile(relativePath, destinationPath);
            } else {
                File dir = new File(destinationPath);
                if (!dir.exists())
                    dir.mkdirs();
                for (int i = 0; i < assets.length; ++i) {
                    Log.i("FILE_LIST:: ", relativePath + "/" + assets[i]);
                    copyLocalAsset(relativePath + "/" + assets[i], destinationPath + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    public static String getNewPolicyVersion(Context context) {
        String newVersion = "";
        try {
            String[] files = context.getAssets().list("privacyPolicy");
            if (files != null && files.length > 0) {
                newVersion = files[0];
            }
        } catch (Exception e) {
            FTLog.crashlyticsLog(e.getMessage());
        }
        return newVersion;
    }

    private void copyFile(String filename, String destinationPath) {
        Context context = FTApp.getInstance();
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            out = new FileOutputStream(destinationPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
}
