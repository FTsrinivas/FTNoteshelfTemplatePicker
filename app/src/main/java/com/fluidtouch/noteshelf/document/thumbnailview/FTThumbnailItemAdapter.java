package com.fluidtouch.noteshelf.document.thumbnailview;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.document.search.FTTextHighlightView;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.thesurix.gesturerecycler.GestureAdapter;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class FTThumbnailItemAdapter extends GestureAdapter<FTNoteshelfPage, FTThumbnailItemViewHolder> implements FTThumbnailItemViewHolder.Listener, GestureAdapter.OnDataChangeListener<FTNoteshelfPage> {
    private FinderAdapterListener mListener;
    private RecyclerView thumbnailRecyclerView;
    private Bitmap bitmapStub;
    private RectF pdfPageRect = new RectF();

    FTThumbnailItemAdapter(FinderAdapterListener thumbnailListener) {
        this.mListener = thumbnailListener;
    }

    @NonNull
    @Override
    public FTThumbnailItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        setDataChangeListener(this);
        this.thumbnailRecyclerView = (RecyclerView) viewGroup;
        return new FTThumbnailItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_thumbnail_recycler_view, viewGroup, false), this);
    }

    @Override
    public void onBindViewHolder(@NonNull FTThumbnailItemViewHolder viewHolder, int position) {
        FTNoteshelfPage page = getItem(position);
        viewHolder.pageUUID = page.uuid;
        viewHolder.mPageNumberTextView.setText(String.valueOf(page.pageIndex() + 1));
        if (!pdfPageRect.equals(page.getPageRect())) {
            bitmapStub = BitmapUtil.getTempThumbnailBitmap(viewHolder.itemView.getContext(), page.getPdfPageRect());
            pdfPageRect = page.getPdfPageRect();
        }
        viewHolder.mThumbnailImageView.setImageBitmap(bitmapStub);
        setItemAtPosition(viewHolder, page);

        if (mListener.isEditMode()) {
            int padding = (int) viewHolder.itemView.getContext().getResources().getDimension(R.dimen.new_5dp);
            viewHolder.mThumbnailSelectFrame.setPadding(padding, padding, padding, padding);
            if (mListener.selectedPages().contains(page)) {
                viewHolder.mThumbnailSelectFrame.setBackgroundResource(R.drawable.thumbnail_select_bg);
            } else {
                viewHolder.mThumbnailSelectFrame.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            int padding = (int) viewHolder.itemView.getContext().getResources().getDimension(R.dimen.new_2dp);
            viewHolder.mThumbnailSelectFrame.setPadding(padding, padding, padding, padding);
            if (mListener.currentPageIndex() - 1 == getItem(position).pageIndex()) {
                viewHolder.mThumbnailSelectFrame.setBackgroundResource(R.drawable.thumbnail_select_bg);
            } else {
                viewHolder.mThumbnailSelectFrame.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        setBookmarkIcon(page.isBookmarked, viewHolder);
    }

    @Override
    public void onViewRecycled(@NonNull FTThumbnailItemViewHolder holder) {
        super.onViewRecycled(holder);
        int position = holder.getAbsoluteAdapterPosition();
        if (position >= 0) {
            getItem(position).thumbnail().removeThumbnail();
        }
        holder.mThumbnailImageView.setImageBitmap(null);
    }

    private void setItemAtPosition(final FTThumbnailItemViewHolder viewHolder, FTNoteshelfPage page) {
        if (viewHolder != null) {
            FTLog.debug(FTLog.FINDER_OPERATIONS, "Bitmap request at index" + viewHolder.getAbsoluteAdapterPosition());
            ObservingService.getInstance().addObserver(FTPageThumbnail.strObserver + page.uuid, new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    if (arg instanceof FTPageThumbnail.FTThumbnail) {
                        FTPageThumbnail.FTThumbnail ftThumbnail = (FTPageThumbnail.FTThumbnail) arg;
                        if (viewHolder.pageUUID.equals(ftThumbnail.getPageUUID()) && ftThumbnail.getThumbImage() != null && !ftThumbnail.getThumbImage().isRecycled()) {
                            FTLog.debug(FTLog.FINDER_OPERATIONS, "Generated bitmap at index" + viewHolder.getAbsoluteAdapterPosition());
                            viewHolder.mThumbnailImageView.post(() -> {
                                viewHolder.mThumbnailImageView.setImageBitmap(ftThumbnail.getThumbImage());
                                addTextHighLighters(viewHolder, page);
                            });
                            if (!page.thumbnail().shouldGenerateThumbnail())
                                ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + ftThumbnail.getPageUUID(), this);
                        }
                    }
                }
            });
            page.thumbnail().thumbnailImage(viewHolder.itemView.getContext());
        }
    }

    private void addTextHighLighters(FTThumbnailItemViewHolder viewHolder, FTNoteshelfPage page) {
        viewHolder.mTextHighlightView.removeAllViews();
        ArrayList<FTSearchableItem> searchableItems = page.getSearchableItems();
        if (!searchableItems.isEmpty()) {
            SizeF thSize = new SizeF(viewHolder.mTextHighlightView.getWidth(), viewHolder.mTextHighlightView.getHeight());
            if (thSize.getWidth() > 0) {
                SizeF aspectSize = FTGeometryUtils.aspectSize(thSize, new SizeF(page.getPageRect().width(), page.getPageRect().height()));
                float scaleFactor = thSize.getWidth() / aspectSize.getWidth();
                float width = page.getPageRect().width() * scaleFactor;
                float height = page.getPageRect().height() * scaleFactor;
                FTTextHighlightView.ParentRectInfo parentRectInfo = new FTTextHighlightView.ParentRectInfo();
                parentRectInfo.scaleFactor = scaleFactor;
                for (FTSearchableItem searchableItem : searchableItems) {
                    parentRectInfo.width = width;
                    parentRectInfo.height = height;
                    parentRectInfo.rotation = searchableItem.getRotation();
                    viewHolder.mTextHighlightView.addChildView(searchableItem.getBoundingRect(), parentRectInfo);
                }
            }
        }
    }

    void onScrollingStops(int firstVisiblePosition, int lastVisiblePosition) {
        while (firstVisiblePosition <= lastVisiblePosition && getItemCount() > 0 && firstVisiblePosition >= 0 && firstVisiblePosition < getItemCount()) {
            setItemAtPosition((FTThumbnailItemViewHolder) thumbnailRecyclerView.findViewHolderForAdapterPosition(firstVisiblePosition), getItem(firstVisiblePosition));
            ++firstVisiblePosition;
        }
    }

    @Override
    public void onThumbnailClicked(int position) {
        if (mListener.isEditMode() || mListener.isExportMode()) {
            FTFirebaseAnalytics.logEvent("Finder_EditPage_SelectPage");
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
    public void onBookmarkSingleClicked(FTThumbnailItemViewHolder viewHolder) {
        if (mListener.isEditMode() && mListener.isShowingBookmarks())
            return;
        int position = viewHolder.getAbsoluteAdapterPosition();
        if (getItem(position).isBookmarked) {
            setBookmarkIcon(false, viewHolder);
            getItem(position).bookmarkColor = "";
            getItem(position).bookmarkTitle = "";
            getItem(position).isBookmarked = false;
            getItem(position).setPageDirty(true);
            if (mListener.isShowingBookmarks()) {
                mListener.checkPageCountAndUpdate();
            }
            if (getItemCount() == 0) {
                mListener.noBookmarkedPages();
            }
        } else {
            FTFirebaseAnalytics.logEvent("Finder_TapBookmark");
            setBookmarkIcon(true, viewHolder);
            getItem(position).isBookmarked = true;
            getItem(position).setPageDirty(true);
        }
    }

    @Override
    public void onBookmarkLongClicked(View atView, FTThumbnailItemViewHolder viewHolder) {
        FTFirebaseAnalytics.logEvent("Finder_LongPressBookmark");
        mListener.showBookmarkDialog(atView, getItem(viewHolder.getAbsoluteAdapterPosition()));
    }

    @Override
    public boolean isExportMode() {
        return mListener.isExportMode();
    }

    @Override
    public void onItemRemoved(FTNoteshelfPage page, int i) {
        //Not using for swiping
    }

    @Override
    public void onItemReorder(FTNoteshelfPage page, int fromPosition, int toPosition) {
        FTFirebaseAnalytics.logEvent("Finder_DragToRearrange");
        mListener.swapPages(fromPosition, toPosition);
    }

    private void setBookmarkIcon(boolean isBookmarked, FTThumbnailItemViewHolder viewHolder) {
        int position = viewHolder.getAbsoluteAdapterPosition();
        if (isBookmarked) {
            if (getItem(position).bookmarkColor.equals("8ACCEA") || getItem(position).bookmarkColor.isEmpty()) {
                getItem(position).bookmarkColor = "bookmark_blue";
                viewHolder.mBookmarkButton.setImageResource(R.drawable.bookmark_blue);
            } else {
                viewHolder.mBookmarkButton.setImageResource(viewHolder.itemView.getContext().getResources().getIdentifier(getItem(position).bookmarkColor, "drawable", viewHolder.itemView.getContext().getPackageName()));
            }
        } else {
            viewHolder.mBookmarkButton.setImageResource(R.drawable.bookmark_off);
        }
    }
}