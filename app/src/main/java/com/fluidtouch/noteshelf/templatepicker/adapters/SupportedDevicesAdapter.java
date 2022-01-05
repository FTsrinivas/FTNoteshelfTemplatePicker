package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateTabsDialog;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SupportedDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    List<ItemModel> itemModelList;
    private int lastSelectedPosition = -1;

    FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener;
    String tabSelected;
    public SupportedDevicesAdapter(Context mContext, List<ItemModel> itemModelList,
                                   FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener,
                                   String tabSelected) {
        this.mContext = mContext;
        this.itemModelList = itemModelList;
        this.deviceSelectionListener = deviceSelectionListener;
        this.tabSelected = tabSelected;
    }

    @Override
    public int getItemViewType(int position) {
        return FTConstants.VIEWTYPE_ROW_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup rowItemGrp = (ViewGroup) inflater.inflate(R.layout.supporteddevices_rowitems,parent,false);
        RowItemViewHolder rowItemViewHolder = new RowItemViewHolder(rowItemGrp,parent.getContext()) ;
        return rowItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RowItemViewHolder rowItemViewHolder = (RowItemViewHolder) holder;
        if (itemModelList.get(position).getDisplayName() != null) {
            rowItemViewHolder.txtName.setText(itemModelList.get(position).getDisplayName());
        }

        rowItemViewHolder.checkImageView.setImageDrawable(rowItemViewHolder.mContext.getDrawable(R.drawable.check_mark_bg));

        String tabSelected = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");
        String deviceSize  = FTApp.getPref().get(SystemPref.LAST_SELECTED_PAPER, "A4 8.3 x 11.7\"\"");
        Log.d("TemplatePicker==>", "DeviceSelection:: SupportedDevicesAdapter onBindViewHolder deviceSize::-" + deviceSize+
                    " getDisplayName:: "+itemModelList.get(position).getDisplayName());

        /*if (deviceSize.contains(itemModelList.get(position).getDisplayName())) {
            rowItemViewHolder.checkImageView.setImageDrawable(rowItemViewHolder.mContext.getDrawable(R.drawable.check_mark_bg));
        } else {
            rowItemViewHolder.checkImageView.setImageDrawable(null);
        }*/

        if (deviceSize.contains(itemModelList.get(position).getDisplayName())) {
            rowItemViewHolder.checkImageView.setImageDrawable(rowItemViewHolder.mContext.getDrawable(R.drawable.check_mark_bg));
        } else {
            rowItemViewHolder.checkImageView.setImageDrawable(null);
        }

    }

    @Override
    public int getItemCount() {
        return itemModelList.size();
    }

    private class RowItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtName;
        ImageView checkImageView;
        RelativeLayout deviceItemLyt;
        Context mContext;

        public RowItemViewHolder(@NonNull View itemView,Context mContext) {
            super(itemView);
            txtName         = itemView.findViewById(R.id.txtName);
            checkImageView  = itemView.findViewById(R.id.ivListItem);
            deviceItemLyt   = itemView.findViewById(R.id.deviceItem);

            this.mContext = mContext;

            deviceItemLyt.setOnClickListener(this);
            checkImageView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.ivListItem:
                case R.id.deviceItem:
                    if (getAdapterPosition() != -1) {
                        String dimension    = null;
                        String[] splitString;
                        lastSelectedPosition = getAdapterPosition();
                        ItemModel clickedDataItem = itemModelList.get(lastSelectedPosition);
                        Toast.makeText(view.getContext(), "You clicked " + clickedDataItem.getDisplayName(), Toast.LENGTH_SHORT).show();

                        FTApp.getPref().save(SystemPref.LAST_SELECTED_TAB, tabSelected.toLowerCase());
                        FTApp.getPref().save(SystemPref.LAST_SELECTED_PAPER, clickedDataItem.getDisplayName());
                        FTTemplatesInfoSingleton.getInstance().setRecentlySelctedDevice(clickedDataItem);

                        FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                        FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();

                        if (selectedDeviceInfo.getLayoutType().equalsIgnoreCase("Port")) {
                            dimension   = clickedDataItem.getDimension_port();
                        } else {
                            dimension   = clickedDataItem.getDimension_land();
                        }

                        splitString = dimension.split("_");

                        ftSelectedDeviceInfo.setPageWidth(Integer.parseInt(splitString[0]));
                        ftSelectedDeviceInfo.setPageHeight(Integer.parseInt(splitString[1]));
                        ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                        ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());
                        ftSelectedDeviceInfo.setThemeBgClrName(selectedDeviceInfo.getThemeMoreBgClrName());
                        ftSelectedDeviceInfo.setThemeBgClrHexCode(selectedDeviceInfo.getThemeBgClrHexCode());

                        ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
                        ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());
                        ftSelectedDeviceInfo.setHorizontalLineClr(selectedDeviceInfo.getHorizontalLineClr());
                        ftSelectedDeviceInfo.setVerticalLineClr(selectedDeviceInfo.getVerticalLineClr());
                        ftSelectedDeviceInfo.setHorizontalMoreLineClr(selectedDeviceInfo.getHorizontalMoreLineClr());
                        ftSelectedDeviceInfo.setVerticaMorelLineClr(selectedDeviceInfo.getVerticaMorelLineClr());

                        ftSelectedDeviceInfo.setSelectedDeviceName(clickedDataItem.getDisplayName());
                        ftSelectedDeviceInfo.setItemModel(clickedDataItem);
                        ftSelectedDeviceInfo.selectSavedDeviceInfo();
                        Log.d("FTChoosePaperTemplate 2==>","2");

                        deviceSelectionListener.onDeviceSelected(tabSelected.toLowerCase(),clickedDataItem,"adapter");
                        notifyDataSetChanged();
                    }
                    break;
            }
        }
    }

}
