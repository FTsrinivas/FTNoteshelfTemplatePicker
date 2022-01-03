package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import android.content.Context;
import android.graphics.RectF;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPlist;

import java.util.ArrayList;

public class FTNSDocumentInfoPlistItem extends FTFileItemPlist {
    RectF defaultPageRect = new RectF();
    FTNoteshelfDocument parentDocument;
    private ArrayList<FTNoteshelfPage> currentPages;

    public FTNSDocumentInfoPlistItem(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTNSDocumentInfoPlistItem(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public ArrayList<FTNoteshelfPage> getPages(Context context) {
        if (this.currentPages == null || this.currentPages.isEmpty()) {
            ArrayList<FTNoteshelfPage> noteshelfPages = new ArrayList<>();

            NSDictionary rootDict = this.contentDictionary(context);
            if (rootDict.objectForKey("pages") != null) {
                NSArray localPages = (NSArray) this.contentDictionary(context).objectForKey("pages");
                for (int i = 0; i < localPages.count(); i++) {
                    NSDictionary dict = (NSDictionary) localPages.objectAtIndex(i);
                    FTNoteshelfPage page = new FTNoteshelfPage(context, dict);
                    page.setParentDocument(this.parentDocument);
                    noteshelfPages.add(page);
                }
            }

            this.currentPages = noteshelfPages;
        }

        return this.currentPages;
    }

    public void setPages(Context context, ArrayList<FTNoteshelfPage> pages) {
        this.currentPages = pages;
        this.contentDictionary(context).put("pages", pages);
        this.isModified = true;
    }

    public void insertPage(Context context, FTNoteshelfPage page, Integer index) {
        if (this.getPages(context) != null) {
            this.currentPages.add(index, page);
            this.isModified = true;
        }
    }

    void deletePage(Context context, FTNoteshelfPage page) {
        if (this.getPages(context) != null) {
            int index = this.currentPages.indexOf(page);
            if (index >= 0) {
                this.currentPages.remove(index);
            }
            this.isModified = true;
        }
    }

    void movePage(FTNoteshelfPage page, Integer toIndex) {
        synchronized (this) {
            //ArrayList<HashMap<String, Integer>> pageIndices = new ArrayList<>();
            int index = this.currentPages.indexOf(page);
            if (index >= 0) {
                this.currentPages.remove(index);
                if (toIndex > currentPages.size()) {
                    toIndex = currentPages.size();
                }
                this.currentPages.add(toIndex, page);

//                HashMap<String, Integer> dic = new HashMap<>();
//                dic.put("old", index);
//                dic.put("new", toIndex);
//                pageIndices.add(dic);
//                int pageCount = this.pages.size();
//                int startIndex = toIndex + 1;
//                for (int i = startIndex; i < pageCount; i++) {
//                    HashMap<String, Integer> dic2 = new HashMap<>();
//                    dic2.put("old", i - 1);
//                    dic2.put("new", i);
//                    pageIndices.add(dic2);
//                }
            }
        }

    }

    @Override
    public Boolean saveContentsOfFileItem(Context context) {
        if (this.getPages(context).size() > 0) {
            int pagesSize = this.currentPages.size();
            NSArray pages = new NSArray(pagesSize);
            for (int i = 0; i < pagesSize; ++i) {
                if (this.currentPages.get(i) != null) {
                    NSDictionary dict = this.currentPages.get(i).dictionaryRepresentation();
                    pages.setValue(i, dict);
                }
            }
            this.setObject(context, pages, "pages");
        } else {
            NSArray pages = new NSArray(0);
            this.setObject(context, pages, "pages");
        }
        return super.saveContentsOfFileItem(context);
    }

    public void unloadContentsOfFileItem() {
        if (!this.isModified && !this.forceSave) {
            this.currentPages.clear();
            this.currentPages = null;
        }
    }
}
