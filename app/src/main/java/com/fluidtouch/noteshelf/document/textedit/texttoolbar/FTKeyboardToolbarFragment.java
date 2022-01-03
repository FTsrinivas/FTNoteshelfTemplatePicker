package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import androidx.fragment.app.Fragment;

public abstract class FTKeyboardToolbarFragment extends Fragment {

    public interface Callbacks {
        FTKeyboardToolbarFragment getToolBarFragment();
    }

}
