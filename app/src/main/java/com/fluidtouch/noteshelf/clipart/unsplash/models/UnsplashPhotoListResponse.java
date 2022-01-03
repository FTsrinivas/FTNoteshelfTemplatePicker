package com.fluidtouch.noteshelf.clipart.unsplash.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class UnsplashPhotoListResponse implements Parcelable {

    List<UnsplashPhotoInfo> mUnsplashPhotosList;

    public UnsplashPhotoListResponse() {
    }

    public List<UnsplashPhotoInfo> getmUnsplashPhotosList() {
        return mUnsplashPhotosList;
    }

    public void setmUnsplashPhotosList(List<UnsplashPhotoInfo> mUnsplashPhotosList) {
        this.mUnsplashPhotosList = mUnsplashPhotosList;
    }

    protected UnsplashPhotoListResponse(Parcel in) {
        mUnsplashPhotosList = in.createTypedArrayList(UnsplashPhotoInfo.CREATOR);
    }

    public static final Creator<UnsplashPhotoListResponse> CREATOR = new Creator<UnsplashPhotoListResponse>() {
        @Override
        public UnsplashPhotoListResponse createFromParcel(Parcel in) {
            return new UnsplashPhotoListResponse(in);
        }

        @Override
        public UnsplashPhotoListResponse[] newArray(int size) {
            return new UnsplashPhotoListResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mUnsplashPhotosList);
    }
}
