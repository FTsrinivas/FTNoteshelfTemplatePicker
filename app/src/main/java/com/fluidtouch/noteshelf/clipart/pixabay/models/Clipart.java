package com.fluidtouch.noteshelf.clipart.pixabay.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sreenu on 26/04/19
 */
public class Clipart {
    @SerializedName("previewURL")
    @Expose
    private String previewURL;
    @SerializedName("largeImageURL")
    @Expose
    private String largeImageURL;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("views")
    @Expose
    private Integer views;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("tags")
    @Expose
    private String tags;
    @SerializedName("user")
    @Expose
    private String user;

    public String getLargeImageURL() {
        return largeImageURL;
    }

    public void setLargeImageURL(String largeImageURL) {
        this.largeImageURL = largeImageURL;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }
}
