package com.fluidtouch.noteshelf.pdfexport.shape;

import com.fluidtouch.noteshelf.pdfexport.text.Position;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
 * A simple rectangular shape.
 */
public class Rect extends AbstractShape {

    @Override
    public void add(PDDocument pdDocument, PDPageContentStream contentStream,
                    Position upperLeft, float width, float height) throws IOException {
        contentStream.addRect(upperLeft.getX(), upperLeft.getY() - height,
                width, height);
    }

}
