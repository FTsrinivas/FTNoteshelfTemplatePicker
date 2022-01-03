package com.fluidtouch.noteshelf.document.thumbnailview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.FTPageInsertPosition;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTFinderInsertPagePopup extends FTBaseDialog.Popup {

    @BindView(R.id.insert_page_above_text_view)
    TextView mInsertPageAboveTextView;
    @BindView(R.id.insert_page_below_text_view)
    TextView mInsertPageBelowTextView;
    @BindView(R.id.thumbnail_duplicate_popup_duplicate)
    TextView mDuplicateTextView;
    @BindView(R.id.divider)
    View mDivider;

    private int mPageCount;
    private PageInsertOptionListener mListener;

    public FTFinderInsertPagePopup(int pageCount, PageInsertOptionListener listener) {
        mPageCount = pageCount;
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_duplicate_thumbnail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (isMobile()) {
            ViewGroup viewGroup = getView().findViewById(R.id.duplicate_popup_layout);
            ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            viewGroup.setLayoutParams(layoutParams);
        }

        mDuplicateTextView.setText(getString((mPageCount == 1 ? R.string.duplicate_page_singular : R.string.duplicate_page_plural), mPageCount));
        mInsertPageAboveTextView.setVisibility(mPageCount == 1 ? View.VISIBLE : View.GONE);
        mInsertPageBelowTextView.setVisibility(mPageCount == 1 ? View.VISIBLE : View.GONE);
        mDivider.setVisibility(mPageCount == 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isMobile() && atView != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            int[] location = new int[2];
            atView.getLocationOnScreen(location);
            layoutParams.y = Math.abs(location[1] - (atView.getHeight() + ScreenUtil.convertDpToPx(getContext(), mPageCount == 1 ? 216 : 108)));
            window.setAttributes(layoutParams);
        }
    }

    @OnClick({R.id.insert_page_above_text_view, R.id.insert_page_below_text_view, R.id.thumbnail_duplicate_popup_duplicate, R.id.thumbnail_duplicate_popup_cancel})
    void onInsertOptionSelected(View view) {
        switch (view.getId()) {
            case R.id.insert_page_above_text_view:
                if (mListener != null)
                    mListener.onSelected(FTPageInsertPosition.PREVIOUS_TO_CURRENT);
                break;
            case R.id.thumbnail_duplicate_popup_duplicate:
                if (mListener != null) mListener.onSelected(FTPageInsertPosition.AT_CURRENT);
                break;
            case R.id.insert_page_below_text_view:
                if (mListener != null) mListener.onSelected(FTPageInsertPosition.NEXT_TO_CURRENT);
                break;
        }
        dismiss();
    }

    public interface PageInsertOptionListener {
        void onSelected(FTPageInsertPosition insertPosition);
    }
}