package com.noteshelf.cloud;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.Tasks;

import java.util.concurrent.Callable;

public class FTTaskListener<T> {

    private TaskListenerCallback mListener;

    public void setListener(TaskListenerCallback listener) {
        mListener = listener;
    }

    public void executeTask(Callable<T> enNotePublishRequest) {
        Task<T> task = Tasks.call(enNotePublishRequest);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mListener.onFailure(e);
            }
        });
        task.addOnSuccessListener(updatedNotebook -> {
            mListener.onSuccess(updatedNotebook);
        });
    }

   public interface TaskListenerCallback {
        void onSuccess(Object object);

        void onFailure(Exception var1);
    }
}
