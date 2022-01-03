package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;
import android.util.SizeF;

import java.util.ArrayList;

import static com.fluidtouch.noteshelf.textrecognition.handwriting.shapes.FTShapeUtility.SHAPE_MIN_LINE_LENGTH;

public class FTShapeEllipse extends FTShape {
    PointF center;
    SizeF boundingRectSize;
    float rotatedAngle;

    PointF pointOnEllipse(float angle) {
        PointF newPoint;
        float x = (float) (center.x + (boundingRectSize.getWidth() / 2) * Math.cos(DEGREES_RADIANS(angle)));
        float y = (float) (center.y + (boundingRectSize.getHeight() / 2) * Math.sin(DEGREES_RADIANS(angle)));
        newPoint = FTShapeUtility.rotatePointByAngle(center, new PointF(x, y), rotatedAngle);
        return newPoint;
    }

    @Override
    public ArrayList<PointF> drawingPoints() {
        ArrayList<PointF> points = new ArrayList<>();
        float area = boundingRectSize.getWidth() * boundingRectSize.getHeight();
        if (area > SHAPE_MIN_LINE_LENGTH) {
            float kMaxAngle = 360;
            for (int t = 0; t <= kMaxAngle; t = t + 1) {
                PointF point = pointOnEllipse(t);
                points.add(point);
            }
        }
        return points;
    }

    public void validatePerfectCircle() {
        float KVariancePercentage = 20;

        if (boundingRectSize.getWidth() != 0 && boundingRectSize.getHeight() != 0) {
            float max = boundingRectSize.getHeight();
            float variance = (boundingRectSize.getHeight() / boundingRectSize.getWidth()) * 100;
            if (boundingRectSize.getWidth() < boundingRectSize.getHeight()) {
                variance = (boundingRectSize.getWidth() / boundingRectSize.getHeight()) * 100;
                max = boundingRectSize.getWidth();
            }

            variance = 100 - variance;
            if (variance <= KVariancePercentage) {
                boundingRectSize = new SizeF(max, max);
                rotatedAngle = 0;
            }
        }
    }

    @Override
    public ArrayList<FTShape> validate() {
        validatePerfectCircle();
        return null;
    }

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypeEllipse;
    }

    @Override
    public String shapeName() {
        return "Ellipse";
    }
}
