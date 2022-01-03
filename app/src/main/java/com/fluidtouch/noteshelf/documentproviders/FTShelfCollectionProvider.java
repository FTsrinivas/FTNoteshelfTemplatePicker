package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;

import java.util.ArrayList;
import java.util.List;

public class FTShelfCollectionProvider {
    private final FTShelfCollectionLocal localShelfProvider;
    private final FTShelfCollectionSystem systemShelfProvider;
    public final FTShelfCollectionRecent recentShelfProvider;
    public final FTShelfCollectionPinned pinnedShelfProvider;
    private FTShelfCollection cloudShelfProvider; //Currently not using

    private final FTShelfProviderMode providerMode = FTShelfProviderMode.LOCAL;

    private static FTShelfCollectionProvider mInstance;

    private FTShelfCollectionProvider() {
        this.localShelfProvider = new FTShelfCollectionLocal();
        this.systemShelfProvider = new FTShelfCollectionSystem();
        this.recentShelfProvider = new FTShelfCollectionRecent();
        this.pinnedShelfProvider = new FTShelfCollectionPinned();
    }

    public static synchronized FTShelfCollectionProvider getInstance() {
        if (mInstance == null) {
            mInstance = new FTShelfCollectionProvider();
        }
        return mInstance;
    }

    public FTShelfCollection currentProvider() {
        if (this.providerMode == FTShelfProviderMode.LOCAL) {
            return this.localShelfProvider;
        } else {
            return this.cloudShelfProvider;
        }
    }

    public void shelfs(final ShelfItemCollectionBlock onCompletion) {
        List<FTShelfItemCollection> shelfItemCollections = new ArrayList<>();
        Context context = FTApp.getInstance().getCurActCtx();
        this.currentProvider().shelfs(shelfs -> {
            shelfItemCollections.addAll(shelfs);
            shelfItemCollections.sort((first, second) -> first.getDisplayTitle(context).compareToIgnoreCase(second.getDisplayTitle(context)));
            systemShelfProvider.shelfs(systemShelfs -> {
                shelfItemCollections.addAll(systemShelfs);
                onCompletion.didFetchCollectionItems(shelfItemCollections);
            });
        });
    }

    public void recents(ShelfItemCollectionBlock onCompletion) {
        recentShelfProvider.shelfs(onCompletion::didFetchCollectionItems);
    }

    public void pinned(ShelfItemCollectionBlock onCompletion) {
        pinnedShelfProvider.shelfs(onCompletion::didFetchCollectionItems);
    }

    public List<FTShelfItem> allLocalShelfItems(Context context) {
        return localShelfProvider.allLocalShelfItems(context);
    }
}