package com.fluidtouch.noteshelf.globalsearch.models;

import android.graphics.RectF;

import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.util.ArrayList;

/**
 * Create by Vineet 25/09/2019
 **/

public class FTSearchResultPage extends FTSearchSectionContent {
    private String uuid;
    private RectF pdfPageRect;
    private FTUrl bookURL;
    private int pageIndex;
    private ArrayList<FTSearchableItem> searchableItems;
    private FTNoteshelfPage noteshelfPage;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RectF getPdfPageRect() {
        return pdfPageRect;
    }

    public void setPdfPageRect(RectF pdfPageRect) {
        this.pdfPageRect = pdfPageRect;
    }

    public FTUrl getBookURL() {
        return bookURL;
    }

    public void setBookURL(FTUrl bookURL) {
        this.bookURL = bookURL;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public ArrayList<FTSearchableItem> getSearchableItems() {
        return searchableItems;
    }

    public void setSearchableItems(ArrayList<FTSearchableItem> searchableItems) {
        this.searchableItems = new ArrayList<>();
        for (int i = 0; i < searchableItems.size(); i++) {
            this.searchableItems.add(searchableItems.get(i));
        }
    }

    public FTNoteshelfPage getNoteshelfPage() {
        return noteshelfPage;
    }

    public void setNoteshelfPage(FTNoteshelfPage noteshelfPage) {
        this.noteshelfPage = noteshelfPage;
    }
}