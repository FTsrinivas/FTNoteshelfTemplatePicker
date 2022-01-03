package com.fluidtouch.noteshelf.pdfexport.shape;

import android.graphics.Color;

import com.fluidtouch.noteshelf.pdfexport.text.DrawListener;
import com.fluidtouch.noteshelf.pdfexport.text.Position;
import com.fluidtouch.noteshelf.pdfexport.util.CompatibilityHelper;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
 * Abstract base class for shapes which performs the
 * {@link #fill(PDDocument, PDPageContentStream, Position, float, float, Color, DrawListener)}
 * and (@link
 * {@link #draw(PDDocument, PDPageContentStream, Position, float, float, Color, Stroke, DrawListener)}
 * .
 */
public abstract class AbstractShape implements Shape {

    @Override
    public void draw(PDDocument pdDocument, PDPageContentStream contentStream,
                     Position upperLeft, float width, float height, Color color,
                     Stroke stroke, DrawListener drawListener) throws IOException {

        add(pdDocument, contentStream, upperLeft, width, height);

        if (stroke != null) {
            stroke.applyTo(contentStream);
        }
        if (color != null) {
            contentStream.setStrokingColor((int) color.red(), (int) color.green(), (int) color.blue());
        }
        contentStream.stroke();

        if (drawListener != null) {
            drawListener.drawn(this, upperLeft, width, height);
        }

    }

    @Override
    public void fill(PDDocument pdDocument, PDPageContentStream contentStream,
                     Position upperLeft, float width, float height, Color color,
                     DrawListener drawListener) throws IOException {

        add(pdDocument, contentStream, upperLeft, width, height);

        if (color != null) {
            contentStream.setNonStrokingColor((int) color.red(), (int) color.green(), (int) color.blue());
        }
        CompatibilityHelper.fillNonZero(contentStream);

        if (drawListener != null) {
            drawListener.drawn(this, upperLeft, width, height);
        }

    }

}
