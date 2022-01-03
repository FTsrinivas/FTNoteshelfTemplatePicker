package com.noteshelf.cloud.backup;

import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;

/**
 * Created by sreenu on 03/12/20.
 */
public interface FTRestoreHandlerCallback {
    void onBookRestored(FTDocumentItem documentItem, Error error);

    void onRestoreCompleted(Error error);
}
