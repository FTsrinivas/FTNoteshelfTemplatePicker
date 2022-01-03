package com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem;

import android.os.Parcel;
import android.os.Parcelable;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.File;
import java.io.FileInputStream;

public class FTDocumentItem extends FTShelfItem implements Parcelable {
    public FTDocument.FTDocumentDelegate delegate;
    //download progress info
    boolean isDownloaded;
    float downloadProgress;
    boolean isDownloading;
    //upload progress info
    boolean isUploaded;
    float uploadProgress;
    boolean isUploading;
    //updated once the package is downloaded
    String documentUUID;

    public FTDocumentItem(FTUrl fileURL) {
        super(fileURL);
        this.setFileURL(fileURL);
        if (fileURL != null) {
            File file = new File(fileURL.getPath() + "/" + FTConstants.METADATA_FOLDER_NAME + "/" + FTConstants.PROPERTIES_PLIST);
            if (file.exists()) {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
                    if (dictionary != null) {
                        this.setDocumentUUID(dictionary.objectForKey(FTConstants.DOCUMENT_ID_KEY).toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected FTDocumentItem(Parcel in) {
        super(in);
        isDownloaded = in.readByte() != 0;
        downloadProgress = in.readFloat();
        isDownloading = in.readByte() != 0;
        isUploaded = in.readByte() != 0;
        uploadProgress = in.readFloat();
        isUploading = in.readByte() != 0;
        documentUUID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isDownloaded ? 1 : 0));
        dest.writeFloat(downloadProgress);
        dest.writeByte((byte) (isDownloading ? 1 : 0));
        dest.writeByte((byte) (isUploaded ? 1 : 0));
        dest.writeFloat(uploadProgress);
        dest.writeByte((byte) (isUploading ? 1 : 0));
        dest.writeString(documentUUID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FTDocumentItem> CREATOR = new Creator<FTDocumentItem>() {
        @Override
        public FTDocumentItem createFromParcel(Parcel in) {
            return new FTDocumentItem(in);
        }

        @Override
        public FTDocumentItem[] newArray(int size) {
            return new FTDocumentItem[size];
        }
    };

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public float getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(float downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public float getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(float uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
    }

    public String getDocumentUUID() {
        return documentUUID;
    }

    public void setDocumentUUID(String documentUUID) {
        this.documentUUID = documentUUID;
    }
}
