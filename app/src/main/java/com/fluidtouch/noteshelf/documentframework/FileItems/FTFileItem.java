package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class FTFileItem implements Parcelable {
    public String fileName;
    public FTFileItem parent;
    public ArrayList<FTFileItem> children;
    public boolean isDirectory;
    public boolean isModified = false;
    public boolean forceSave = false;
    public boolean deleted = false;
    public Object content;
    private FTUrl fileItemURL;

    //******************************* Constructor Methods
    public FTFileItem(Context context, FTUrl fileURL, Boolean isDirectory) {
        this.isDirectory = isDirectory;
        this.setFileItemURL(fileURL);
        this.fileName = FTDocumentUtils.getFileName(context, fileURL);
        this.children = new ArrayList<>();
    }

    public FTFileItem(String fileName, Boolean isDirectory) {
        this.isDirectory = isDirectory;
        this.fileName = fileName;
        this.children = new ArrayList<>();
    }

    protected FTFileItem(Parcel in) {
        fileName = in.readString();
        parent = in.readParcelable(FTFileItem.class.getClassLoader());
        children = in.createTypedArrayList(FTFileItem.CREATOR);
        isDirectory = in.readByte() != 0;
        isModified = in.readByte() != 0;
        forceSave = in.readByte() != 0;
        deleted = in.readByte() != 0;
        fileItemURL = new FTUrl(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeParcelable(parent, flags);
        dest.writeTypedList(children);
        dest.writeByte((byte) (isDirectory ? 1 : 0));
        dest.writeByte((byte) (isModified ? 1 : 0));
        dest.writeByte((byte) (forceSave ? 1 : 0));
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeString(fileItemURL.getPath());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FTFileItem> CREATOR = new Creator<FTFileItem>() {
        @Override
        public FTFileItem createFromParcel(Parcel in) {
            return new FTFileItem(in);
        }

        @Override
        public FTFileItem[] newArray(int size) {
            return new FTFileItem[size];
        }
    };

    public FTUrl getFileItemURL() {
        if (this.parent != null) {
            FTUrl baseUri = this.parent.getFileItemURL();
            return FTUrl.withAppendedPath(baseUri, this.fileName);
        }
        return this.fileItemURL;
    }

    public void setFileItemURL(FTUrl fileURL) {
        this.fileItemURL = fileURL;
    }

    //******************************* Operations

    public Object getContent(Context context) {
        if (this.content == null) {
            return this.loadContentsOfFileItem(context);
        }
        return this.content;
    }

    public void setContent(Object newContent) {
//        if (this.content != newContent) {
        this.content = newContent;
        this.isModified = true;
//        }
    }

    public void updateContent(Object content) {
        this.content = content;
        this.isModified = true;
        this.deleted = false;
    }

    public void deleteContent() {
        this.content = null;
        this.deleted = true;
        this.isModified = false;
    }

    public Boolean writeUpdatesToURL(URL url, Error error) {

        return true;
    }

    public void addChildItem(FTFileItem childItem) {
        if (this.isDirectory) {
            this.children.add(childItem);
            childItem.parent = this;
        }
    }

    public void deleteChildItem(FTFileItem childItem) {
        if (this.isDirectory) {
            this.children.remove(childItem);
        }
    }

    public FTFileItem childFileItemWithName(String fileName) {
        FTFileItem childItem = null;
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i) != null && this.children.get(i).fileName.equals(fileName)) {
                childItem = this.children.get(i);
                break;
            }
        }
        return childItem;
    }

    public Boolean deleteFileItem() {
        //Subclasses should override to customize deletion
        Boolean success = true;
        File file = new File(this.getFileItemURL().getPath());
        if (file.exists()) {
            success = file.delete();
        }
        return success;
    }

    //******************************* Disk Operations
    public boolean writeUpdatesToURL(Context context, FTUrl fileURL) {
        boolean success = true;

        if (this.deleted) {
            success = this.deleteFileItem();
            if (success)
                this.deleted = false;
        } else if (this.isDirectory) {
            File file = new File(this.getFileItemURL().getPath());
            if (!file.exists()) {
                success = success && file.mkdirs();
            }
            for (int i = 0; i < this.children.size(); i++) {
                FTFileItem fileItem = this.children.get(i);
                fileItem.writeUpdatesToURL(context, this.getFileItemURL());
            }
        } else if (this.isModified || this.forceSave) {
            success = this.saveContentsOfFileItem(context);
            if (success) {
                this.isModified = false;
                this.forceSave = false;
            }
        }
        return success;
    }

    public Boolean saveContentsOfFileItem(Context context) {
        //Overwrite in subclass
        File file = new File(this.getFileItemURL().getPath());
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    public Object loadContentsOfFileItem(Context context) {
        //Overwrite in subclass
        return null;
    }

    public void unloadContentsOfFileItem() {
        synchronized (this) {
            if (!this.isModified && !this.forceSave) {
                content = null;
            }
        }
    }
}
