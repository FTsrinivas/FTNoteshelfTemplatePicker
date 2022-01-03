package com.fluidtouch.noteshelf.document.thumbnailview;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fluidtouch.noteshelf.document.search.FTTextHighlightView;
import com.fluidtouch.noteshelf2.R;
import com.thesurix.gesturerecycler.GestureViewHolder;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTThumbnailItemViewHolder extends GestureViewHolder {
    @BindView(R.id.thumbnail_select_frame)
    FrameLayout mThumbnailSelectFrame;
    @BindView(R.id.thumbnail_image_view)
    ImageView mThumbnailImageView;
    @BindView(R.id.thumbnail_highlight_view)
    FTTextHighlightView mTextHighlightView;
    @BindView(R.id.thumbnail_bookmark_button)
    ImageButton mBookmarkButton;
    @BindView(R.id.thumbnail_page_number_text_view)
    TextView mPageNumberTextView;

    String pageUUID;
    private Listener mListener;

    public FTThumbnailItemViewHolder(@NotNull View itemView, Listener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mListener = listener;
    }

    @OnClick(R.id.thumbnail_card_view)
    void onThumbnailClicked() {
        mListener.onThumbnailClicked(getBindingAdapterPosition());
    }

    @OnClick(R.id.thumbnail_bookmark_button)
    void onBookmarkSingleClicked() {
        mListener.onBookmarkSingleClicked(this);
    }

    @OnLongClick(R.id.thumbnail_bookmark_button)
    void onBookmarkLongClicked() {
        mListener.onBookmarkLongClicked(mBookmarkButton, this);
    }

    @Override
    public boolean canDrag() {
        return !mListener.isExportMode();
    }

    @Override
    public boolean canSwipe() {
        return false;
    }

    interface Listener {
        void onThumbnailClicked(int position);

        void onBookmarkSingleClicked(FTThumbnailItemViewHolder viewHolder);

        void onBookmarkLongClicked(View atView, FTThumbnailItemViewHolder viewHolder);

        boolean isExportMode();
    }
}
