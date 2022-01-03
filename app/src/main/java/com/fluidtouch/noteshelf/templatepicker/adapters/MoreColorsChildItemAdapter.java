package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMoreDetailsPopupNew;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreColorsChildItemAdapter extends RecyclerView.Adapter<MoreColorsChildItemAdapter.MoreColorsChildItemViewHolder> {

    private FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup;
    ArrayList<FTTemplateColors> ChildItemList;
    FragmentActivity applicationContext;
    private int lastSelectedPosition = -1;

    private String selectedClrHex;

    MoreColorsHeaderItemAdapter moreColorsHeaderItemAdapter;

    MoreColorsChildItemAdapter(FragmentActivity applicationContext, ArrayList<FTTemplateColors> childItemList,
                                  FTTemplateMoreDetailsPopupNew ftTemplateBgLinesInfoPopup,
                                  MoreColorsHeaderItemAdapter moreColorsHeaderItemAdapter) {
        this.applicationContext = applicationContext;
        this.ChildItemList = childItemList;
        this.ftTemplateBgLinesInfoPopup = ftTemplateBgLinesInfoPopup;
        this.moreColorsHeaderItemAdapter = moreColorsHeaderItemAdapter;
    }

    @NonNull
    @Override
    public MoreColorsChildItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.templates_line_height_item_info_popup, viewGroup, false);
        return new MoreColorsChildItemViewHolder(view,viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull MoreColorsChildItemViewHolder childViewHolder, int position) {

        selectedClrHex = FTApp.getPref().get(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX, "#F7F7F2-1.0");;
        String colorName = ChildItemList.get(position).getColorName();
        childViewHolder.ChildItemTitle.setText(colorName);

        GradientDrawable bgShape = (GradientDrawable) childViewHolder.templateClrIV.getBackground();
        bgShape.setColor(getColor(ChildItemList.get(position).getColorHex()));
        bgShape.setStroke(2,Color.parseColor("#33000000"));

        Log.d("TemplatePicker==>","onBindViewHolder selectedClrHex::-"+selectedClrHex+" getColorHex::-"+ChildItemList.get(position).getColorHex());
        if (selectedClrHex.equalsIgnoreCase(ChildItemList.get(position).getColorHex())) {
            childViewHolder.ivListItem.setImageDrawable(applicationContext.getDrawable(R.drawable.check_mark_bg));
        } else {
            childViewHolder.ivListItem.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return ChildItemList.size();
    }

    class MoreColorsChildItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.lineheightTitleTV)
        TextView ChildItemTitle;

        @BindView(R.id.templateClrIV)
        View templateClrIV;

        @BindView(R.id.lineheightLytID)
        RelativeLayout lineheightLytID;

        @BindView(R.id.ivListItem)
        ImageView ivListItem;

        Context mContext;
        MoreColorsChildItemViewHolder(View itemView,Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.mContext = context;
            ivListItem.setOnClickListener(this);
            lineheightLytID.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ivListItem:
                case R.id.lineheightLytID:
                    Log.d("TemplatePicker==>","onClick getAdapterPosition::-"+getAdapterPosition() +" getAbsoluteAdapterPosition::-"+getAbsoluteAdapterPosition()+ " getBindingAdapterPosition::-"+getBindingAdapterPosition());
                    if (getAdapterPosition() != -1) {
                        lastSelectedPosition = getAdapterPosition();
                        FTTemplateColors clickedDataItem = ChildItemList.get(lastSelectedPosition);
                        Toast.makeText(view.getContext(), "You clicked " + clickedDataItem.getColorName(), Toast.LENGTH_SHORT).show();

                        FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

                        ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                        ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                        ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                        ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());
                        ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
                        ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());

                        ftSelectedDeviceInfo.setThemeBgClrName(clickedDataItem.getColorName());
                        ftSelectedDeviceInfo.setThemeBgClrHexCode(clickedDataItem.getColorHex());
                        ftSelectedDeviceInfo.setHorizontalLineClr(clickedDataItem.getHorizontalLineColor());
                        ftSelectedDeviceInfo.setVerticalLineClr(clickedDataItem.getVerticalLineColor());

                        ftSelectedDeviceInfo.setThemeMoreBgClrName(clickedDataItem.getColorName());
                        ftSelectedDeviceInfo.setThemeMoreBgClrHexCode(clickedDataItem.getColorHex());
                        ftSelectedDeviceInfo.setHorizontalMoreLineClr(clickedDataItem.getHorizontalLineColor());
                        ftSelectedDeviceInfo.setVerticaMorelLineClr(clickedDataItem.getVerticalLineColor());

                        ftSelectedDeviceInfo.selectSavedDeviceInfo();

                        FTApp.getPref().save(SystemPref.TEMPLATE_COLOR_SELECTED, true);
                        FTApp.getPref().save(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view3");
                        FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_VIEW, ChildItemList.get(lastSelectedPosition).getColorHex());
                        FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, true);
                        ivListItem.setImageDrawable(applicationContext.getDrawable(R.drawable.check_mark_bg));
                        ftTemplateBgLinesInfoPopup.colorSelected();
                        moreColorsHeaderItemAdapter.notifyDataSetChanged();

                        /*
                        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_HEX, clickedDataItem.getColorHex());
                        FTApp.getPref().save(SystemPref.TEMPLATE_COLOUR_SELECTED_CLR_NAME, clickedDataItem.getColorName());
                        FTTemplateUtil ftTemplateUtil = FTTemplateUtil.getInstance();
                        if(ChildItemList.get(lastSelectedPosition).getColorHex().equalsIgnoreCase("#FFFFFF-1.0") ||
                                ChildItemList.get(lastSelectedPosition).getColorHex().equalsIgnoreCase("#F7F7F2-1.0")) {
                            ftTemplateUtil.fTTemplateMoreColorsSerializedObject(ChildItemList.get(lastSelectedPosition).getColorHex(),
                                    ChildItemList.get(lastSelectedPosition).getColorName(),"#000000-0.15", "#000000-0.15");
                        } else {
                            ftTemplateUtil.fTTemplateMoreColorsSerializedObject(ChildItemList.get(lastSelectedPosition).getColorHex(),
                                    ChildItemList.get(lastSelectedPosition).getColorName(),ChildItemList.get(lastSelectedPosition).getVerticalLineColor(),
                                    ChildItemList.get(lastSelectedPosition).getHorizontalLineColor());
                        }*/


                    }
                    break;
            }
        }

    }

    private int hexToDecimal(String hexaClr) {
        return Integer.parseInt(hexaClr, 16);
    }

    private int getColor(String colorName) {
        String hexcolorNameCnvrted = null;

        if (colorName.contains("#")) {
            hexcolorNameCnvrted = colorName.split("#")[1].split("-")[0];
        }

        int bgcolorRed = hexToDecimal(hexcolorNameCnvrted.substring(0, 2));
        int bgcolorGreen = hexToDecimal(hexcolorNameCnvrted.substring(2, 4));
        int bgcolorBlue = hexToDecimal(hexcolorNameCnvrted.substring(4, 6));
        int bgcolorAlpha = Math.round(Float.parseFloat(colorName.split("-")[1]) * 255);

        return Color.argb(bgcolorAlpha, bgcolorRed, bgcolorGreen, bgcolorBlue);
    }

}
