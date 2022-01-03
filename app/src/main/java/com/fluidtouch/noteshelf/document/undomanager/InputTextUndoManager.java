package com.fluidtouch.noteshelf.document.undomanager;

import android.text.SpannableStringBuilder;
import android.widget.EditText;

public class InputTextUndoManager {
    private InputTextHistory mInputTextHistory;
    private EditText mEditText;

    public InputTextUndoManager(EditText editText) {
        this.mEditText = editText;
        this.mInputTextHistory = new InputTextHistory();
    }

    public void add(CharSequence text) {
        mInputTextHistory.addUndo(new InputTextItem(text));
    }

    public void undo() {
        add(new SpannableStringBuilder(mEditText.getText()));
        mEditText.setText(mInputTextHistory.getPrev(mEditText.getText()));
    }

    public void redo() {
        mEditText.setText(mInputTextHistory.getNext());
    }
}
