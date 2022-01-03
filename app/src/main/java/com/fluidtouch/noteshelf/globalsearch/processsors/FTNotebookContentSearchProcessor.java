package com.fluidtouch.noteshelf.globalsearch.processsors;

import android.content.Context;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchCallback;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultPage;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSectionTitle;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by Vineet on 24/9/2019
 */

public class FTNotebookContentSearchProcessor implements FTSearchProcessor {
    private Context context;
    private String searchKey;
    private FTGlobalSearchCallback callback;
    private String token = FTDocumentUtils.getUDID();
    private List<FTShelfItemCollection> categories;
    private List<FTShelfItem> notebooks;
    private boolean cancelledSearch = false;
    private int loopingIndex = 0;

    FTNotebookContentSearchProcessor(Context context, FTGlobalSearchCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void setDataToProcess(List<FTShelfItemCollection> categories, List<FTShelfItem> notebooks) {
        this.categories = categories;
        this.notebooks = notebooks;
    }

    @Override
    public String startProcessing(String searchKey) {
        this.searchKey = searchKey;
        processAllBooksForContent();
        return this.token;
    }

    private void processAllBooksForContent() {
        if (loopingIndex >= notebooks.size() || this.cancelledSearch) {
            callback.onSearchingFinished();
            return;
        }
        Object synchLock = new Object();
        synchronized (synchLock) {
            try {
                if (loopingIndex > 0)
                    synchLock.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FTNoteshelfDocument notebook = FTDocumentFactory.documentForItemAtURL(notebooks.get(loopingIndex).getFileURL());
            notebook.openDocument(context, (success, error) -> {
                if (success) {
                    FTLog.debug(FTLog.GLOBAL_SEARCH, "Processing notebook " + notebook.getDisplayTitle(context));
                    FTSearchSectionTitle titleSection = new FTSearchSectionTitle(); //Notebook as title
                    titleSection.title = notebook.getDisplayTitle(context);
                    titleSection.categoryName = notebooks.get(loopingIndex).getShelfCollection().getDisplayTitle(context);
                    titleSection.type = "NOTEBOOK";
                    titleSection.items = new ArrayList<>();
                    Observer searchObserver = (o, arg) -> {
                        FTNoteshelfPage searchPage = (FTNoteshelfPage) arg;
                        FTSearchResultPage page = new FTSearchResultPage(); //Notebook as search item
                        page.setUuid(searchPage.uuid);
                        page.setPageIndex(searchPage.pageIndex());
                        page.setPdfPageRect(searchPage.getPdfPageRect());
                        page.setBookURL(searchPage.getParentDocument().getFileURL());
                        page.setSearchableItems(searchPage.getSearchableItems());
                        page.setNoteshelfPage(searchPage);
                        titleSection.items.add(page);
                        if (cancelledSearch) {
                            notebook.stopSearching();
                            callback.onSearchingFinished();
                        }
                    };
                    ObservingService.getInstance().addObserver("searchObserver_" + notebook.getDocumentUUID(), searchObserver);
                    notebook.searchDocumentForKey(context, searchKey, cancelled -> {
                        ObservingService.getInstance().removeObserver("searchObserver_" + notebook.getDocumentUUID(), searchObserver);
                        if (cancelledSearch) {
                            notebook.stopSearching();
                            callback.onSearchingFinished();
                        } else {
                            if (!titleSection.items.isEmpty()) {
                                FTLog.debug(FTLog.GLOBAL_SEARCH, "Found searchItems count = " + titleSection.items.size() + " for notebook " + titleSection.title);
                                titleSection.itemCount = titleSection.items.size();
                                callback.onSearchFinding(notebook);
                                callback.onSectionFinding(titleSection);
                            }
                            loopingIndex++;
                            processAllBooksForContent();
                        }
                    });
                } else {
                    FTLog.error(FTLog.GLOBAL_SEARCH, "Failed processing notebook " + notebook.getDisplayTitle(context));
                    loopingIndex++;
                    processAllBooksForContent();
                }
            });
        }
    }

    @Override
    public void cancelSearching() {
        this.cancelledSearch = true;
    }
}