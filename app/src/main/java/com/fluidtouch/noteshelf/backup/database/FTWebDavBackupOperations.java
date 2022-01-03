package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;

import java.util.List;

public class FTWebDavBackupOperations implements FTBackupOperations {

    private FTWebDavBackupCloudTableDao getDao() {
        DaoSession session = FTApp.getDaoMaster().newSession();
        return session.getFTWebDavBackupCloudTableDao();
    }

    public void insertItem(FTWebDavBackupCloudTable item) {
        getDao().insertOrReplace(item);
    }

    public void updateItem(FTWebDavBackupCloudTable item) {
        getDao().update(item);
    }

    public List<FTWebDavBackupCloudTable> getList() {
        return getDao().queryBuilder().list();
    }

    public List<FTWebDavBackupCloudTable> getFolderList(String relativePath) {
        return getDao().queryBuilder().where(FTWebDavBackupCloudTableDao.Properties.RelativePath.eq(relativePath)).list();
    }

    public List<FTWebDavBackupCloudTable> getList(String documentId) {
        return getDao().queryBuilder().where(FTWebDavBackupCloudTableDao.Properties.DocumentUUId.eq(documentId)).list();
    }

    @Override
    public void insertOrReplace(FTBackupItem backupItem) {
        getDao().insertOrReplace((FTWebDavBackupCloudTable) backupItem);
    }

    public List<FTWebDavBackupCloudTable> getErrorList() {
        return getDao().queryBuilder().where(FTWebDavBackupCloudTableDao.Properties.Error.notEq("")).list();
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