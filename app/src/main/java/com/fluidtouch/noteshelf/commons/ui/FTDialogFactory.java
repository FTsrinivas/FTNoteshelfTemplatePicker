package com.fluidtouch.noteshelf.commons.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf2.R;

public class FTDialogFactory {
    public static void showAlertDialog(String title, String message, final OnAlertDialogShownListener onAlertListener) {
        Context context = FTApp.getInstance().getCurActCtx();
        showAlertDialog(context, title, message, context.getString(R.string.yes), context.getString(R.string.no), true, onAlertListener);
    }

    public static void showAlertDialog(Context context, String title, String message, String positiveTitle, String negativeTitle, final OnAlertDialogShownListener onAlertListener) {
        showAlertDialog(context, title, message, positiveTitle, negativeTitle, true, onAlertListener);
    }

    public static void showAlertDialog(Context context, String title, String message, String positiveTitle, String negativeTitle, boolean isCancelable, final OnAlertDialogShownListener onAlertListener) {
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onAlertListener != null) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        onAlertListener.onPositiveClick(dialog, which);
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        onAlertListener.onNegativeClick(dialog, which);
                    }
                }
            }
        };

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton(positiveTitle, onClickListener);
        alertBuilder.setNegativeButton(negativeTitle, onClickListener);
        AlertDialog alert = alertBuilder.create();
        alert.setCancelable(isCancelable);
        alert.setCanceledOnTouchOutside(isCancelable);
        alert.show();
    }

    public static AlertDialog showAlertDialog(Context context, int layout, int posTitle, int negTitle, final OnCustomAlertDialogShownListener onAlertListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(layout);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        try {
        } catch (Exception e) {
            /*WindowManager$BadTokenException will be caught and the app would
             not display the 'Force Close' message*/
            e.printStackTrace();
        }

        if (onAlertListener != null) {
            TextView posTextView = alertDialog.findViewById(R.id.alert_dialog_action_buttons_pos_text_view);
            TextView negTextView = alertDialog.findViewById(R.id.alert_dialog_action_buttons_neg_text_view);

            posTextView.setText(context.getString(posTitle));
            negTextView.setText(context.getString(negTitle));

            posTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAlertListener.onPositiveClick(alertDialog);
                }
            });

            negTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAlertListener.onNegativeClick();
                    alertDialog.dismiss();
                }
            });
        }

        return alertDialog;
    }

    public interface OnAlertDialogShownListener {
        void onPositiveClick(DialogInterface dialog, int which);

        void onNegativeClick(DialogInterface dialog, int which);
    }

    public interface OnCustomAlertDialogShownListener {
        void onPositiveClick(AlertDialog alertDialog);

        void onNegativeClick();
    }
}
