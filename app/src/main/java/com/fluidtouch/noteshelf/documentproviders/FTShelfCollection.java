package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;

public abstract class FTShelfCollection {
    public static void shelfProvider(FTShelfCollectionBlock onCompletion) {

    }

    public static void refreshShelfProvider(FTShelfCollectionBlock onCompletion) {

    }

    public abstract FTShelfItemCollection collectionWithTitle(Context context, String title);

    public abstract void shelfs(FTShelfItemCollectionBlock onCompletion);

    public abstract void createShelfWithTitle(Context context, String title, FTItemCollectionAndErrorBlock block);

    public abstract void deleteShelf(Context context, FTShelfItemCollection shelf, FTItemCollectionAndErrorBlock block);

    public abstract void renameShelf(Context context, String title, FTItemCollectionAndErrorBlock block, FTShelfItemCollection shelf);

    //Completion Block Interface
    public interface FTItemCollectionAndErrorBlock {
        void didFinishForShelfItemCollection(FTShelfItemCollection shelf, Error error);
    }
}
