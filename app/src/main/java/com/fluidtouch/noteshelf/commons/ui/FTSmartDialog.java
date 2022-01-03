package com.fluidtouch.noteshelf.commons.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.fluidtouch.noteshelf2.R;
import com.wang.avi.AVLoadingIndicatorView;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSmartDialog extends DialogFragment {
    @BindView(R.id.spinner)
    AVLoadingIndicatorView mSpinner;
    @BindView(R.id.progress_indicator)
    CircularProgressIndicator mProgressIndicator;
    @BindView(R.id.task_done_image_view)
    ImageView mTaskDoneImageView;
    @BindView(R.id.message_text_view)
    TextView mMessageTextView;
    @BindView(R.id.task_cancel_button)
    Button mTaskCancelButton;

    private FTSmartDialogMode mode = FTSmartDialogMode.SPINNER;
    private String message;
    private int currentProgressValue;
    private int maxProgressValue;
    private boolean isTaskDone = false;
    private OnTaskCancelListener taskCancelListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                //Do nothing
            }
        };
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.2f);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mode", mode);
        outState.putString("message", message);
        outState.putInt("currentProgressValue", currentProgressValue);
        outState.putInt("maxProgressValue", maxProgressValue);
        outState.putBoolean("isTaskDone", isTaskDone);
        outState.putParcelable("taskCancelListener", taskCancelListener);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle outState) {
        super.onViewStateRestored(outState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.smart_dialog_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle outState) {
        ButterKnife.bind(this, view);
        if (outState != null) {
            this.mode = (FTSmartDialogMode) outState.getSerializable("mode");
            this.message = outState.getString("message");
            this.currentProgressValue = outState.getInt("currentProgressValue");
            this.maxProgressValue = outState.getInt("maxProgressValue");
            this.isTaskDone = outState.getBoolean("isTaskDone");
            this.taskCancelListener = outState.getParcelable("taskCancelListener");
        }
        applyCurrentState();
    }

    public FTSmartDialog show(@NonNull FragmentManager fragmentManager) {
        try {
            fragmentManager.beginTransaction().add(this, getTag()).commitNowAllowingStateLoss();
        } catch (IllegalStateException e) {
            Log.d(FTSmartDialog.class.getName(), "Exception", e);
        }
        return this;
    }

    @Override
    public void dismiss() {
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.task_cancel_button)
    void onTaskCancelClicked() {
        dismiss();
        taskCancelListener.onTaskCancelled();
    }

    public FTSmartDialog setMode(FTSmartDialogMode mode) {
        this.mode = mode;
        applyCurrentState();
        return this;
    }

    public FTSmartDialog setProgress(int currentProgressValue, int maxProgressValue) {
        this.currentProgressValue = currentProgressValue;
        this.maxProgressValue = maxProgressValue;
        applyCurrentState();
        return this;
    }

    public FTSmartDialog setMessage(String progressText) {
        this.message = progressText;
        applyCurrentState();
        return this;
    }

    public FTSmartDialog setTaskDone() {
        this.isTaskDone = true;
        applyCurrentState();
        return this;
    }

    public FTSmartDialog setCancellable(OnTaskCancelListener taskCancelListener) {
        this.taskCancelListener = taskCancelListener;
        applyCurrentState();
        return this;
    }

    public enum FTSmartDialogMode {
        SPINNER, PROGRESS_INDICATOR
    }

    private void applyCurrentState() {
        if (getView() != null) {
            //Hiding all views first
            mSpinner.setVisibility(View.GONE);
            mProgressIndicator.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.GONE);
            mTaskCancelButton.setVisibility(View.GONE);
            mTaskDoneImageView.setVisibility(View.GONE);
            //Displaying only required view(s)
            if (mode == FTSmartDialogMode.SPINNER && !isTaskDone) {
                mSpinner.setVisibility(View.VISIBLE);
                isTaskDone = false;
            } else if (mode == FTSmartDialogMode.PROGRESS_INDICATOR && !isTaskDone) {
                mProgressIndicator.setVisibility(View.VISIBLE);
                mProgressIndicator.setProgress(currentProgressValue, maxProgressValue);
                isTaskDone = false;
            }
            if (message != null) {
                mMessageTextView.setVisibility(View.VISIBLE);
                mMessageTextView.setText(message);
            }
            if (isTaskDone) {
                mTaskDoneImageView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(this::dismiss, 2000);
            }
            if (taskCancelListener != null && !isTaskDone) {
                mTaskCancelButton.setVisibility(View.VISIBLE);
                isTaskDone = false;
            }
        }
    }

    public interface OnTaskCancelListener extends Parcelable {
        void onTaskCancelled();

        @Override
        default int describeContents() {
            return 0;
        }

        @Override
        default void writeToParcel(Parcel dest, int flags) {
        }
    }
}