package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FTFileItemPlist extends FTFileItem {

    public FTFileItemPlist(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTFileItemPlist(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    protected NSDictionary contentDictionary(Context context) {
        return (NSDictionary) this.getContent(context);
    }

    @Override
    public Object loadContentsOfFileItem(Context context) {
        File file = new File(this.getFileItemURL().getPath());
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                this.content = PropertyListParser.parse(inputStream);
            } catch (Exception ex) {
                ex.printStackTrace();
                this.content = new NSDictionary();
            }
        }

        if (null == this.content) {
            this.content = new NSDictionary();
        }
        return this.getContent(context);
    }

    @Override
    public Boolean saveContentsOfFileItem(Context context) {
        try {
            NSDictionary rootDict = (NSDictionary) this.getContent(context);
            if (rootDict != null) {
                File file = new File(this.getFileItemURL().getPath());
                file.getParentFile().mkdirs(); //To create intermediate directory if needed
                //write data to temp file
                File file_temp = new File(file.getParent(), "_" + fileName);
                FileOutputStream outputStream = new FileOutputStream(file_temp);
                outputStream.write(rootDict.toXMLPropertyList().getBytes());
                outputStream.close();
                //rename to original file
                file_temp.renameTo(file);
            } else {
                FTLog.crashlyticsLog("FileItem " + this.fileName + " is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setObject(Context context, Object obj, String key) {
        this.contentDictionary(context).put(key, obj);
        this.setContent(this.contentDictionary(context));
    }

    public Object objectForKey(Context context, String key) {
        return this.contentDictionary(context).objectForKey(key);
    }
}
