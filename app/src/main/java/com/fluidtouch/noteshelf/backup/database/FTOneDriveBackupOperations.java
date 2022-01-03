package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;

import java.util.List;

public class FTOneDriveBackupOperations implements FTBackupOperations {

    private FTOneDriveBackupCloudTableDao getDao() {
        DaoSession session = FTApp.getDaoMaster().newSession();
        return session.getFTOneDriveBackupCloudTableDao();
    }

    public void insertItem(FTOneDriveBackupCloudTable item) {
        getDao().insertOrReplace(item);
    }

    public void updateItem(FTOneDriveBackupCloudTable item) {
        getDao().update(item);
    }

    public List<FTOneDriveBackupCloudTable> getList() {
        return getDao().queryBuilder().list();
    }

    public List<FTOneDriveBackupCloudTable> getFolderList(String relativePath) {
        return getDao().queryBuilder().where(FTOneDriveBackupCloudTableDao.Properties.RelativePath.eq(relativePath)).list();
    }

    public List<FTOneDriveBackupCloudTable> getList(String documentId) {
        return getDao().queryBuilder().where(FTOneDriveBackupCloudTableDao.Properties.DocumentUUId.eq(documentId)).list();
    }

    @Override
    public void insertOrReplace(FTBackupItem backupItem) {
        getDao().insertOrReplace((FTOneDriveBackupCloudTable) backupItem);
    }

    public List<FTOneDriveBackupCloudTable> getErrorList() {
        return getDao().queryBuilder().where(FTOneDriveBackupCloudTableDao.Properties.Error.notEq("")).list();
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
