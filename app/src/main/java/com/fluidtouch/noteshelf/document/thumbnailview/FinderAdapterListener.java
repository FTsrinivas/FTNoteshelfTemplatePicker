package com.fluidtouch.noteshelf.document.thumbnailview;

import android.view.View;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;

import java.util.ArrayList;

public interface FinderAdapterListener {
    void onSelectUpdateUI();

    void displayThumbnailAsPage(int position);

    void swapPages(int fromPosition, int toIndex);

    void noBookmarkedPages();

    boolean isShowingBookmarks();

    void checkPageCountAndUpdate();

    void showBookmarkDialog(View view, FTNoteshelfPage page);

    boolean isExportMode();

    boolean isEditMode();

    ArrayList<FTNoteshelfPage> selectedPages();

    int currentPageIndex();
}