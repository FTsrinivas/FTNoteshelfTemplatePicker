package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;

import java.util.ArrayList;

public class FTShapeLine extends FTShape {
    float length;
    PointF startPoint, endPoint;

    public FTShapeLine(PointF startPoint, PointF endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        length = FTShapeUtility.distanceBetween2Points(startPoint, endPoint);
    }

    @Override
    public ArrayList<PointF> drawingPoints() {
        ArrayList<PointF> array = new ArrayList<>();
        float distance = FTShapeUtility.distanceBetween2Points(startPoint, endPoint);
        if (distance > FTShapeUtility.SHAPE_MIN_LINE_LENGTH) {
            array = FTShapeUtility.pointsInLine(startPoint, endPoint);
        }
        return array;
    }

    @Override
    public ArrayList<FTShape> validate() {

        ArrayList<FTShape> newShapes = null;

        FTShapeLineType lineType = FTShapeUtility.isStraitLine(startPoint, endPoint);
        switch (lineType) {
            case FTShapeLineTypeVerticle: {
                endPoint = new PointF(startPoint.x, endPoint.y);
            }
            break;

            case FTShapeLineTypeHorizontal: {
                endPoint = new PointF(endPoint.x, startPoint.y);
            }
            break;

            default: {

            }
            break;
        }

        return newShapes;
    }

    public FTShapeArrow getArrowAtPoint(PointF point, FTShapeLine line) {
        float tipX = line.endPoint.x;
        float tipY = line.endPoint.y;

        float dx = line.endPoint.x - line.startPoint.x;
        float dy = line.endPoint.y - line.startPoint.y;
        int arrowLength = 7; //can be adjusted

        float theta = (float) Math.atan2(dy, dx);
        float rad = DEGREES_RADIANS(35); //35 angle, can be adjusted

        float x = (float) (tipX - arrowLength * Math.cos(theta + rad));
        float y = (float) (tipY - arrowLength * Math.sin(theta + rad));

        float phi2 = DEGREES_RADIANS(-35);//-35 angle, can be adjusted
        float x2 = (float) (tipX - arrowLength * Math.cos(theta + phi2));
        float y2 = (float) (tipY - arrowLength * Math.sin(theta + phi2));

        PointF pointA = new PointF(x, y);
        PointF centerPoint = line.endPoint;
        PointF pointB = new PointF(x2, y2);

        FTShapeLine lineA = new FTShapeLine(pointA, centerPoint);
        FTShapeLine lineB = new FTShapeLine(centerPoint, pointB);
        FTShapeArrow arrow = new FTShapeArrow(lineA, lineB);
        return arrow;
    }

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypeLine;
    }

    @Override
    public String shapeName() {
        return "Line";
    }
}
