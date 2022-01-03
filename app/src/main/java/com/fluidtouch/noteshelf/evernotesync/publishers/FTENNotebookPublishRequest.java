package com.fluidtouch.noteshelf.evernotesync.publishers;

import android.util.Log;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.asyncclient.EvernoteUserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.thrift.TException;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.evernotesync.models.FTENPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Vineet on 26/03/2019
 */

public class FTENNotebookPublishRequest {
    //region Member Variables
    private static final String EN_NOTEBOOK_NAME = "Noteshelf";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EvernoteNoteStoreClient noteStoreClient;
    private EvernoteUserStoreClient userStoreClient;

    private FTENNotebook enNotebookRecord;
    private FTENRequestCallback listener;

    Exception exception = null;
    //endregion

    //region Constructor
    public FTENNotebookPublishRequest(FTENNotebook enNotebook, FTENRequestCallback listener) {
        this.enNotebookRecord = enNotebook;
        this.listener = listener;
    }
    //endregion

    public void startPublishing() {
        executorService.execute(() -> {
            if (enNotebookRecord != null) {
                try {
                    this.noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
                    this.userStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getUserStoreClient();
                    Notebook notebook = getNotebook();
                    if (notebook != null) {
                        Note note;
                        if (enNotebookRecord.getEnGUID().isEmpty()) {
                            note = createNote();
                            List<Resource> resources = note.getResources();
                            if (resources != null && !resources.isEmpty()) {
                                for (Resource resource : resources) {
                                    for (FTENPage pageRecord : enNotebookRecord.getEnPages()) {
                                        if (resource.getAttributes().getFileName().replace(".jpg", "").equals(pageRecord.getNsGUID())) {
                                            pageRecord.setEnGUID(resource.getGuid());
                                            FTENSyncRecordUtil.updateEnPageAfterSync(pageRecord);
                                        }
                                    }
                                }
                            }
                        } else {
                            note = noteStoreClient.getNote(enNotebookRecord.getEnGUID(), true, true, true, true);
                            note.setTitle(FTDocumentUtils.getFileNameWithoutExtension(FTApp.getInstance().getCurActCtx(), FTUrl.parse(enNotebookRecord.getUrl())));
                            Log.d(FTLog.NS_EVERNOTE, "Updating existing Note...");
                            note = noteStoreClient.updateNote(note);
                        }
                        enNotebookRecord.setLastSynced(FTDeviceUtils.getTimeStamp());
                        enNotebookRecord.setEnGUID(note.getGuid());
                        enNotebookRecord.setErrorCode(null);
                        enNotebookRecord.setErrorDescription(null);
                        enNotebookRecord.update();
                        Log.d(FTLog.NS_EVERNOTE, "Updated Note in DB after EN operation");
                    }
                } catch (EDAMNotFoundException | EDAMUserException | EDAMSystemException | TException e) {
                    exception = e;
                } catch (Exception e) {
                    exception = e;
                }
            }
            listener.onCompletion(enNotebookRecord, exception);
        });
    }

    private Note createNote() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        Note note = new Note();
        note.setGuid(UUID.randomUUID().toString());
        note.setTitle(FTDocumentUtils.getFileNameWithoutExtension(FTApp.getInstance().getCurActCtx(), FTUrl.parse(enNotebookRecord.getUrl())));
        note.setNotebookGuid(FTApp.getPref().get(SystemPref.EVERNOTE_NOTEBOOK_GUID, ""));
        note.setContent(EvernoteUtil.NOTE_PREFIX + EvernoteUtil.NOTE_SUFFIX);
        Log.d(FTLog.NS_EVERNOTE, "Creating new Note...");
        return noteStoreClient.createNote(note);
    }

    private Notebook getNotebook() throws TException, EDAMUserException, EDAMSystemException {
        Notebook notebook = null;
        storeUserInfo();
        for (Notebook existingNotebook : noteStoreClient.listNotebooks()) {
            if (existingNotebook.getName().equals(EN_NOTEBOOK_NAME) ||
                    existingNotebook.getGuid().equals(FTApp.getPref().get(SystemPref.EVERNOTE_NOTEBOOK_GUID, ""))) {
                notebook = existingNotebook;
                break;
            }
        }
        if (notebook == null) {
            Log.d(FTLog.NS_EVERNOTE, "Creating root folder Noteshelf in Evernote...");
            Notebook newNotebook = new Notebook();
            newNotebook.setGuid(UUID.randomUUID().toString());
            newNotebook.setName(EN_NOTEBOOK_NAME);
            notebook = noteStoreClient.createNotebook(newNotebook);
        }
        FTApp.getPref().save(SystemPref.EVERNOTE_NOTEBOOK_GUID, notebook.getGuid());
        return notebook;
    }

    private void storeUserInfo() throws TException, EDAMUserException, EDAMSystemException {
        long uploadLimit = userStoreClient.getUser().getAccounting().getUploadLimit();
        long uploadedSize = noteStoreClient.getSyncState().getUploaded();
        FTApp.getPref().save(SystemPref.EVERNOTE_USER_UPLOAD_LIMIT, uploadLimit / (1024F * 1024F));
        FTApp.getPref().save(SystemPref.EVERNOTE_USER_UPLOADED_SIZE, uploadedSize / (1024F * 1024F));
    }
}