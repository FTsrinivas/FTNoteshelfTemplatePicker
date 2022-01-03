package com.fluidtouch.noteshelf.globalsearch.processsors;

import android.content.Context;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchCallback;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultBook;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSectionTitle;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vineet on 24/9/2019
 */
public class FTNotebookTitleSearchProcessor implements FTSearchProcessor {
    private Context context;
    private String searchKey;
    private FTGlobalSearchCallback callback;
    private String token = FTDocumentUtils.getUDID();
    private List<FTShelfItemCollection> categories;
    private List<FTShelfItem> notebooks;
    private int loopingIndex = 0;

    FTNotebookTitleSearchProcessor(Context context, FTGlobalSearchCallback callback) {
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
        this.searchKey = searchKey.toLowerCase().trim();
        processAllBooksForTitles();
        return this.token;
    }

    private void processAllBooksForTitles() {
        if (loopingIndex >= this.categories.size()) {
            callback.onSearchingFinished();
            return;
        }
        categories.get(loopingIndex).shelfItems(context, FTShelfSortOrder.BY_NAME, null, "",
                (notebooks, error) -> {
                    if (!notebooks.isEmpty()) {
                        FTSearchSectionTitle titleSection = new FTSearchSectionTitle(); //Category as title
                        titleSection.title = FTNotebookTitleSearchProcessor.this.categories.get(loopingIndex).getDisplayTitle(context);
                        titleSection.items = new ArrayList<>();
                        for (FTShelfItem notebook : notebooks) {
                            if (notebook instanceof FTGroupItem) {
                                for (FTShelfItem groupNotebook : ((FTGroupItem) notebook).getChildren()) {
                                    String displayName = groupNotebook.getDisplayTitle(context).toLowerCase();
                                    if (displayName.contains(searchKey.toLowerCase())) {
                                        FTSearchResultBook book = new FTSearchResultBook(); //Notebook as search item
                                        book.setTitle(groupNotebook.getDisplayTitle(context));
                                        book.setFileURL(groupNotebook.getFileURL());
                                        titleSection.items.add(book);
                                    }
                                }
                            } else {
                                String displayName = notebook.getDisplayTitle(context).toLowerCase();
                                if (displayName.contains(searchKey.toLowerCase())) {
                                    FTSearchResultBook book = new FTSearchResultBook(); //Notebook as search item
                                    book.setTitle(notebook.getDisplayTitle(context));
                                    book.setFileURL(notebook.getFileURL());
                                    titleSection.items.add(book);
                                }
                            }
                        }
                        if (!titleSection.items.isEmpty()) {
                            titleSection.itemCount = titleSection.items.size();
                            callback.onSectionFinding(titleSection);
                        }
                    }
                    loopingIndex++;
                    FTNotebookTitleSearchProcessor.this.processAllBooksForTitles();
                });
    }

    @Override
    public void cancelSearching() {
    }
}