package com.fluidtouch.noteshelf.evernotesync;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.DaoSession;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.evernotesync.models.FTENPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vineet on 22/03/2019
 */

public class FTENSyncRecordUtil {
    private static final DaoSession daoSession = FTApp.getDaoMaster().newSession();

    public static void enableEvernoteSyncForNotebook(Context context, FTNoteshelfDocument nsNotebook) {
        synchronized (daoSession) {
            if (context != null && nsNotebook != null) {
                FTENNotebook enNotebook = daoSession.getFTENNotebookDao().load(nsNotebook.getDocumentUUID());
                if (enNotebook == null) {
                    enNotebook = mapNsNotebookToEnNotebook(nsNotebook, new FTENNotebook());
                    enNotebook.setIsContentDirty(true);
                    daoSession.getFTENNotebookDao().insert(enNotebook);
                    addAllEnPages(nsNotebook.pages(context));
                } else {
                    if (!enNotebook.getSyncEnabled()) {
                        addAllEnPages(nsNotebook.pages(context));
                    }
                    enNotebook = mapNsNotebookToEnNotebook(nsNotebook, enNotebook);
                    enNotebook.update();
                }
            }
        }
    }

    public static void disableSyncForNotebook(String notebookGUID) {
        synchronized (daoSession) {
            FTENNotebook enNotebook = daoSession.getFTENNotebookDao().load(notebookGUID);
            if (enNotebook != null) {
                daoSession.getFTENPageDao().deleteInTx(enNotebook.getEnPages());
                enNotebook.setSyncEnabled(false);
                enNotebook.setIsContentDirty(false);
                enNotebook.resetEnPages();
                enNotebook.setErrorCode(null);
                enNotebook.setErrorDescription(null);
                enNotebook.update();
            }
        }
    }

    public static boolean isSyncEnabledForNotebook(String notebookGUID) {
        synchronized (daoSession) {
            FTENNotebook enNotebook = daoSession.getFTENNotebookDao().load(notebookGUID);
            return enNotebook != null && enNotebook.getSyncEnabled();
        }
    }

    public static List<FTENNotebook> getAllEnNotebooks() {
        synchronized (daoSession) {
            return daoSession.getFTENNotebookDao().loadAll();
        }
    }

    public static FTENNotebook getEnNotebook(String notebookGUID) {
        synchronized (daoSession) {
            return daoSession.getFTENNotebookDao().load(notebookGUID);
        }
    }

    public static void addPageToEvernoteSyncRecord(FTNoteshelfPage nsPage) {
        synchronized (daoSession) {
            if (nsPage != null) {
                FTENPage enPage = daoSession.getFTENPageDao().load(nsPage.uuid);
                if (enPage == null) {
                    enPage = mapNsPageToEnPage(nsPage, new FTENPage());
                    daoSession.getFTENPageDao().insert(enPage);
                } else {
                    enPage = mapNsPageToEnPage(nsPage, enPage);
                    daoSession.getFTENPageDao().update(enPage);
                }
                FTENNotebook enNotebook = getEnNotebook(nsPage.getParentDocument().getDocumentUUID());
                if (enNotebook != null && enNotebook.getSyncEnabled()) {
                    enNotebook.setIsContentDirty(true);
                    enNotebook.setLastUpdated(enPage.getLastUpdated());
                    enNotebook.resetEnPages();
                    enNotebook.update();
                }
            }
        }
    }

    private static void addAllEnPages(List<FTNoteshelfPage> nsPages) {
        synchronized (daoSession) {
            if (nsPages != null && !nsPages.isEmpty()) {
                List<FTENPage> enPages = new ArrayList<>(nsPages.size());
                for (FTNoteshelfPage nsPage : nsPages) {
                    FTENPage enPage = mapNsPageToEnPage(nsPage, new FTENPage());
                    enPages.add(enPage);
                }
                daoSession.getFTENPageDao().insertOrReplaceInTx(enPages);
            }
        }
    }

    public static void updateEnPageAfterSync(FTENPage updatedEnPage) {
        synchronized (daoSession) {
            if (updatedEnPage != null) {
                FTENPage enPage = daoSession.getFTENPageDao().load(updatedEnPage.getNsGUID());
                if (enPage != null) {
                    updatedEnPage.setIsDirty(false);
                    daoSession.getFTENPageDao().update(updatedEnPage);
                }
            }
        }
    }

    public static void removeEvernoteSyncForPages(List<FTNoteshelfPage> pages) {
        synchronized (daoSession) {
            if (pages != null && !pages.isEmpty()) {
                FTENNotebook enNotebook = daoSession.getFTENNotebookDao().load(pages.get(0).getParentDocument().getDocumentUUID());
                if (enNotebook != null) {
                    List<FTENPage> enPagesToDelete = new ArrayList<>();
                    for (FTNoteshelfPage nsPage : pages) {
                        FTENPage enPage = daoSession.getFTENPageDao().load(nsPage.uuid);
                        if (enPage != null) {
                            enPage = mapNsPageToEnPage(nsPage, enPage);
                            enPage.setDeleted(true);
                            enPagesToDelete.add(enPage);
                        }
                    }
                    if (!enPagesToDelete.isEmpty()) {
                        daoSession.getFTENPageDao().updateInTx(enPagesToDelete);
                    }
                    enNotebook.setIsContentDirty(true);
                    enNotebook.setLastUpdated(FTDeviceUtils.getTimeStamp());
                    enNotebook.resetEnPages();
                    enNotebook.update();
                }
            }
        }
    }

    public static void deleteEnPageAfterSync(String pageGUID) {
        synchronized (daoSession) {
            FTENPage enPage = daoSession.getFTENPageDao().load(pageGUID);
            if (enPage != null) {
                enPage.delete();
            }
        }
    }

    public static FTENPage getEnPage(String pageGUID) {
        synchronized (daoSession) {
            return daoSession.getFTENPageDao().load(pageGUID);
        }
    }

    private static FTENNotebook mapNsNotebookToEnNotebook(FTNoteshelfDocument nsNotebook, FTENNotebook enNotebook) {
        enNotebook.setNsGUID(nsNotebook.getDocumentUUID());
        if (!enNotebook.getSyncEnabled() && enNotebook.getLastSynced() > 0) {
            enNotebook.setIsContentDirty(true);
            enNotebook.setLastUpdated(enNotebook.getLastSynced());
        } else {
            enNotebook.setLastUpdated(FTDeviceUtils.getTimeStamp());
        }
        enNotebook.setSyncEnabled(true);
        enNotebook.setUrl(nsNotebook.getFileURL().getPath());
        return enNotebook;
    }

    private static FTENPage mapNsPageToEnPage(FTNoteshelfPage nsPage, FTENPage enPage) {
        enPage.setNsGUID(nsPage.uuid);
        enPage.setIndex(nsPage.pageIndex() + 1);
        enPage.setLastUpdated(nsPage.lastUpdated);
        enPage.setIsDirty(true);
        enPage.setEnNotebookGUID(nsPage.getParentDocument().getDocumentUUID());
        enPage.setDeleted(false);
        return enPage;
    }
}