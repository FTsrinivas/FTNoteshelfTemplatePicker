package com.fluidtouch.noteshelf.document.thumbnailview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTBookmarkItemViewHolder extends RecyclerView.ViewHolder {
    private final BookmarkedPageClickListener listener;

    @BindView(R.id.bookmark_item_check_badge)
    ImageView mBookmarkCheckBadge;
    @BindView(R.id.bookmark_icon)
    ImageView mBookmarkIcon;
    @BindView(R.id.bookmark_title_text_view)
    TextView mBookmarkTitle;
    @BindView(R.id.bookmarked_page_number_text_view)
    TextView mBookmarkedPageNumber;

    public FTBookmarkItemViewHolder(@NonNull View itemView, BookmarkedPageClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.listener = listener;
    }

    @OnClick(R.id.bookmark_item_layout)
    void onBookmarkItemSingleClick() {
        listener.onBookmarkItemSingleClick(getAbsoluteAdapterPosition());
    }

    @OnLongClick(R.id.bookmark_item_layout)
    void onBookmarkItemLongClick() {
        listener.onBookmarkItemLongClick(this);
    }

    public interface BookmarkedPageClickListener {
        void onBookmarkItemSingleClick(int position);

        void onBookmarkItemLongClick(FTBookmarkItemViewHolder viewHolder);
    }
}
