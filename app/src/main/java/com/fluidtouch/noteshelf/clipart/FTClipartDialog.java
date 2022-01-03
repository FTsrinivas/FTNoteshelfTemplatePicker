package com.fluidtouch.noteshelf.clipart;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.clipart.pixabay.dialog.FTPixabayClipartFragment;
import com.fluidtouch.noteshelf.clipart.unsplash.dialog.FTUnsplashClipartFragment;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTClipartDialog extends FTBaseDialog.Popup implements FTPixabayClipartFragment.Callback, FTUnsplashClipartFragment.Callback {
    @BindView(R.id.pixabay_tab)
    ImageView mPixabayTab;
    @BindView(R.id.unsplash_tab)
    ImageView mUnsplashTab;
    @BindView(R.id.network_error_layout)
    LinearLayout mNetworkErrorLayout;
    @BindView(R.id.empty_clipart_error_layout)
    LinearLayout mEmptyClipartErrorLayout;
    @BindView(R.id.empty_clipart_error_text)
    TextView mEmptyClipartErrorText;

    private String mSearchKey = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (isMobile()) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            bottomSheetDialog.getBehavior().setDraggable(false);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.START);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_clipart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        FTAnimationUtils.showEndPanelAnimation(getContext(), view, true, null);

        if (FTApp.getPref().get(SystemPref.IS_PIXABAY_SELECTED, true)) {
            mPixabayTab.callOnClick();
        } else {
            mUnsplashTab.callOnClick();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissAll();
    }

    @Override
    public void dismissAll() {
        super.dismissAll();
        ObservingService.getInstance().postNotification("closeAddNew", null);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (!fragment.isDetached())
                getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    @OnClick(R.id.pixabay_tab)
    void onPixabayTabSelected() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_MediaLib_Pixabay");
        FTApp.getPref().save(SystemPref.IS_PIXABAY_SELECTED, true);
        mPixabayTab.setBackgroundResource(R.drawable.tab_item_bg);
        mUnsplashTab.setBackgroundResource(0);

        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new FTPixabayClipartFragment(mSearchKey)).commitAllowingStateLoss();
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            getChildFragmentManager().beginTransaction().remove(fragments.get(0)).commitAllowingStateLoss();
        }
    }

    @OnClick(R.id.unsplash_tab)
    void onUnsplashTabSelected() {
        FTFirebaseAnalytics.logEvent("NB_AddNew_MediaLib_Unsplash");
        FTApp.getPref().save(SystemPref.IS_PIXABAY_SELECTED, false);
        mUnsplashTab.setBackgroundResource(R.drawable.tab_item_bg);
        mPixabayTab.setBackgroundResource(0);

        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, new FTUnsplashClipartFragment(mSearchKey)).commitAllowingStateLoss();
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            getChildFragmentManager().beginTransaction().remove(fragments.get(0)).commitAllowingStateLoss();
        }
    }

    @OnClick(R.id.network_error_layout)
    void onNetworkIconClicked() {
        if (FTApp.getPref().get(SystemPref.IS_PIXABAY_SELECTED, true)) {
            onPixabayTabSelected();
        } else {
            onUnsplashTabSelected();
        }
    }

    @OnClick(R.id.dialog_back_button)
    void onBackButtonClicked() {
        FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), false, this::dismiss);
    }

    public void setSearchKey(String searchKey) {
        mSearchKey = searchKey;
    }

    @Override
    public void updateErrorUI(ClipartError errorCode) {
        switch (errorCode) {
            case NONE:
                mEmptyClipartErrorLayout.setVisibility(View.GONE);
                mNetworkErrorLayout.setVisibility(View.GONE);
                break;
            case NO_RECENTS:
                mEmptyClipartErrorLayout.setVisibility(View.VISIBLE);
                mNetworkErrorLayout.setVisibility(View.GONE);
                break;
            case NETWORK_ERROR:
                mNetworkErrorLayout.setVisibility(View.VISIBLE);
                mEmptyClipartErrorLayout.setVisibility(View.GONE);
                break;
            case NO_RESULTS:
                mEmptyClipartErrorLayout.setVisibility(View.VISIBLE);
                mNetworkErrorLayout.setVisibility(View.GONE);
                mEmptyClipartErrorText.setText(R.string.no_result_found);
                break;
        }
    }

    @Override
    public void setClipartImageAnnotation(String bitmapUri) {
        if (getActivity() != null) {
            ((ClipartDialogListener) getActivity()).setClipartImageAnnotation(bitmapUri);
        }
    }

    @Override
    public void dismissOnClipartSelected() {
        dismissAll();
    }

    public interface ClipartDialogListener {
        void setClipartImageAnnotation(String clipartImagePath);
    }
}