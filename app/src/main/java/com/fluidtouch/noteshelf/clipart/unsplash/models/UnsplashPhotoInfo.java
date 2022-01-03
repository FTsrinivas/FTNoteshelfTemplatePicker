package com.fluidtouch.noteshelf.clipart.unsplash.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UnsplashPhotoInfo implements Parcelable {

    private String id;
    private String thumbURL;
    private String regularURL;
    private String alt_description;
    private UnsplashPhotoOwner mUnsplashPhotoOwner;

    public String getRegularURL() {
        return regularURL;
    }

    public void setRegularURL(String regularURL) {
        this.regularURL = regularURL;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

    public UnsplashPhotoInfo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlt_description() {
        return alt_description;
    }

    public void setAlt_description(String alt_description) {
        this.alt_description = alt_description;
    }

    public UnsplashPhotoOwner getmUnsplashPhotoOwner() {
        return mUnsplashPhotoOwner;
    }

    public void setmUnsplashPhotoOwner(UnsplashPhotoOwner mUnsplashPhotoOwner) {
        this.mUnsplashPhotoOwner = mUnsplashPhotoOwner;
    }

    protected UnsplashPhotoInfo(Parcel in) {
        id = in.readString();
        thumbURL = in.readString();
        regularURL = in.readString();
        alt_description = in.readString();
        mUnsplashPhotoOwner = in.readParcelable(UnsplashPhotoOwner.class.getClassLoader());
    }

    public static final Creator<UnsplashPhotoInfo> CREATOR = new Creator<UnsplashPhotoInfo>() {
        @Override
        public UnsplashPhotoInfo createFromParcel(Parcel in) {
            return new UnsplashPhotoInfo(in);
        }

        @Override
        public UnsplashPhotoInfo[] newArray(int size) {
            return new UnsplashPhotoInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(thumbURL);
        dest.writeString(regularURL);
        dest.writeString(alt_description);
        dest.writeParcelable(mUnsplashPhotoOwner, flags);
    }
}
