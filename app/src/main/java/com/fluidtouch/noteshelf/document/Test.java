package com.fluidtouch.noteshelf.document;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sreenu on 26/05/20.
 */
public class Test implements Parcelable {
    private String name = "Name";
    private String class_ = "Name";
    private String study = "Name";
    private String path = "Name";
    private String blow = "Name";

    protected Test(Parcel in) {
        name = in.readString();
        class_ = in.readString();
        study = in.readString();
        path = in.readString();
        blow = in.readString();
    }

    public static final Creator<Test> CREATOR = new Creator<Test>() {
        @Override
        public Test createFromParcel(Parcel in) {
            return new Test(in);
        }

        @Override
        public Test[] newArray(int size) {
            return new Test[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(class_);
        dest.writeString(study);
        dest.writeString(path);
        dest.writeString(blow);
    }
}
