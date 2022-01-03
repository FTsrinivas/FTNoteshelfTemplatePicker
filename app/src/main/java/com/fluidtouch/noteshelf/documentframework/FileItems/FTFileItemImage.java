package com.fluidtouch.noteshelf.documentframework.FileItems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fluidtouch.noteshelf.documentframework.FTUrl;

import java.io.File;
import java.io.FileOutputStream;

public class FTFileItemImage extends FTFileItem {
    public FTFileItemImage(Context context, FTUrl fileURL, Boolean isDirectory) {
        super(context, fileURL, isDirectory);
    }

    public FTFileItemImage(String fileName, Boolean isDirectory) {
        super(fileName, isDirectory);
    }

    public Bitmap image(Context context) {
        return (Bitmap) this.getContent(context);
    }

    public void setImage(Bitmap image) {
        this.updateContent(image);
    }

    @Override
    public Object loadContentsOfFileItem(Context context) {
        File imgFile = new File(this.getFileItemURL().getPath());
        if (imgFile.exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap imageData = BitmapFactory.decodeFile(imgFile.getPath(), bmOptions);
            this.content = imageData;
            return content;
        }
        return null;
    }

    @Override
    public Boolean saveContentsOfFileItem(Context context) {
        try {
            Bitmap imageData = this.image(context);
            FileOutputStream out = new FileOutputStream(this.getFileItemURL().getPath());
            imageData.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
