package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;

import ir.mahdi.mzip.zip.ZipArchive;

public class ZipUtil {
    public static synchronized void zip(Context context, String src, String archiveFormat, ZipCompletionBlock zipCompletionBlock) {
        zip(context, src, zipFolderPath(), archiveFormat, zipCompletionBlock);
    }

    public static synchronized void zip(Context context, String src, String dest, String archiveFormat, ZipCompletionBlock zipCompletionBlock) {
//        File zipperDir = new File(zipFolderPath());
//        if (!zipperDir.exists()) {
//            zipperDir.mkdirs();
//        }

        String destination = dest + FTDocumentUtils.getFileNameWithoutExtension(context, FTUrl.parse(src)) + archiveFormat;

        ZipArchive.zip(src, destination, "");

        Error error = null;
        File zippedFile = new File(destination);
        if (!zippedFile.exists())
            error = new Error("Error! Unable to perform zip");
//        else
//            new File(src).delete();

        zipCompletionBlock.onZippingDone(zippedFile, error);
    }

    public static synchronized void unzip(Context context, String src, ZipCompletionBlock zipCompletionBlock) {
        unzip(context, src, zipFolderPath(), zipCompletionBlock);
    }

    public static synchronized void unzip(Context context, String src, String dest, ZipCompletionBlock zipCompletionBlock) {
        File zipperDir = new File(zipFolderPath());
        if (zipperDir.exists()) {
            FTFileManagerUtil.deleteFilesInsideFolder(zipperDir);
        } else {
            zipperDir.mkdirs();
        }

        File zippedFile = new File(src);

        ZipArchive.unzip(zippedFile.getPath(), dest, "");
        zippedFile.delete();

        Error error = new Error();
        File unzippedFile = null;
        for (File file : zipperDir.listFiles()) {
            String unzippedFileName = file.getName();
            if (file.getName().contains(".")) {
                unzippedFileName = FTDocumentUtils.getFileNameWithoutExtension(context, FTUrl.parse(file.getName()));
            }
            if (!unzippedFileName.toLowerCase().contains("macosx")) {
                unzippedFile = new File(dest, file.getName());
                error = null;
                break;
            }
        }
        zipCompletionBlock.onZippingDone(unzippedFile, error);
    }

    public static synchronized void unzipDownloadThemes(FTUrl fileURL, ZipCompletionBlock zipCompletionBlock) {
        File zippedFile = new File(fileURL.getPath());

        String source = zippedFile.getPath();
        String destination = zipFolderPath();
        File file = new File(destination);
        if (!file.exists())
            file.mkdirs();
        ZipArchive.unzip(source, destination, "");
        zippedFile.delete();
        zipCompletionBlock.onZippingDone(new File(destination), new Error("Success fully unzip"));
    }

    public static synchronized String zipFolderPath() {
        return FTConstants.DOCUMENTS_ROOT_PATH + "/Temp/Zipper/";
    }

    public interface ZipCompletionBlock {
        void onZippingDone(File file, Error error);
    }
}