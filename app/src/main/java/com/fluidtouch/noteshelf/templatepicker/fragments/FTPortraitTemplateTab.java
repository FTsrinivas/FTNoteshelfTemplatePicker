package com.fluidtouch.noteshelf.templatepicker.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.templatepicker.FTTemplateTabsDialog;
import com.fluidtouch.noteshelf.templatepicker.adapters.SupportedDevicesAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf.templatepicker.common.util.MyCustomLayoutManager;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTPortraitTemplateTab extends Fragment {

    @BindView(R.id.deviceModelsRecyclerView)
    RecyclerView deviceModelsRecyclerView;

    MyCustomLayoutManager mLayoutManager;

    ArrayList<ItemModel> spinnerItems;
    FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener;
    String tabSelected;
    int tabPosition;
    public FTPortraitTemplateTab(ArrayList<ItemModel> spinnerItems,
                                 FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener,
                                 int tabPosition) {
        this.spinnerItems = spinnerItems;
        this.deviceSelectionListener = deviceSelectionListener;
        this.tabPosition =  tabPosition;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.portrait_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (tabPosition == 0) {
            tabSelected = "Portrait";
        } else {
            tabSelected = "Landscape";
        }

        Log.d("TemplatePicker==>", "FTPortraitTemplateTab tabSelected Status::-"
                + tabSelected.toLowerCase().equalsIgnoreCase("landscape"));

        if (tabSelected.toLowerCase().equalsIgnoreCase("landscape")) {
            for (int i=0;i<spinnerItems.size();i++) {

                Log.d("TemplatePicker==>", "FTPortraitTemplateTab Status::-"
                        + spinnerItems.get(i).getDisplayName().equalsIgnoreCase("Mobile")+
                        " getDisplayName::-"+spinnerItems.get(i).getDisplayName());

                if (spinnerItems.get(i).getDisplayName().equalsIgnoreCase("Mobile")) {
                    spinnerItems.remove(i);
                }
            }
        }

        SupportedDevicesAdapter supportedDevicesAdapter
                = new SupportedDevicesAdapter(getActivity(),spinnerItems, deviceSelectionListener,tabSelected);
        mLayoutManager = new MyCustomLayoutManager(getActivity());
        deviceModelsRecyclerView.setHasFixedSize(true);
        deviceModelsRecyclerView.setAdapter(supportedDevicesAdapter);
        deviceModelsRecyclerView.setLayoutManager(mLayoutManager);
    }

}
