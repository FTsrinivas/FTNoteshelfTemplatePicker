package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;
import android.graphics.RectF;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class FTShapeRectangle extends FTShapePolygon {

    private static RectF boundingRect(ArrayList<PointF> vertices) {
        MatOfPoint inputArray = FTShapeUtility.pointsToInputArray(vertices);
        Rect rect = Imgproc.boundingRect(inputArray);
        return new RectF(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
    }

    private static ArrayList<PointF> verticesForRect(RectF inRect) {
        ArrayList<PointF> array = new ArrayList<>();
        array.add(new PointF(inRect.left, inRect.top));
        array.add(new PointF(inRect.right, inRect.top));
        array.add(new PointF(inRect.right, inRect.bottom));
        array.add(new PointF(inRect.left, inRect.bottom));
        array.add(new PointF(inRect.left, inRect.top));
        return array;
    }

    public void validateRectangle() {
        int straightLineCount = 0;

        ArrayList<FTShapeLine> lineArray = getLines();
        for (int i = 0; i < lineArray.size(); i++) {
            FTShapeLine ftShapeLine = lineArray.get(i);
            FTShapeLineType lineType = FTShapeUtility.isStraitLine(ftShapeLine.startPoint, ftShapeLine.endPoint);
            if (lineType != FTShapeLineType.FTShapeLineTypeNormal) {
                straightLineCount++;
            }
        }

        if (straightLineCount >= 3) {
            RectF boudingRect = boundingRect(vertices);
            vertices = verticesForRect(boudingRect);
        }
    }

    @Override
    public ArrayList<FTShape> validate() {
        super.validate();
        validateRectangle();
        return null;
    }

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypeRectangle;
    }

    @Override
    public String shapeName() {
        return "Rectangle";
    }

}

