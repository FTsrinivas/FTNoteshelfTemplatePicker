package com.fluidtouch.noteshelf.document.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTBookmarkFragment extends Fragment {
    @BindView(R.id.bookmark_edit_layout)
    LinearLayout mEditLayout;
    @BindView(R.id.bookmark_info_text_view)
    TextView mInfoTextView;
    @BindView(R.id.bookmark_title_edit_text)
    EditText mTitleEditText;
    @BindView(R.id.bookmark_switch)
    SwitchMaterial mBookmarkSwitch;
    @BindView(R.id.bookmark_thumbnail_image_view)
    CardView mThumbnailImage;
    @BindView(R.id.bookmark_page_preview_layout)
    RelativeLayout mPagePreviewLayout;
    @BindView(R.id.dialog_back_button)
    ImageView mBackButton;

    private ImageView mSelectedImageView;
    private FTNoteshelfPage mCurrentPage;

    public FTBookmarkFragment() {

    }

    public FTBookmarkFragment(FTNoteshelfPage currentPage) {
        mCurrentPage = currentPage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        if (getActivity() != null) {
            if (mCurrentPage == null)
                mCurrentPage = ((FTDocumentActivity) getActivity()).getCurrentPage();
            if (mCurrentPage.isBookmarked) {
                ImageView imageView = view.findViewWithTag(mCurrentPage.bookmarkColor);
                if (imageView != null) {
                    imageView.setImageResource(R.drawable.bookmark_selected_circle);
                    mSelectedImageView = imageView;
                    FTApp.getPref().save(SystemPref.LAST_SELECTED_BOOKMARK_COLOR, imageView.getTag().toString());
                }
                mTitleEditText.setText(mCurrentPage.bookmarkTitle.isEmpty() ? FTApp.getPref().get(SystemPref.LAST_BOOKMARK_TITLE_USED, "") : mCurrentPage.bookmarkTitle);
                mEditLayout.setVisibility(View.VISIBLE);
                mInfoTextView.setVisibility(View.GONE);
                mPagePreviewLayout.setVisibility(View.GONE);
            } else {
                mEditLayout.setVisibility(View.GONE);
                mInfoTextView.setVisibility(View.VISIBLE);
                mPagePreviewLayout.setVisibility(View.VISIBLE);
            }
            mBookmarkSwitch.setChecked(mCurrentPage.isBookmarked);
        }
        mTitleEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (mCurrentPage != null) {
                    mCurrentPage.bookmarkTitle = mTitleEditText.getText().toString();
                    FTApp.getPref().save(SystemPref.LAST_BOOKMARK_TITLE_USED, mCurrentPage.bookmarkTitle);
                    mCurrentPage.setPageDirty(true);
                }
                InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        view.setOnClickListener(v -> {
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentPage == null && getActivity() != null) {
            mCurrentPage = ((FTDocumentActivity) getActivity()).getCurrentPage();
        }
    }

    @Override
    public void onDestroy() {
        if (mCurrentPage != null) {
            mCurrentPage.bookmarkTitle = mTitleEditText.getText().toString();
            mCurrentPage.setPageDirty(true);
        }
        super.onDestroy();
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        if ((getParentFragment() instanceof FTBookmarkDialog)) {
            ((FTBookmarkDialog) getParentFragment()).dismiss();
        } else
            FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), false, () -> {
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().remove(FTBookmarkFragment.this).commit();
                }
            });
    }

    @OnClick({R.id.bookmark_blue_color, R.id.bookmark_purple_color, R.id.bookmark_red_color,
            R.id.bookmark_orange_color, R.id.bookmark_yellow_color, R.id.bookmark_green_color,
            R.id.bookmark_dark_grey_color, R.id.bookmark_light_grey_color})
    void onColorSelected(ImageView view) {
        FTFirebaseAnalytics.logEvent("NB_AddNew_Bookmark_ColorCode");
        if (mSelectedImageView != null) {
            mSelectedImageView.setImageResource(0);
        }
        view.setImageResource(R.drawable.bookmark_selected_circle);

        mCurrentPage.isBookmarked = true;
        mCurrentPage.bookmarkColor = view.getTag().toString();
        mCurrentPage.setPageDirty(true);

        FTApp.getPref().save(SystemPref.LAST_SELECTED_BOOKMARK_COLOR, view.getTag().toString());

        mSelectedImageView = view;

        ObservingService.getInstance().postNotification("page_bookmark", mCurrentPage);
    }

    @OnCheckedChanged(R.id.bookmark_switch)
    void onBookmarkEnabled(CompoundButton buttonView, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("NB_AddNew_Bookmark");

        mEditLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mInfoTextView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        mEditLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mPagePreviewLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);

        mThumbnailImage.setCardBackgroundColor(getResources().getColor(isChecked ? R.color.selected_item_bg : R.color.bookmark_thumbnail_bg, null));
        String tag = "bookmark_blue";
        if (mCurrentPage.isBookmarked) {
            tag = FTApp.getPref().get(SystemPref.LAST_SELECTED_BOOKMARK_COLOR, "bookmark_blue");
        }
        mSelectedImageView = getView().findViewWithTag(tag);
        if (mSelectedImageView != null) {
            mSelectedImageView.setImageResource(R.drawable.bookmark_selected_circle);
        }
        mTitleEditText.setText(mCurrentPage.bookmarkTitle.isEmpty() ? FTApp.getPref().get(SystemPref.LAST_BOOKMARK_TITLE_USED, "") : mCurrentPage.bookmarkTitle);
        mCurrentPage.isBookmarked = isChecked;
        mCurrentPage.setPageDirty(true);

        ObservingService.getInstance().postNotification("page_bookmark", mCurrentPage);
    }
}