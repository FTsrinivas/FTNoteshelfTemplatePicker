package com.fluidtouch.noteshelf.backup.database;

import com.fluidtouch.noteshelf.FTApp;

import java.util.List;

/**
 * Created by Sreenu on 05/02/19
 */
public class FTDropboxBackupOperations implements FTBackupOperations {
    private FTDropboxBackupCloudTableDao getDao() {
        DaoSession session = FTApp.getDaoMaster().newSession();
        return session.getFTDropboxBackupCloudTableDao();
    }

    public void insertItem(FTDropboxBackupCloudTable item) {
        getDao().insertOrReplace(item);
    }

    public List<FTDropboxBackupCloudTable> getList() {
        return getDao().queryBuilder().list();
    }

    public List<FTDropboxBackupCloudTable> getFolderList(String relativePath) {
        return getDao().queryBuilder().where(FTDropboxBackupCloudTableDao.Properties.RelativePath.eq(relativePath)).list();
    }

    public List<FTDropboxBackupCloudTable> getList(String documentId) {
        return getDao().queryBuilder().where(FTDropboxBackupCloudTableDao.Properties.DocumentUUId.eq(documentId)).list();
    }

    public void updateItem(FTDropboxBackupCloudTable item) {
        getDao().update(item);
    }

    @Override
    public void insertOrReplace(FTBackupItem backupItem) {
        getDao().insertOrReplace((FTDropboxBackupCloudTable) backupItem);
    }

    @Override
    public List<? extends FTBackupItem> getErrorList() {
        return getDao().queryBuilder().where(FTDropboxBackupCloudTableDao.Properties.Error.notEq("")).list();
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
