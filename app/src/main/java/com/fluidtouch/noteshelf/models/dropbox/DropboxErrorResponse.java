package com.fluidtouch.noteshelf.models.dropbox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sreenu on 26/02/19
 */
public class DropboxErrorResponse {
    @SerializedName(".tag")
    @Expose
    private String tag;
    @SerializedName("from_lookup")
    @Expose
    private String fromLookup;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFromLookup() {
        return fromLookup;
    }

    public void setFromLookup(String fromLookup) {
        this.fromLookup = fromLookup;
    }
}
