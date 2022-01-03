package com.fluidtouch.noteshelf.evernotesync;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.evernotesync.models.FTENPage;
import com.fluidtouch.noteshelf.evernotesync.publishers.FTENNotebookPublishRequest;
import com.fluidtouch.noteshelf.evernotesync.publishers.FTENPagePublishRequest;
import com.fluidtouch.noteshelf.preferences.SystemPref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Vineet on 26/03/2019
 */

public class FTENPublishManager {

    //region Singleton Instance
    private static final FTENPublishManager publishManager = new FTENPublishManager();
    //region Member Variables
    public boolean isEngineUnderExecution = false;
    public ObservingService observingService = new ObservingService();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean publishInProgress = false;
    private Iterator<FTENNotebook> notebookIterator;
    private Iterator<FTENPage> pageIterator;
    private List<FTENNotebook> ignoreNotebooks = new ArrayList<>();
    //endregion
    private Timer timer;

    public static FTENPublishManager getInstance() {
        return publishManager;
    }
    //endregion

    public void enablePublisher() {
        executorService.execute(() -> {
            timer = new Timer();
            isEngineUnderExecution = true;
            FTLog.debug(FTLog.NS_EVERNOTE, "Evernote publisher under execution: " + isEngineUnderExecution);
            scanForNotebooks();
        });
    }

    public void disablePublisher() {
        timer.cancel();
        executorService.shutdownNow();
    }

    //region Notebook Process
    private void scanForNotebooks() {
        try {
            //needs to change this timer to Executors
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (EvernoteSession.getInstance().isLoggedIn() && !publishInProgress) {
                        List<FTENNotebook> enNotebooks = FTENSyncRecordUtil.getAllEnNotebooks();
                        if (enNotebooks != null && !enNotebooks.isEmpty()) {
                            notebookIterator = enNotebooks.iterator();
                            nextNotebook();
                        } else {
                            scanForNotebooks();
                        }
                    } else {
                        scanForNotebooks();
                    }
                }
            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextNotebook() {
        if (notebookIterator != null && notebookIterator.hasNext()) {
            FTENNotebook enNotebook = notebookIterator.next();
            // Not uploading currently open document
            if ((enNotebook.getSyncEnabled() && enNotebook.getLastUpdated() >= enNotebook.getLastSynced()
                    && !FTApp.CURRENT_EDIT_DOCUMENT_UIDS.contains(enNotebook.getNsGUID())) || enNotebook.getNsGUID().equals(FTApp.getPref().get(SystemPref.UNSYNCED_DOCUMENT_UUID, ""))) {
                if (!publishInProgress) {
                    FTLog.crashlyticsLog("Evernote: Publishing notebook...");
                    FTLog.debug(FTLog.NS_EVERNOTE, "Publishing " + FTDocumentUtils.getFileNameWithoutExtension(FTApp.getInstance().getCurActCtx(), FTUrl.parse(enNotebook.getUrl())));
                    publishInProgress = true;
                    FTENNotebookPublishRequest enNotePublishRequest = new FTENNotebookPublishRequest(enNotebook, (o, e) -> {
                        if (e == null) {
                            FTENNotebook updatedNotebook = (FTENNotebook) o;
                            publishInProgress = false;
                            observingService.postNotification("evernote_error", null);
                            if (updatedNotebook.getLastSynced() > enNotebook.getLastSynced()) {
                                ignoreNotebooks.remove(updatedNotebook);
                            }
                            if (enNotebook.getIsContentDirty()) {
                                scanForPages(updatedNotebook);
                            } else {
                                nextNotebook();
                            }
                        } else {
                            onFailure(e);
                        }
                    });
                    enNotePublishRequest.startPublishing();
                } else {
                    nextNotebook();
                }
            } else {
                nextNotebook();
            }
        } else {
            scanForNotebooks();
        }
    }
    //endregion

    //region Page Process
    private void scanForPages(FTENNotebook enNotebook) {
        List<FTENPage> enPages = enNotebook.getEnPages();
        if (enPages != null && !enPages.isEmpty()) {
            pageIterator = enPages.iterator();
            nextPage();
        } else {
            enNotebook.update();
            nextNotebook();
        }
    }

    private synchronized void nextPage() {
        if (pageIterator != null && pageIterator.hasNext()) {
            FTENPage enPage = pageIterator.next();
            FTENNotebook enNotebook = enPage.getEnNotebook();
            if (enNotebook == null || !enNotebook.getSyncEnabled()) {
                nextNotebook();
                return;
            }
            if (!pageIterator.hasNext()) {
                enNotebook.setIsContentDirty(false);
                enNotebook.update();
            }
            if (enPage.getIsDirty() && enNotebook.getSyncEnabled()) {
                if (!publishInProgress) {
                    publishInProgress = true;
                    FTLog.debug(FTLog.NS_EVERNOTE, "Publishing page " + enPage.getIndex());
                    FTENPagePublishRequest enResourcePublishRequest = new FTENPagePublishRequest(enPage, enPage.getLastUpdated(), (object, e) -> {
                        if (e == null) {
                            FTApp.getPref().get(SystemPref.UNSYNCED_DOCUMENT_UUID, "");
                            publishInProgress = false;
                            observingService.postNotification("evernote_error", null);
                            nextPage();
                        } else {
                            onFailure(e);
                        }
                    });
                    enResourcePublishRequest.startPublishing();
                    FTApp.getPref().get(SystemPref.UNSYNCED_DOCUMENT_UUID, enNotebook.getNsGUID());
                }
            } else {
                nextPage();
            }
        } else {
            nextNotebook();
        }
    }
    //endregion

    //region OnFailure
    public void onFailure(Exception e) {
        publishInProgress = false;
        FTLog.error(FTLog.NS_EVERNOTE, "Evernote Publish failed! Cause: " + e.getMessage());
        handleException(null, new FTENSyncError(e));
    }

    private void handleException(FTENNotebook enNotebook, FTENSyncError error) {
        publishInProgress = false;
        FTApp.getPref().get(SystemPref.UNSYNCED_DOCUMENT_UUID, "");
        if (error != null && error.getErrorCode() != null && error.getErrorDescription() != null) {
            switch (error.getErrorCode()) {
                //Ignore completely
                case "INTERNAL_ERROR":
                case "SHARD_UNAVAILABLE":
                case "RATE_LIMIT_REACHED":
                    break;
                //Notebook level errors
                case "DATA_CONFLICT":
                case "ENML_VALIDATION":
                case "DATA_REQUIRED":
                case "LIMIT_REACHED":
                case "LEN_TOO_SHORT":
                case "LEN_TOO_LONG":
                case "BAD_DATA_FORMAT":
                case "TOO_FEW":
                case "TOO_MANY":
                case "UNKNOWN":
                case "TAKEN_DOWN":
                case "UNSUPPORTED_OPERATION":
                    if (enNotebook != null && !ignoreNotebooks.contains(enNotebook)) {
                        enNotebook.setErrorCode(error.getErrorCode());
                        enNotebook.setErrorDescription(error.getErrorDescription());
                        enNotebook.update();
                        ignoreNotebooks.add(enNotebook);
                    }
                    break;
                //Global level errors
                case "QUOTA_REACHED":
                    //ToDo: Request user to check available quota
                case "PERMISSION_DENIED":
                    //ToDo: Request user to check for permissions in Evernote website
                case "INVALID_AUTH":
                    //ToDo: Log out and request user to log in again
                case "AUTH_EXPIRED":
                    //ToDo: Log out and request user to log in again
                    FTApp.getPref().save(SystemPref.EVERNOTE_GLOBAL_ERROR, error.getErrorDescription());
                    break;
                default:
                    break;
            }
            observingService.postNotification("evernote_error", null);
        }
        scanForNotebooks();
    }
    //endregion

    //region ErrorList
    public List<String> getErrorList() {
        List<FTENNotebook> notebookRecords = FTENSyncRecordUtil.getAllEnNotebooks();
        List<String> errors = new ArrayList<>();
        String globalError = FTApp.getPref().get(SystemPref.EVERNOTE_GLOBAL_ERROR, null);
        if (globalError != null) {
            errors.add(globalError);
        }
        if (notebookRecords != null) {
            for (FTENNotebook notebookRecord : notebookRecords) {
                if (!TextUtils.isEmpty(notebookRecord.getErrorDescription())) {
                    errors.add(notebookRecord.getErrorDescription());
                }
            }
        }
        return errors;
    }
    //endregion

    //region UserInfo
    public void getUserName(UserInfoCallback listener) {
        if (!EvernoteSession.getInstance().isLoggedIn()) return;
        AsyncTask.execute(() -> {
            String userName = "";
            long uploadLimit = 0;
            long uploadedSize = 0;
            try {
                User user = EvernoteSession.getInstance().getEvernoteClientFactory().getUserStoreClient().getUser();
                userName = user.getUsername();
                uploadLimit = user.getAccounting().getUploadLimit();
                uploadedSize = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient().getSyncState().getUploaded();
            } catch (Exception e) {
                FTLog.logCrashException(e);
            }
            FTApp.getPref().save(SystemPref.EVERNOTE_USERNAME, userName);
            FTApp.getPref().save(SystemPref.EVERNOTE_USER_UPLOAD_LIMIT, uploadLimit / (1024F * 1024F));
            FTApp.getPref().save(SystemPref.EVERNOTE_USER_UPLOADED_SIZE, uploadedSize / (1024F * 1024F));
            listener.onUserInfoProduced();
        });
    }

    public interface UserInfoCallback {
        void onUserInfoProduced();
    }
    //endregion
}