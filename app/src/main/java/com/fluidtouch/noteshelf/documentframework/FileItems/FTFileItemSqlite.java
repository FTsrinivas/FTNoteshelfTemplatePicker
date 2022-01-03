package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.DatabaseHelper;

import java.io.File;
import java.io.IOException;

public class FTFileItemSqlite extends FTFileItem {
    private DatabaseHelper dbHelper;

    public FTFileItemSqlite(Context context, FTUrl fileItemURL, Boolean isDirectory) {
        super(context, fileItemURL, isDirectory);
    }

    public FTFileItemSqlite(Context context, String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
        //this.dbHelper.insertData();
    }

    protected synchronized DatabaseHelper databaseHelper(Context context) {


        if (dbHelper == null) {
            File fileMain = new File(this.getFileItemURL().getPath());
            String[] folders = this.getFileItemURL().getPath().split("/");
            File fileTemp = null;
            File fileTemp_journal = null;
            if (folders.length > 2) {
                File temp = new File(context.getCacheDir() + "/" + folders[folders.length - 3]);
                if (!temp.exists()) {
                    temp.mkdirs();
                }
                fileTemp = new File(context.getCacheDir() + "/" + folders[folders.length - 3], fileName);
                fileTemp_journal = new File(context.getCacheDir() + "/" + folders[folders.length - 3], fileName + "-journal");
            } else {
                fileTemp = new File(context.getCacheDir(), fileName);
                fileTemp_journal = new File(context.getCacheDir(), fileName + "-journal");
            }
            File fileMain_journal = new File(this.getFileItemURL().getPath() + "-journal");
            try {
                if (fileMain.exists()) {
                    if (fileTemp.exists()) {
                        fileTemp.delete();
                        fileTemp_journal.delete();
                    }
                    FTDocumentUtils.copyFile(fileMain, fileTemp);
                }
                if (fileMain_journal.exists()) {
                    if (fileTemp_journal.exists()) {
                        fileTemp_journal.delete();
                    }
                    FTDocumentUtils.copyFile(fileMain_journal, fileTemp_journal);
                }
                this.dbHelper = new DatabaseHelper(context, fileTemp.getPath());

            } catch (IOException e) {
                this.dbHelper = new DatabaseHelper(context, fileMain.getPath());
                e.printStackTrace();
                FTLog.logCrashException(e);
            } catch (Exception e) {
                this.dbHelper = new DatabaseHelper(context, fileMain.getPath());
                e.printStackTrace();
                FTLog.logCrashException(e);
            }
        }
        return dbHelper;
    }

    @Override
    public void unloadContentsOfFileItem() {
        super.unloadContentsOfFileItem();
        //this.dbHelper = null;
        //this.sqliteDB = null;
    }
}