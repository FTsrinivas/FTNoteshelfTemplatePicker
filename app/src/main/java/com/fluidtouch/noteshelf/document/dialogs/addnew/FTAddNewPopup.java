package com.fluidtouch.noteshelf.document.dialogs.addnew;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.List;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddNewPopup extends FTBaseDialog.Popup implements DismissListener {
    @BindView(R.id.page_tab)
    ImageView mPageTab;
    @BindView(R.id.media_tab)
    ImageView mMediaTab;
    @BindView(R.id.tag_tab)
    ImageView mTagTab;

    private int selectedTab = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.START);
            }
        }
        return dialog;
    }

    private Observer closeDialogObserver = (observable, o) -> {
        if (isAdded()) {
            dismiss();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_add_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        ObservingService.getInstance().addObserver("closeAddNew", closeDialogObserver);

        View prevSelectedTab = view.findViewById(FTApp.getPref().get(SystemPref.LAST_SELECTED_ADD_NEW_TAB, mPageTab.getId()));
        if (prevSelectedTab != null) {
            prevSelectedTab.callOnClick();
        } else {
            mPageTab.callOnClick();
        }
    }

    @OnClick({R.id.page_tab, R.id.media_tab, R.id.tag_tab})
    void onTabSelected(View view) {
        if (selectedTab == view.getId()) {
            return;
        }
        FTApp.getPref().save(SystemPref.LAST_SELECTED_ADD_NEW_TAB, view.getId());
        switch (view.getId()) {
            case R.id.page_tab:
                getChildFragmentManager().beginTransaction().replace(R.id.child_container_layout, new FTAddNewPageFragment()).commit();
                mPageTab.setBackgroundResource(R.drawable.tab_item_bg);
                mMediaTab.setBackgroundResource(0);
                mTagTab.setBackgroundResource(0);
                break;
            case R.id.media_tab:
                getChildFragmentManager().beginTransaction().replace(R.id.child_container_layout, new FTAddNewMediaFragment()).commit();
                mMediaTab.setBackgroundResource(R.drawable.tab_item_bg);
                mPageTab.setBackgroundResource(0);
                mTagTab.setBackgroundResource(0);
                break;
            case R.id.tag_tab:
                getChildFragmentManager().beginTransaction().replace(R.id.child_container_layout, new FTAddNewTagFragment()).commit();
                mTagTab.setBackgroundResource(R.drawable.tab_item_bg);
                mPageTab.setBackgroundResource(0);
                mMediaTab.setBackgroundResource(0);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        ObservingService.getInstance().removeObserver("closeAddNew", closeDialogObserver);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (!fragment.isDetached())
                getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }
}
