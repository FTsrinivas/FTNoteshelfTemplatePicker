package com.fluidtouch.noteshelf.globalsearch.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SizeF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.document.search.FTTextHighlightView;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchImageView;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultBook;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultPage;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSection;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSectionContent;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSectionTitle;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 2019-07-23
 */

public class FTGlobalSearchAdapter extends BaseRecyclerAdapter<FTSearchSection, RecyclerView.ViewHolder> {
    public static final int CONTENT = 0;
    public static final int TITLE = 1;

    private GlobalSearchAdapterCallback mListener;
    private RecyclerView mRecyclerView;
    private Bitmap bitmapStub;
    private RectF pdfPageRect = new RectF();

    public FTGlobalSearchAdapter(GlobalSearchAdapterCallback listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mRecyclerView = (RecyclerView) parent;
        if (viewType == CONTENT) {
            return new GlobalSearchContentViewHolder(getView(parent, R.layout.item_content_global_search_recycler_view));
        } else {
            return new GlobalSearchTitleViewHolder(getView(parent, R.layout.item_header_global_search_recycler_view));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == CONTENT) {
            configureContent((GlobalSearchContentViewHolder) holder, position);
        } else {
            configureTitle((GlobalSearchTitleViewHolder) holder, position);
        }
    }

    private void configureContent(GlobalSearchContentViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        FTSearchSection searchItem = getItem(position);
        if (searchItem instanceof FTSearchResultPage) {
            holder.notebookTileTextView.setVisibility(View.GONE);
            holder.highlighterLayout.setVisibility(View.VISIBLE);
            FTSearchSectionContent content = (FTSearchSectionContent) searchItem;
            FTSearchResultPage pageContent = (FTSearchResultPage) content;
            holder.thumbnailUUID = pageContent.getUuid();
            if (pageContent.getNoteshelfPage().thumbnail().getThumbImage() != null) {
                holder.contentImageView.setImageBitmap(pageContent.getNoteshelfPage().thumbnail().getThumbImage());
                addTextHighLighters(holder, ((FTSearchResultPage) getItem(position)));
            } else {
                if (!pdfPageRect.equals(pageContent.getPdfPageRect())) {
                    bitmapStub = BitmapUtil.getTempThumbnailBitmap(context, pageContent.getPdfPageRect());
                    pdfPageRect = pageContent.getPdfPageRect();
                }
                holder.contentImageView.setImageBitmap(bitmapStub);
            }
            holder.contentImageView.setPage(pageContent.getNoteshelfPage());
            holder.contentImageView.isContent = true;
        } else {
            holder.notebookTileTextView.setVisibility(View.VISIBLE);
            holder.highlighterLayout.setVisibility(View.GONE);
            FTSearchSectionContent content = (FTSearchSectionContent) searchItem;
            FTSearchResultBook notebookContent = (FTSearchResultBook) content;
            holder.notebookTileTextView.setText(notebookContent.getTitle());
//            holder.contentBackgroundLayout.setBackgroundResource(0);
            holder.contentImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(notebookContent.getFileURL().getPath()), FTConstants.COVER_SHELF_IMAGE_NAME)));
            holder.contentImageView.isContent = false;
        }
    }

    private void configureTitle(GlobalSearchTitleViewHolder holder, int position) {
        holder.itemView.getLayoutParams().width = ScreenUtil.getScreenWidth(holder.itemView.getContext());
        FTSearchSection searchItem = getItem(position);
        FTSearchSectionTitle title = (FTSearchSectionTitle) searchItem;
        holder.titleTextView.setText(title.title);
        if (!TextUtils.isEmpty(title.type) && title.type.equals("NOTEBOOK")) {
            holder.countTextView.setText(holder.itemView.getContext().getString(R.string.set_content_count, title.categoryName, ((FTSearchSectionTitle) searchItem).itemCount) + " " + holder.itemView.getContext().getString(R.string.pages));
        } else {
            holder.countTextView.setText(holder.itemView.getContext().getString(R.string.set_titles_count, ((FTSearchSectionTitle) searchItem).itemCount));
        }
    }

    private void setItemAtPosition(GlobalSearchContentViewHolder holder, FTSearchResultPage page) {
        ObservingService.getInstance().addObserver(FTPageThumbnail.strObserver + page.getNoteshelfPage().uuid, new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof FTPageThumbnail.FTThumbnail) {
                    FTPageThumbnail.FTThumbnail ftThumbnail = (FTPageThumbnail.FTThumbnail) arg;
                    if (holder.thumbnailUUID.equals(ftThumbnail.getPageUUID()) && ftThumbnail.getThumbImage() != null) {
                        page.getNoteshelfPage().setIsinUse(true);
                        holder.contentImageView.setImageBitmap(ftThumbnail.getThumbImage());
                        addTextHighLighters(holder, page);
                    }
                    if (!page.getNoteshelfPage().thumbnail().shouldGenerateThumbnail())
                        ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + ftThumbnail.getPageUUID(), this);
                }
            }
        });
        page.getNoteshelfPage().thumbnail().thumbnailImage(holder.itemView.getContext());
    }

    private void addTextHighLighters(GlobalSearchContentViewHolder viewHolder, FTSearchResultPage page) {
        viewHolder.highlighterLayout.removeAllViews();
        ArrayList<FTSearchableItem> searchableItems = page.getSearchableItems();
        if (!searchableItems.isEmpty()) {
            SizeF thSize = new SizeF(viewHolder.highlighterLayout.getWidth(), viewHolder.highlighterLayout.getHeight());
            if (thSize.getWidth() > 0) {
                SizeF aspectSize = FTGeometryUtils.aspectSize(thSize, new SizeF(page.getPdfPageRect().width(), page.getPdfPageRect().height()));
                float scaleFactor = thSize.getWidth() / aspectSize.getWidth();
                float width = page.getPdfPageRect().width() * scaleFactor;
                float height = page.getPdfPageRect().height() * scaleFactor;
                FTTextHighlightView.ParentRectInfo parentRectInfo = new FTTextHighlightView.ParentRectInfo();
                parentRectInfo.scaleFactor = scaleFactor;
                for (FTSearchableItem searchableItem : searchableItems) {
                    parentRectInfo.width = width;
                    parentRectInfo.height = height;
                    parentRectInfo.rotation = searchableItem.getRotation();
                    viewHolder.highlighterLayout.addChildView(searchableItem.getBoundingRect(), parentRectInfo);
                }
            }
        }
    }

    public void onScrollingStops(int firstVisiblePosition, int lastVisiblePosition) {
        while (firstVisiblePosition <= lastVisiblePosition && getItemCount() > 0) {
            if (firstVisiblePosition >= 0 && getItem(firstVisiblePosition) instanceof FTSearchResultPage) {
                setItemAtPosition((GlobalSearchContentViewHolder) mRecyclerView.findViewHolderForAdapterPosition(firstVisiblePosition), ((FTSearchResultPage) getItem(firstVisiblePosition)));
            }
            firstVisiblePosition++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof FTSearchSectionContent) {
            return CONTENT;
        } else {
            return TITLE;
        }
    }

    public interface GlobalSearchAdapterCallback {
        void openSelectedDocument(Object item);
    }

    class GlobalSearchContentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.global_search_image_view)
        FTGlobalSearchImageView contentImageView;
        @BindView(R.id.global_search_text_highlight_view)
        FTTextHighlightView highlighterLayout;
        @BindView(R.id.global_search_notebook_title_text_view)
        TextView notebookTileTextView;
        @BindView(R.id.item_global_search_content_background_layout)
        RelativeLayout contentBackgroundLayout;

        String thumbnailUUID = "";

        GlobalSearchContentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_global_search_content_layout)
        void onContentClicked() {
            mListener.openSelectedDocument(getItem(getBindingAdapterPosition()));
        }
    }

    class GlobalSearchTitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_header_global_search_title_text_view)
        TextView titleTextView;
        @BindView(R.id.item_header_global_search_count_text_view)
        TextView countTextView;

        GlobalSearchTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}