package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.Serializable;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSelectPagesPopup extends FTBaseDialog.Popup {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            bottomSheetDialog.getBehavior().setDraggable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            dialog.getWindow().setGravity(Gravity.TOP | Gravity.END);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_select_pages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick({R.id.select_pages_current_page, R.id.select_pages_all_pages, R.id.select_pages_select_pages})
    void onOptionSelected(View view) {
        SelectPageOption option = SelectPageOption.CURRENT_PAGE;
        switch (view.getId()) {
            case R.id.select_pages_current_page:
                FTFirebaseAnalytics.logEvent("Share_CurrentPage");
                option = SelectPageOption.CURRENT_PAGE;
                if (getActivity() != null)
                    ((SelectPagesPopupListener) getActivity()).onSelectedPagesToShare(option);
                break;
            case R.id.select_pages_all_pages:
                FTFirebaseAnalytics.logEvent("NB_Share_AllPages");
                option = SelectPageOption.ALL_PAGES;
                if (getActivity() != null)
                    ((SelectPagesPopupListener) getActivity()).onSelectedPagesToShare(option);
                break;
            case R.id.select_pages_select_pages:
                FTFirebaseAnalytics.logEvent("Share_SelectPages");
                option = SelectPageOption.SELECT_PAGES;
                if (getActivity() != null)
                    ((SelectPagesPopupListener) getActivity()).onSelectedPagesToShare(option);
                dismiss();
                break;

        }
    }

    public interface SelectPagesPopupListener {
        void onSelectedPagesToShare(SelectPageOption option);
    }

    public enum SelectPageOption implements Serializable {
        CURRENT_PAGE, ALL_PAGES, SELECT_PAGES
    }
}
