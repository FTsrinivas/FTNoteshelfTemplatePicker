package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.interfaces.TemplateCategorySelection;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

public class FTTemplateCategoriesAdapter extends
        RecyclerView.Adapter<FTTemplateCategoriesAdapter.CategoryViewHolder> {

    private int selectedItem;
    private TemplateCategorySelection templateCategorySelection;

    Context applicationContext;
    ArrayList<TemplatesInfoModel> templatesInfoModels;

    // Constuctor
    public FTTemplateCategoriesAdapter(Context applicationContext, ArrayList<TemplatesInfoModel> templatesInfoModels,
                                       TemplateCategorySelection templateCategorySelection) {

        this.applicationContext = applicationContext;
        this.templatesInfoModels       = templatesInfoModels;
        selectedItem            = FTApp.getPref().get(SystemPref.CATEGORY_SELECTED_POSITION, 0);
        this.templateCategorySelection = templateCategorySelection;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        // Here we inflate the corresponding layout of the child item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_category_items_new, viewGroup, false);
        return new CategoryViewHolder(view,viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder categoryViewHolder, final int position) {

        String categoryItem = templatesInfoModels.get(position).get_categoryName();
        Log.d("TemplatePicker==>","FluidTouch onBindViewHolder getCategory_name::-"+categoryItem.toUpperCase());
        categoryViewHolder.categoryItemTitle.setText(categoryItem.toUpperCase());
        int tabPosition = FTApp.getPref().get(SystemPref.CATEGORY_SELECTED_POSITION, 0);

        Log.d("TemplatePicker==>"," savePositionOfListView tabPosition::-"+tabPosition);
        if (tabPosition == position) {
            categoryViewHolder.indicatorView.setVisibility(View.VISIBLE);
        }

        if (selectedItem == position) {
            categoryViewHolder.categoryItemTitle.setTextColor(this.applicationContext.getResources().getColor(R.color.recylerview_selcted_item_clr));
            categoryViewHolder.categoryItemTitle.setAlpha(1f);
            categoryViewHolder.indicatorView.setVisibility(View.VISIBLE);
        } else {
            categoryViewHolder.categoryItemTitle.setTextColor(Color.parseColor("#000000"));
            categoryViewHolder.categoryItemTitle.setAlpha(0.6f);
            categoryViewHolder.indicatorView.setVisibility(View.GONE);
        }

        categoryViewHolder.categoryItemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int previousItem = selectedItem;
                selectedItem = position;
                notifyItemChanged(previousItem);
                notifyItemChanged(position);
                FTApp.getPref().save(SystemPref.CATEGORY_SELECTED_POSITION, position);

                templateCategorySelection.categorySelected(categoryViewHolder.categoryItemTitle.getText().toString(),
                        position,categoryViewHolder.categoryItemTitle.getY());
            }
        });
    }

    @Override
    public int getItemCount() {
        return templatesInfoModels.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryItemTitle;
        Context mContext;
        View indicatorView;
        CategoryViewHolder(View itemView, Context context) {
            super(itemView);
            this.mContext = context;
            categoryItemTitle = itemView.findViewById(R.id.templatecategoryItemID);
            indicatorView = itemView.findViewById(R.id.indicator);
        }
    }
}
