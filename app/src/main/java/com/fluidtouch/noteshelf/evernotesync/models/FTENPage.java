package com.fluidtouch.noteshelf.evernotesync.models;

import com.fluidtouch.noteshelf.backup.database.DaoSession;
import com.fluidtouch.noteshelf.backup.database.FTENNotebookDao;
import com.fluidtouch.noteshelf.backup.database.FTENPageDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

@Entity
public class FTENPage {
    @Id
    private String nsGUID = "";
    private String enGUID = "";
    private boolean isDirty;
    private boolean deleted;
    private long lastUpdated;
    private int index;
    private String enNotebookGUID = "";
    @ToOne(joinProperty = "enNotebookGUID")
    private FTENNotebook enNotebook;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1933306202)
    private transient FTENPageDao myDao;
    @Generated(hash = 759800622)
    private transient String enNotebook__resolvedKey;

    @Generated(hash = 518779815)
    public FTENPage(String nsGUID, String enGUID, boolean isDirty, boolean deleted,
                    long lastUpdated, int index, String enNotebookGUID) {
        this.nsGUID = nsGUID;
        this.enGUID = enGUID;
        this.isDirty = isDirty;
        this.deleted = deleted;
        this.lastUpdated = lastUpdated;
        this.index = index;
        this.enNotebookGUID = enNotebookGUID;
    }

    @Generated(hash = 1749920355)
    public FTENPage() {
    }

    public String getNsGUID() {
        return this.nsGUID;
    }

    public void setNsGUID(String nsGUID) {
        this.nsGUID = nsGUID;
    }

    public String getEnGUID() {
        return this.enGUID;
    }

    public void setEnGUID(String enGUID) {
        this.enGUID = enGUID;
    }

    public boolean getIsDirty() {
        return this.isDirty;
    }

    public void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getEnNotebookGUID() {
        return this.enNotebookGUID;
    }

    public void setEnNotebookGUID(String enNotebookGUID) {
        this.enNotebookGUID = enNotebookGUID;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 358598243)
    public FTENNotebook getEnNotebook() {
        String __key = this.enNotebookGUID;
        if (enNotebook__resolvedKey == null || enNotebook__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FTENNotebookDao targetDao = daoSession.getFTENNotebookDao();
            FTENNotebook enNotebookNew = targetDao.load(__key);
            synchronized (this) {
                enNotebook = enNotebookNew;
                enNotebook__resolvedKey = __key;
            }
        }
        return enNotebook;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1144827335)
    public void setEnNotebook(FTENNotebook enNotebook) {
        synchronized (this) {
            this.enNotebook = enNotebook;
            enNotebookGUID = enNotebook == null ? null : enNotebook.getNsGUID();
            enNotebook__resolvedKey = enNotebookGUID;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2031688936)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFTENPageDao() : null;
    }
}