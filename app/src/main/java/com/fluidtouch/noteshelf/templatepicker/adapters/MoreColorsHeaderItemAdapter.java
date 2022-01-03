package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.templatepicker.FTTemplateMoreDetailsPopupNew;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTColorVariants;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

public class MoreColorsHeaderItemAdapter extends RecyclerView.Adapter<MoreColorsHeaderItemAdapter.ParentViewHolder> {

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private ArrayList<FTColorVariants>  ftColorVariantsArrayList;

    private FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup;
    FragmentActivity applicationContext;

    public MoreColorsHeaderItemAdapter(FragmentActivity applicationContext,
                                       ArrayList<FTColorVariants> ftColorVariantsArrayList,
                                       FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup) {
        this.applicationContext = applicationContext;
        this.ftColorVariantsArrayList = ftColorVariantsArrayList;
        this.ftTemplateBgLinesInfoPopup = ftTemplateBgLinesInfoPopup;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_color_section_header_lyt, viewGroup, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder parentViewHolder, int position) {

        Log.d("TemplatePickerV2","RemoveUnwanted MoreColorsHeaderItemAdapterNew onBindViewHolder");


        LinearLayoutManager layoutManager = new LinearLayoutManager(parentViewHolder.ChildRecyclerView.getContext(),
                    LinearLayoutManager.VERTICAL, false);
        parentViewHolder.ParentItemTitle.setText(ftColorVariantsArrayList.get(position).getTitle().toUpperCase());
        layoutManager.setInitialPrefetchItemCount(ftColorVariantsArrayList.size());

        MoreColorsChildItemAdapter childItemAdapter
                    = new MoreColorsChildItemAdapter(applicationContext,
                                ftColorVariantsArrayList.get(position).getmFTTemplateColors(),
                                ftTemplateBgLinesInfoPopup,this);
        parentViewHolder.ChildRecyclerView.setLayoutManager(layoutManager);
        parentViewHolder.ChildRecyclerView.setAdapter(childItemAdapter);
        parentViewHolder.ChildRecyclerView.setRecycledViewPool(viewPool);

    }

    @Override
    public int getItemCount() {
        return ftColorVariantsArrayList.size();
    }

    class ParentViewHolder extends RecyclerView.ViewHolder {

        private TextView ParentItemTitle;
        private RecyclerView ChildRecyclerView;

        ParentViewHolder(final View itemView) {
            super(itemView);
            ParentItemTitle   = itemView.findViewById(R.id.parent_item_title);
            ChildRecyclerView = itemView.findViewById(R.id.child_recyclerview);
        }
    }
}
