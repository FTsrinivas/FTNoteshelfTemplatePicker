package com.fluidtouch.noteshelf.document.thumbnailview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf2.R;

public class FTBookmarkItemAdapter extends BaseRecyclerAdapter<FTNoteshelfPage, FTBookmarkItemViewHolder> implements FTBookmarkItemViewHolder.BookmarkedPageClickListener {
    private FinderAdapterListener mListener;

    public FTBookmarkItemAdapter(FinderAdapterListener bookmarkListener) {
        this.mListener = bookmarkListener;
    }

    @NonNull
    @Override
    public FTBookmarkItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new FTBookmarkItemViewHolder(super.getView(viewGroup, R.layout.item_bookmark_recycler_view), this);
    }

    @Override
    public void onBindViewHolder(@NonNull FTBookmarkItemViewHolder viewHolder, int position) {
        FTNoteshelfPage page = getItem(position);
        if (page.bookmarkColor.isEmpty() || page.bookmarkColor.equals("8ACCEA")) {
            page.bookmarkColor = "bookmark_blue";
            viewHolder.mBookmarkIcon.setImageResource(R.drawable.bookmark_blue);
            page.setPageDirty(true);
        } else {
            viewHolder.mBookmarkIcon.setImageResource(viewHolder.itemView.getResources().getIdentifier(page.bookmarkColor, "drawable", viewHolder.itemView.getContext().getPackageName()));
        }
        if (page.bookmarkTitle.isEmpty()) {
            viewHolder.mBookmarkTitle.setText(R.string.untitled);
        } else {
            viewHolder.mBookmarkTitle.setText(page.bookmarkTitle);
        }
        if (mListener.isEditMode() || mListener.isExportMode()) {
            viewHolder.mBookmarkCheckBadge.setVisibility(View.VISIBLE);
            if (mListener.selectedPages().contains(page)) {
                viewHolder.mBookmarkCheckBadge.setImageResource(R.drawable.check_badge);
            } else {
                viewHolder.mBookmarkCheckBadge.setImageResource(R.drawable.checkbadgeoff_dark);
            }
        } else {
            viewHolder.mBookmarkCheckBadge.setVisibility(View.GONE);
        }
        viewHolder.mBookmarkedPageNumber.setText(String.valueOf(page.pageIndex() + 1));
    }

    @Override
    public void onBookmarkItemSingleClick(int position) {
        if (mListener.isEditMode() || mListener.isExportMode()) {
            FTNoteshelfPage selectedPage = getItem(position);
            if (mListener.selectedPages().contains(selectedPage)) {
                mListener.selectedPages().remove(getItem(position));
            } else {
                mListener.selectedPages().add(getItem(position));
            }
            notifyItemChanged(position);
            mListener.onSelectUpdateUI();
        } else {
            mListener.displayThumbnailAsPage(getItem(position).pageIndex());
        }
    }

    @Override
    public void onBookmarkItemLongClick(FTBookmarkItemViewHolder viewHolder) {
        mListener.showBookmarkDialog(viewHolder.itemView, getItem(viewHolder.getAbsoluteAdapterPosition()));
    }
}