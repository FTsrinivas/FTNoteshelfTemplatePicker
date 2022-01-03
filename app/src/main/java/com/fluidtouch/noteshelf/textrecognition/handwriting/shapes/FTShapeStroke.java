package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;

import java.util.ArrayList;

public class FTShapeStroke {
    public ArrayList<PointF> points;

    public FTShapeStroke() {
        points = new ArrayList<>();
    }

    public void addPoint(PointF point) {
        points.add(point);
    }

    public void clearAllPoints() {
        points.clear();
    }

    public ArrayList<PointF> shapePoints() {
        ArrayList<PointF> strokes = new ArrayList<>();

        FTShape shape = FTShapeFactory.sharedFTShapeFactory().getShapeForPoints(points);
        if (shape != null) {
            ArrayList<FTShape> newShapes = shape.validate();
            ArrayList<PointF> strokePoints = shape.drawingPoints();
            if (strokePoints.size() > 0) {
                strokes.addAll(strokePoints);
            }
            for (int i = 0; i < newShapes.size(); i++) {
                strokePoints = newShapes.get(i).drawingPoints();
                if (strokePoints.size() > 0) {
                    strokes.addAll(strokePoints);
                }
            }
        }

        return strokes;
    }

}

