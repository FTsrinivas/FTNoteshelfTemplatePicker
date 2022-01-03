package com.fluidtouch.noteshelf.document.undomanager;

class InputTextItem {
    private CharSequence mText;

    InputTextItem(CharSequence text) {
        mText = text;
    }

    public CharSequence getText() {
        return mText;
    }
}