package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.store.view.FTResizeImageView;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddPaperThemeDialog extends DialogFragment {

    @BindView(R.id.edtThemeName)
    TextView txtEmail;
    @BindView(R.id.imaTheme)
    FTResizeImageView imaTheme;
    DialogResult callback;
    Bitmap bitmap;
    int finalX = 215, finalY = 200;

    public static FTAddPaperThemeDialog newInstance(DialogResult listener, Bitmap bitmap) {
        FTAddPaperThemeDialog ftAddThemeDialog = new FTAddPaperThemeDialog();
        ftAddThemeDialog.callback = listener;
        ftAddThemeDialog.bitmap = bitmap;
        return ftAddThemeDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = dialog.getWindow();
        //dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_addtheme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (bitmap != null) {
            imaTheme.setImageBitmap(bitmap);
        } else {
            imaTheme.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btnCreateTheme)
    public void onCreate() {
        Log.d("TemplatePicker==>"," Custom Theme btnCreateTheme length::-"+txtEmail.getText().toString().trim().length());

        if (txtEmail.getText().toString().trim().length() == 0) {
            txtEmail.setError(getString(R.string.field_cannot_be_empty));
            return;
        }
        if (bitmap != null)
            bitmap = imaTheme.crop();
        File customPapersDir = new File(FTConstants.CUSTOM_PAPERS_PATH);
        if (!customPapersDir.exists()) {
            customPapersDir.mkdirs();
        }
        if (customPapersDir.exists()) {
            Stream<File> stream = Arrays.stream(customPapersDir.listFiles());
            if (stream.anyMatch(entry -> FTDocumentUtils.getFileNameWithoutExtension(getContext(), FTUrl.parse(entry.getName())).equals(txtEmail.getText().toString()))) {
                txtEmail.setError(getString(R.string.name_already_exists));
            } else {
                Log.d("TemplatePicker==>"," Custom Theme onCreate bitmap::-"+bitmap);
                callback.onDataSubmit(txtEmail.getText().toString(), bitmap);
                dismiss();
            }
        }
    }

    @OnClick(R.id.imgAddDialogClose)
    public void onClose() {
        dismiss();
    }

    public interface DialogResult {
        public void onDataSubmit(String name, Bitmap bitmap);
    }
}

