package com.fluidtouch.noteshelf.shelf.listeners;

import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;

public interface ShelfOnGroupingActionsListener {
    void startGrouping(FTShelfItem draggingItem, FTShelfItem mergingItem, int draggingPosition, int endPosition);
}
