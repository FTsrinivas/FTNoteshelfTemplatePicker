package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import android.content.Context;

import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemSqlite;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class FTNSqliteAnnotationFileItem extends FTFileItemSqlite {

    FTNoteshelfPage associatedPage;
    private ArrayList<FTAnnotation> annotationsArray;

    FTNSqliteAnnotationFileItem(Context c, FTUrl fileItemURL, Boolean isDirectory) {
        super(c, fileItemURL, isDirectory);
    }

    FTNSqliteAnnotationFileItem(Context c, String fileName, Boolean isDirectory) {
        super(c, fileName, isDirectory);
    }

    @Override
    public Object loadContentsOfFileItem(Context context) {
        return this.getAnnotationsArray(context);
    }

    @Override
    public synchronized Boolean saveContentsOfFileItem(Context context) {
        boolean result = this.databaseHelper(context).saveAnnotations(annotationsArray);
        if (result) {
            try {
                String[] folders = this.getFileItemURL().getPath().split("/");
                File fileTemp = null;
                File fileTemp_journal = null;
                if (folders.length > 2) {
                    fileTemp = new File(context.getCacheDir() + "/" + folders[folders.length - 3], fileName);
                    fileTemp_journal = new File(context.getCacheDir() + "/" + folders[folders.length - 3], fileName + "-journal");
                } else {
                    fileTemp = new File(context.getCacheDir(), fileName);
                    fileTemp_journal = new File(context.getCacheDir(), fileName + "-journal");
                }
                File fileMain = new File(this.getFileItemURL().getPath());
                File fileMain_journal = new File(this.getFileItemURL().getPath() + "-journal");
                if (fileTemp.exists()) {
                    fileMain.delete();
                    FTDocumentUtils.copyFile(fileTemp, fileMain);
                }
                if (fileTemp_journal.exists()) {
                    fileMain_journal.delete();
                    FTDocumentUtils.copyFile(fileTemp_journal, fileMain_journal);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void addAnnotation(Context context, FTAnnotation annotation) {
        this.getAnnotationsArray(context).add(annotation);
        this.updateContent(this.getAnnotationsArray(context));
    }

    public void addAnnotation(Context context, FTAnnotation annotation, int index) {
        this.getAnnotationsArray(context).add(index, annotation);
        this.updateContent(this.getAnnotationsArray(context));
    }

    public void removeAnnotation(Context context, FTAnnotation annotation) {
        int index = this.getAnnotationsArray(context).indexOf(annotation);
        if (index >= 0) {
            this.getAnnotationsArray(context).remove(index);
            this.updateContent(this.getAnnotationsArray(context));
        }
    }

    public synchronized ArrayList<FTAnnotation> getAnnotationsArray(Context context) {
        if (this.annotationsArray == null) {
            try {
                this.annotationsArray = this.databaseHelper(context).getAllAnnotationsForPage(context, this.associatedPage);
            } catch (Exception e) {
                e.printStackTrace();
                this.annotationsArray = this.databaseHelper(context).getAllAnnotationsForPage(context, this.associatedPage);
            }
        }
        return this.annotationsArray;
    }

    public void setAnnotationsArray(ArrayList<FTAnnotation> annotationsArray) {
        this.annotationsArray = annotationsArray;
        this.updateContent(this.annotationsArray);
    }

    public ArrayList<FTTextAnnotation> getTextAnnotationsForKey(Context context, String keyword) {
        return this.databaseHelper(context).textAnnotationsContainingKeyword(context, keyword);
    }

    @Override
    public void unloadContentsOfFileItem() {
        synchronized (this) {
            if (!this.isModified && !this.forceSave) {
                annotationsArray.clear();
                annotationsArray = null;
            }
            super.unloadContentsOfFileItem();
        }
    }
}
