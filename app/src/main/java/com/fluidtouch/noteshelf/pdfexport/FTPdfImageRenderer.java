package com.fluidtouch.noteshelf.pdfexport;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.SizeF;

import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FTPdfImageRenderer {

    public void render(FTAnnotation annotation, Canvas canvas) {
        FTImageAnnotation imageAnnotation = (FTImageAnnotation) annotation;
        RectF boundingRect = imageAnnotation.getBoundingRect();
        boundingRect = FTGeometryUtils.integralRect(boundingRect);
        if (boundingRect.width() <= 0 || boundingRect.height() <= 0) {
            FTLog.logCrashException(new Exception("Image annotation boundingRect with zero dimensions"));
            return;
        }

        try {
            Bitmap image = BitmapFactory.decodeFile(((FTImageAnnotationV1) imageAnnotation).getImageUrl().getPath());
            Matrix matrix = imageAnnotation.getTransMatrix();
            matrix.mapRect(boundingRect);
            matrix.preTranslate(boundingRect.left, boundingRect.top);

            Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0,
                    image.getWidth(), image.getHeight(), matrix, true);
            resizedBitmap.setHasAlpha(true);

            canvas.drawBitmap(resizedBitmap, null, boundingRect, null);
            resizedBitmap.recycle();
            image.recycle();
            resizedBitmap = null;
            image = null;
        } catch (Exception e) {
            FTLog.logCrashException(e);
        }
    }

    public void pdfBoxImageRender(FTAnnotation annotation, PDDocument document, PDPage pdPage, PDPageContentStream contentStream, FTNoteshelfPage noteshelfPage) {
        FTImageAnnotation imageAnnotation = (FTImageAnnotation) annotation;
        RectF boundingRect = imageAnnotation.getBoundingRect();
        boundingRect = FTGeometryUtils.integralRect(boundingRect);
        if (boundingRect.width() <= 0 || boundingRect.height() <= 0) {
            FTLog.logCrashException(new Exception("Image annotation boundingRect with zero dimensions"));
            return;
        }

        try {
            Bitmap image = BitmapFactory.decodeFile(((FTImageAnnotationV1) imageAnnotation).getImageUrl().getPath());
            Matrix matrix = imageAnnotation.getTransMatrix();
            matrix.mapRect(boundingRect);
            matrix.preTranslate(boundingRect.left, boundingRect.top);

            Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0,
                    image.getWidth(), image.getHeight(), matrix, true);
            resizedBitmap.setHasAlpha(true);

            PDImageXObject ximage = JPEGFactory.createFromImage(document, resizedBitmap);
            float scale = 1;
            if (pdPage.getRotation() == 90 || pdPage.getRotation() == 270) {
                SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getHeight(), pdPage.getMediaBox().getWidth()));
                scale = aspectSize.getWidth() / noteshelfPage.getPageRect().width();
            } else {
                SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getWidth(), pdPage.getMediaBox().getHeight()));
                scale = aspectSize.getWidth() / noteshelfPage.getPageRect().width();
            }
            boundingRect = FTGeometryUtils.scaleRect(boundingRect, scale);
            PointF startPoint = noteshelfPage.getFTPdfDocumentRef().covertDeviceCoordinateToPage(noteshelfPage.associatedPageIndex - 1, pdPage.getRotation(), (int) boundingRect.left, (int) boundingRect.bottom);
            contentStream.drawImage(ximage, startPoint.x, startPoint.y, boundingRect.width(), boundingRect.height());
            resizedBitmap.recycle();
            image.recycle();
            resizedBitmap = null;
            image = null;
        } catch (Exception e) {
            FTLog.logCrashException(e);
        }
    }

    private InputStream getInputStream(FTUrl imageUrl) {
        File initialFile = new File(imageUrl.getPath());
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return targetStream;
    }
}
