package com.fluidtouch.noteshelf.commons.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.fluidtouch.noteshelf.backup.database.DaoMaster;
import com.fluidtouch.noteshelf.backup.database.FTENNotebookDao;
import com.fluidtouch.noteshelf.backup.database.FTENPageDao;
import com.fluidtouch.noteshelf.backup.database.FTOneDriveBackupCloudTableDao;
import com.fluidtouch.noteshelf.backup.database.FTWebDavBackupCloudTableDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Sreenu on 10/05/19
 */
public class FTSqliteOpenHelper extends DaoMaster.DevOpenHelper {
    public FTSqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //ToDo: Update this method to support the database upgradation
        if (oldVersion < newVersion) {
            if (oldVersion == 1) {
                if (newVersion == 2) {
                    FTENNotebookDao.createTable(db, true);
                    FTENPageDao.createTable(db, true);
                }
            } else if (oldVersion == 2 && newVersion == 3) {
                FTOneDriveBackupCloudTableDao.createTable(db, true);
            } else if (oldVersion == 3 && newVersion == 4) {
                FTWebDavBackupCloudTableDao.createTable(db, true);
            }
        }
    }
}