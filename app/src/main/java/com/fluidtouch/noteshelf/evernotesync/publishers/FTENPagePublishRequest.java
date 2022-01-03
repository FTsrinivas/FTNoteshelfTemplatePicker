package com.fluidtouch.noteshelf.evernotesync.publishers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SizeF;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.thrift.TException;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.document.FTLock;
import com.fluidtouch.noteshelf.document.FTTextureManager;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.evernotesync.models.FTENPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.renderingengine.renderer.FTOffscreenBitmap;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;

import org.benjinus.pdfium.util.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Vineet on 26/03/2019
 */

public class FTENPagePublishRequest {
    //region Member variables
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EvernoteNoteStoreClient noteStoreClient;

    private FTENPage enPageRecord;
    private long currentSyncTime;
    private FTENRequestCallback listener;

    private FTRenderManager renderManager = new FTRenderManager(FTApp.getInstance().getCurActCtx(), FTRenderMode.offScreen);

    private List<FTENPage> pageRecords;
    private List<Resource> pageResources;

    Exception exception = null;
    //endregion

    //region Constructor
    public FTENPagePublishRequest(FTENPage pageRecord, long currentSyncTime, FTENRequestCallback listener) {
        this.enPageRecord = pageRecord;
        this.currentSyncTime = currentSyncTime;
        this.listener = listener;
    }
    //endregion

    public void startPublishing() {
        executorService.execute(() -> {
            try {
                this.noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
                Note note = noteStoreClient.getNote(enPageRecord.getEnNotebook().getEnGUID(), true, true, true, true);
                if (note != null) {
                    this.pageRecords = this.enPageRecord.getEnNotebook().getEnPages();
                    this.pageResources = note.getResources();
                    if (this.pageResources == null) {
                        this.pageResources = new ArrayList<>();
                    }

                    if (this.enPageRecord.getEnGUID().isEmpty()) {
                        Log.d(FTLog.NS_EVERNOTE, "Creating new pageResource...");
                        this.pageResources.add(createResource(this.enPageRecord));
                    } else {
                        Resource pageResource = noteStoreClient.getResource(enPageRecord.getEnGUID(), true, true, true, true);
                        if (pageResource != null) {
                            if (this.enPageRecord.getDeleted()) {
                                Log.d(FTLog.NS_EVERNOTE, "Deleting pageResource...");
                                deleteResource(this.enPageRecord.getEnGUID());
                            } else {
                                this.pageResources = updateResource(pageResource);
                            }
                        }
                    }

                    if (this.enPageRecord.getDeleted()) {
                        FTENSyncRecordUtil.deleteEnPageAfterSync(this.enPageRecord.getNsGUID());
                        this.enPageRecord.getEnNotebook().resetEnPages();
                        this.pageRecords = this.enPageRecord.getEnNotebook().getEnPages();
                    }

                    note = updateResourcesInNote(note);

                    this.updateLastSyncToServer();
                    FTENNotebook enNotebook = this.enPageRecord.getEnNotebook();
                    enNotebook.setLastSynced(note.getUpdated() / 1000);
                    enNotebook.setErrorCode(null);
                    enNotebook.setErrorDescription(null);
                    enNotebook.update();

                    this.pageResources = note.getResources();
                    if (!this.enPageRecord.getDeleted() && this.pageResources != null) {
                        for (Resource updatedPageResource : this.pageResources) {
                            if (updatedPageResource.getAttributes().getFileName().replace(".jpg", "").equals(this.enPageRecord.getNsGUID())) {
                                this.enPageRecord.setEnGUID(updatedPageResource.getGuid());
                            }
                        }
                        if (FTENSyncRecordUtil.getEnPage(this.enPageRecord.getNsGUID()).getLastUpdated() <= currentSyncTime) {
                            this.enPageRecord.setIsDirty(false);
                        }
                    }
                    FTENSyncRecordUtil.updateEnPageAfterSync(this.enPageRecord);
                    Log.d(FTLog.NS_EVERNOTE, "Updated pageResource in DB after EN operation");
                }
            } catch (EDAMNotFoundException | EDAMUserException | EDAMSystemException | TException e) {
                exception = e;
            } catch (Exception e) {
                exception = e;
            }
            listener.onCompletion(enPageRecord, exception);
        });
    }

    //region Resource Creation
    private Resource createResource(FTENPage pageRecord) {
        Resource pageResource = new Resource();
        ResourceAttributes pageResourceAttributes = new ResourceAttributes();
        pageResourceAttributes.setAttachment(false);
        pageResourceAttributes.setFileName(pageRecord.getNsGUID() + ".jpg");
        pageResource.setAttributes(pageResourceAttributes);
        pageResource.setMime("image/jpeg");
        pageResource = generateData(pageResource);
        pageResource.setNoteGuid(pageRecord.getEnNotebook().getEnGUID());
        pageResource.setGuid(pageRecord.getNsGUID());
        return pageResource;
    }
    //endregion

    //region Resource Updation
    private List<Resource> updateResource(Resource pageResource) {
        if (resourceUsageInPages() == 1) {
            Log.d(FTLog.NS_EVERNOTE, "Updating pageResource...");
            deleteResource(pageResource.getGuid());
        }
        this.pageResources.add(createResource(this.enPageRecord));
        return this.pageResources;
    }
    //endregion

    //region Resource Deletion
    private void deleteResource(String resourceGUID) {
        if (!this.pageResources.isEmpty()) {
            Iterator<Resource> pageResourceIterator = this.pageResources.iterator();
            while (pageResourceIterator.hasNext()) {
                if (pageResourceIterator.next().getGuid().equals(resourceGUID)) {
                    if (resourceUsageInPages() == 1) {
                        pageResourceIterator.remove();
                    }
                    break;
                }
            }
        }
    }
    //endregion

    //region Content Generation
    private String getNoteContent() {
        String noteContent = EvernoteUtil.NOTE_PREFIX;
        int pageCount = 0;
        for (FTENPage pageRecord : this.pageRecords) {
            if (!pageRecord.getDeleted()) {
                Resource pageResource = getResourceFromCurrentResources(pageRecord.getNsGUID(), pageRecord.getEnGUID());
                if (pageResource != null) {
                    if (pageResource.getData() == null) {
                        generateData(pageResource);
                    }
                    noteContent += "<div style = \"padding: 0px 0px 0px 0px;margin-bottom:15px;\">";
                    noteContent += "<div style=\"max-width:" + pageResource.getWidth() + ";margin:0px auto 0px auto;padding:0px 0px 0px 0px;display:block;background-color:white;background-color:#ffffff;box-shadow:1px 1px 3px rgba(0,0,0,.25);-webkit-box-shadow:1px 1px 3px rgba(0,0,0,.25);border-radius:7px;\">";
                    noteContent += "<en-media style=\"margin: 0px; padding:0px; border-radius:7px;\" width=\"" + pageResource.getWidth() + "\" height=\"" + pageResource.getHeight() + "\" hash=\"" + EvernoteUtil.bytesToHex(pageResource.getData().getBodyHash()) + "\" type=\"" + pageResource.getMime() + "\"/>";
                    noteContent += "</div>";
                    noteContent += "<div style=\"color:rgb(128, 128, 128);margin-top:5px;text-align:center;\">";
                    noteContent += " Page " + (++pageCount) + " of " + this.pageRecords.size();
                    noteContent += "</div>";
                    noteContent += "</div>";
                }
            }
        }
        noteContent += EvernoteUtil.NOTE_SUFFIX;
        return noteContent;
    }
    //endregion

    //region Data Generation
    private Resource generateData(Resource pageResource) {
        Bitmap bitmap = generateImageForPage();
        if (bitmap != null) {
            try {
                //Convert bitmap to byte[]
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bytes = stream.toByteArray();
                stream.flush();
                stream.close();
                //Create data object and set body with hash
                Data data = new Data();
                data.setBody(bytes);
                data.setBodyHash(EvernoteUtil.hash(bytes));
                pageResource.setWidth((short) bitmap.getWidth());
                pageResource.setHeight((short) bitmap.getHeight());
                pageResource.setData(data);
                return pageResource;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pageResource;
    }
    //endregion

    //region Page Image Generation
    private Bitmap generateImageForPage() {
        Bitmap bitmap = null;
        Context context = FTApp.getInstance().getCurActCtx();
        FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(FTUrl.parse(this.enPageRecord.getEnNotebook().getUrl()));
        document.openDocumentWhileInBackground();
        for (FTNoteshelfPage pageToConsider : document.pages(context)) {
            if (this.enPageRecord.getNsGUID().equals(pageToConsider.uuid)) {
                FTLock lock = new FTLock();
                int[] texture = new int[1];
                texture[0] = 0;
                FTTextureManager.sharedInstance().texture(pageToConsider, 1, new FTTextureManager.TextureGenerationCallBack() {
                    @Override
                    public void onCompletion(int textureID) {
                        texture[0] = textureID;
                        lock.signal();
                    }
                });
                lock.waitTillSignal();
                FTOffscreenBitmap bitmapInfo = renderManager.generateFullImage(pageToConsider.getPageAnnotations(),
                        texture[0],
                        pageToConsider.getPageRect(),
                        0.0f,
                        false,
                        1,
                        "EN image generation");
                bitmap = bitmapInfo.image;
                break;
            }
        }
        return bitmap;
    }
    //endregion

    //region Note Helper Methods
    private Note updateResourcesInNote(Note note) throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        note.setResources(this.pageResources);
        note.setContent(getNoteContent());
        return noteStoreClient.updateNote(note);
    }
    //endregion

    //region User Info methods
    private void updateLastSyncToServer() {
        Date date = new Date(System.currentTimeMillis());
        Format simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm aaa", Locale.getDefault());
        FTApp.getPref().save(SystemPref.EVERNOTE_LAST_SYNC, simpleDateFormat.format(date));
    }
    //endregion

    //region Helper methods
    private int resourceUsageInPages() {
        int count = 0;
        for (FTENPage pageRecord : this.pageRecords) {
            if (pageRecord.getEnGUID().equals(this.enPageRecord.getEnGUID())) {
                ++count;
            }
        }
        return count;
    }

    private Resource getResourceFromCurrentResources(String pageGUID, String resourceGUID) {
        Resource resource = null;
        for (Resource pageResource : this.pageResources) {
            if (pageResource.getGuid().equals(pageGUID) || pageResource.getGuid().equals(resourceGUID)) {
                resource = pageResource;
                break;
            }
        }
        return resource;
    }
    //endregion
}