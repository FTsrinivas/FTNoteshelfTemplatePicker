package com.fluidtouch.noteshelf.evernotesync.models;

import com.fluidtouch.noteshelf.backup.database.DaoSession;
import com.fluidtouch.noteshelf.backup.database.FTENNotebookDao;
import com.fluidtouch.noteshelf.backup.database.FTENPageDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity
public class FTENNotebook {
    @Id
    private String nsGUID = "";
    private String enGUID = "";
    private String url = "";
    private boolean syncEnabled;
    private boolean deleted;
    private boolean isContentDirty;
    private long lastUpdated;
    private long lastSynced;
    private String errorCode;
    private String errorDescription;
    @ToMany(joinProperties = {@JoinProperty(name = "nsGUID", referencedName = "enNotebookGUID")})
    private List<FTENPage> enPages;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 818967648)
    private transient FTENNotebookDao myDao;

    @Generated(hash = 2019029408)
    public FTENNotebook(String nsGUID, String enGUID, String url, boolean syncEnabled, boolean deleted,
                        boolean isContentDirty, long lastUpdated, long lastSynced, String errorCode,
                        String errorDescription) {
        this.nsGUID = nsGUID;
        this.enGUID = enGUID;
        this.url = url;
        this.syncEnabled = syncEnabled;
        this.deleted = deleted;
        this.isContentDirty = isContentDirty;
        this.lastUpdated = lastUpdated;
        this.lastSynced = lastSynced;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    @Generated(hash = 1285651707)
    public FTENNotebook() {
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

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean getSyncEnabled() {
        return this.syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean getIsContentDirty() {
        return this.isContentDirty;
    }

    public void setIsContentDirty(boolean isContentDirty) {
        this.isContentDirty = isContentDirty;
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastSynced() {
        return this.lastSynced;
    }

    public void setLastSynced(long lastSynced) {
        this.lastSynced = lastSynced;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1925451638)
    public List<FTENPage> getEnPages() {
        if (enPages == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FTENPageDao targetDao = daoSession.getFTENPageDao();
            List<FTENPage> enPagesNew = targetDao._queryFTENNotebook_EnPages(nsGUID);
            synchronized (this) {
                if (enPages == null) {
                    enPages = enPagesNew;
                }
            }
        }
        return enPages;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1037639058)
    public synchronized void resetEnPages() {
        enPages = null;
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
    @Generated(hash = 1784450505)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFTENNotebookDao() : null;
    }
}