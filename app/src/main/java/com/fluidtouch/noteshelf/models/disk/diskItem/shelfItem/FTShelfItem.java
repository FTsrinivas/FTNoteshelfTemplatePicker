package com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.FTDiskItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;


public class FTShelfItem extends FTDiskItem implements Parcelable {
    //basic parameters
//    private Date fileModificationDate;
//    private Date fileCreationDate;

    public FTGroupItem parent;
    public FTShelfItemCollection shelfCollection;
    boolean enSyncEnabled;

    public FTShelfItem(FTUrl fileURL) {
        super(fileURL);
    }

    protected FTShelfItem(Parcel in) {
        super(in);
        parent = in.readParcelable(FTGroupItem.class.getClassLoader());
        shelfCollection = in.readParcelable(FTShelfItemCollection.class.getClassLoader());
        enSyncEnabled = in.readByte() != 0;
    }

    public static final Creator<FTShelfItem> CREATOR = new Creator<FTShelfItem>() {
        @Override
        public FTShelfItem createFromParcel(Parcel in) {
            return new FTShelfItem(in);
        }

        @Override
        public FTShelfItem[] newArray(int size) {
            return new FTShelfItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(parent, flags);
        dest.writeParcelable(shelfCollection, flags);
        dest.writeByte((byte) (enSyncEnabled ? 1 : 0));
    }

    public Date getFileModificationDate() {
        File notebook = new File(this.getFileURL().getPath());
        Date modifiedDate = new Date(notebook.lastModified());
        return modifiedDate;
    }

    public Date getFileCreationDate() {
        Date date = new Date();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                File file = new File(getFileURL().getPath());
                if (file.exists()) {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    if (attr != null) {
                        long creationDate = attr.creationTime().toMillis();
                        date.setTime(creationDate);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public FTGroupItem getParent() {
        return parent;
    }

    public void setParent(FTGroupItem parent) {
        this.parent = parent;
    }

    public FTShelfItemCollection getShelfCollection() {
        return shelfCollection;
    }

    public void setShelfCollection(FTShelfItemCollection shelfCollection) {
        this.shelfCollection = shelfCollection;
    }

    public boolean isEnSyncEnabled() {
        return enSyncEnabled;
    }
}
