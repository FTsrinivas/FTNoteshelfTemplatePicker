package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTUrl;

public class FTFileItemPDF extends FTFileItem implements Parcelable {
    public String documentPassword;
    Integer pageCount;
    private FTPdfDocumentRef pdfDocumentRef;


    public FTFileItemPDF(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTFileItemPDF(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    protected FTFileItemPDF(Parcel in) {
        super(in);
        documentPassword = in.readString();
        if (in.readByte() == 0) {
            pageCount = null;
        } else {
            pageCount = in.readInt();
        }
        pdfDocumentRef = in.readParcelable(FTPdfDocumentRef.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(documentPassword);
        if (pageCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(pageCount);
        }
        dest.writeParcelable(pdfDocumentRef, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FTFileItemPDF> CREATOR = new Creator<FTFileItemPDF>() {
        @Override
        public FTFileItemPDF createFromParcel(Parcel in) {
            return new FTFileItemPDF(in);
        }

        @Override
        public FTFileItemPDF[] newArray(int size) {
            return new FTFileItemPDF[size];
        }
    };

    @Override
    public Object loadContentsOfFileItem(Context context) {
        return null;
    }

    public synchronized FTPdfDocumentRef pageDocumentRef(Context context) {
//        Log.i("FTFile pdfDocumentPtr", "pdfDocumentPtr");
        if (this.pdfDocumentRef == null) {
            this.pdfDocumentRef = new FTPdfDocumentRef(context, this.getFileItemURL(), this.documentPassword);
        }
        return this.pdfDocumentRef;
    }

    public Integer pageCount(Context context) {
        if (this.pdfDocumentRef == null) {
            this.pdfDocumentRef = new FTPdfDocumentRef(context, this.getFileItemURL(), this.documentPassword);
        }
        return this.pdfDocumentRef.pageCount();
    }

    @Override
    public void unloadContentsOfFileItem() {
        synchronized (this) {
            if (!this.isModified && !this.forceSave && this.pdfDocumentRef != null) {
                pdfDocumentRef.closeDocument();
                pdfDocumentRef = null;
            }
        }
    }
}
