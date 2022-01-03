package com.fluidtouch.noteshelf.document.thumbnailview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTFinderFilterPopup extends FTBaseDialog.Popup {
    @BindView(R.id.finder_filter_all_pages_check_image_view)
    ImageView mAllPagesCheck;
    @BindView(R.id.finder_filter_bookmarked_check_image_view)
    ImageView mBookmarkedCheck;

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
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            bottomSheetDialog.getBehavior().setDraggable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.END);
                window.getAttributes().y = ScreenUtil.convertDpToPx(getContext(), 145);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_finder_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        updateViews(FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, false));

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onFilterOptionSelected(FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, false));
    }

    @OnClick({R.id.finder_filter_all_pages, R.id.finder_filter_bookmarked})
    void onFilterOptionClicked(View view) {
        boolean isBookmarkView = view.getId() == R.id.finder_filter_bookmarked;
        FTFirebaseAnalytics.logEvent(isBookmarkView ? "Finder_Filters_Bookmarked" : "Finder_Filters_AllPages");
        updateViews(isBookmarkView);
        if (mListener != null) mListener.onFilterOptionSelected(isBookmarkView);
        dismiss();
    }

    private void updateViews(boolean isBookmarkView) {
        mAllPagesCheck.setVisibility(isBookmarkView ? View.GONE : View.VISIBLE);
        mBookmarkedCheck.setVisibility(isBookmarkView ? View.VISIBLE : View.GONE);

        FTApp.getPref().save(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, isBookmarkView);
    }

    interface Listener {
        void onFilterOptionSelected(boolean isBookmarkView);
    }
}
