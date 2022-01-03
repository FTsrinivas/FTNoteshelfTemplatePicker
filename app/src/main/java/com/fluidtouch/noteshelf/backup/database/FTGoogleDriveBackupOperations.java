package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;

import java.util.List;

/**
 * Created by Sreenu on 30/01/19
 */
public class FTGoogleDriveBackupOperations implements FTBackupOperations {

    private FTGoogleDriveBackupCloudTableDao getDao() {
        DaoSession session = FTApp.getDaoMaster().newSession();
        return session.getFTGoogleDriveBackupCloudTableDao();
    }

    public void insertItem(FTGoogleDriveBackupCloudTable item) {
        getDao().insertOrReplace(item);
    }

    public void updateItem(FTGoogleDriveBackupCloudTable item) {
        getDao().update(item);
    }

    public List<FTGoogleDriveBackupCloudTable> getList() {
        return getDao().queryBuilder().list();
    }

    public List<FTGoogleDriveBackupCloudTable> getFolderList(String relativePath) {
        return getDao().queryBuilder().where(FTGoogleDriveBackupCloudTableDao.Properties.RelativePath.eq(relativePath)).list();
    }

    public List<FTGoogleDriveBackupCloudTable> getList(String documentId) {
        return getDao().queryBuilder().where(FTGoogleDriveBackupCloudTableDao.Properties.DocumentUUId.eq(documentId)).list();
    }

    @Override
    public void insertOrReplace(FTBackupItem backupItem) {
        getDao().insertOrReplace((FTGoogleDriveBackupCloudTable) backupItem);
    }

    public List<FTGoogleDriveBackupCloudTable> getErrorList() {
        return getDao().queryBuilder().where(FTGoogleDriveBackupCloudTableDao.Properties.Error.notEq("")).list();
    }

    @Override
    public void deleteAll() {
        getDao().deleteAll();
    }

    @Override
    public void delete(String documentUUID) {
        getDao().deleteByKey(documentUUID);
    }
}
