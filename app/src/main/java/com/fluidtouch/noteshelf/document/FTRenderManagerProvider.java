package com.fluidtouch.noteshelf.document;

import android.content.Context;

import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;

import java.util.ArrayList;

/**
 * Created by sreenu on 4/5/21.
 */
class FTRenderManagerProvider {
    private final ArrayList<FTRenderManager> inDrawingViews = new ArrayList<>();
    private final ArrayList<FTRenderManager> outDrawingViews = new ArrayList<>();
    private static FTRenderManagerProvider mInstance;

    private FTRenderManagerProvider() {
        //Stub
    }

    public static synchronized FTRenderManagerProvider getInstance() {
        if (mInstance == null) {
            mInstance = new FTRenderManagerProvider();
        }
        return mInstance;
    }

    public synchronized FTRenderManager attachManager(Context context) {
        if (outDrawingViews.size() == 0) {
            outDrawingViews.add(new FTRenderManager(context, FTRenderMode.offScreen));
        }
        inDrawingViews.add(outDrawingViews.get(0));
        outDrawingViews.remove(0);
        return inDrawingViews.get(inDrawingViews.size() - 1);
    }

    public synchronized void detachManager(FTRenderManager manager) {
        inDrawingViews.remove(manager);
        manager.destroy();
//        outDrawingViews.add(manager);
    }

    public synchronized void destroyAll() {
        if (true)
            return;
        for (int i = 0; i < inDrawingViews.size(); i++) {
            inDrawingViews.get(i).destroy();
        }
        for (int i = 0; i < outDrawingViews.size(); i++) {
            outDrawingViews.get(i).destroy();
        }

        inDrawingViews.clear();
        outDrawingViews.clear();
    }
}
