package com.fluidtouch.noteshelf.store.ui;

import com.fluidtouch.noteshelf.store.model.FTStorePackItem;

public interface FTStoreCallbacks {

    void onStoreItemSelected(FTStorePackItem ftStorePackItem);

    void onDownloadButtonClick(FTStorePackItem ftStorePackItem);

    void onSignClick();

    void onSignOut();

    void onProfileIconClick(int showLocation);

}
