package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemFactory;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FTFileManagerUtil {
    private FTFileManagerUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isFileExits(Uri uri) {
        return isFileExits(uri.getPath());
    }

    public static boolean isFileExits(String filePath) {
        return new File(filePath).exists();
    }

    public static void deleteRecursive(@NonNull String fileOrDirPath) {
        deleteRecursive(new File(fileOrDirPath));
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] files = fileOrDirectory.listFiles();
                if (files != null) {
                    for (File child : files) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }

    public static void deleteFilesInsideFolder(String fileOrDirPath) {
        deleteFilesInsideFolder(new File(fileOrDirPath));
    }

    public static void copyRecursively(File fromFile, File toFile) {
        if (fromFile.isDirectory() && fromFile.exists()) {
            if (!toFile.exists()) {
                toFile.mkdirs();
            }
            File[] files = fromFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyRecursively(file, new File(toFile, file.getName()));
                }
            }
        } else {
            if (!toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }
            try {
                createFileFromInputStream(new FileInputStream(fromFile), toFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void moveFile(File sourceFile, File destinationFile) {
        copyRecursively(sourceFile, destinationFile);
        deleteRecursive(sourceFile);
    }

    public static void deleteFilesInsideFolder(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory() && fileOrDirectory.exists()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
    }

    public static File createFileFromInputStream(InputStream inputStream, String fileUrl) {
        try {
            File f = new File(fileUrl);
            OutputStream outputStream = new FileOutputStream(f);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File copyFileFromAssets(Context context, String fileRelativePath) {
        String copyingToDirectory = ContextCompat.getDataDir(context) + "/" + fileRelativePath;
        File file = new File(copyingToDirectory);

        if (file.exists()) {
            return file;
        }

        try {
            return createFileFromInputStream(context.getAssets().open(fileRelativePath), copyingToDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FileInputStream getFileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static File getPhotoFileUri(Context context, String fileName) {
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "NoteShelf");
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    public static String getFileMimeTypeByUri(Context context, Uri contentUri) {
        if (contentUri == null)
            return "";
        String mimeType = context.getContentResolver().getType(contentUri);
        if (mimeType == null || mimeType.equals(context.getString(R.string.mime_type_all))) {
            String fileExtension = FTFileItemFactory.getFileExtension(new File(FileUriUtils.getPath(context, contentUri)));
            if (fileExtension != null && !fileExtension.isEmpty()) {
                if (fileExtension.contains(FTConstants.NSA_EXTENSION.substring(1)))
                    mimeType = context.getString(R.string.mime_type_application_nsa);
                else if (fileExtension.contains(FTConstants.iOS_NOTESHELF_EXTENSION.substring(1)))
                    mimeType = context.getString(R.string.mime_type_iOS_noteshelf);
                else if (fileExtension.contains("pdf"))
                    mimeType = context.getString(R.string.mime_type_application_pdf);
            }

        } else if (mimeType.equals("application/octet-stream") ||
                mimeType.equals("multipart/x-zip") ||
                mimeType.equals("application/zip") ||
                mimeType.equals("application/zip-compressed") ||
                mimeType.equals("application/x-zip-compressed") ||
                mimeType.equals("application/x-zip")
        ) {
            mimeType = context.getString(R.string.mime_type_application_all);
        } else if (mimeType.contains("pdf"))
            mimeType = context.getString(R.string.mime_type_application_pdf);
        if (mimeType == null) {
            mimeType = "";
        }
        return mimeType;
    }

    public static String getFileNameFromUri(Context context, Uri contentUri) {
        String fileName = FileUriUtils.getFileName(context, contentUri);
        if (fileName != null) {
            String[] strings = fileName.split("\\.");
            if (strings.length > 0) {
                String extension = strings[strings.length - 1];
                fileName = strings[0];
                fileName = fileName + "." + extension.toLowerCase();
            }
        }
        return fileName;
    }

    public static String getExtensionRemovedPath(String path) {
        String[] strings = path.split("/");
        StringBuilder finalPath = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            finalPath.append(removeExtension(strings[i]));
            if (i + 1 < strings.length) {
                finalPath.append("/");
            }
        }
        return finalPath.toString();
    }

    public static String getRelativePath(String path) {
        String relativePath = "";
        if (path.contains("/"))
            relativePath = path.substring(0, path.lastIndexOf("/")).concat("/");
        if (relativePath.isEmpty()) relativePath = path;
        return relativePath;
    }

    private static String removeExtension(String name) {
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf("."));
        }
        return name;
    }
    public static String removeFileExtension(String name) {
        if (name.contains(".")) {
            return name.substring(0, name.lastIndexOf("."));
        }
        return name;
    }
}