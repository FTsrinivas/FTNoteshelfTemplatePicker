package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;

import java.io.IOException;
import java.util.UUID;

class FTHandwritingRecognitionPackage {
    private Context context;
    private Editor editor;
    private Engine engine;
    private String partIdentifier = UUID.randomUUID().toString();

    FTHandwritingRecognitionPackage(Context context, Editor editor, Engine engine) {
        this.context = context;
        this.editor = editor;
        this.engine = engine;
    }

    void assignPartToEditor() {
        ContentPackage contentPackage = null;
        try {
            String fullPath = ContextCompat.getDataDir(context).getPath() + "/" + partIdentifier + ".iink";
            engine.deletePackage(fullPath);
            contentPackage = engine.openPackage(fullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (contentPackage == null) {
            try {
                contentPackage = createPackage(partIdentifier);
                editor.setPart(contentPackage.getPart(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ContentPackage createPackage(String packageName) throws IOException {
        // Create a new content package with name
        String fullPath = ContextCompat.getDataDir(context).getPath() + "/" + packageName + ".iink";
        engine.deletePackage(fullPath);
        ContentPackage contentPackage = engine.createPackage(fullPath);
        // Add a blank page type Text Document
        ContentPart part = contentPackage.createPart("Text");
        return contentPackage;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.engine.deletePackage(this.partIdentifier);
    }
}
