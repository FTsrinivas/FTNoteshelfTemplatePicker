package com.fluidtouch.noteshelf.clipart.unsplash.models;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class UnsplashPhotoOwner implements Parcelable {

    private String id;
    private String username;
    private String name;
    private String first_name;
    private String portfolio_url;
    private String html;

    public UnsplashPhotoOwner() {

    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getPortfolio_url() {
        return portfolio_url;
    }

    public void setPortfolio_url(String portfolio_url) {
        this.portfolio_url = portfolio_url;
    }

    protected UnsplashPhotoOwner(Parcel in) {
        id = in.readString();
        username = in.readString();
        name = in.readString();
        first_name = in.readString();
        portfolio_url = in.readString();
        html = in.readString();
    }

    public static final Creator<UnsplashPhotoOwner> CREATOR = new Creator<UnsplashPhotoOwner>() {
        @Override
        public UnsplashPhotoOwner createFromParcel(Parcel in) {
            return new UnsplashPhotoOwner(in);
        }

        @Override
        public UnsplashPhotoOwner[] newArray(int size) {
            return new UnsplashPhotoOwner[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(name);
        dest.writeString(first_name);
        dest.writeString(portfolio_url);
        dest.writeString(html);
    }
}
