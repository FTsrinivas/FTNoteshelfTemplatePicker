package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf2.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSelectThemeStyleDialog extends DialogFragment {

    DialogResult callback;

    public static FTSelectThemeStyleDialog newInstance(DialogResult listener) {
        FTSelectThemeStyleDialog ftSelectThemeStyleDialog = new FTSelectThemeStyleDialog();
        ftSelectThemeStyleDialog.callback = listener;
        return ftSelectThemeStyleDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL)
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                else
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_cover_style, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick({R.id.customcover1, R.id.customcover2, R.id.customcover3, R.id.customcover4, R.id.customcover5, R.id.customcover6})
    public void onCoverChoose(View v) {
        if (callback != null)
            callback.onDataSubmit(v.getId());
        dismiss();
    }

    @OnClick(R.id.imgSelectDialogClose)
    public void onClose() {
        dismiss();
    }

    public interface DialogResult {
        public void onDataSubmit(int selectedTheme);
    }
}
