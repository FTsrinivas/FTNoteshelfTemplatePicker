package com.fluidtouch.noteshelf.commons.settingsUI.dialogs;

import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.touchManagement.FTWritingStyle;

import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWritingStyleDialog extends FTBaseDialog {
    @BindView(R.id.handWritingText)
    TextView mHandWritingText;
    @BindView(R.id.handwriting_dialog_language_text_view)
    TextView mLanguageSelectedTextView;
    @BindView(R.id.handwriting_dialog_writing_style_grid_layout)
    GridLayout writingStyleGridLayout;

    @BindView(R.id.writing_style_left_bottom_view)
    ImageView leftBottomView;
    @BindView(R.id.writing_style_right_bottom_view)
    ImageView rightBottomView;
    @BindView(R.id.writing_style_left_center_view)
    ImageView leftCenterView;
    @BindView(R.id.writing_style_right_center_view)
    ImageView rightCornerView;
    @BindView(R.id.writing_style_left_top_view)
    ImageView leftTopView;
    @BindView(R.id.writing_style_right_top_view)
    ImageView rightTopView;

    private static View view;
    private FTWritingStyle prevWritingStyle;

    private Observer languageChangeObserver = (observable, o) -> {
        if (getActivity() != null && isVisible()) {
            getActivity().runOnUiThread(() -> {
                String langCode = (String) o;
                FTLanguageResourceManager.getInstance().setCurrentLanguageCode(langCode);
                mLanguageSelectedTextView.setText(FTLanguageResourceManager.getInstance().currentLanguageDisplayName());
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.dialog_writing_style, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (BuildConfig.FLAVOR.contains("beta") || BuildConfig.FLAVOR.contains("dev")) {
            mHandWritingText.setText(mHandWritingText.getText().toString() + "(" + FTApp.getPref().get(SystemPref.CURRENT_HW_REG, "Samsung") + ")");
        }
        updateViewForWritingStyle(FTWritingStyle.rightBottom, false);

        ObservingService.getInstance().addObserver("languageChange", languageChangeObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
//        prevWritingStyle = FTApp.getPref().writingStyle();
//        updateViewForWritingStyle(prevWritingStyle, true);
        mLanguageSelectedTextView.setText(FTLanguageResourceManager.getInstance().currentLanguageDisplayName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.select_language_fragment);
            if (fragment != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObservingService.getInstance().removeObserver("languageChange", languageChangeObserver);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        if (getActivity() != null) {
            Fragment fragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.select_language_fragment);
            if (fragment != null) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }

    @OnClick(R.id.handwriting_dialog_language_layout)
    void onLanguageLayoutClicked() {
        FTFirebaseAnalytics.logEvent("settings", "handwriting", "recognition_language");
        //FTSelectLanguageDialog.newInstance().show(getChildFragmentManager());
    }

    @OnClick({R.id.writing_style_left_bottom_view,
            R.id.writing_style_right_bottom_view,
            R.id.writing_style_left_center_view,
            R.id.writing_style_right_center_view,
            R.id.writing_style_left_top_view,
            R.id.writing_style_right_top_view})
    void onWritingStyleSelected(ImageView view) {
        FTWritingStyle selectedWritingStyle = FTWritingStyle.rightBottom;
        switch (view.getId()) {
            case R.id.writing_style_left_bottom_view:
                selectedWritingStyle = FTWritingStyle.leftTop;
                break;
            case R.id.writing_style_right_bottom_view:
                selectedWritingStyle = FTWritingStyle.rightTop;
                break;
            case R.id.writing_style_left_center_view:
                selectedWritingStyle = FTWritingStyle.leftCenter;
                break;
            case R.id.writing_style_right_center_view:
                selectedWritingStyle = FTWritingStyle.rightCorner;
                break;
            case R.id.writing_style_left_top_view:
                selectedWritingStyle = FTWritingStyle.leftBottom;
                break;
            case R.id.writing_style_right_top_view:
                selectedWritingStyle = FTWritingStyle.rightBottom;
                break;
        }
//        if (prevWritingStyle != selectedWritingStyle) {
//            updateViewForWritingStyle(prevWritingStyle, false);
//            updateViewForWritingStyle(selectedWritingStyle, true);
//
//            FTApp.getPref().setWritingStyle(selectedWritingStyle);
//            FTStylusPenManager.getInstance().setWritingStyle(selectedWritingStyle);
//            prevWritingStyle = selectedWritingStyle;
//        }
    }

    private void updateViewForWritingStyle(FTWritingStyle writingStyle, boolean isSelected) {
        deselectAllHandwritingSelections();
        switch (writingStyle) {
            case leftBottom:
                leftTopView.setImageResource(isSelected ? R.drawable.writing_3_sel : R.drawable.writing_3);
                break;
            case rightBottom:
                rightTopView.setImageResource(isSelected ? R.drawable.writing_6_sel : R.drawable.writing_6);
                break;
            case leftCenter:
                leftCenterView.setImageResource(isSelected ? R.drawable.writing_2_sel : R.drawable.writing_2);
                break;
            case rightCorner:
                rightCornerView.setImageResource(isSelected ? R.drawable.writing_5_sel : R.drawable.writing_5);
                break;
            case leftTop:
                leftBottomView.setImageResource(isSelected ? R.drawable.writing_1_sel : R.drawable.writing_1);
                break;
            case rightTop:
                rightBottomView.setImageResource(isSelected ? R.drawable.writing_4_sel : R.drawable.writing_4);
                break;
        }
    }

    private void deselectAllHandwritingSelections() {
        leftTopView.setImageResource(R.drawable.writing_3);
        rightTopView.setImageResource(R.drawable.writing_6);
        leftCenterView.setImageResource(R.drawable.writing_2);
        rightCornerView.setImageResource(R.drawable.writing_5);
        leftBottomView.setImageResource(R.drawable.writing_1);
        rightBottomView.setImageResource(R.drawable.writing_4);
    }
}