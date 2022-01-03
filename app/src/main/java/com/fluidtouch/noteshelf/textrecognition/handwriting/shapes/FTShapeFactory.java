package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;//


import android.graphics.PointF;
import android.util.SizeF;

import com.fluidtouch.noteshelf.commons.FTLog;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.matchShapes;
import static org.opencv.imgproc.Imgproc.minAreaRect;

public class FTShapeFactory {
    private static volatile FTShapeFactory sharedInstance = new FTShapeFactory();

    public static FTShapeFactory sharedFTShapeFactory() {
        return sharedInstance;
    }

    public float DEGREES_RADIANS(double angle) {
        return (float) Math.toRadians(angle);
    }

    MatOfPoint2f getCircleAproximation(Point[] contours) {
        MatOfPoint2f approx = new MatOfPoint2f();
        double epsilon = arcLength(new MatOfPoint2f(contours), true) * (2.0f / 100.0f);
        approxPolyDP(new MatOfPoint2f(contours), approx, epsilon, false);
        return approx;
    }

    Point[] pointsToInputArray(ArrayList<PointF> points) {
        Point[] contours = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            PointF pointF = points.get(i);
            contours[i] = new Point(pointF.x, pointF.y);
        }
        return contours;
    }

    FTShapeEllipse isCircle(Point[] contours) {

        FTShapeEllipse outEllipse = null;

        FTShapeEllipse tEllipse = getEllipseForPoints(contours);
        ArrayList<PointF> points = tEllipse.drawingPoints();
        Point[] ellipseContour = pointsToInputArray(points);
        if (ellipseContour.length > 0 && contours.length > 0) {
            double match = matchShapes(new MatOfPoint2f(ellipseContour), new MatOfPoint2f(contours), CHAIN_APPROX_NONE, 0);
            if (match >= 0 && match < 0.2) {
                //Perfect match if match is closer to 0
                outEllipse = tEllipse;
            }
        }

        return outEllipse;
    }

    FTShapeEllipse getEllipseForPoints(Point[] contours) {

        MatOfPoint2f inCenter;
        float[] radius = new float[contours.length];
        RotatedRect rect = minAreaRect(new MatOfPoint2f(contours));

        FTShapeEllipse ellipse = new FTShapeEllipse();
        ellipse.center = new PointF((float) rect.center.x, (float) rect.center.y);
        ellipse.boundingRectSize = new SizeF((float) rect.size.width, (float) rect.size.height);
        ellipse.rotatedAngle = (float) rect.angle;
        return ellipse;
    }

    public FTShape getShapeForPoints(ArrayList<PointF> inPoints) {
        FTShape shape = null;
        if (inPoints.size() > 0) {
            try {
                MatOfPoint2f contours = new MatOfPoint2f(pointsToInputArray(inPoints));
                MatOfPoint2f approx;
                FTShapeEllipse ellipse = isCircle(contours.toArray());
                approx = getCircleAproximation(contours.toArray());

                if (approx.toList().size() > 7 && ellipse != null) {
                    shape = ellipse;
                } else {
                    approxPolyDP(contours, approx, arcLength(contours, true) * (2.0f / 100.0f), false);
                    if (approx.toList().size() <= 8) {
                        //Filter again - it will remove unneccessary points
                        approxPolyDP(contours, approx, arcLength(contours, true) * (4.0f / 100.0f), false);
                    } else {
                        approxPolyDP(contours, approx, arcLength(contours, true) * (1.0f / 100.0f), false);
                    }

                    if (approx.toList().size() == 2) {
                        PointF startPoint = new PointF((float) approx.toArray()[0].x, (float) approx.toArray()[0].y);
                        PointF endPoint = new PointF((float) approx.toArray()[1].x, (float) approx.toArray()[1].y);
                        FTShapeLine line = new FTShapeLine(startPoint, endPoint);
                        shape = line;
                    } else {
                        FTShapePolygon polygonShape = null;
                        if (approx.toList().size() == 4) { //One 3+1 last point is user lift the finger up
                            polygonShape = new FTShapeTriangle();
                        } else if (approx.toList().size() == 5) {
                            polygonShape = new FTShapeRectangle();
                        } else {
                            polygonShape = new FTShapePolygon();
                        }

                        ArrayList<PointF> vertices = new ArrayList<>();
                        for (int i = 0; i < approx.toList().size(); i++) {
                            vertices.add(new PointF((float) approx.toArray()[i].x, (float) approx.toArray()[i].y));
                        }
                        polygonShape.vertices = vertices;
                        shape = polygonShape;
                    }
                }
            } catch (Exception e) {
                FTLog.logCrashException(e);
            }
        }
        return shape;
    }

    public boolean isCurve(ArrayList<PointF> inPoints) {
        boolean isCurve = false;
        if (inPoints.size() > 0) {
            MatOfPoint2f contours = new MatOfPoint2f(pointsToInputArray(inPoints));
            MatOfPoint2f approx = new MatOfPoint2f();
            float epsilon = (float) arcLength(contours, true) * (4.0f / 100.0f);
            approxPolyDP(contours, approx, epsilon, false);
            if (approx.toList().size() > 3) {
                isCurve = true;
            }
        }
        return isCurve;
    }
}
