package com.fluidtouch.noteshelf.welcome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SizeF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.document.views.FTDrawingView;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.currentStroke.FTStrokeAttributes;
import com.fluidtouch.renderingengine.renderer.FTGLContextFactory;
import com.fluidtouch.renderingengine.touchManagement.FTStylusType;
import com.fluidtouch.renderingengine.utils.FTGLUtils;

import java.util.ArrayList;

/**
 * Created by sreenu on 17/07/20.
 */
public class WelcomeScreenDrawingView extends FTDrawingView {
    public FTToolBarTools selectedTool = FTToolBarTools.PEN;
    public FTPenType penType = FTPenType.pen;
    public String selectedColor = PenRackPref.DEFAULT_PEN_COLOR;
    private final ArrayList<FTAnnotation> annotations = new ArrayList<>();
    public int texture = 0;

    public WelcomeScreenDrawingView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public WelcomeScreenDrawingView(Context context) {
        super(context);
    }

    @Override
    public void processTouchEvent(MotionEvent e) {
        super.processTouchEventFromChild(e);
    }

    @Override
    public void enableStylus(FTStylusType type) {

    }

    @Override
    public boolean isStylusEnabled() {
        return false;
    }

    @Override
    public FTStrokeAttributes currentStrokeAttributes() {
        return new FTStrokeAttributes(penType, Color.parseColor(selectedColor), PenRackPref.DEFAULT_SIZE);
    }

    @Override
    public ArrayList<FTAnnotation> annotations() {
        return annotations;
    }

    @Override
    public int bgTexture() {
//        if (texture == 0) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pape_ruled);
//        FTGLContextFactory.getInstance().setCurrentContextAsShared();
        texture = FTGLUtils.textureFromBitmap(bitmap, "welcome screen BG");
//        }
        return texture;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
//        queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                FTGLUtils.deleteGLTexture(texture, true, "welcome screen BG");
//                texture = 0;
//            }
//        });
    }

    @Override
    public FTStroke stroke() {
        FTStroke stroke = new FTStroke(getContext());
        annotations.add(stroke);
        return stroke;
    }

    @Override
    public boolean isInHighlighterMode() {
        if (selectedTool == FTToolBarTools.HIGHLIGHTER) {
            return true;
        }
        return false;
    }

    @Override
    public SizeF contentSize() {
        return new SizeF(getResources().getDimension(R.dimen.new_700dp), getResources().getDimension(R.dimen.new_350dp));
    }

    @Override
    public RectF visibleFrame() {
        return new RectF(0, 0, getResources().getDimension(R.dimen.new_700dp), getResources().getDimension(R.dimen.new_350dp));
    }

    @Override
    public synchronized void reloadInRect(RectF rect) {
        if (onScreenRenderManager != null) {
            onScreenRenderManager.renderAnnotations(annotations(), rect, scale, () -> {
            });
        }
    }
}