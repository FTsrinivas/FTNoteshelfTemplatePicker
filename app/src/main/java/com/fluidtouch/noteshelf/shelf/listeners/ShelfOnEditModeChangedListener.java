package com.fluidtouch.noteshelf.shelf.listeners;

public interface ShelfOnEditModeChangedListener {
    void onEditModeChanged(boolean isInEditMode, int count);

    void onSelectedItemsCountChanged(boolean isInEditMode, int count);
}
