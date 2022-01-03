package com.fluidtouch.noteshelf.pdfexport;

import android.content.Context;
import android.graphics.Canvas;

import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;

public class FTPdfRenderer {
    private Context mContext;
    private FTPdfImageRenderer pdfImageRenderer;
    private FTPdfTextRenderer pdfTextRenderer;
    private FTPdfStrokeRenderer pdfStrokeRenderer;

    public FTPdfRenderer(Context context) {
        mContext = context;
    }

    public boolean render(FTAnnotation annotation, Canvas canvas) {
        if (annotation instanceof FTImageAnnotation) {
            if (pdfImageRenderer == null)
                pdfImageRenderer = new FTPdfImageRenderer();
            pdfImageRenderer.render(annotation, canvas);
            return false;
        } else if (annotation instanceof FTTextAnnotation) {
            if (pdfTextRenderer == null)
                pdfTextRenderer = new FTPdfTextRenderer(mContext);
            pdfTextRenderer.render(annotation, canvas);
            return false;
        } else if (annotation instanceof FTStroke) {
            if (pdfStrokeRenderer == null)
                pdfStrokeRenderer = new FTPdfStrokeRenderer();
            return pdfStrokeRenderer.render(annotation, canvas);
        } else {
            return false;
        }
    }
}
