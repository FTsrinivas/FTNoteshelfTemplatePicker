package com.fluidtouch.noteshelf.templatepicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.document.thumbnailview.FTPageMoveToFragment;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.adapters.SupportedDevicesAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.FTDeviceDataInfo;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.FTDevicesDetailedInfo;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf.templatepicker.common.util.MyCustomLayoutManager;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTTemplateTabsDialog extends FTBaseDialog {

    /*@BindView(R.id.simpleTabLayout)
    TabLayout tabLayout;*/
    @BindView(R.id.simpleRecyclerView)
    RecyclerView deviceModelsRecyclerView;

    MyCustomLayoutManager mLayoutManager;

    @BindView(R.id.closeTabsBtn)
    ImageButton closeTabsBtn;

    FTTemplatesInfoSingleton mFTTemplatesInfoSingleton;
    ArrayList<FTDevicesDetailedInfo> ftDeviceDataInfoList;

    DeviceSelectionListener deviceSelectionListener;

    public static FTTemplateTabsDialog newInstance() {
        FTTemplateTabsDialog ftTemplateTabsDialog = new FTTemplateTabsDialog();
        Log.d("TemplatePicker==>","FTTemplateTabsDialog newInstance::-");
        return ftTemplateTabsDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.1f);
                window.setBackgroundDrawableResource(R.drawable.window_bg);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_template_picker_tabs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mFTTemplatesInfoSingleton          = FTTemplatesInfoSingleton.getInstance();
        ftDeviceDataInfoList               = mFTTemplatesInfoSingleton.getSupportedDevicesInfo();

        ArrayList<ItemModel> devicesInfoList = getSpinnerItems();

        String tabSelected = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");
        Log.d("TemplatePicker==>","FTTemplateTabsDialog tabSelected onViewCreated::-"+tabSelected);

        SupportedDevicesAdapter supportedDevicesAdapter
                = new SupportedDevicesAdapter(getActivity(),devicesInfoList, deviceSelectionListener,tabSelected);
        mLayoutManager = new MyCustomLayoutManager(getActivity());
        deviceModelsRecyclerView.setHasFixedSize(true);
        deviceModelsRecyclerView.setAdapter(supportedDevicesAdapter);
        deviceModelsRecyclerView.setLayoutManager(mLayoutManager);

        closeTabsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() != null) {
            this.deviceSelectionListener = (DeviceSelectionListener) getParentFragment();
        }
    }

    private ArrayList<ItemModel> getSpinnerItems() {
        ArrayList<ItemModel> items = null;
        items = new ArrayList<>();
        for (int i = 0; i< ftDeviceDataInfoList.size(); i++) {
            items.add(new ItemModel(
                    ftDeviceDataInfoList.get(i).getDimension(),
                    ftDeviceDataInfoList.get(i).getDimension_land(),
                    ftDeviceDataInfoList.get(i).getDimension_port(),
                    ftDeviceDataInfoList.get(i).getDisplayName(),
                    ftDeviceDataInfoList.get(i).getIdentifier()));
        }

        return items;
    }

    public interface DeviceSelectionListener{
        void onDeviceSelected(String tabSelected,ItemModel clickedItemInfo,String origin);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfiguration) {
        super.onConfigurationChanged(newConfiguration);
        Log.i("##Day",""+(newConfiguration.screenHeightDp)/newConfiguration.densityDpi);

        ViewTreeObserver observer = getView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Log.v("##Day",
                        String.format("new width=%d; new height=%d", getView().getWidth(),
                                getView().getHeight()));
                getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }
}

