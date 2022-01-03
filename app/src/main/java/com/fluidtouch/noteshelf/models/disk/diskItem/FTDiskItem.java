package com.fluidtouch.noteshelf.models.disk.diskItem;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;

public class FTDiskItem implements Parcelable {
    FTUrl fileURL;
    String uuid = FTDocumentUtils.getUDID();
    RKShelfItemType type = RKShelfItemType.DOCUMENT;
    private String title;
    private String displayTitle;

    public FTDiskItem(FTUrl fileURL) {
        super();
        this.fileURL = fileURL;
    }

    protected FTDiskItem(Parcel in) {
        fileURL = new FTUrl(in.readString());
        uuid = in.readString();
        type = RKShelfItemType.getType(in.readInt());
        title = in.readString();
        displayTitle = in.readString();
    }

    public static final Creator<FTDiskItem> CREATOR = new Creator<FTDiskItem>() {
        @Override
        public FTDiskItem createFromParcel(Parcel in) {
            return new FTDiskItem(in);
        }

        @Override
        public FTDiskItem[] newArray(int size) {
            return new FTDiskItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileURL.getPath());
        dest.writeString(uuid);
        dest.writeInt(type.ordinal());
        dest.writeString(title);
        dest.writeString(displayTitle);
    }

    public String getTitle(Context context) {
        return FTDocumentUtils.getFileNameWithoutExtension(context, this.fileURL);
    }

    public String getDisplayTitle(Context context) {
        return FTDocumentUtils.getFileNameWithoutExtension(context, this.fileURL);
    }

    public FTUrl getFileURL() {
        return this.fileURL;
    }

    public void setFileURL(FTUrl path) {
        this.fileURL = path;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RKShelfItemType getType() {
        return type;
    }

    public void setType(RKShelfItemType type) {
        this.type = type;
    }
}