package com.fluidtouch.noteshelf.shelf.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTCategoryMoveToAdapter extends BaseRecyclerAdapter<FTShelfItemCollection, FTCategoryMoveToAdapter.CategoryMoveToViewHolder> {
    private FTCategoryMoveToAdapterCallback listener;
    private FTShelfItemCollection currentCollection;
    private boolean isEvernoteView;

    public FTCategoryMoveToAdapter(FTCategoryMoveToAdapterCallback listener, FTShelfItemCollection movingDocumentCategory, boolean isEvernoteView) {
        this.listener = listener;
        this.currentCollection = movingDocumentCategory;
        this.isEvernoteView = isEvernoteView;
    }

    @NonNull
    @Override
    public CategoryMoveToViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = super.getView(viewGroup, R.layout.item_category_recycler_view);
        return new CategoryMoveToViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryMoveToViewHolder holder, int position) {
        FTShelfItemCollection ftShelfItemCollection = getItem(position);
        if (ftShelfItemCollection.isTrash(holder.itemView.getContext())) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            if (isSameCategory(ftShelfItemCollection) && !isEvernoteView) {
                holder.categoryNameTextView.setText(new StringBuilder().append("▸ ").append(ftShelfItemCollection.getDisplayTitle(holder.itemView.getContext())));
            } else {
                holder.categoryNameTextView.setText(ftShelfItemCollection.getDisplayTitle(holder.itemView.getContext()));
            }
        }
    }

    private boolean isSameCategory(FTShelfItemCollection collection) {
        return (currentCollection != null && collection.getFileURL().equals(currentCollection.getFileURL()));
    }

    public interface FTCategoryMoveToAdapterCallback {
        void showInCategoryPanel(FTShelfItemCollection ftShelfItemCollection);
    }

    class CategoryMoveToViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_name)
        TextView categoryNameTextView;
        @BindView(R.id.item_category_layout)
        LinearLayout parentLayout;

        CategoryMoveToViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (isEvernoteView) parentLayout.setBackgroundResource(R.color.ns_dialog_bg);
        }

        @OnClick(R.id.item_category_layout)
        void onItemClicked() {
            FTLog.crashlyticsLog("UI: Selected a category and navigated into shelf panel");
            listener.showInCategoryPanel(getItem(getAdapterPosition()));
        }
    }
}