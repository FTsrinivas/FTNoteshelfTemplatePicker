package com.fluidtouch.noteshelf.shelf.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FTRenameDialog extends DialogFragment {
    //region View Bindings
    @BindView(R.id.rename_dialog_title_text_view)
    TextView renameDialogTitleTextView;
    @BindView(R.id.rename_dialog_edit_text)
    EditText renameDialogEditText;
    @BindView(R.id.rename_dialog_desc_text_view)
    TextView renameDialogDescTextView;
    @BindView(R.id.alert_dialog_action_buttons_pos_text_view)
    TextView positiveButton;
    @BindView(R.id.alert_dialog_action_buttons_neg_text_view)
    TextView negitiveButton;
    @BindView(R.id.renam_dialog_checkbox)
    CheckBox renameDialogCheckbox;
    @BindView(R.id.txtError)
    TextView txtError;
    //endregion

    //region Memeber Variables
    private RenameType type;
    private String displayName;
    private int position;
    private RenameListener listener;
    //endregion

    //region Instance
    public static FTRenameDialog newInstance(RenameType type, String displayName, final int position, RenameListener listener) {
        FTRenameDialog dialog = new FTRenameDialog();
        dialog.type = type;
        dialog.displayName = displayName;
        dialog.position = position;
        dialog.listener = listener;
        return dialog;
    }
    //endregion

    private String getPositiveButtonText(RenameType type) {
        int textId;
        if (type.equals(RenameType.NEW_CATEGORY) || type.equals(RenameType.NEW_GROUP)) {
            textId = R.string.create;
        } else if (type.equals(RenameType.ASK_PASSWORD)) {
            textId = R.string.ok;
        } else {
            textId = R.string.update;
        }

        return getString(textId);
    }

    //region Lifecycle methods
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = alertDialogBuilder.create();
        View view = dialog.getLayoutInflater().inflate(R.layout.rename_dialog, null);
        dialog.setView(view);
        ButterKnife.bind(this, view);
        setRetainInstance(true);

        positiveButton.setText(getPositiveButtonText(type));
        negitiveButton.setText(getString(R.string.cancel));
        renameDialogEditText.setText(displayName);

        setUpUI(type);

        if (type.equals(RenameType.ASK_PASSWORD)) {
            renameDialogCheckbox.setVisibility(View.VISIBLE);
            renameDialogCheckbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    txtError.setVisibility(View.GONE);
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }

        renameDialogEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER
                    && getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return true;
            }
            return false;
        });

        dialog.setOnDismissListener(this);
        return dialog;
    }

    //region UI Refresh
    private void setUpUI(RenameType type) {
        switch (type) {
            case NEW_CATEGORY:
                renameDialogTitleTextView.setText(getString(R.string.new_category));
                renameDialogEditText.setText("");
                renameDialogEditText.setHint(getString(R.string.untitled));
                renameDialogDescTextView.setText(getString(R.string.enter_a_name_for_your, getString(R.string.category)));
                renameDialogCheckbox.setVisibility(View.GONE);
                break;
            case RENAME_CATEGORY:
                refreshUI(R.string.rename_category, R.string.untitled, R.string.category);
                break;
            case NEW_GROUP:
                renameDialogTitleTextView.setText(getString(R.string.new_group));
                renameDialogEditText.setText("");
                renameDialogEditText.setHint(getString(R.string.untitled));
                renameDialogDescTextView.setText(getString(R.string.enter_a_name_for_your, getString(R.string.group)));
                renameDialogCheckbox.setVisibility(View.GONE);
                break;
            case RENAME_GROUP:
                refreshUI(R.string.group_title, R.string.untitled, R.string.group);
                break;
            case NEW_NOTEBOOK:
            case RENAME_NOTEBOOK:
                refreshUI(R.string.notebook_title, R.string.notebook_name, R.string.notebook);
                break;
            case ASK_PASSWORD:
                renameDialogTitleTextView.setText(getString(R.string.password_title));
                renameDialogEditText.setHint(getString(R.string.enter_password));
                renameDialogEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                renameDialogEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                renameDialogDescTextView.setVisibility(View.GONE);
                renameDialogCheckbox.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void refreshUI(int title, int hint, int desc) {
        renameDialogTitleTextView.setText(getString(title));
        renameDialogEditText.setHint(getString(hint));
        renameDialogDescTextView.setText(getString(R.string.enter_a_name_for_your, getString(desc)));
        renameDialogCheckbox.setVisibility(View.GONE);
    }
    //endregion

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        dismissKeyboard();
    }

    @Override
    public void dismiss() {
        dismissKeyboard();
        super.dismiss();
    }

    public void setErrorMessage() {
        txtError.setText(R.string.wrong_password);
        txtError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null)
            return;
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            int dialogWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.rename_dialog_width);
            int dialogHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        }
    }
    //endregion

    //region OnClick
    @OnClick(R.id.alert_dialog_action_buttons_pos_text_view)
    public void onPositiveClick() {
        if (type == RenameType.NEW_CATEGORY || type == RenameType.NEW_GROUP) {
            String name = renameDialogEditText.getHint().toString();
            if (!TextUtils.isEmpty(renameDialogEditText.getText().toString())) {
                name = renameDialogEditText.getText().toString().trim().replaceAll(" +", " ");
            }
            (listener).renameShelfItem(name, position, this);
            dismiss();
        } else {
            if (TextUtils.isEmpty(renameDialogEditText.getText().toString())) {
                renameDialogEditText.setError(getString(R.string.field_cannot_be_empty));
            } else if (TextUtils.isEmpty(renameDialogEditText.getText().toString().trim().replaceAll(" +", " "))) {
                renameDialogEditText.setError(getString(R.string.field_cannot_be_empty));
            } else {
                dismissKeyboard();
                if (type == RenameType.ASK_PASSWORD && !renameDialogCheckbox.isChecked()) {
                    txtError.setText(R.string.please_read_and_enable_the_checkbox_to_import);
                    txtError.setVisibility(View.VISIBLE);
                    return;
                }
                FTLog.crashlyticsLog("UI: Confirmed create/rename of notebook in dialog");
                (listener).renameShelfItem(renameDialogEditText.getText().toString().trim().replaceAll(" +", " "), position, this);
                if (type != RenameType.ASK_PASSWORD)
                    dismiss();
            }
        }
    }

    @OnClick(R.id.alert_dialog_action_buttons_neg_text_view)
    public void onNegativeClick() {
        FTLog.crashlyticsLog("UI: Cancelled create/rename");
        dismissKeyboard();
        //Log.d("TemplatePicker==>","VMK PasswordProtected FTRenameDialog onNegativeClick::-");
        (listener).dialogActionCancel();
        dismissAllDialogs(getParentFragmentManager());
        dismiss();
    }

    public static void dismissAllDialogs(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();

        if (fragments == null)
            return;

        for (Fragment fragment : fragments) {
            if (fragment instanceof DialogFragment) {
                DialogFragment dialogFragment = (DialogFragment) fragment;
                dialogFragment.dismissAllowingStateLoss();
            }

            FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            if (childFragmentManager != null)
                dismissAllDialogs(childFragmentManager);
        }
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(renameDialogEditText.getWindowToken(), 0);
    }
    //endregion

    public enum RenameType {
        NEW_CATEGORY, RENAME_CATEGORY, NEW_GROUP, RENAME_GROUP, NEW_NOTEBOOK, RENAME_NOTEBOOK, ASK_PASSWORD
    }

    public interface RenameListener {
        void renameShelfItem(String updatedName, int position, DialogFragment dialogFragment);

        void dialogActionCancel();
    }
}