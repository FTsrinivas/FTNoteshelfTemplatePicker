package com.fluidtouch.noteshelf.document.thumbnailview;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;

public interface BookmarkListener {
    void showBookmarkDialog(FTNoteshelfPage page, int position);
}