package com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Size;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.renderer.offscreenRenderer.FTOffscreenRenderer;

import java.util.ArrayList;

public class FTThumbnailGenerator {
    private static volatile FTThumbnailGenerator sharedInstance;
    final ArrayList<FTThumbnailGenerationRequest> requestsArray;
    private FTThumbnailGenerationRequest currentRequest;
    private FTRenderManager thumbnailOffscreenRenderer = null;
    private boolean isPaused;
    private Thread thumbnailExecuteThread;
    private Object mPauseLock;
    private FTRenderMode ftRenderMode = FTRenderMode.thumbnailGen;

    private FTThumbnailGenerator() {
        requestsArray = new ArrayList<>();
        mPauseLock = new Object();
    }

    public static synchronized FTThumbnailGenerator sharedThumbnailGenerator(FTRenderMode mode) {
        if (sharedInstance == null) {
            sharedInstance = new FTThumbnailGenerator();
        }
        sharedInstance.ftRenderMode = mode;
        return sharedInstance;
    }

    void generateThumbnailForPDFPage(Context context, FTNoteshelfPage page) {
        FTThumbnailGenerationRequest request = FTThumbnailGenerationRequest.thumbnailRequestForPage(context, page, new FTThumbnailGenerationRequest.FTThumbnailGenerationRequestDelegate() {
            @Override
            public void didCompleteRequest(final FTThumbnailGenerationRequest request, final Bitmap image) {
                request.page.thumbnail().updateThumbnailImage(request.page.uuid);
            }
        });
        request.setFtRenderMode(ftRenderMode);
        synchronized (requestsArray) {
            requestsArray.add(request);
        }

        if (null == thumbnailExecuteThread) {
            startExecution();
        }
        if (isPaused) {
            resume();
        }
    }

    public void releaseThumbnailRenderer() {
        if (null != thumbnailOffscreenRenderer) {
            synchronized (thumbnailOffscreenRenderer) {
                thumbnailOffscreenRenderer.destroy();
                thumbnailOffscreenRenderer = null;
            }
        }
    }

    public void cancelAllThumbnailGeneration() {
        synchronized (requestsArray) {
            requestsArray.clear();
        }
    }

    public FTRenderManager thumbnailOffscreenRenderer(Context context) {
        if (thumbnailOffscreenRenderer == null) {
            thumbnailOffscreenRenderer = new FTRenderManager(context, ftRenderMode);
        }
        return thumbnailOffscreenRenderer;
    }

    void cancelThumbnailGenerationForPage(FTNoteshelfPage page) {
        //        NSPredicate * predicate = [NSPredicate predicateWithFormat:@ "page.uuid like %@",[page uuid]]
        //        ;
        //        NSArray * filteredArray =[self.requestsArray filteredArrayUsingPredicate:predicate];
        //        if (filteredArray.count == 1)
        //        [self.requestsArray removeObject:filteredArray.firstObject];
    }

    private void startExecution() {
        if (null == thumbnailExecuteThread) {
            thumbnailExecuteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isPaused) {
                        if (requestsArray.size() > 0) {
                            FTThumbnailGenerationRequest request = null;
                            synchronized (requestsArray) {
                                request = requestsArray.get(0);
                                requestsArray.remove(0);
                            }
                            if (null != request) {
                                request.executeRequest();
                            }
                        }

                        synchronized (mPauseLock) {
                            if (requestsArray.size() == 0 && !isPaused) {
                                pause();
                            }

                            while (isPaused) {
                                try {
                                    mPauseLock.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                }
            });
            thumbnailExecuteThread.start();
        }
    }

    public void pause() {
        synchronized (mPauseLock) {
            isPaused = true;
        }
    }

    public void resume() {
        synchronized (mPauseLock) {
            isPaused = false;
            mPauseLock.notifyAll();
        }

    }

    public void destorySharedInstance() {
        sharedInstance = null;
    }

}