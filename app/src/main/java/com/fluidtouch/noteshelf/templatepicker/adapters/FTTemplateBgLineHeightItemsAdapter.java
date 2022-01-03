package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMoreDetailsPopupNew;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class FTTemplateBgLineHeightItemsAdapter extends
        RecyclerView.Adapter<FTTemplateBgLineHeightItemsAdapter.LineHeightViewHolder> {

    private RecyclerView.SmoothScroller smoothScroller;
    private int selectedItem;

    FragmentActivity applicationContext;
    FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup;

    ArrayList<FTLineTypes> ftLineTypesArrayList;
    boolean typeOfLineSelected;
    FTTemplateUtil ftTemplateUtil = FTTemplateUtil.getInstance();
    private int lastSelectedPosition = -1;

    public FTTemplateBgLineHeightItemsAdapter(FragmentActivity applicationContext,
                                              ArrayList<FTLineTypes> ftLineTypesArrayList,
                                              FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup) {
        this.applicationContext = applicationContext;
        this.ftLineTypesArrayList = ftLineTypesArrayList;
        this.ftTemplateBgLinesInfoPopup = ftTemplateBgLinesInfoPopup;

        selectedItem = FTApp.getPref().get(SystemPref.TEMPLATE_LINE_TYPE_POSITION, 2);;
        smoothScroller = new LinearSmoothScroller(applicationContext) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
    }

    @NonNull
    @Override
    public LineHeightViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Here we inflate the corresponding layout of the child item
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.templates_lineheight_popup_new, viewGroup, false);
        return new LineHeightViewHolder(viewGroup.getContext(),view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LineHeightViewHolder categoryViewHolder, final int position) {

        String categoryItem = ftLineTypesArrayList.get(position).getLineType();

        if (position == 0) {
            categoryViewHolder.lineHeightImageView.setBackgroundResource(R.mipmap.extra_small_lineheight);
        } else if (position == 1) {
            categoryViewHolder.lineHeightImageView.setBackgroundResource(R.mipmap.small_lineheight);
        } else if (position == 2) {
            categoryViewHolder.lineHeightImageView.setBackgroundResource(R.mipmap.default_lineheight);
        } else {
            categoryViewHolder.lineHeightImageView.setBackgroundResource(R.mipmap.wide_lineheight);
        }

        if (selectedItem == position) {
            categoryViewHolder.linearLayout.setBackground(categoryViewHolder.mContext.getDrawable(R.drawable.line_height_selection_bg));
        } else {
            categoryViewHolder.linearLayout.setBackground(categoryViewHolder.mContext.getDrawable(R.drawable.line_height_unselection_bg));
        }

        Log.d("TemplatePicker==>","Line Selected Adapter selectedItem::-"+selectedItem+
                " lastSelectedPosition status::-"+lastSelectedPosition+" position::-"+position);

    }

    @Override
    public int getItemCount() {
        return ftLineTypesArrayList.size();
    }

    class LineHeightViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View lineHeightImageView;
        Context mContext;
        LinearLayout linearLayout;

        LineHeightViewHolder(Context mContext, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = mContext;
            lineHeightImageView = itemView.findViewById(R.id.lineHeightIV);
            lineHeightImageView.setOnClickListener(this);
            linearLayout = itemView.findViewById(R.id.linearLayout);

            linearLayout.setOnClickListener(this);
            lineHeightImageView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.lineHeightIV:
                case R.id.linearLayout:
                    lastSelectedPosition = getAdapterPosition();
                    FTLineTypes clickedDataItem = ftLineTypesArrayList.get(lastSelectedPosition);

                    FTSelectedDeviceInfo selectedDeviceInfo   = FTSelectedDeviceInfo.selectedDeviceInfo();
                    FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

                    ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                    ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                    ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());

                    ftSelectedDeviceInfo.setThemeBgClrName(selectedDeviceInfo.getThemeBgClrName());
                    ftSelectedDeviceInfo.setThemeBgClrHexCode(selectedDeviceInfo.getThemeBgClrHexCode());
                    ftSelectedDeviceInfo.setHorizontalLineClr(selectedDeviceInfo.getHorizontalLineClr());
                    ftSelectedDeviceInfo.setVerticalLineClr(selectedDeviceInfo.getVerticalLineClr());
                    ftSelectedDeviceInfo.setVerticalLineSpacing(clickedDataItem.getVerticalLineSpacing());
                    ftSelectedDeviceInfo.setHorizontalLineSpacing(clickedDataItem.getHorizontalLineSpacing());

                    ftSelectedDeviceInfo.setThemeMoreBgClrName(selectedDeviceInfo.getThemeBgClrName());
                    ftSelectedDeviceInfo.setThemeMoreBgClrHexCode(selectedDeviceInfo.getThemeBgClrHexCode());
                    ftSelectedDeviceInfo.setHorizontalMoreLineClr(selectedDeviceInfo.getHorizontalLineClr());
                    ftSelectedDeviceInfo.setVerticaMorelLineClr(selectedDeviceInfo.getVerticalLineClr());

                    ftSelectedDeviceInfo.setLineType(clickedDataItem.getLineType());

                    ftSelectedDeviceInfo.selectSavedDeviceInfo();

                    FTApp.getPref().save(SystemPref.TEMPLATE_LINE_TYPE_POSITION, lastSelectedPosition);
                    FTApp.getPref().save(SystemPref.TEMPLATE_LINE_TYPE_SELECTED, true);

                    int previousItem    = selectedItem;
                    selectedItem        = lastSelectedPosition;

                    notifyItemChanged(previousItem);
                    notifyItemChanged(lastSelectedPosition);

                    FTSelectedDeviceInfo selectedDeviceInfoDummy   = FTSelectedDeviceInfo.selectedDeviceInfo();

                    Log.d("::TemplatePickerV2","ColourVariants FTTemplateBgLineHeightItemsAdapter ColourVariants and Line Types getThemeBgClrName "
                            +selectedDeviceInfoDummy.getThemeBgClrName()+" getThemeMoreBgClrName:: "
                            +selectedDeviceInfoDummy.getThemeMoreBgClrName()+" getLineType:: "
                            +selectedDeviceInfoDummy.getLineType());

                    ftTemplateBgLinesInfoPopup.typeOfLineSelected();
                    break;
            }
        }
    }
}
