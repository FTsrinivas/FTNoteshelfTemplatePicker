package com.fluidtouch.noteshelf.documentframework.Utilities;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupCodes {
    Context mContext;

    public String copyDirorfileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {
        File sd_path = new File(ContextCompat.getDataDir(mContext).getPath());
        String dest_dir_path = sd_path + addLeadingSlash(arg_destinationDir);
        File dest_dir = new File(dest_dir_path);

        createDir(dest_dir);

        AssetManager asset_manager = mContext.getApplicationContext().getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for (int i = 0; i < files.length; i++) {

            String abs_asset_file_path = addTrailingSlash(arg_assetDir) + files[i];
            String sub_files[] = asset_manager.list(abs_asset_file_path);

            if (sub_files.length == 0) {
                // It is a file
                String dest_file_path = addTrailingSlash(dest_dir_path) + files[i];
                copyAssetFile(abs_asset_file_path, dest_file_path);
            } else {
                // It is a sub directory
                copyDirorfileFromAssetManager(abs_asset_file_path, addTrailingSlash(arg_destinationDir) + files[i]);
            }
        }

        return dest_dir_path;
    }


    public void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = mContext.getApplicationContext().getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }

    public String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    public void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
