package com.fluidtouch.noteshelf.document.undomanager;

import java.util.ArrayList;
import java.util.List;

public class InputTextHistory {
    private int mPosition = 0;

    private List<InputTextItem> mUndoHistory = new ArrayList<>();
    private List<InputTextItem> mRedoHistory = new ArrayList<>();

    public void addUndo(InputTextItem item) {
        if ((mPosition > 0 && item.getText().toString().equals(mUndoHistory.get(mPosition - 1).getText().toString())))
            return;
        mUndoHistory.add(item);
        mPosition++;
    }

    private void removeUndo(int position) {
        mUndoHistory.remove(position);
    }

    private void addRedo(InputTextItem item) {
        if (!mRedoHistory.isEmpty() && item.getText().toString().equals(mRedoHistory.get(mRedoHistory.size() - 1).getText().toString()))
            return;
        mRedoHistory.add(item);
    }

    private void removeRedo(int position) {
        mRedoHistory.remove(position);
    }

    public CharSequence getPrev(CharSequence text) {
        if (mPosition > 0 && text.toString().equals(mUndoHistory.get(mPosition - 1).getText().toString())) {
            --mPosition;
            addRedo(mUndoHistory.get(mPosition));
            removeUndo(mPosition);
        }
        if (mPosition > 0) {
            return mUndoHistory.get(mPosition - 1).getText();
        } else {
            return "";
        }
    }

    public CharSequence getNext() {
        if (!mRedoHistory.isEmpty()) {
            addUndo(mRedoHistory.get(mRedoHistory.size() - 1));
            removeRedo(mRedoHistory.size() - 1);
            return mUndoHistory.get(mUndoHistory.size() - 1).getText();
        } else {
            return "";
        }
    }
}
