package com.fluidtouch.noteshelf.globalsearch.processsors;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchCallback;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchType;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;

import java.util.List;

import kr.pe.burt.android.lib.androidoperationqueue.AndroidOperationQueue;

/**
 * Created by Vineet on 24/9/2019
 */

public class FTNotebookAllSearchProcessor implements FTSearchProcessor {

    private Context context;
    private String searchKey;
    private String token = FTDocumentUtils.getUDID();
    private List<FTShelfItemCollection> categories;
    private List<FTShelfItem> notebooks;
    private FTGlobalSearchCallback callback;
    private FTSearchProcessor currentProcessor;
    private String processingToken = "";
    private AndroidOperationQueue operationQueue = new AndroidOperationQueue("GlobalSearchOperation");

    FTNotebookAllSearchProcessor(Context context, FTGlobalSearchCallback callback) {
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
        FTLog.crashlyticsLog("Global Search: Started global search");
        Log.d("globalSearch", "Started Global Search...");
        this.searchKey = searchKey;
        operationQueue.addOperation((queue, bundle) -> processAllBooksForTitles());
        operationQueue.start();
        return this.token;
    }

    @Override
    public void cancelSearching() {
        if (this.currentProcessor != null) {
            this.currentProcessor.cancelSearching();
        }
        operationQueue.stop();
    }

    private void processAllBooksForTitles() {
        this.currentProcessor = FTSearchProcessorFactory.getProcessor(context, FTGlobalSearchType.TITLES, new FTGlobalSearchCallback() {
            @Override
            public void onSectionFinding(FTSearchSection searchSection) {
                callback.onSectionFinding(searchSection);
            }

            @Override
            public void onSearchFinding(FTNoteshelfDocument document) {

            }

            @Override
            public void onSearchingFinished() {
                processAllBooksForContent();
            }
        });
        this.currentProcessor.setDataToProcess(this.categories, this.notebooks);
        this.processingToken = this.currentProcessor.startProcessing(this.searchKey);
    }

    private void processAllBooksForContent() {
        this.currentProcessor = FTSearchProcessorFactory.getProcessor(context, FTGlobalSearchType.CONTENT, new FTGlobalSearchCallback() {
            @Override
            public void onSectionFinding(FTSearchSection searchSection) {
                callback.onSectionFinding(searchSection);
            }

            @Override
            public void onSearchFinding(FTNoteshelfDocument document) {
                callback.onSearchFinding(document);
            }

            @Override
            public void onSearchingFinished() {
                callback.onSearchingFinished();
                Log.d("globalSearch", "Completed Global Search...");
                FTLog.crashlyticsLog("Global Search: Completed global search");
            }
        });
        this.currentProcessor.setDataToProcess(this.categories, this.notebooks);
        this.processingToken = this.currentProcessor.startProcessing(this.searchKey);
    }
}