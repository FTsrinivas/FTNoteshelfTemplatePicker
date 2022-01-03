package com.fluidtouch.noteshelf.stylusPrediction;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import com.fluidtouch.noteshelf.FTApp;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.prediction.SpenPrediction;
import com.samsung.android.sdk.pen.prediction.SpenPredictionListener;

public class FTStylusPenPredictionManager {

    public enum FTStylusPenType {
        SPen, HPen;
    }

    public interface FTStylusPenPredictionDelegate {
        void didRecievePredictionPoint(PointF point, FTStylusPenType stylusStype);
    }

    private static final FTStylusPenPredictionManager ourInstance = new FTStylusPenPredictionManager();
    private SpenPrediction prediction;

    public FTStylusPenPredictionDelegate delegate;

    public static FTStylusPenPredictionManager getInstance() {
        return ourInstance;
    }

    FTStylusPenPredictionManager() {
        if (initiateSpen()) {
            initializeSPenPrdiction();
        }
    }

    private boolean initiateSpen() {
        boolean supports = true;
        try {
            Spen spensdk = new Spen();
            spensdk.initialize(FTApp.getInstance().getApplicationContext());
        } catch (SsdkUnsupportedException e) {
            supports = false;
            e.printStackTrace();
        } catch (LinkageError error) {
            supports = false;
            error.printStackTrace();
        }
        return supports;
    }

    private void initializeSPenPrdiction() {
        try {
            Context _appContext = FTApp.getInstance().getApplicationContext();
            if (null != _appContext) {
                prediction = new SpenPrediction(_appContext);
                prediction.setPredictionListener(new SpenPredictionListener() {
                    @Override
                    public void onPredictTouch(float x, float y) {
                        PointF predicitonPoint = new PointF(x, y);
                        if (null != delegate) {
                            delegate.didRecievePredictionPoint(predicitonPoint, FTStylusPenType.SPen);
                        }
                    }
                });
            }
        } catch (LinkageError e) {
            Log.d("Noteshelf", "does not support samsung prediction");
        } finally {
            Log.d("Noteshelf", "does not support samsung prediction");
        }
    }

    public void onTouch(MotionEvent e) {
        if (null != prediction) {
            prediction.onTouchEvent(e);
        }
    }
}
