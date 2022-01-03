package com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask;

/**
 * Created by Vineet on 21/10/19
 */
public class FTBackgroundTaskProtocols {
    public enum FTBackgroundTaskStatus {
        NONE, WAITING, INPROGRESS, FINISHED
    }

    public interface FTBackgroundTask {
        void onStatusChange(FTBackgroundTaskStatus status);
    }

    public interface FTBackgroundTaskProcessor {
        boolean canAcceptNewTask();

        void startTask(FTBackgroundTask task, OnCompletion onCompletion);
    }

    public interface OnCompletion {
        void didFinish();
    }
}