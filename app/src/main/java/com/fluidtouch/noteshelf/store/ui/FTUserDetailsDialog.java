package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTUserDetailsDialog extends DialogFragment {

    @BindView(R.id.layMain)
    ConstraintLayout layMain;
    @BindView(R.id.txtEmail)
    TextView txtEmail;
    FTStoreCallbacks callback;
    String email = "";
    int y = 0;

    public static FTUserDetailsDialog newInstance(String email, int y, FTStoreCallbacks callback) {
        FTUserDetailsDialog ftUserDetailsDialog = new FTUserDetailsDialog();
        ftUserDetailsDialog.email = email;
        ftUserDetailsDialog.y = y;
        ftUserDetailsDialog.callback = callback;
        return ftUserDetailsDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.RIGHT | Gravity.TOP);
        dialogWindow.setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TypedValue tv = new TypedValue();
        lp.y += y;
//        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
//            lp.y += actionBarHeight;
//        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_signin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
//        layMain.setPadding(0, y, ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.twenty)), 0);
        txtEmail.setText(email);
    }

    @OnClick(R.id.txtLogout)
    public void onSignOut() {
        callback.onSignOut();
        dismiss();
    }
}
