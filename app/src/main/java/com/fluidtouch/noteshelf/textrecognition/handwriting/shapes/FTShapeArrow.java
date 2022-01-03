package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;

import java.util.ArrayList;

public class FTShapeArrow extends FTShape {

    FTShapeLine lineA, lineB;

    public FTShapeArrow(FTShapeLine lineA, FTShapeLine lineB) {
        this.lineA = lineA;
        this.lineB = lineB;
    }

    @Override
    public ArrayList<PointF> drawingPoints() {
        ArrayList<PointF> points = new ArrayList<>();

        ArrayList<PointF> array = lineA.drawingPoints();
        if (array.size() > 0) {
            points.addAll(array);
        }
        array = lineB.drawingPoints();
        if (array.size() > 0) {
            points.addAll(array);
        }
        return points;
    }

    @Override
    public ArrayList<FTShape> validate() {
        lineA.validate();
        lineB.validate();
        return null;
    }

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypeArrow;
    }

    @Override
    public String shapeName() {
        return "Arrow";
    }
}
