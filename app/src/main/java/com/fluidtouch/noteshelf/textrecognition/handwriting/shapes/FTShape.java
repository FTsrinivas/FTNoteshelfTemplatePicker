package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;

import java.util.ArrayList;


enum FTShapeType {
    FTShapeTypeNone,
    FTShapeTypeLine,
    FTShapeTypeEllipse,
    FTShapeTypeArrow,
    FTShapeTypeTriangle,
    FTShapeTypeRectangle,
    FTShapeTypePolygon
}

public abstract class FTShape {
    public FTShapeType type;

    public ArrayList<PointF> drawingPoints() {
        return null;
    }

    public ArrayList<FTShape> validate() {
        return null;
    }

    public FTShapeType type() {
        return FTShapeType.FTShapeTypeNone;
    }


    public String shapeName() {
        return "None";
    }

    public float DEGREES_RADIANS(double angle) {
        return (float) Math.toRadians(angle);
    }

}


