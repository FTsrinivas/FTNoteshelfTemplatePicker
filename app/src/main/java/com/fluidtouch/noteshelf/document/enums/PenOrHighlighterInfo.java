package com.fluidtouch.noteshelf.document.enums;

import android.os.Parcel;
import android.os.Parcelable;

public class PenOrHighlighterInfo implements Parcelable {

    int penRackType;
    int penSize;
    int penColor;
    String penType;
    int penTypeID;

    public int getPenTypeID() {
        return penTypeID;
    }

    public void setPenTypeID(int penTypeID) {
        this.penTypeID = penTypeID;
    }

    private PenOrHighlighterInfo() {
        //Stub
    }

    public static PenOrHighlighterInfo getInfo(int penRackType, int penColor, int penSize, String penType) {
        PenOrHighlighterInfo info = new PenOrHighlighterInfo();
        info.penRackType = penRackType;
        info.penColor = penColor;
        info.penSize = penSize;
        info.penType = penType;
        return info;
    }

    public int getPenRackType() {
        return penRackType;
    }

    public void setPenRackType(int penRackType) {
        this.penRackType = penRackType;
    }

    public int getPenSize() {
        return penSize;
    }

    public void setPenSize(int penSize) {
        this.penSize = penSize;
    }

    public int getPenColor() {
        return penColor;
    }

    public void setPenColor(int penColor) {
        this.penColor = penColor;
    }

    public String getPenType() {
        return penType;
    }

    public void setPenType(String penType) {
        this.penType = penType;
    }

    public static Creator<PenOrHighlighterInfo> getCREATOR() {
        return CREATOR;
    }

    public PenOrHighlighterInfo(Parcel in) {
        penRackType = in.readInt();
        penSize = in.readInt();
        penColor = in.readInt();
        penType = in.readString();
        penTypeID = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(penRackType);
        dest.writeInt(penSize);
        dest.writeInt(penColor);
        dest.writeString(penType);
        dest.writeInt(penTypeID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PenOrHighlighterInfo> CREATOR = new Creator<PenOrHighlighterInfo>() {
        @Override
        public PenOrHighlighterInfo createFromParcel(Parcel in) {
            return new PenOrHighlighterInfo(in);
        }

        @Override
        public PenOrHighlighterInfo[] newArray(int size) {
            return new PenOrHighlighterInfo[size];
        }
    };
}
