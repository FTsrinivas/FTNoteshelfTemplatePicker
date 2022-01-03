package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTGotoPageDialog extends DialogFragment {
    @BindView(R.id.page_number_edit_text)
    EditText mPageNumberEditText;

    private final String numberRegexStr = "^[0-9]*$";

    private Listener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            mListener = (Listener) getParentFragment();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setDimAmount(0.3f);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawableResource(R.drawable.window_bg_noshadow);
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_goto_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        FTNoteshelfPage currentPage = mListener.getCurrentPage();
        if (currentPage != null) {
            FTNoteshelfDocument currentDocument = currentPage.getParentDocument();
            mPageNumberEditText.setHint(getString(R.string.input_goto_page, currentDocument.pages(getContext()).size()));
            mPageNumberEditText.setFilters(new InputFilter[]{(InputFilter) (source, start, end, dest, dstart, dend) -> {
                String outPutString = mPageNumberEditText.getText().toString() + source.toString();
                if (!TextUtils.isEmpty(outPutString) && outPutString.matches(numberRegexStr)) {
                    int num = Integer.parseInt(outPutString);
                    if (num >= 1 && num <= currentDocument.pages(getContext()).size()) {
                        return source;
                    }
                }
                return "";
            }});
        }
        mPageNumberEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (!TextUtils.isEmpty(mPageNumberEditText.getText()) && actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent != null && (keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                view.findViewById(R.id.button_ok).callOnClick();
                return true;
            }
            return false;
        });
    }

    @OnClick(R.id.button_ok)
    void onOKClicked() {
        String input = mPageNumberEditText.getText().toString();
        if (!TextUtils.isEmpty(input) && input.matches(numberRegexStr)) {
            int pageNumber = Integer.parseInt(input);
            FTNoteshelfPage currentPage = mListener.getCurrentPage();
            if (currentPage != null) {
                FTNoteshelfDocument currentDocument = currentPage.getParentDocument();
                if (pageNumber >= 1 && pageNumber <= currentDocument.pages(getContext()).size()) {
                    mListener.onPageNumberSelected(pageNumber - 1);
                    dismissAllowingStateLoss();
                }
            }
        }
    }

    @OnClick(R.id.button_cancel)
    void onCancelClicked() {
        dismissAllowingStateLoss();
    }

    interface Listener {
        void onPageNumberSelected(int pageNumber);

        FTNoteshelfPage getCurrentPage();
    }
}