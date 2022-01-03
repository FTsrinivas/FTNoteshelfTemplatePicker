package com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask;

import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;

import kr.pe.burt.android.lib.androidoperationqueue.AndroidOperationQueue;

/**
 * Created by Vineet on 21/10/19
 */
public abstract class FTBackgroundTaskManager {
    public Queue<FTBackgroundTaskProtocols.FTBackgroundTask> taskList = new LinkedList<>();
    public AndroidOperationQueue dispatchQueue = new AndroidOperationQueue("DispatchWQueue");

    private FTBackgroundTaskProtocols.FTBackgroundTaskProcessor processor;

    public FTBackgroundTaskManager(Context context) {
        this.processor = this.getTaskProcessor(context);
    }

    public void addBackgroundTask(FTBackgroundTaskProtocols.FTBackgroundTask newTask) {
        synchronized (this) {
            this.taskList.add(newTask);
            if (!this.processor.canAcceptNewTask()) {
                newTask.onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus.WAITING);
            }
        }
        executeNextTask();
    }

    private synchronized void executeNextTask() {
        if (!this.processor.canAcceptNewTask()) {
            return;
        }
        this.dispatchQueue.addOperation((queue, bundle) -> {
            FTBackgroundTaskProtocols.FTBackgroundTask taskToExe = null;
            synchronized (this) {
                if (!this.taskList.isEmpty()) {
                    taskToExe = this.taskList.remove();
                }
            }
            FTBackgroundTaskProtocols.FTBackgroundTask task = taskToExe;
            if (task != null) {
                if (!canExecuteTask(task)) {
                    executeNextTask();
                    return;
                }
                task.onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus.INPROGRESS);
                this.processor.startTask(task, () -> {
                    task.onStatusChange(FTBackgroundTaskProtocols.FTBackgroundTaskStatus.FINISHED);
                    executeNextTask();
                });
            }
        });
        if (!dispatchQueue.isActivated()) {
            this.dispatchQueue.start();
        }

    }

    public void cancelTask() {
        dispatchQueue.stop();
        taskList.clear();
    }

    public abstract String dispatchQueueID();

    public abstract FTBackgroundTaskProtocols.FTBackgroundTaskProcessor getTaskProcessor(Context context);

    public abstract boolean canExecuteTask(FTBackgroundTaskProtocols.FTBackgroundTask task);
}