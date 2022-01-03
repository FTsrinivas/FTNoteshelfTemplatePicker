package com.fluidtouch.noteshelf.globalsearch.processsors;

import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;

import java.util.List;

/**
 * Created by Vineet on 24/9/2019
 */
public interface FTSearchProcessor {

    void setDataToProcess(List<FTShelfItemCollection> categories, List<FTShelfItem> notebooks);

    String startProcessing(String searchKey);

    void cancelSearching();
}