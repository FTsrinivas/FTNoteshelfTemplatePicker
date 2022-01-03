package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;
import android.graphics.RectF;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;

enum FTShapeLineType {
    FTShapeLineTypeNormal,
    FTShapeLineTypeHorizontal,
    FTShapeLineTypeVerticle,
}

public class FTShapeUtility {

    public static float SHAPE_VARIANCE_PERCENTAGE = 0.3f;
    public static int SHAPE_MIN_LINE_LENGTH = 2;
    public static int SHAPE_CIRCLE_MIN_AREA = 8;

    public static PointF rotatePointByAngle(PointF center, PointF point1, float angle) {
        angle = (float) Math.toRadians(angle);
        float px = point1.x;
        float py = point1.y;

        float ox = center.x;
        float oy = center.y;

        float pX = (float) (Math.cos(angle) * (px - ox) - Math.sin(angle) * (py - oy) + ox);
        float pY = (float) (Math.sin(angle) * (px - ox) + Math.cos(angle) * (py - oy) + oy);
        return new PointF(pX, pY);
    }

    public static float distanceBetween2Points(PointF point1, PointF point2) {
        float dx = point2.x - point1.x;
        float dy = point2.y - point1.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    public static PointF pointOnLine(PointF point2, PointF point1, float distance) {
        PointF newPoint = new PointF();

        float vx = point2.x - point1.x;
        float vy = point2.y - point1.y;

        //Then calculate the length:
        float mag = (float) Math.sqrt(vx * vx + vy * vy);

        //Normalize the vector to unit length:
        vx /= mag;
        vy /= mag;

        //Finally calculate the new vector, which is x2y2 + vxvy * (mag + distance).
        newPoint.x = (float) (point1.x + vx * (mag - distance));
        newPoint.y = (float) (point1.y + vy * (mag - distance));

        return newPoint;
    }

    public static ArrayList<PointF> pointsInLine(PointF startPoint, PointF endPoint) {
        ArrayList<PointF> points = new ArrayList<>();
        float kMinDistance = 1;
        float totalDistance = distanceBetween2Points(startPoint, endPoint);
        for (int distance = 0; distance < totalDistance; distance += kMinDistance) {
            PointF newPoint = pointOnLine(startPoint, endPoint, distance);
            points.add(newPoint);
        }
        points.add(endPoint);

        return points;
    }

    public static FTShapeLineType isStraitLine(PointF pointA, PointF pointB) {
        float KLineVarianceDifference = 10;

        FTShapeLineType lineType = FTShapeLineType.FTShapeLineTypeNormal;
        float angle = (float) Math.abs(angleBetweenPoints(pointA, pointB));
        //check if horizontal
        float diff = Math.abs(angle - 180);
        if (diff <= KLineVarianceDifference || angle <= KLineVarianceDifference) {
            lineType = FTShapeLineType.FTShapeLineTypeHorizontal;
        }
        //check if verticle
        diff = Math.abs(angle - 90);
        if (diff <= KLineVarianceDifference) {
            lineType = FTShapeLineType.FTShapeLineTypeVerticle;
        }
        return lineType;
    }

    public static ArrayList<PointF> verticesForRect(RectF inRect) {
        ArrayList<PointF> array = new ArrayList<>();
        array.add(new PointF(inRect.left, inRect.top));
        array.add(new PointF(inRect.right, inRect.top));
        array.add(new PointF(inRect.right, inRect.bottom));
        array.add(new PointF(inRect.left, inRect.bottom));
        array.add(new PointF(inRect.left, inRect.top));
        return array;
    }

    public static MatOfPoint pointsToInputArray(ArrayList<PointF> points) {
        Point[] contours = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            PointF pointF = points.get(i);
            contours[i] = new Point(pointF.x, pointF.y);
        }
        return new MatOfPoint(contours);
    }

    public static double angleBetweenPoints(PointF p1, PointF p2) {
        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;
        return Math.toDegrees(Math.atan2(yDiff, xDiff));
    }
}
