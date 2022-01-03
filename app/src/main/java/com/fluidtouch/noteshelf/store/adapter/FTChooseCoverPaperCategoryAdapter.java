package com.fluidtouch.noteshelf.store.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.store.data.FTDownloadedStorePackData;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTChooseCoverPaperCategoryAdapter extends BaseRecyclerAdapter<FTNTheme, FTChooseCoverPaperCategoryAdapter.ViewHolder> {

    private ChooseCoverPaperCategoryAdapterCallback mListener;
    private int selectedPos = 0;

    public FTChooseCoverPaperCategoryAdapter(ChooseCoverPaperCategoryAdapterCallback listener) {
        this.mListener = listener;
    }

    public void setSelectedPos(int pos) {
        selectedPos = pos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(getView(viewGroup, R.layout.item_choose_cover_paper_title));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FTNTheme themeCategory = getItem(position);
        holder.categoryTextView.setText(themeCategory.categoryName);
        if (!themeCategory.categoryName.equals(holder.itemView.getContext().getString(R.string.free_downloads))) {
            if (selectedPos == position) {
                holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.ns_blue, null));
                holder.categoryTextView.setTextColor(Color.WHITE);
                FTDownloadedStorePackData.getInstance(holder.itemView.getContext()).setData(themeCategory.categoryName, "true");
            } else {
                if (themeCategory.isDownloadTheme && !FTDownloadedStorePackData.getInstance(holder.itemView.getContext()).getData(themeCategory.categoryName, "").equals("true")) {
                    holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.ns_blue, null));
                } else {
                    holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.ns_text, null));
                }
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    public void selectTheme(@NonNull FTNTheme selectedCategory) {
        List<FTNTheme> themes = getAll();
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).categoryName.equals(selectedCategory.categoryName)) {
                selectedPos = i;
                break;
            }
        }
        notifyItemChanged(selectedPos);
    }

    public interface ChooseCoverPaperCategoryAdapterCallback {
        void onTemplateCategorySelected(FTNTheme themeCategory);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_choose_cover_paper_category_text_view)
        TextView categoryTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.choose_cover_paper_category_layout)
        void onCategorySelected() {
            int position = getBindingAdapterPosition();
            FTNTheme category = getItem(position);
            if (getItem(position).categoryName.equals(itemView.getContext().getString(R.string.free_downloads))) {
                FTStoreActivity.start(itemView.getContext());
            } else {
                FTFirebaseAnalytics.logEvent((category.ftThemeType == FTNThemeCategory.FTThemeType.COVER ? "ChooseCover_" : "ChoosePaper_") + category.diplayName);
                notifyItemChanged(selectedPos);
                selectedPos = position;
                notifyItemChanged(selectedPos);
                mListener.onTemplateCategorySelected(getItem(position));
            }
            FTDownloadedStorePackData.getInstance(itemView.getContext()).setData(category.categoryName, "true");
        }
    }
}