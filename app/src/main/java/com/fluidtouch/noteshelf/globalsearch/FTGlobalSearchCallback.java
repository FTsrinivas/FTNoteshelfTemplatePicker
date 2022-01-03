package com.fluidtouch.noteshelf.globalsearch;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSection;

import java.io.Serializable;

/**
 * Created by Vineet on 25/9/2019
 */

public interface FTGlobalSearchCallback extends Serializable {
    void onSectionFinding(FTSearchSection searchItem);

    void onSearchFinding(FTNoteshelfDocument document);

    void onSearchingFinished();
}