package com.fluidtouch.noteshelf.welcome.screens;

import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf.welcome.WelcomeScreenDrawingView;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTPenType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTWelcomeScreenThirdFragment extends Fragment {
    @BindView(R.id.welcome_screen3_pen_layout)
    LinearLayout penLayout;
    @BindView(R.id.penmask_image_view)
    ImageView penmaskImageView;
    @BindView(R.id.calligraphymask_image_view)
    ImageView calligraphymaskImageView;
    @BindView(R.id.finemask_image_view)
    ImageView finemaskImageView;
    @BindView(R.id.highlightmask_image_view)
    ImageView highlightmaskImageView;
    @BindView(R.id.select_a_pen_image_view)
    ImageView selectAPenImageView;

    private View prevSelectedView;
    private WelcomeScreenDrawingView drawingView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        return inflater.inflate(R.layout.welcome_screen3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        FTLog.crashlyticsLog(FTWelcomeScreenThirdFragment.class.getSimpleName());

        prevSelectedView = penmaskImageView;

        ((LinearLayout.LayoutParams) penmaskImageView.getLayoutParams()).topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        ((LinearLayout.LayoutParams) calligraphymaskImageView.getLayoutParams()).topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        ((LinearLayout.LayoutParams) finemaskImageView.getLayoutParams()).topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        ((LinearLayout.LayoutParams) highlightmaskImageView.getLayoutParams()).topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);

        RelativeLayout layFtDrawingView = view.findViewById(R.id.welcome_screen3_writing_layout);
//        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
//            layFtDrawingView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.new_200dp)));
//        } else {
//            layFtDrawingView.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDimensionPixelOffset(R.dimen.new_586dp), getResources().getDimensionPixelOffset(R.dimen.new_262dp)));
//        }
        drawingView = view.findViewById(R.id.welcome_screen_drawing_view);
        drawingView.setIsCurrentPage(true);
        float scale = 2;
        drawingView.scale = scale;
        drawingView.reloadInRect(null);
        drawingView.setOnTouchListener((v, event) -> {
            selectAPenImageView.setVisibility(View.INVISIBLE);
            layFtDrawingView.requestDisallowInterceptTouchEvent(true);
            drawingView.processTouchEvent(event);
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        prevSelectedView.clearAnimation();
        LinearLayout.LayoutParams penmaskImageViewLayoutParams = (LinearLayout.LayoutParams) penmaskImageView.getLayoutParams();
        penmaskImageViewLayoutParams.topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        penmaskImageView.setLayoutParams(penmaskImageViewLayoutParams);

        calligraphymaskImageView.clearAnimation();
        LinearLayout.LayoutParams calligraphymaskImageViewLayoutParams = (LinearLayout.LayoutParams) calligraphymaskImageView.getLayoutParams();
        calligraphymaskImageViewLayoutParams.topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        calligraphymaskImageView.setLayoutParams(calligraphymaskImageViewLayoutParams);

        finemaskImageView.clearAnimation();
        LinearLayout.LayoutParams finemaskImageViewLayoutParams = (LinearLayout.LayoutParams) finemaskImageView.getLayoutParams();
        finemaskImageViewLayoutParams.topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        finemaskImageView.setLayoutParams(finemaskImageViewLayoutParams);

        highlightmaskImageView.clearAnimation();
        LinearLayout.LayoutParams highlightmaskImageViewLayoutParams = (LinearLayout.LayoutParams) highlightmaskImageView.getLayoutParams();
        highlightmaskImageViewLayoutParams.topMargin = -getResources().getDimensionPixelOffset(R.dimen.new_282dp);
        highlightmaskImageView.setLayoutParams(highlightmaskImageViewLayoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        FTAnimationUtils.valueAnimatorTop(penmaskImageView, -getResources().getDimensionPixelOffset(R.dimen.new_282dp), -getResources().getDimensionPixelOffset(R.dimen.new_50dp), 500);
        FTAnimationUtils.valueAnimatorTop(calligraphymaskImageView, -getResources().getDimensionPixelOffset(R.dimen.new_282dp), -getResources().getDimensionPixelOffset(R.dimen.new_50dp), 500);
        FTAnimationUtils.valueAnimatorTop(finemaskImageView, -getResources().getDimensionPixelOffset(R.dimen.new_282dp), -getResources().getDimensionPixelOffset(R.dimen.new_50dp), 500);
        FTAnimationUtils.valueAnimatorTop(highlightmaskImageView, -getResources().getDimensionPixelOffset(R.dimen.new_282dp), -getResources().getDimensionPixelOffset(R.dimen.new_50dp), 500);
        new Handler().postDelayed(() -> onPenToolSelected(prevSelectedView), 500);
        new Handler().postDelayed(() -> drawingView.onScreenRenderManager.renderAnnotations(drawingView.annotations(),
                new RectF(drawingView.getLeft(), drawingView.getTop(), drawingView.getRight(), drawingView.getBottom()), drawingView.scale, null), 10);
    }

    @OnClick({R.id.penmask_image_view, R.id.calligraphymask_image_view, R.id.finemask_image_view, R.id.highlightmask_image_view})
    void onPenToolSelected(View view) {
        if (prevSelectedView != null && prevSelectedView.getId() == view.getId() && ((LinearLayout.LayoutParams) prevSelectedView.getLayoutParams()).topMargin == 0)
            return;
        drawingView.selectedTool = FTToolBarTools.PEN;
        switch (view.getId()) {
            case R.id.penmask_image_view:
                drawingView.penType = FTPenType.pen;
                drawingView.selectedColor = PenRackPref.DEFAULT_PEN_COLOR;
                break;
            case R.id.calligraphymask_image_view:
                drawingView.penType = FTPenType.caligraphy;
                drawingView.selectedColor = "#d0170a";
                break;
            case R.id.finemask_image_view:
                drawingView.penType = FTPenType.pilotPen;
                drawingView.selectedColor = "#0a5cb8";
                break;
            case R.id.highlightmask_image_view:
                drawingView.penType = FTPenType.flatHighlighter;
                drawingView.selectedColor = PenRackPref.DEFAULT_HIGHLIGHTER_COLOR;
                drawingView.selectedTool = FTToolBarTools.HIGHLIGHTER;
                break;
        }
        if (prevSelectedView != null) {
            FTAnimationUtils.valueAnimatorTop(prevSelectedView, 0, -getResources().getDimensionPixelOffset(R.dimen.new_50dp));
        }
        prevSelectedView = view;
        FTAnimationUtils.valueAnimatorTop(prevSelectedView, -getResources().getDimensionPixelOffset(R.dimen.new_50dp), 0);
    }
}