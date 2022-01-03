package com.fluidtouch.noteshelf.pdfexport;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.SizeF;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTSegment;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTPdfStrokeRenderer {

    public boolean render(FTAnnotation annotation, Canvas canvas) {

        boolean isHighlighter = false;
        FTStroke ftStroke = (FTStroke) annotation;
        Paint paint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ftStroke.strokeColor);
        Path path = new Path();
        List<FTSegment> pointListOut = new ArrayList<>();
        Simplify simplify = new Simplify<FTSegment>(new FTSegment[0], new PointExtractor<FTSegment>() {
            @Override
            public double getX(FTSegment point) {
                return midpointX(point.startPoint, point.endPoint);
            }

            @Override
            public double getY(FTSegment point) {
                return midpointY(point.startPoint, point.endPoint);
            }
        });

        FTSegment[] newSegments = (FTSegment[]) simplify.simplify(ftStroke.getSegments().toArray(new FTSegment[ftStroke.segmentCount]), 0.05, true);


        for (int f = 0; f < newSegments.length; f++) {
            FTSegment segment = newSegments[f];
            paint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(ftStroke.strokeColor);
            float thicknesMultiplayFactor = 1;
            if (ftStroke.penType == FTPenType.pen) {
                thicknesMultiplayFactor = penThicknessMultiplyFactor(ftStroke.strokeWidth);
            } else if (ftStroke.penType == FTPenType.caligraphy) {
                paint.setStrokeJoin(Paint.Join.BEVEL);
                thicknesMultiplayFactor = calPenThicknessMultiplyFactor(ftStroke.strokeWidth);
            } else if (ftStroke.penType == FTPenType.pilotPen) {
                thicknesMultiplayFactor = sharpPenThicknessMultiplyFactor(ftStroke.strokeWidth);
            } else if (ftStroke.penType == FTPenType.highlighter || ftStroke.penType == FTPenType.flatHighlighter) {
                thicknesMultiplayFactor = markerThicknessMultiplyFactor(ftStroke.strokeWidth);
            }
            paint.setStrokeWidth(segment.thickness * thicknesMultiplayFactor);
            if (f == 0) {
                if (ftStroke.penType == FTPenType.flatHighlighter || ftStroke.penType == FTPenType.caligraphy)
                    paint.setStrokeCap(Paint.Cap.SQUARE);
                else
                    paint.setStrokeCap(Paint.Cap.ROUND);
                PointF startPoint = new PointF(segment.startPoint.x, segment.startPoint.y);
                PointF endPoint = new PointF(segment.startPoint.x + (segment.endPoint.x - segment.startPoint.x) / 2, segment.startPoint.y + (segment.endPoint.y - segment.startPoint.y) / 2);
                path.moveTo(startPoint.x, startPoint.y);
                PointF midPoint = new PointF(startPoint.x + (endPoint.x - startPoint.x) / 2, startPoint.y + (endPoint.y - startPoint.y) / 2);
                path.cubicTo(startPoint.x, startPoint.y, midPoint.x, midPoint.y, endPoint.x, endPoint.y);
            } else {
                if (ftStroke.penType != FTPenType.flatHighlighter || ftStroke.penType != FTPenType.caligraphy)
                    paint.setStrokeCap(Paint.Cap.ROUND);
                else if (ftStroke.penType == FTPenType.caligraphy)
                    paint.setStrokeCap(Paint.Cap.BUTT);
                FTSegment segmentPre = newSegments[f - 1];
                PointF startPoint = new PointF(segmentPre.startPoint.x + (segmentPre.endPoint.x - segmentPre.startPoint.x) / 2, segmentPre.startPoint.y + (segmentPre.endPoint.y - segmentPre.startPoint.y) / 2);
                PointF midPoint = new PointF(segment.startPoint.x, segment.startPoint.y);
                PointF endPoint = new PointF(segment.startPoint.x + (segment.endPoint.x - segment.startPoint.x) / 2, segment.startPoint.y + (segment.endPoint.y - segment.startPoint.y) / 2);
                //path.moveTo(startPoint.x, startPoint.y);
                path.cubicTo(startPoint.x, startPoint.y, midPoint.x, midPoint.y, endPoint.x, endPoint.y);
                if (f == ftStroke.segmentCount - 1) {
                    if (ftStroke.penType == FTPenType.caligraphy)
                        paint.setStrokeCap(Paint.Cap.BUTT);
                    else
                        paint.setStrokeCap(Paint.Cap.ROUND);
                    if (ftStroke.penType != FTPenType.highlighter && ftStroke.penType != FTPenType.flatHighlighter)
                        canvas.drawPath(path, paint);
                    //path.moveTo(midPoint.x, midPoint.y);
                    PointF midPoint2 = new PointF(endPoint.x + (segment.endPoint.x - endPoint.x) / 2, endPoint.y + (segment.endPoint.y - endPoint.y) / 2);
                    path.cubicTo(midPoint.x, midPoint.y, midPoint2.x, midPoint2.y, segment.endPoint.x, segment.endPoint.y);
                }
            }
            if (ftStroke.penType != FTPenType.highlighter && ftStroke.penType != FTPenType.flatHighlighter)
                canvas.drawPath(path, paint);

        }
        if (ftStroke.penType == FTPenType.highlighter || ftStroke.penType == FTPenType.flatHighlighter) {
            if (ftStroke.penType == FTPenType.flatHighlighter) {
                paint.setStrokeCap(Paint.Cap.SQUARE);
            } else {
                paint.setStrokeCap(Paint.Cap.ROUND);
            }
            paint.setAlpha(150);
            canvas.drawPath(path, paint);
            isHighlighter = true;
        }
        path.reset();
        paint = null;
        return isHighlighter;
    }

    private float penThicknessMultiplyFactor(float brushWidth) {
        float thicknessCorrectionFactor = 1;
        if (brushWidth == 1)
            thicknessCorrectionFactor = 0.3f;
        else if (brushWidth == 2)
            thicknessCorrectionFactor = 0.4f;
        else if (brushWidth == 3) {
            thicknessCorrectionFactor = 0.5f;
        } else if (brushWidth == 4) {
            thicknessCorrectionFactor = 0.55f;
        } else if (brushWidth >= 5 && brushWidth < 7) {
            thicknessCorrectionFactor = 0.75f;
        } else if (brushWidth >= 7 && brushWidth <= 8) {
            thicknessCorrectionFactor = 0.85f;
        } else {
            thicknessCorrectionFactor = 0.85f;
        }
        return thicknessCorrectionFactor;
    }

    private float calPenThicknessMultiplyFactor(float brushWidth) {
        float thicknessCorrectionFactor = 1f;
        if (brushWidth == 1)
            thicknessCorrectionFactor = 0.3f;
        else if (brushWidth == 2)
            thicknessCorrectionFactor = 0.4f;
        else if (brushWidth == 3)
            thicknessCorrectionFactor = 0.5f;
        else if (brushWidth > 3 && brushWidth <= 6)
            thicknessCorrectionFactor = 0.65f;
        else if (brushWidth >= 7 && brushWidth <= 8)
            thicknessCorrectionFactor = 0.75f;
        else
            thicknessCorrectionFactor = 0.85f;
        return thicknessCorrectionFactor;
    }

    private float markerThicknessMultiplyFactor(float brushWidth) {
        float thicknessCorrectionFactor = 1f;
        int brushWidthInt = (int) brushWidth;
        switch (brushWidthInt) {
            case 1:
                thicknessCorrectionFactor = 0.5f;
                break;
            case 2:
                thicknessCorrectionFactor = 0.65f;
                break;
            case 3:
                thicknessCorrectionFactor = 0.85f;
                break;
            case 4:
                thicknessCorrectionFactor = 0.9f;
                break;
            case 5:
                thicknessCorrectionFactor = 0.9f;
                break;
            case 6:
                thicknessCorrectionFactor = 0.9f;
                break;

            default:
                break;
        }
        return thicknessCorrectionFactor;
    }

    public float sharpPenThicknessMultiplyFactor(float brushWidth) {
        float thicknessCorrectionFactor = 1f;
        if (brushWidth == 1)
            thicknessCorrectionFactor = 0.3f;
        else if (brushWidth == 2)
            thicknessCorrectionFactor = 0.4f;
        else if (brushWidth == 3) {
            thicknessCorrectionFactor = 0.5f;
        } else if (brushWidth == 4) {
            thicknessCorrectionFactor = 0.55f;
        } else if (brushWidth >= 5 && brushWidth < 7) {
            thicknessCorrectionFactor = 0.65f;
        } else if (brushWidth >= 7 && brushWidth <= 8) {
            thicknessCorrectionFactor = 0.65f;
        } else {
            thicknessCorrectionFactor = 0.85f;
        }
        return thicknessCorrectionFactor;
    }

    private double midpointX(PointF a, PointF b) {
        return (a.x + b.x) / 2;
    }

    private double midpointY(PointF a, PointF b) {
        return (a.y + b.y) / 2;
    }


    public void pdfBoxStokerender(FTAnnotation annotation, PDPageContentStream contentStream, PDPage pdPage, FTNoteshelfPage noteshelfPage) {
        int yOffset = (int) pdPage.getMediaBox().getHeight();
        if (pdPage.getRotation() == 90 || pdPage.getRotation() == 270) {
            yOffset = (int) pdPage.getMediaBox().getWidth();
        }
        yOffset = yOffset + (int) pdPage.getMediaBox().getLowerLeftY();
        int xOffset = (int) pdPage.getMediaBox().getLowerLeftX();
        FTStroke ftStroke = (FTStroke) annotation;
        Paint paint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ftStroke.strokeColor);
        Simplify simplify = new Simplify<FTSegment>(new FTSegment[0], new PointExtractor<FTSegment>() {
            @Override
            public double getX(FTSegment point) {
                return midpointX(point.startPoint, point.endPoint);
            }

            @Override
            public double getY(FTSegment point) {
                return midpointY(point.startPoint, point.endPoint);
            }
        });

        ArrayList<ArrayList<FTSegment>> nonErasedstrokes = new ArrayList<>();
        ArrayList<FTSegment> tempSegments = new ArrayList<>();

        for (int i = 0; i < ftStroke.getSegments().size(); i++) {
            FTSegment segment = ftStroke.getSegments().get(i);
            if (segment.isSegmentErased() || ftStroke.getSegments().size() - 1 == i) {
                if (tempSegments.size() > 0) {
                    ArrayList<FTSegment> segments = new ArrayList<>();
                    segments.addAll(tempSegments);
                    nonErasedstrokes.add(segments);
                    tempSegments.clear();
                }
            } else {
                tempSegments.add(segment);
            }
        }
        for (int s = 0; s < nonErasedstrokes.size(); s++) {
            FTSegment[] newSegments = (FTSegment[]) simplify.simplify(nonErasedstrokes.get(s).toArray(new FTSegment[nonErasedstrokes.get(s).size()]), 0.05, true);
//        FTSegment[] newSegments = (FTSegment[]) ftStroke.getSegments().toArray(new FTSegment[ftStroke.segmentCount]);
            try {
                //lineCapStyle 0 for butt cap, 1 for round cap, and 2 for projecting square cap.
                //lineJoinStyle 0 for miter join, 1 for round join, and 2 for bevel join.
                if (ftStroke.penType == FTPenType.flatHighlighter || ftStroke.penType == FTPenType.caligraphy) {
                    contentStream.setLineJoinStyle(2);
                    contentStream.setLineCapStyle(2);
                } else {
                    contentStream.setLineCapStyle(1);
                    contentStream.setLineJoinStyle(1);
                }
                contentStream.setStrokingColor(Color.red(ftStroke.strokeColor), Color.green(ftStroke.strokeColor), Color.blue(ftStroke.strokeColor));
                int restPos = 0;
                for (int f = 0; f < newSegments.length; f++, restPos++) {
                    FTSegment segment1 = newSegments[f];
//                if (!segment1.isSegmentErased()) {
                    SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getWidth(), pdPage.getMediaBox().getHeight()));
                    if (pdPage.getRotation() == 90 || pdPage.getRotation() == 270)
                        aspectSize = FTGeometryUtils.aspectSize(new SizeF(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()), new SizeF(pdPage.getMediaBox().getHeight(), pdPage.getMediaBox().getWidth()));
                    float scale = aspectSize.getWidth() / noteshelfPage.getPageRect().width();
                    RectF boundingRect = FTGeometryUtils.scaleRect(segment1.boundingRect, scale);
                    PointF startPoint1 = FTGeometryUtils.scalePointF(new PointF(segment1.startPoint.x, segment1.startPoint.y), scale);
                    PointF endPoint1 = FTGeometryUtils.scalePointF(new PointF(segment1.endPoint.x, segment1.endPoint.y), scale);
                    FTSegment segment = new FTSegment(startPoint1, endPoint1, segment1.thickness, boundingRect, segment1.opacity);
                    float thicknesMultiplayFactor = 1;
                    if (ftStroke.penType == FTPenType.pen) {
                        thicknesMultiplayFactor = penThicknessMultiplyFactor(ftStroke.strokeWidth);
                    } else if (ftStroke.penType == FTPenType.caligraphy) {
                        thicknesMultiplayFactor = calPenThicknessMultiplyFactor(ftStroke.strokeWidth);
                    } else if (ftStroke.penType == FTPenType.pilotPen) {
                        thicknesMultiplayFactor = sharpPenThicknessMultiplyFactor(ftStroke.strokeWidth);
                    }
//                    else if (ftStroke.penType == FTPenType.highlighter || ftStroke.penType == FTPenType.flatHighlighter) {
//                        thicknesMultiplayFactor = markerThicknessMultiplyFactor(ftStroke.strokeWidth);
//                    }
                    contentStream.setLineWidth(segment.thickness * thicknesMultiplayFactor * scale);
                    //thicknesMultiplayFactor not necessary
//                    contentStream.setLineWidth(segment.thickness * scale);
                    if (restPos == 0) {
                        PointF startPoint = new PointF(segment.startPoint.x, segment.startPoint.y);
                        PointF endPoint = new PointF(segment.startPoint.x + (segment.endPoint.x - segment.startPoint.x) / 2, segment.startPoint.y + (segment.endPoint.y - segment.startPoint.y) / 2);
                        contentStream.moveTo(xOffset + startPoint.x, yOffset - startPoint.y);
                        PointF midPoint = new PointF(startPoint.x + (endPoint.x - startPoint.x) / 2, startPoint.y + (endPoint.y - startPoint.y) / 2);
                        contentStream.curveTo2(xOffset + midPoint.x, yOffset - midPoint.y, xOffset + endPoint.x, yOffset - endPoint.y);
                        //contentStream.curveTo2(midPoint.x, yOffset - midPoint.y, endPoint.x, yOffset - endPoint.y);
                    } else {
                        FTSegment segmentPre1 = newSegments[f - 1];
                        FTSegment segmentPre = new FTSegment(FTGeometryUtils.scalePointF(new PointF(segmentPre1.startPoint.x, segmentPre1.startPoint.y), scale), FTGeometryUtils.scalePointF(new PointF(segmentPre1.endPoint.x, segmentPre1.endPoint.y), scale), segmentPre1.thickness, FTGeometryUtils.scaleRect(segmentPre1.boundingRect, scale), segmentPre1.opacity);
                        PointF startPoint = new PointF(segmentPre.startPoint.x + (segmentPre.endPoint.x - segmentPre.startPoint.x) / 2, segmentPre.startPoint.y + (segmentPre.endPoint.y - segmentPre.startPoint.y) / 2);
                        PointF midPoint = new PointF(segment.startPoint.x, segment.startPoint.y);
                        PointF endPoint = new PointF(segment.startPoint.x + (segment.endPoint.x - segment.startPoint.x) / 2, segment.startPoint.y + (segment.endPoint.y - segment.startPoint.y) / 2);
                        //path.moveTo(startPoint.x, startPoint.y);
                        if (ftStroke.penType != FTPenType.highlighter && ftStroke.penType != FTPenType.flatHighlighter && ftStroke.penType != FTPenType.caligraphy) {
                            contentStream.moveTo(xOffset + startPoint.x, yOffset - startPoint.y);
                            contentStream.curveTo2(xOffset + midPoint.x, yOffset - midPoint.y, xOffset + endPoint.x, yOffset - endPoint.y);
                        } else {
                            contentStream.curveTo(xOffset + startPoint.x, yOffset - startPoint.y, xOffset + midPoint.x, yOffset - midPoint.y, xOffset + endPoint.x, yOffset - endPoint.y);
                        }
                        if (f == ftStroke.segmentCount - 1) {
                            PointF midPoint2 = new PointF(endPoint.x + (segment.endPoint.x - endPoint.x) / 2, endPoint.y + (segment.endPoint.y - endPoint.y) / 2);
                            contentStream.curveTo2(xOffset + midPoint2.x, yOffset - midPoint2.y, xOffset + segment.endPoint.x, yOffset - segment.endPoint.y);
                        }
                    }
                    if (ftStroke.penType != FTPenType.highlighter && ftStroke.penType != FTPenType.flatHighlighter && ftStroke.penType != FTPenType.caligraphy)
                        contentStream.stroke();
//                } else {
//                    if (ftStroke.penType == FTPenType.caligraphy) {
//                        contentStream.stroke();
//                    }
//                    if (ftStroke.penType == FTPenType.highlighter || ftStroke.penType == FTPenType.flatHighlighter) {
//                        drawHighlighter(contentStream);
//                    }
//                    restPos = -1;
//                }
                }
                if (ftStroke.penType == FTPenType.caligraphy) {
                    contentStream.stroke();
                }
                if (ftStroke.penType == FTPenType.highlighter || ftStroke.penType == FTPenType.flatHighlighter) {
                    drawHighlighter(contentStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void drawHighlighter(PDPageContentStream contentStream) throws IOException {
//add blend mode
        PDExtendedGraphicsState pdExtGfxState = new PDExtendedGraphicsState();
        pdExtGfxState.setStrokingAlphaConstant(0.5f);
        pdExtGfxState.getCOSObject().setItem(COSName.BM, COSName.MULTIPLY); // pdExtGfxState.setBlendMode(BlendMode.MULTIPLY) doesn't work yet, maybe in later version
        contentStream.setGraphicsStateParameters(pdExtGfxState);
        contentStream.stroke();
        //rest to back
        pdExtGfxState = new PDExtendedGraphicsState();
        pdExtGfxState.setStrokingAlphaConstant(1f);
        pdExtGfxState.getCOSObject().setItem(COSName.BM, COSName.NORMAL);
        contentStream.setGraphicsStateParameters(pdExtGfxState);
    }
}
