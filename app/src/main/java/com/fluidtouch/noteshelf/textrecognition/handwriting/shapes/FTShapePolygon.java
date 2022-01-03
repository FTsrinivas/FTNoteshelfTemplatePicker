package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


import android.graphics.PointF;

import java.util.ArrayList;

import static com.fluidtouch.noteshelf.textrecognition.handwriting.shapes.FTShapeUtility.SHAPE_VARIANCE_PERCENTAGE;
import static com.fluidtouch.noteshelf.textrecognition.handwriting.shapes.FTShapeUtility.distanceBetween2Points;

public class FTShapePolygon extends FTShape {
    public ArrayList<PointF> vertices;

    public ArrayList<FTShapeLine> getLines() {
        ArrayList<FTShapeLine> lines = new ArrayList<>();
        for (int i = 1; i < vertices.size(); i++) {
            PointF pointA = vertices.get(i - 1);
            PointF pointB = vertices.get(i);
            lines.add(new FTShapeLine(pointA, pointB));
        }
        return lines;
    }

    public ArrayList<PointF> validatePoints() {
        float kThresholdDistance = 0;
        ArrayList<PointF> newPoints = new ArrayList<>();

        for (int i = 1; i < vertices.size(); i++) {
            FTShapeLine line = new FTShapeLine(vertices.get(i - 1), vertices.get(i));
            line.validate();
            PointF pointA = line.startPoint;
            PointF pointB = line.endPoint;
            vertices.set(i - 1, pointA);
            vertices.set(i, pointB);
            kThresholdDistance = distanceBetween2Points(pointA, pointB) * SHAPE_VARIANCE_PERCENTAGE;

            if (i == vertices.size() - 1) {
                //Last Point means snap to the first point if distance is closer
                PointF fistPoint = vertices.get(0);
                float distance = distanceBetween2Points(pointB, fistPoint);
                if (distance <= kThresholdDistance && vertices.size() > 2) {
                    vertices.set(i, vertices.get(0));
                }
            }

            newPoints.add(pointA);
        }
        newPoints.add(vertices.get(vertices.size() - 1));
        return newPoints;
    }

    @Override
    public ArrayList<PointF> drawingPoints() {
        ArrayList<PointF> points = new ArrayList<>();
        ArrayList<FTShapeLine> lines = getLines();
        for (int i = 0; i < lines.size(); i++) {
            ArrayList<PointF> linePoints = lines.get(i).drawingPoints();
            if (linePoints.size() > 0) {
                points.addAll(linePoints);
            }
        }
        return points;
    }

    @Override
    public ArrayList<FTShape> validate() {
        vertices = validatePoints();
        return null;
    }

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypePolygon;
    }

    @Override
    public String shapeName() {
        return "Polygon";
    }
}

