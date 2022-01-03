package com.fluidtouch.noteshelf.documentproviders;

import java.util.List;

public interface ShelfItemCollectionBlock {
    void didFetchCollectionItems(List<FTShelfItemCollection> shelfs);
}
