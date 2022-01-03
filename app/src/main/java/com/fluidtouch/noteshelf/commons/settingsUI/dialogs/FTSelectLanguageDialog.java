package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.settingsUI.adapters.FTLanguageItemAdapter;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.lasso.FTLassoConvertToText;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTRecognitionLangResource;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSelectLanguageDialog extends FTBaseDialog {
    @BindView(R.id.language_recycler_view)
    RecyclerView languageRecyclerView;
    @BindView(R.id.layout_container)
    LinearLayout layoutContainer;
    @BindView(R.id.layout_header)
    RelativeLayout layoutHeader;

    private boolean isConvertToText;

    public static FTSelectLanguageDialog newInstance(boolean isConvertToText) {
        FTSelectLanguageDialog selectLanguageDialog = new FTSelectLanguageDialog();
        selectLanguageDialog.isConvertToText = isConvertToText;
        return selectLanguageDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        view.setOnClickListener(null);
        List<FTRecognitionLangResource> recognitionLangResources = new ArrayList<>();
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            recognitionLangResources = FTLanguageResourceManager.getInstance().availableLanguageResourcesForSHW();
        } else {
            recognitionLangResources = FTLanguageResourceManager.getInstance().availableLanguageResources();
        }
        if (isConvertToText) {
            if (!ScreenUtil.isMobile(getContext()))
                layoutContainer.setLayoutParams(new FrameLayout.LayoutParams(ScreenUtil.convertDpToPx(getContext(), 580), ScreenUtil.convertDpToPx(getContext(), 510)));
            layoutHeader.setVisibility(View.VISIBLE);
        }
        FTLanguageItemAdapter adapter = new FTLanguageItemAdapter(getActivity(), isConvertToText);
        adapter.updateAll(recognitionLangResources);
        languageRecyclerView.setAdapter(adapter);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(getResources().getDrawable(R.drawable.divider_dark));
        languageRecyclerView.addItemDecoration(itemDecorator);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getParentFragment() instanceof FTLassoConvertToText) {
            Dialog dialog = getDialog();
            if (dialog != null && !(dialog instanceof BottomSheetDialog)) {
                Size size = getDialogSizeByPercentage(0.75f, 0.50f);
                dialog.getWindow().setLayout(size.getWidth(), size.getHeight());
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @OnClick({R.id.dialog_language_close_button, R.id.dialog_language_done_button, R.id.image_back})
    void onDoneClicked() {
        dismiss();
    }
}