package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

public class FTDeviceModelSpinnerItemsAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<ItemModel> items;
    public FTDeviceModelSpinnerItemsAdapter(Context context, ArrayList<ItemModel> items) {
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ItemModel cell = (ItemModel) getItem(position);
        /*Log.d("TemplatePicker==>"," isSectionHeader::-"+cell.isSectionHeader());
        //If the cell is a section header we inflate the header layout
        if(cell.isSectionHeader()) {
            v = inflater.inflate(R.layout.supported_devices_spinner_header_section, null);
            v.setClickable(false);
            TextView header = (TextView) v.findViewById(R.id.deviceModelSpinnerItemID);
            header.setText(cell.getDeviceTitle());
        } else {
            v = inflater.inflate(R.layout.supported_devices_spinner_item_view, null);
            TextView time_time = (TextView) v.findViewById(R.id.textSeparator);
            time_time.setText(cell.getDisplayName());
        }*/
        v = inflater.inflate(R.layout.supported_devices_spinner_item_view, null);
        TextView time_time = (TextView) v.findViewById(R.id.textSeparator);
        time_time.setText(cell.getDisplayName());
        return v;
    }
}

