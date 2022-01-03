package com.fluidtouch.noteshelf.document.views;

import static java.lang.Math.abs;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SizeF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.annotation.FTStrokeV1;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.FTTextureManager;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.stylusPrediction.FTStylusPenPredictionManager;
import com.fluidtouch.noteshelf.textrecognition.handwriting.shapes.FTShape;
import com.fluidtouch.noteshelf.textrecognition.handwriting.shapes.FTShapeFactory;
import com.fluidtouch.noteshelf.zoomlayout.FTWritingView;
import com.fluidtouch.renderingengine.DrawingSurfaceView;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.currentStroke.FTStrokeAttributes;
import com.fluidtouch.renderingengine.touchManagement.FTStylusPenManager;
import com.fluidtouch.renderingengine.touchManagement.FTStylusType;
import com.fluidtouch.renderingengine.touchManagement.FTTouch;

import java.util.ArrayList;


public class FTDrawingView extends DrawingSurfaceView {

    static long gestureEnableTimer = 500;

    private int bgTexture = 0;

    public boolean isRendering = false;
    Context mContext;
    private DrawingViewCallbacksListener mListener;
    private GestureDetector longPressGestureDetector;
    private GestureDetector doubleTapGestureDetector;
    private boolean avoidRenderingForNow = true;
    private SizeF currentSize;
    private FTStrokeAttributes ftStrokeAttributes = null;
    OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
            SizeF newSize = new SizeF(getWidth(), getHeight());
            if ((null == currentSize) || (currentSize.getWidth() != newSize.getWidth() || currentSize.getHeight() != newSize.getHeight())) {
                if (newSize.getWidth() > 0 && newSize.getHeight() > 0) {
                    currentSize = newSize;
                    reloadInRect(null);
                }
            }
        }
    };

    public FTDrawingView(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        initialize();
    }

    public FTDrawingView(Context context) {
        super(context);
        mContext = context;
        initialize();
    }

    public void setAvoidRenderingForNow(boolean avoid) {
        avoidRenderingForNow = avoid;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void initialize() {
        FTStylusPenPredictionManager.getInstance().delegate = (point, stylusStype) -> {
            if (!FTApp.isProduction()) {
                if (FTApp.getPref().isPredictionEnabled()) {
                    FTStylusPenManager.getInstance().setPredictionPoint(point);
                }
            } else
                FTStylusPenManager.getInstance().setPredictionPoint(point);
        };

        doubleTapGestureDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                //yo
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });

        doubleTapGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (mListener != null)
                    mListener.onOutsideTouch(FTToolBarTools.TEXT, motionEvent);
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent motionEvent) {
                mListener.onDoubleTap(motionEvent);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                return false;
            }
        });

        longPressGestureDetector = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                if (mListener != null)
                    return mListener.canProcessTouchDownMotionEventForLongTap(motionEvent);
                else
                    return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }


            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                mListener.onSingleTapped(motionEvent);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
//                mScrolling = true;
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                if (mListener != null)
                    mListener.onLongPress(motionEvent);
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }
        });

        addOnLayoutChangeListener(onLayoutChangeListener);

//        setOnHoverListener(new View.OnHoverListener() {
//            public boolean onHover(View view, MotionEvent motionEvent) {
//                if ((mListener.currentMode() == FTToolBarTools.PEN || mListener.currentMode() == FTToolBarTools.HIGHLIGHTER) || isStylushBtnPressed) {
//                    if (((motionEvent.getButtonState() & 32) == 32 || (motionEvent.getButtonState() & 64) == 64 || (motionEvent.getButtonState() & 2) == 2 || (motionEvent.getButtonState() & 4) == 4)) {
//                        if (mListener.currentMode() != FTToolBarTools.ERASER) {
//                            currentMode = mListener.currentMode();
//                            mListener.setCurrentMode(FTToolBarTools.ERASER);
//                        }
//                        isStylushBtnPressed = true;
//                    } else {
//                        if (currentMode == FTToolBarTools.HIGHLIGHTER)
//                            mListener.setCurrentMode(FTToolBarTools.HIGHLIGHTER);
//                        else
//                            mListener.setCurrentMode(FTToolBarTools.PEN);
//                        isStylushBtnPressed = false;
//                    }
//                }
//                return true;
//            }
//        });

//        setBackgroundColor(Color.BLUE);
//        setAlpha(0.3f);
    }

    @Override
    public void enableStylus(FTStylusType type) {
        FTApp.getPref().saveStylusEnabled(true);
        if (mContext instanceof FTDocumentActivity)
            ((FTDocumentActivity) mContext).setStylusMode();
    }

    @Override
    public boolean isStylusEnabled() {
        return FTApp.getPref().isStylusEnabled();
    }

    @Override
    public boolean isPressureSensitiveEnabled(FTStylusType stylus) {
        return FTApp.getPref().get(SystemPref.STYLUS_PRESSURE_ENABLED, false);
    }

    @Override
    public ArrayList<FTAnnotation> annotations() {
        if (mListener != null)
            return mListener.getPageAnnotations();
        else
            return new ArrayList<>();
    }

    public void setDeviceScale(float scale) {
        deviceScale = scale;
    }

    @Override
    public synchronized void reloadInRect(RectF rect) {
        reloadInRect(rect, false);
    }

    public synchronized void reloadInRect(RectF rect, boolean isBackgroundImageChanged) {
        if (!avoidRenderingForNow) {
            final SizeF pageSize = new SizeF(this.getWidth(), this.getHeight());
            if (pageSize.getWidth() == 0 || pageSize.getHeight() == 0) {
                return;
            }

            RectF visibleRect = visibleFrame();
            if (null == visibleRect)
                return;

            FTWritingView parent = ((FTWritingView) getParent());
            if (onScreenRenderManager != null) {
                isRendering = true;
                if (rect == null) {
                    if (null != ((FTWritingView) getParent()))
                        ((FTWritingView) getParent()).setVisibility(INVISIBLE);
                }
                FTTextureManager.sharedInstance().texture(mListener.currentPage(), scale, new FTTextureManager.TextureGenerationCallBack() {
                    @Override
                    public void onCompletion(int textureID) {
                        bgTexture = textureID;
                        if (onScreenRenderManager != null)
                            onScreenRenderManager.renderAnnotations(annotations(), rect, scale, () -> {
                                post(() -> {
                                    if (null != parent && parent.getVisibility() != VISIBLE) {
                                        parent.setAlpha(0f);
                                        parent.setVisibility(VISIBLE);
                                        parent.animate()
                                                .alpha(1f)
                                                .setListener(null);

                                        if (null != mListener)
                                            mListener.onRenderComplete();
                                    }
                                    isRendering = false;
                                });
                            });
                    }
                });
            }
        }
    }

    @Override
    public void processTouchEvent(MotionEvent e) {
        if (null == mListener)
            return;
        if (e.getAction() == MotionEvent.ACTION_CANCEL) {
            Log.i("Event", "Cancel");
        }

        if (e.getAction() != MotionEvent.ACTION_CANCEL) {
            longPressGestureDetector.onTouchEvent(e);
        }

        FTToolBarTools currentMode = mListener.currentMode();
        if ((currentMode == FTToolBarTools.PEN) || (currentMode == FTToolBarTools.HIGHLIGHTER) || (currentMode == FTToolBarTools.ERASER)) {
            FTStylusPenPredictionManager.getInstance().onTouch(e);
            super.processTouchEvent(e);
        } else if (currentMode == FTToolBarTools.TEXT) {
            doubleTapGestureDetector.onTouchEvent(e);
        } else if (currentMode == FTToolBarTools.LASSO) {
//            mListener.selectLasso(e);
        }
//        if(e.getAction() == MotionEvent.ACTION_UP && e.getPointerCount() == 1) {
//            scheduleGestureEnable();
//        }
//        longPressGestureDetector.onTouchEvent(e);
    }

    public void processTouchEventFromChild(MotionEvent e) {
        super.processTouchEvent(e);
    }

    @Override
    public FTStrokeAttributes currentStrokeAttributes() {
//        FTToolBarTools currentMode = mListener.currentMode();
//        SharedPreferences preferences = mContext.getSharedPreferences("PenRackPref", Context.MODE_PRIVATE);
//        String mPrefPenKey = "", mPrefSizeKey = "", mPrefColorKey = "", default_color = PenRackPref.DEFAULT_PEN_COLOR, default_pen = FTPenType.pen.toString();
//        if (currentMode == FTToolBarTools.PEN) {
//            mPrefPenKey = "selectedPen";
//            mPrefSizeKey = "selectedPenSize";
//            mPrefColorKey = "selectedPenColor";
//            default_color = PenRackPref.DEFAULT_PEN_COLOR;
//        } else if (currentMode == FTToolBarTools.HIGHLIGHTER) {
//            mPrefPenKey = "selectedPen_h";
//            mPrefSizeKey = "selectedPenSize_h";
//            mPrefColorKey = "selectedPenColor_h";
//            default_color = PenRackPref.DEFAULT_HIGHLIGHTER_COLOR;
//            default_pen = FTPenType.highlighter.toString();
//        }
//        return new FTStrokeAttributes(FTPenType.valueOf(preferences.getString(mPrefPenKey, default_pen)), preferences.getInt(mPrefColorKey, Color.parseColor(default_color)), preferences.getInt(mPrefSizeKey, PenRackPref.DEFAULT_SIZE));
        return ftStrokeAttributes;

    }

    public void setListeners(DrawingViewCallbacksListener listener) {
        this.mListener = listener;
    }

    @Override
    public FTStroke stroke() {
        return new FTStrokeV1(mContext);
    }

    @Override
    public void stylusPenTouchBegan(FTTouch touch) {
        cancelScheduleGestureDisable();
        cancelScheduleGestureEnable();

        if (mListener != null && touch.stylusType == FTStylusType.activeStylus) {
            mListener.enableAllGesture(false);
        } else {
            scheduleGestureDisable();
        }
        if (mListener != null && mListener.currentMode() == FTToolBarTools.ERASER) {
            mListener.performEraseAction(touch.currentPosition, UITouchPhase.BEGAN);
            mListener.onEraserBegin(touch);
        } else {
            super.stylusPenTouchBegan(touch);
        }
    }

    @Override
    public void stylusPenTouchMoved(FTTouch touch) {
        if (mListener != null && mListener.currentMode() == FTToolBarTools.ERASER) {
            mListener.performEraseAction(touch.currentPosition, UITouchPhase.MOVED);
            mListener.onEraserMove(touch);
        } else {
            super.stylusPenTouchMoved(touch);
        }
    }

    @Override
    public void stylusPenTouchCancelled(FTTouch touch) {
        cancelScheduleGestureDisable();
        if (mListener != null && mListener.currentMode() == FTToolBarTools.ERASER) {
            mListener.performEraseAction(touch.currentPosition, UITouchPhase.CANCELLED);
            mListener.onEraserCancel(touch);
        } else {
            super.stylusPenTouchCancelled(touch);
        }
        scheduleGestureEnable();
    }

    @Override
    public void stylusPenTouchEnded(FTTouch touch) {
        cancelScheduleGestureDisable();
        if (mListener != null && mListener.currentMode() == FTToolBarTools.ERASER) {
            mListener.performEraseAction(touch.currentPosition, UITouchPhase.ENDED);
            mListener.onEraserEnded(touch);
        } else {
            super.stylusPenTouchEnded(touch);
        }
        scheduleGestureEnable();
    }

    @Override
    public boolean isInHighlighterMode() {
        FTToolBarTools currentMode = mListener.currentMode();
        if (currentMode == FTToolBarTools.HIGHLIGHTER) {
            return true;
        }
        return false;
    }

    @Override
    public void addCurrentStrokeAnnotation(FTStroke stroke) {
        if (null != mListener)
            mListener.addCurrentStrokeAnnotation(stroke);
    }

    private Handler gestureDisableHandler = new Handler();
    Runnable gestureDisableTask = new Runnable() {
        @Override
        public void run() {
            if (null != mListener)
                mListener.enableAllGesture(false);
        }
    };

    public void scheduleGestureDisable() {
        gestureDisableHandler.postDelayed(gestureDisableTask, FTDrawingView.gestureEnableTimer);
    }

    public void cancelScheduleGestureDisable() {
        gestureDisableHandler.removeCallbacks(gestureDisableTask);
    }

    private Handler gestureEnableHandler = new Handler();
    Runnable gestureEnableTask = new Runnable() {
        @Override
        public void run() {
            if (null != mListener)
                mListener.enableAllGesture(true);
        }
    };

    public void scheduleGestureEnable() {
        cancelScheduleGestureEnable();
        gestureEnableHandler.postDelayed(gestureEnableTask, FTDrawingView.gestureEnableTimer);
    }

    public void cancelScheduleGestureEnable() {
        gestureEnableHandler.removeCallbacks(gestureEnableTask);
    }

    public ArrayList<FTStroke> shapeDetectedStroke() {

        ArrayList<FTStroke> strokes = new ArrayList<>();
        if (currentStroke != null) {
            ArrayList<PointF> strokePoints = currentStroke.stroke.points();
            if (strokePoints.size() > 0) {
                FTShape shape = FTShapeFactory.sharedFTShapeFactory().getShapeForPoints(strokePoints);
                Log.i("shape", shape.shapeName());
                ArrayList<FTShape> newShapes = shape.validate();

                FTStroke stroke = strokeForShape(shape,
                        currentStroke.stroke.averageThickness,
                        currentStroke.stroke.averageAplha);
                if (stroke != null) {
                    strokes.add(stroke);
                }
                if (newShapes != null)
                    for (int i = 0; i < newShapes.size(); i++) {

                        stroke = strokeForShape(newShapes.get(i),
                                currentStroke.stroke.averageThickness,
                                currentStroke.stroke.averageAplha);
                        if (stroke != null) {
                            strokes.add(stroke);
                        }
                    }
            }
        }
        return strokes;
    }

    FTStroke strokeForShape(FTShape shape, float thickness, float alpha) {
        ArrayList<PointF> shapesPoints = shape.drawingPoints();
        PointF offset = new PointF();
        FTPenType penType = FTPenType.pen;

        FTStroke newStroke = new FTStrokeV1(mContext);
        newStroke.strokeColor = currentStrokeAttributes().strokeColor;
        newStroke.strokeWidth = currentStrokeAttributes().strokeSize;
        newStroke.penType = currentStrokeAttributes().penType;
        addSegments(
                shapesPoints, newStroke, thickness, offset,
                alpha);

        return newStroke;
    }

    public void addSegments(ArrayList<PointF> drawingPoints, FTStroke inStroke, float brushWidth, PointF offset, float avgAlpha) {

        RectF boundRect = null;
        for (int i = 1; i < drawingPoints.size(); i++) {
            PointF startPoint = new PointF(drawingPoints.get(i - 1).x, drawingPoints.get(i - 1).y);
            PointF endPoint = new PointF(drawingPoints.get(i).x, drawingPoints.get(i).y);

            float halfPenWidth = (float) (brushWidth * 0.5);
            float x = (Math.min(startPoint.x, endPoint.x) - halfPenWidth);
            float y = (Math.min(startPoint.y, endPoint.y) - halfPenWidth);
            float width = (abs(startPoint.x - endPoint.x) + brushWidth) + x;
            float height = (abs(startPoint.y - endPoint.y) + brushWidth) + y;

            RectF segmentBounds = new RectF(x, y, width, height);

            inStroke.addSegment(startPoint, endPoint, brushWidth, avgAlpha, segmentBounds);

            if (boundRect == null) {
                boundRect = new RectF();
                boundRect.set(segmentBounds);
            } else {
                boundRect.union(segmentBounds);
            }
        }
        inStroke.setBoundingRect(boundRect);
    }


    public void reset() {
//        backgroundImage = null;
    }

    @Override
    public SizeF contentSize() {
        return mListener.contentSize();
    }

    @Override
    public RectF visibleFrame() {
        if (null == mListener)
            return null;
        return mListener.visibleFrame();
    }

    @Override
    public int bgTexture() {
        return bgTexture;
    }

    public void setFtStrokeAttributes(FTStrokeAttributes ftStrokeAttributes) {
        this.ftStrokeAttributes = ftStrokeAttributes;
    }

    public enum UITouchPhase {
        BEGAN, MOVED, ENDED, CANCELLED
    }

    public interface DrawingViewCallbacksListener {

        void enableAllGesture(boolean enable);

        void onEraserBegin(FTTouch ftTouch);

        void onEraserMove(FTTouch ftTouch);

        void onEraserCancel(FTTouch ftTouch);

        void onEraserEnded(FTTouch ftTouch);

        SizeF eraserSize();

        void addInputTextView(FTToolBarTools tools, FTAnnotation annotation, MotionEvent event);

        void addAudioView(FTAnnotation annotation, FTAudioRecording recording, boolean isFromBroadcast);

        void onLongPress(MotionEvent motionEvent);

        void onOutsideTouch(FTToolBarTools text, MotionEvent motionEvent);

        SizeF contentSize();

        RectF visibleFrame();

        ArrayList<FTAnnotation> getPageAnnotations();

        void onRenderComplete();

        void onSingleTapped(MotionEvent motionEvent);

        FTToolBarTools currentMode();

        void setCurrentMode(FTToolBarTools tool);

        void refreshOffscreen(RectF rect);

        void addCurrentStrokeAnnotation(FTStroke stroke);

        void performEraseAction(PointF erasePoint, FTDrawingView.UITouchPhase phase);

        void onDoubleTap(MotionEvent motionEvent);

        FTNoteshelfPage currentPage();

        boolean canProcessTouchDownMotionEventForLongTap(MotionEvent motionEvent);

        int bgTexture();
    }
}
