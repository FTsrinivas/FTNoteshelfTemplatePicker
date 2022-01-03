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

import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateTabsDialog;
import com.fluidtouch.noteshelf.templatepicker.adapters.SupportedDevicesAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf.templatepicker.common.util.MyCustomLayoutManager;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTLandscapeTemplateTab extends Fragment {

    @BindView(R.id.deviceModelsRecyclerView)
    RecyclerView deviceModelsRecyclerView;

    MyCustomLayoutManager mLayoutManager;

    ArrayList<ItemModel> spinnerItems;

    FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener;
    public FTLandscapeTemplateTab(ArrayList<ItemModel> spinnerItems, FTTemplateTabsDialog.DeviceSelectionListener deviceSelectionListener) {
        this.spinnerItems = spinnerItems;
        this.deviceSelectionListener = deviceSelectionListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landscape_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        SupportedDevicesAdapter supportedDevicesAdapter = new SupportedDevicesAdapter(getActivity(),spinnerItems,deviceSelectionListener,"landscape");
        mLayoutManager = new MyCustomLayoutManager(getActivity());
        deviceModelsRecyclerView.setHasFixedSize(true);
        deviceModelsRecyclerView.setAdapter(supportedDevicesAdapter);
        deviceModelsRecyclerView.setLayoutManager(mLayoutManager);
    }
}
