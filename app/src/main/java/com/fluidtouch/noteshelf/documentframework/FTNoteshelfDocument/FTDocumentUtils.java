package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.fluidtouch.noteshelf.commons.utils.FTAESEnDecrypt;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FTDocumentUtils {

    public static String ROOT_DIR_NAME = "Noteshelf.nsdata";
    static String PASSWORD_SECRET = "Noteshelf laladodola";
    static String NO_PASSWORD_STRING = "???NO PASS???";

    public static FTUrl noteshelfDocumentsDirectory() {
        String directoryPath = FTConstants.DOCUMENTS_ROOT_PATH + "/" + ROOT_DIR_NAME;
        FTUrl dirUri = FTUrl.parse(directoryPath);
        return dirUri;
    }

    public static String getFileName(Context context, FTUrl uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(Uri.parse(uri.getPath()), null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String getFileNameWithoutExtension(Context context, FTUrl uri) {
        String result = FTDocumentUtils.getFileName(context, uri);
        if (result != null && result.lastIndexOf(".") > 0) {
            result = result.substring(0, result.lastIndexOf("."));
        }
        return result;
    }

    public static String getUDID() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public static void copyFile(File sourceLocation, File targetLocation) throws IOException {
        if (!sourceLocation.isDirectory()) {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    static String encryptString(String string, boolean allowDefaultValue, String privateKey) {
        if (string == null) string = "";

        String encryptedString = "";
        try {
            encryptedString = FTAESEnDecrypt.encrypt(PASSWORD_SECRET, string);
        } catch (Exception e) {
            return "";
        }
        return encryptedString;
    }

    static String decryptString(String string, boolean allowDefaultValue, String privateKey) {
        if (string == null) string = "";
        String decryptedString = "";
        try {
            decryptedString = FTAESEnDecrypt.decrypt(PASSWORD_SECRET, string);
        } catch (Exception e) {
            return "";
        }

        return decryptedString;
    }
}
