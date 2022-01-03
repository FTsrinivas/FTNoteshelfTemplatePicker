package com.fluidtouch.noteshelf.document.dialogs;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.KeyboardHeightProvider;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf2.R;

import java.util.Observer;

public class FTBookmarkDialog extends FTBaseDialog.Popup {

    private FTNoteshelfPage mCurrentPage;

    private Observer keyBoardHeightChangeListener = (o, arg) -> {
        if (getDialog() != null && atView != null) {
            int height = (int) arg;
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            if (height > 0) {
                int diff = ScreenUtil.getScreenHeight(getContext()) - (dialogY + height + getView().getHeight());
                if (dialogX != 0 && diff < 0) {
                    int sourceX = dialogX;
                    int sourceY = ScreenUtil.getScreenHeight(getContext()) - (height + getView().getHeight());
                    window.setGravity(Gravity.TOP | Gravity.START);
                    layoutParams.x = sourceX;
                    layoutParams.y = sourceY;
                    window.setAttributes(layoutParams);
                }
            } else {
                window.setGravity(Gravity.TOP | Gravity.START);
                layoutParams.x = dialogX;
                layoutParams.y = dialogY + atView.getHeight();
                window.setAttributes(layoutParams);
            }
        }
    };

    public FTBookmarkDialog(FTNoteshelfPage currentPage) {
        mCurrentPage = currentPage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return inflater.inflate(R.layout.dialog_bookmark, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_container, new FTBookmarkFragment(mCurrentPage), FTBookmarkFragment.class.getName()).commit();
        ObservingService.getInstance().addObserver(KeyboardHeightProvider.strKeyBoardHeightChangeListener, keyBoardHeightChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FTBookmarkFragment.class.getName());
        if (fragment != null)
            getChildFragmentManager().beginTransaction().remove(fragment).commit();
        ObservingService.getInstance().removeObserver("keyBoardHeightChangeListener", keyBoardHeightChangeListener);
    }
}