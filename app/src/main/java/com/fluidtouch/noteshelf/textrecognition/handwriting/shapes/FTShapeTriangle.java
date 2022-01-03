package com.fluidtouch.noteshelf.textrecognition.handwriting.shapes;


public class FTShapeTriangle extends FTShapePolygon {

    @Override
    public FTShapeType type() {
        return FTShapeType.FTShapeTypeTriangle;
    }

    @Override
    public String shapeName() {
        return "Triangle";
    }
}

