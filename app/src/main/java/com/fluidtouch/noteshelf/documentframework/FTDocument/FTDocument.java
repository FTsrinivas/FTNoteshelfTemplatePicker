package com.fluidtouch.noteshelf.documentframework.FTDocument;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;

import java.util.Date;

public class FTDocument extends FTDocumentItem implements Parcelable {
    public FTFileItem rootFileItem;
    String filePath;
    String documentName;
    //    Bitmap thumbnailImage;
    Boolean hasNonUndoableChanges;

    public FTDocument(FTUrl fileURL) {
        super(fileURL);
    }


    protected FTDocument(Parcel in) {
        super(in);
        rootFileItem = in.readParcelable(FTFileItem.class.getClassLoader());
        filePath = in.readString();
        documentName = in.readString();
        byte tmpHasNonUndoableChanges = in.readByte();
        hasNonUndoableChanges = tmpHasNonUndoableChanges == 0 ? null : tmpHasNonUndoableChanges == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(rootFileItem, flags);
        dest.writeString(filePath);
        dest.writeString(documentName);
        dest.writeByte((byte) (hasNonUndoableChanges == null ? 0 : hasNonUndoableChanges ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FTDocument> CREATOR = new Creator<FTDocument>() {
        @Override
        public FTDocument createFromParcel(Parcel in) {
            return new FTDocument(in);
        }

        @Override
        public FTDocument[] newArray(int size) {
            return new FTDocument[size];
        }
    };

    public void loadInitialDataForDocument(Context context) {

    }

    Date urlModificationDate() {
        return new Date();
    }

    Date urlCreationDate() {
        return new Date();
    }

    public interface FTDocumentDelegate {
        int currentPageDisplayed();
    }
}
