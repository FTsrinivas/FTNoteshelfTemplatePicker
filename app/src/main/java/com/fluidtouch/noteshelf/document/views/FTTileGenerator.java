package com.fluidtouch.noteshelf.document.views;

import android.content.Context;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;

import java.util.ArrayList;

public class FTTileGenerator {
    private static volatile FTTileGenerator sharedInstance = new FTTileGenerator();
    final ArrayList<FTTileGenerationRequest> requestsArray;
    private FTTileGenerationRequest currentRequest;
    private FTRenderManager thumbnailOffscreenRenderer = null;
    private boolean isPaused;
    private Thread thumbnailExecuteThread;
    private Object mPauseLock;

    private FTTileGenerator() {
        requestsArray = new ArrayList<>();
        mPauseLock = new Object();
    }

    public static FTTileGenerator sharedTileGenerator() {
        return sharedInstance;
    }

    public void generateTileForRect(Context context, FTNoteshelfPage page, FTTileImageView ftTileImageView, FTRenderManager renderManager, float scale) {

        FTTileGenerationRequest request = FTTileGenerationRequest.thumbnailRequestForPage(context, page, ftTileImageView, renderManager, scale);
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

    public void releaseTileRenderer() {
        if (null != thumbnailOffscreenRenderer) {
            thumbnailOffscreenRenderer.destroy();
            thumbnailOffscreenRenderer = null;
        }
    }

    public void cancelAllTileGeneration() {
        synchronized (requestsArray) {
            requestsArray.clear();
        }
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
                            FTTileGenerationRequest request = null;
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