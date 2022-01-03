package com.fluidtouch.noteshelf.globalsearch.models;

import com.fluidtouch.noteshelf.documentframework.FTUrl;

/**
 * Create by Vineet 25/09/2019
 **/

public class FTSearchResultBook extends FTSearchSectionContent {
    private String title;
    private FTUrl fileURL;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public FTUrl getFileURL() {
        return fileURL;
    }

    public void setFileURL(FTUrl fileURL) {
        this.fileURL = fileURL;
    }
}