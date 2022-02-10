package com.fluidtouch.noteshelf.commons.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class FTBaseDialog extends DialogFragment {
    private Context mContext;
    public boolean dismissParent = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
           /* coverTheme = (FTNTheme) savedInstanceState.getSerializable("FTCover");
            paperTheme = (FTNTheme) savedInstanceState.getSerializable("FTPaper");*/

        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (isMobile()) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
            bottomSheetDialog.getBehavior().setDraggable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDialog.getBehavior().setPeekHeight(BottomSheetBehavior.SAVE_PEEK_HEIGHT);
            Dialog parentDialog = getParentDialog();
            if (parentDialog != null && parentDialog instanceof BottomSheetDialog) {
                bottomSheetDialog.getBehavior().setState(((BottomSheetDialog) parentDialog).getBehavior().getState());
            }
            return bottomSheetDialog;
        } else {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.0f);
                window.setGravity(Gravity.CENTER);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.getDecorView().setElevation(0);
            }
            return dialog;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //If current fragment is child then, show slide in animation.
        if (isChildFragment() && !isMobile()) {
            FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), true, null);
        }

        //Enable scroll if bottomSheet is fully expanded.
        if (isMobile()) {
            ScrollView scrollView = view.findViewById(R.id.scrollView);
            if (scrollView != null) {
                scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    ((BottomSheetDialog) getDialog()).getBehavior().setDraggable(!scrollView.canScrollVertically(-1));
                });
            }
        }

        if (getDialog() != null)
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                if ((event.getMetaState() & KeyEvent.META_CTRL_ON) != 0) {
                    if (getActivity() != null)
                        getActivity().onKeyDown(keyCode, event);
                    dismiss();
                    return true;
                }
                return false;
            });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismiss();
        if (getFragmentManager() != null) {
            show(getFragmentManager());
        }
    }

    public void show(@NonNull FragmentManager fragmentManager) {
        show(fragmentManager, getTag());
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        try {
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            FTLog.logCrashException(e);
            e.printStackTrace();
        }
    }

    public Context getContext() {
        return mContext == null ? super.getContext() : mContext;
    }

    @Override
    public void dismiss() {
        if (isChildFragment() && !isMobile()) {
            FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), false, this::dismissAllowingStateLoss);
        } else {
            dismissAllowingStateLoss();
        }
    }

    public void dismissAll() {
        dismissAllowingStateLoss();
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof DialogFragment) {
                    ((DialogFragment) fragment).dismissAllowingStateLoss();
                }
            }
        }
    }

    public Size getDialogSizeByPercentage(float widthPer, float heightPer) {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            int width = (int) (ScreenUtil.getScreenWidth(getContext()) * widthPer);
            int height = (int) (ScreenUtil.getScreenHeight(getContext()) * heightPer);
            return new Size(width, height);
        } else {
            int width = (int) (ScreenUtil.getScreenWidth(getContext()) * heightPer);
            int height = (int) (ScreenUtil.getScreenHeight(getContext()) * widthPer);
            return new Size(width, height);
        }
    }

    public boolean isMobile() {
        return getResources().getConfiguration().smallestScreenWidthDp < 600;
    }

    public Dialog getParentDialog() {
        Dialog dialog = null;
        if (getParentFragment() != null && getParentFragment() instanceof DialogFragment) {
            dialog = ((DialogFragment) getParentFragment()).getDialog();
        }
        return dialog;
    }

    public boolean isChildFragment() {
        return getParentFragment() != null && getParentFragment() instanceof DialogFragment;
    }

    public static class Popup extends FTBaseDialog {

        protected View atView;
        protected int dialogX = 0;
        protected int dialogY = 0;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            if (isMobile()) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
                bottomSheetDialog.getBehavior().setDraggable(true);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                bottomSheetDialog.getBehavior().setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
                Dialog parentDialog = getParentDialog();
                if (parentDialog != null && parentDialog instanceof BottomSheetDialog) {
                    bottomSheetDialog.getBehavior().setState(((BottomSheetDialog) parentDialog).getBehavior().getState());
                }
            } else {
                dialog.setCanceledOnTouchOutside(true);
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.window_bg);
                    window.setDimAmount(0.3f);
                    window.setElevation(getResources().getDimensionPixelOffset(R.dimen.new_5dp));
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    TypedValue tv = new TypedValue();
                    if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                        int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                        layoutParams.y += actionBarHeight;
                    }
                }
            }
            return dialog;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            if (dismissParent && isChildFragment() && !isMobile()) {
                FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), true, () -> getParentFragment().getView().setVisibility(View.GONE));
            }

            if (isMobile()) {
                ScrollView scrollView = view.findViewById(R.id.scrollView);
                if (scrollView != null) {
                    scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        ((BottomSheetDialog) getDialog()).getBehavior().setDraggable(!scrollView.canScrollVertically(-1));
                    });
                }
            }

            if (getDialog() != null)
                getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                    if ((event.getMetaState() & KeyEvent.META_CTRL_ON) != 0) {
                        if (getActivity() != null)
                            getActivity().onKeyDown(keyCode, event);
                        dismiss();
                        return true;
                    }
                    return false;
                });
        }

        @Override
        public void onResume() {
            if (!isMobile() && atView != null) {
                Window window = getDialog().getWindow();
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                int[] location = new int[2];
                atView.getLocationOnScreen(location);
                dialogX = location[0];
                dialogY = location[1];
                window.setGravity(Gravity.TOP | Gravity.START);
                layoutParams.x = dialogX;
                layoutParams.y = dialogY + atView.getHeight();
                window.setAttributes(layoutParams);
            }
            super.onResume();
        }

        @Override
        public void dismiss() {
            if (dismissParent && isChildFragment() && !isMobile()) {
                FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), false, () -> {
                    getParentFragment().getView().setVisibility(View.VISIBLE);
                    dismissAllowingStateLoss();
                });
            } else {
                dismissAllowingStateLoss();
            }
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            super.onCancel(dialog);
            if (dismissParent) dismissAll();
            else dismiss();
        }

        public void show(@NonNull View atView, @NonNull FragmentManager fragmentManager) {
            this.atView = atView;
            super.show(fragmentManager);
        }
    }
}