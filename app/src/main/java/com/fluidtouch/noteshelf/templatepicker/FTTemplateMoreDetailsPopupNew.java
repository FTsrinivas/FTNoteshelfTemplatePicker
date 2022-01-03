package com.fluidtouch.noteshelf.templatepicker;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateBgLineHeightItemsAdapter;
import com.fluidtouch.noteshelf.templatepicker.adapters.MoreColorsHeaderItemAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.FTUserSelectedTemplateInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTColorVariants;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.interfaces.LineHeightSelctionInterface;
import com.fluidtouch.noteshelf.templatepicker.interfaces.TemplateBackgroundListener;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTTemplateMoreDetailsPopupNew extends FTBaseDialog.Popup implements LineHeightSelctionInterface {

    @BindView(R.id.lineHeightTitleId)
    TextView lineHeightTitle;

    @BindView(R.id.lineHeightRvID)
    RecyclerView lineHeightRv;

    @BindView(R.id.moreClrsListView)
    RecyclerView moreClrsListView;

    @BindView(R.id.closeMoreInfoPopup)
    ImageView closeMoreInfoPopup;

    @BindView(R.id.moreLytID)
    LinearLayout moreLytID;

    int xCordinateLoc;
    int yCordinateLoc;

    TemplateBackgroundListener.TemplateInfoRequest ftTemplateInfoRequest;
    private FTTemplatesInfoSingleton mFTTemplatesInfoSingleton;
    private FTUserSelectedTemplateInfo ftUserSelectedTemplateInfo;
    private FTTemplateUtil ftTemplateUtil;

    MoreColorsHeaderItemAdapter moreColorsHeaderItemAdapter;

    public FTTemplateMoreDetailsPopupNew(TemplateBackgroundListener.TemplateInfoRequest ftTemplateInfoRequest) {
        super();
        this.ftTemplateInfoRequest = ftTemplateInfoRequest;
        ftTemplateUtil             = FTTemplateUtil.getInstance();
        mFTTemplatesInfoSingleton  = FTTemplatesInfoSingleton.getInstance();
        ftUserSelectedTemplateInfo = FTUserSelectedTemplateInfo.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (isMobile()) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            bottomSheetDialog.getBehavior().setDraggable(true);
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Window window = dialog.getWindow();
            if (window != null) {
                //window.setGravity(Gravity.TOP | Gravity.START);
                window.setDimAmount(0.0f);
            }
        }
        super.dismissParent = false;
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View mView  = inflater.inflate(R.layout.templates_background_more_info_popup_new,
                container,false);
        setDialogPosition();
        return mView;
    }

    private void setDialogPosition() {
        Window window = getDialog().getWindow();
        // set "origin" to top left corner
        window.setGravity(Gravity.TOP|Gravity.LEFT);
        WindowManager.LayoutParams params = window.getAttributes();

        Log.d("TemplatePicker==>","setDialogPosition width::-"+window.getAttributes().width +" height::-"+window.getAttributes().height);
        params.x = xCordinateLoc+ xCordinateLoc/2 +10 ;
        params.y = yCordinateLoc + yCordinateLoc/2 +10;

        window.setElevation(getResources().getDimensionPixelOffset(R.dimen.new_5dp));
        window.setAttributes(params);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (!isMobile()) {
            moreLytID.setVisibility(View.GONE);
        } else {
            moreLytID.setVisibility(View.VISIBLE);
        }

        moreClrsListView.setNestedScrollingEnabled(false);

        mFTTemplatesInfoSingleton = FTTemplatesInfoSingleton.getInstance();
        ArrayList<FTLineTypes> ftLineTypesArrayList = mFTTemplatesInfoSingleton.getmFTLineTypes();
        FTTemplateBgLineHeightItemsAdapter lineHeightItemsAdapterNew =
                new FTTemplateBgLineHeightItemsAdapter(getActivity(), ftLineTypesArrayList,
                        this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        lineHeightRv.setLayoutManager(mLayoutManager);
        lineHeightRv.setAdapter(lineHeightItemsAdapterNew);

        ArrayList<FTColorVariants> ftColorVariantsArrayList = mFTTemplatesInfoSingleton.getfTColorVariants();
        moreColorsHeaderItemAdapter = new MoreColorsHeaderItemAdapter(getActivity(),ftColorVariantsArrayList,this);
        LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        moreClrsListView.setLayoutManager(categoryLayoutManager);
        moreClrsListView.setAdapter(moreColorsHeaderItemAdapter);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isMobile()) {
            moreLytID.setVisibility(View.GONE);
        } else {
            moreLytID.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void typeOfLineSelected() {
        if (ftTemplateInfoRequest != null) {
            /*ftTemplateInfoRequest.tempLineAndColorInfoResponse(
                    ftUserSelectedTemplateInfo.getFtTemplateColors(),
                    ftUserSelectedTemplateInfo.getFtLineTypes(),
                    ftTemplateUtil.getFtSelectedDeviceInfo());*/
            ftTemplateInfoRequest.templateBgColourChangedListener();
        }
    }

    @Override
    public void colorSelected() {
        if (ftTemplateInfoRequest != null) {
            /*ftTemplateInfoRequest.tempLineAndColorInfoResponse(
                    ftUserSelectedTemplateInfo.getFtTemplateColors(),
                    ftUserSelectedTemplateInfo.getFtLineTypes(),
                    ftTemplateUtil.getFtSelectedDeviceInfo());*/
            ftTemplateInfoRequest.moreColorViewSelected();
            ftTemplateInfoRequest.templateBgColourChangedListener();
        }
    }

    @Override
    public void thumbnailCreationStarted() {

    }

    @OnClick(R.id.closeMoreInfoPopup)
    void onClick() {
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }
}
