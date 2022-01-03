package com.fluidtouch.noteshelf.commons;

import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FTLog {
    public static final String HW_RECOGNITION = "HWRecognition";
    public static final String VISION_RECOGNITION = "VisionRecognition";
    public static final String IMAGE_RECOGNITION = "ImageRecognition";
    public static final String LANGUAGE_DOWNLOAD = "LanguageDownload";
    public static final String FINDER_OPERATIONS = "FinderOperations";
    public static final String GLOBAL_SEARCH = "GLOBAL_SEARCH";
    public static final String DROPBOX_RESTORE = "DROPBOX_RESTORE";
    public static final String ONE_DRIVE_BACKUP = "ONE_DRIVE_BACKUP";
    public static final String WEBDAV_BACKUP = "WEBDAV_BACKUP";
    public static final String CLIPARTS = "Cliparts";
    public static final String NS_EVERNOTE = "ns_evernote";
    public static final String DIARIES = "diaryx";
    public static final String BACKUP_PROCESS = "BACKUP_PROCESS";

    public static void debug(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message != null ? message : "null");
        }
    }

    public static void error(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message != null ? message : "null");
        }
    }

    public static void setCrashUserId(String userId) {
        FTApp.getInstance().getAppAnalytics().setCrashUserIdentifier(userId);
    }

    public static void crashlyticsLog(String logValue) {
        FTApp.getInstance().getAppAnalytics().logCrashEvent(logValue);
        saveLog(logValue);
    }

    public static void logCrashCustomKey(String key, String value) {
        FTApp.getInstance().getAppAnalytics().logCrashCustomKey(key, value);
    }

    public static void logCrashException(Exception exception) {
        FTApp.getInstance().getAppAnalytics().logCrashException(exception);
    }

    public static void saveLog(String logValue) {
        int limit = 5000;
        try {
            logValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + " " + logValue;
            ArrayList<String> lines = new ArrayList<>();
            File out = new File(FTConstants.SUPPORT_LOG_FILE_PATH);
            if (out.exists()) {
                FileReader reader = new FileReader(FTConstants.SUPPORT_LOG_FILE_PATH);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
            }
            FileOutputStream outStream = new FileOutputStream(out, true);
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream);
            if (out.exists() == false) {
                out.createNewFile();
            }
            if (lines.size() >= limit) {
                PrintWriter writer = new PrintWriter(out);
                writer.print("");
                writer.close();
                lines.add(logValue);
                int startFrom = Math.max(0, lines.size() - limit);
                for (int i = startFrom; i < lines.size(); i++) {
                    outStreamWriter.append(lines.get(i));
                    outStreamWriter.append("\n");
                }
            } else {
                outStreamWriter.append(logValue);
                outStreamWriter.append("\n");
            }
            outStreamWriter.flush();
            outStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}