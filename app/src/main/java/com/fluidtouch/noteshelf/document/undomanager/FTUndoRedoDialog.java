package com.fluidtouch.noteshelf.document.undomanager;

import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTUndoRedoDialog extends DialogFragment {
    @BindView(R.id.txtUndo)
    protected TextView txtUndo;
    @BindView(R.id.txtRedo)
    protected TextView txtRedo;

    private UndoManager mUndoManager;

    public static FTUndoRedoDialog newInstance(UndoManager undoManager) {
        FTUndoRedoDialog undoRedoDialog = new FTUndoRedoDialog();
//        undoRedoDialog.mUndoManager = undoManager;
        return undoRedoDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.TOP | Gravity.START);
            window.setBackgroundDrawableResource(R.drawable.window_bg);
            window.setDimAmount(0.0f);
            window.setElevation(getResources().getDimensionPixelOffset(R.dimen.new_5dp));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            TypedValue tv = new TypedValue();
            if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                layoutParams.y += actionBarHeight;
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_undo_redo, container, false);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        boolean isUndoEnabled;
        boolean isRedoEnabled;

        mUndoManager = ((FTDocumentActivity) getActivity()).getUndoManager();
        if (mUndoManager.canUndo() && mUndoManager.canRedo()) {
            isUndoEnabled = true;
            isRedoEnabled = true;
        } else if (!mUndoManager.canUndo() && mUndoManager.canRedo()) {
            isUndoEnabled = false;
            isRedoEnabled = true;
        } else if (mUndoManager.canUndo() && !mUndoManager.canRedo()) {
            isUndoEnabled = true;
            isRedoEnabled = false;
        } else {
            isUndoEnabled = false;
            isRedoEnabled = false;
        }
        txtUndo.setEnabled(isUndoEnabled);
        txtRedo.setEnabled(isRedoEnabled);
    }

    @OnClick(R.id.txtUndo)
    protected void undo() {
        FTFirebaseAnalytics.logEvent("Undo_LongPress");
        FTLog.crashlyticsLog("Undo: Undo");
        mUndoManager.undo();
        if (!mUndoManager.canUndo())
            txtUndo.setEnabled(false);
        if (mUndoManager.canRedo())
            txtRedo.setEnabled(true);
    }

    @OnClick(R.id.txtRedo)
    protected void redo() {
        FTFirebaseAnalytics.logEvent("Undo_LongPress_Redo");
        FTLog.crashlyticsLog("Undo: Redo");
        mUndoManager.redo();
        if (!mUndoManager.canRedo())
            txtRedo.setEnabled(false);
        if (mUndoManager.canUndo())
            txtUndo.setEnabled(true);
    }

    @OnClick(R.id.layMain)
    protected void dismissDialog() {
        dismiss();
    }
}
