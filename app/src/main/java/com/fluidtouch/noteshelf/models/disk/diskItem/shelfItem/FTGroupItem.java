package com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem;

import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.util.ArrayList;
import java.util.List;

public class FTGroupItem extends FTShelfItem implements Parcelable {

    private List<FTShelfItem> children = new ArrayList<>();

    public FTGroupItem(FTUrl fileURL) {
        super(fileURL);
    }

    protected FTGroupItem(Parcel in) {
        super(in);
        children = in.createTypedArrayList(FTShelfItem.CREATOR);
    }

    public static final Creator<FTGroupItem> CREATOR = new Creator<FTGroupItem>() {
        @Override
        public FTGroupItem createFromParcel(Parcel in) {
            return new FTGroupItem(in);
        }

        @Override
        public FTGroupItem[] newArray(int size) {
            return new FTGroupItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(children);
    }

    public List<FTShelfItem> getChildren() {
        return children;
    }

    public void setChildren(List<FTShelfItem> children) {
        this.children = children;
    }

    public void addChild(FTShelfItem _child) {
        children.add(_child);
    }

    public void removeChild(FTShelfItem _child) {
        for (int i = 0; i < children.size(); i++) {
            FTShelfItem shelfItem = children.get(i);
            if (shelfItem.getFileURL().equals(_child.getFileURL())) {
                children.remove(i);
                break;
            }
        }
    }

    public void deleteGroupIfEmpty() {
        if (getChildren().isEmpty()) {
            if (getShelfCollection() != null) {
                getShelfCollection().removeChild(this);
            }
            FTFileManagerUtil.deleteRecursive(getFileURL().getPath());
        }
    }
}
