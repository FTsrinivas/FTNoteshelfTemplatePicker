package com.fluidtouch.noteshelf.document.penracks;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.models.penrack.FTNPenRack;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
import com.fluidtouch.noteshelf2.R;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 06/05/19
 */
public class FTEditColorsFragment extends FTBaseDialog.Popup implements FTEditColorsAdapter.EditColorsAdapterContainerListener, FTCustomColorPickerFragment.ColorPickerContainerCallback {
    @BindView(R.id.edit_colors_title_text_view)
    protected TextView mTitleTextView;
    @BindView(R.id.edit_colors_notes_text_view)
    protected TextView mNotesTextView;
    @BindView(R.id.edit_colors_reset_text_view)
    protected TextView mResetTextView;
    @BindView(R.id.edit_colors_delete_image_view)
    protected ImageView mDeleteImageView;
    @BindView(R.id.edit_colors_recycler_view)
    protected RecyclerView mColorsRecyclerView;
    @BindView(R.id.edit_colors_reset_dialog_layout)
    protected LinearLayout mResetLayout;
    @BindView(R.id.edit_colors_delete_dialog_layout)
    protected LinearLayout mDeleteLayout;
    @BindView(R.id.edit_colors_back_image_view)
    ImageView mBackButton;

    private static final String CURRENT_COLORS = "currentColors";

    private String forRackType;
    private HashMap<String, String> mColorsMap = new HashMap<>();
    private FTNPenRack mPenRack;
    private FTEditColorsAdapter mColorsAdapter;
    private FTEditColorsFragment.FTEditColorsContainerCallback mContainerCallback;
    private List<String> removedColors;
    private EditColorsCallback mCallback;

    public static FTEditColorsFragment newInstance(String forRackType, FTEditColorsContainerCallback containerCallback) {
        FTEditColorsFragment fragment = new FTEditColorsFragment();
        fragment.forRackType = forRackType;
        fragment.mContainerCallback = containerCallback;
        return fragment;
    }

    public static FTEditColorsFragment newInstance(String forRackType, EditColorsCallback callback) {
        FTEditColorsFragment fragment = new FTEditColorsFragment();
        fragment.forRackType = forRackType;
        fragment.mCallback = callback;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (isMobile()) {
            Dialog dialog = new Dialog(getContext());
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.0f);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            return dialog;
        } else {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.CENTER);
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                if (mCallback != null) {
                    TypedValue tv = new TypedValue();
                    if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                        int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                        layoutParams.y -= actionBarHeight;
                    }
                }
            }
            return dialog;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_colors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        LinearLayout dialogLayout = view.findViewById(R.id.edit_colors_layout);
        ViewGroup.LayoutParams layoutParams = dialogLayout.getLayoutParams();
        if (mCallback == null) {
            mBackButton.setVisibility(View.VISIBLE);
            mTitleTextView.setText(R.string.edit_colors);
            mNotesTextView.setVisibility(View.VISIBLE);
            if (isMobile()) layoutParams.height = ScreenUtil.convertDpToPx(getContext(), 427);
        } else {
            mBackButton.setVisibility(View.INVISIBLE);
            mTitleTextView.setText(R.string.choose_color);
            mNotesTextView.setVisibility(View.GONE);
            if (!isMobile()) {
                layoutParams.width = ScreenUtil.convertDpToPx(getContext(), 376);
            }
            layoutParams.height = ScreenUtil.convertDpToPx(getContext(), 427);
        }
        dialogLayout.setLayoutParams(layoutParams);
        mPenRack = FTNPenRack.getInstance();
        mColorsAdapter = new FTEditColorsAdapter(this);
        mColorsAdapter.setData(getColors());

        mColorsRecyclerView.setLayoutManager(new FTGridLayoutManager(getContext(), ScreenUtil.convertDpToPx(getContext(), 60)));
        mColorsRecyclerView.setHasFixedSize(true);
        //mColorsRecyclerView.addItemDecoration(new FTEditColorsFragment.SeparatorDecorator(7, -1));
        mColorsRecyclerView.setAdapter(mColorsAdapter);

        new GestureManager.Builder(mColorsRecyclerView)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .build();

        mColorsAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<String>() {
            @Override
            public void onItemRemoved(final String item, final int position) {
                //Not working with swiping now.
            }

            @Override
            public void onItemReorder(final String item, final int fromPos, final int toPos) {
                FTFirebaseAnalytics.logEvent("Pen_EditColor_DragToReorder");
                if (mContainerCallback != null)
                    mContainerCallback.reorder(fromPos, toPos == mColorsAdapter.getItemCount() - 1 ? toPos - 1 : toPos);
                if (!mColorsAdapter.getItem(mColorsAdapter.getItemCount() - 1).equals("")) {
                    new Handler().postDelayed(() -> {
                        for (int i = 0; i < mColorsAdapter.getItemCount(); i++) {
                            if (mColorsAdapter.getData().get(i).equals("")) {
                                mColorsAdapter.remove(i);
                                mColorsAdapter.add("");
                                mColorsAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }, 100);
                }
            }
        });
        //This code is to remove extra space after delete icon in mobile/split mode.
        if (isMobile() || mCallback != null) {
            ConstraintLayout.LayoutParams deleteIconLayoutParams = (ConstraintLayout.LayoutParams) mDeleteImageView.getLayoutParams();
            deleteIconLayoutParams.rightMargin = 0;
            mDeleteImageView.setLayoutParams(deleteIconLayoutParams);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        if (isMobile()) {
            window.setGravity(Gravity.TOP | Gravity.CENTER);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            if (atView != null) {
                if (mCallback != null) {
                    int[] location = new int[2];
                    atView.getLocationOnScreen(location);
                    int sourceX = location[0];
                    int sourceY = location[1];
                    window.setGravity(Gravity.TOP | Gravity.START);
                    layoutParams.x = sourceX + atView.getWidth() / 2;
                    layoutParams.y = sourceY + atView.getHeight() / 2;
                } else {
                    layoutParams.gravity = Gravity.TOP | Gravity.START;
                    int[] location = new int[2];
                    atView.getLocationOnScreen(location);
                    int sourceY = location[1];
                    View dialogLayout = getView().findViewById(R.id.edit_colors_layout);
                    if (dialogLayout.getLayoutParams() != null) {
                        layoutParams.y = Math.abs(sourceY - dialogLayout.getLayoutParams().height);
                    }
                }
            }
        }
        window.setAttributes(layoutParams);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mCallback != null) dismiss();
    }

    @Override
    public void onDestroy() {
        saveColors();
        super.onDestroy();
    }

    @OnClick(R.id.edit_colors_back_image_view)
    void closeDialog() {
        if (mDeleteLayout.getVisibility() == View.VISIBLE) {
            doneDeleting();
            return;
        }
        dismiss();
    }

    @OnClick(R.id.edit_colors_reset_text_view)
    void resetColors() {
        mResetLayout.setVisibility(View.VISIBLE);
        mResetTextView.setVisibility(View.GONE);
        mDeleteImageView.setVisibility(View.GONE);
    }

    @OnClick(R.id.edit_colors_reset_dialog_cancel_text_view)
    void cancelReset() {
        FTFirebaseAnalytics.logEvent("Pen_DeleteColor_Cancel");
        mResetLayout.setVisibility(View.GONE);
        mResetTextView.setVisibility(View.VISIBLE);
        mDeleteImageView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.edit_colors_reset_dialog_reset_text_view)
    void applyReset() {
        FTFirebaseAnalytics.logEvent("Pen_EditColor_Reset");
        mResetLayout.setVisibility(View.GONE);
        mResetTextView.setVisibility(View.VISIBLE);
        mDeleteImageView.setVisibility(View.VISIBLE);
        mPenRack.resetColors(forRackType);
        mColorsMap.clear();
        mColorsAdapter.setData(getColors());
        if (mContainerCallback != null) mContainerCallback.fetchColors();
    }

    @OnClick(R.id.edit_colors_delete_image_view)
    void deleteColors() {
        FTFirebaseAnalytics.logEvent("Pen_EditColor_Delete");
        removedColors = new ArrayList<>();
        mDeleteLayout.setVisibility(View.VISIBLE);
        setModeBasedUI(true);
    }

    @OnClick(R.id.edit_colors_delete_dialog_cancel_text_view)
    void cancelDeleting() {
        FTFirebaseAnalytics.logEvent("Pen_DeleteColor_Cancel");
        mDeleteLayout.setVisibility(View.GONE);
        setModeBasedUI(false);
        mColorsAdapter.setData(getColors());
        if (mContainerCallback != null) mContainerCallback.fetchColors();
    }

    @OnClick(R.id.edit_colors_delete_dialog_reset_text_view)
    void doneDeleting() {
        FTFirebaseAnalytics.logEvent("Pen_DeleteColor_Done");
        for (int i = 0; i < removedColors.size(); i++) {
            String removedColor = removedColors.get(i);
            mColorsMap.remove("#" + removedColor);
            if (mContainerCallback != null) mContainerCallback.removeColor(removedColor);
        }
        removedColors.clear();
        mDeleteLayout.setVisibility(View.GONE);
        setModeBasedUI(false);
    }

    private void setModeBasedUI(boolean isInDeleteMode) {
        mColorsAdapter.setMode(isInDeleteMode);
        mTitleTextView.setText(isInDeleteMode ? getString(R.string.delete_colors) : getString(R.string.edit_colors));
        mNotesTextView.setText(isInDeleteMode ? getString(R.string.Tap_on_a_color) : getString(R.string.tap_to_choose_color));
        mDeleteImageView.setVisibility(isInDeleteMode ? View.GONE : View.VISIBLE);
        mResetTextView.setVisibility(isInDeleteMode ? View.GONE : View.VISIBLE);
        if (isInDeleteMode) {
            mColorsAdapter.remove(mColorsAdapter.getItemCount() - 1);
        } else {
            mColorsAdapter.add("");
        }
    }

    private List<String> getColors() {
        HashMap<String, Object> penRackData = (HashMap<String, Object>) mPenRack.getPenRackData().get(forRackType);
        if (penRackData == null)
            return new ArrayList<>();
        return getStringList((Object[]) penRackData.get(CURRENT_COLORS));
    }

    private List<String> getStringList(Object[] objects) {
        List<String> strings = new ArrayList<>();
        if (objects != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                strings = Arrays.stream(objects).map(Objects::toString).collect(Collectors.toList());
                for (Object object : objects) {
                    String color = (String) object;
                    mColorsMap.put(color, getString(R.string.set_color, color));
                }
            } else {
                for (Object object : objects) {
                    String color = (String) object;
                    strings.add(color);
                    mColorsMap.put(color, getString(R.string.set_color, color));
                }
            }
        }
        strings.add("");
        return strings;
    }

    @Override
    public void onColorSelected(String color, int position) {
        if (mCallback != null) {
            FTFirebaseAnalytics.logEvent("Pen_EditColor_SelectColor");
            mCallback.onColorSelected("#" + color);
            dismiss();
        } else {
            FTFirebaseAnalytics.logEvent("Pen_EditColor_AddColor");
            showColorPickerDialog(color, position);
        }
    }

    @Override
    public void showColorPickerDialog(String color, int position) {
        FTCustomColorPickerFragment.newInstance("#" + color, position, position == mColorsAdapter.getItemCount() - 1, mCallback == null, this).show(atView, getChildFragmentManager());
    }

    @Override
    public void removeColor(String color, int position) {
        removedColors.add(color);
    }

    @Override
    public void showMinimumColorsError(int minimumColorCount) {
        mNotesTextView.setText(getString(R.string.minimum_of_colors, minimumColorCount));
        mNotesTextView.setTextColor(Color.parseColor("#cc4235"));
        new Handler().postDelayed(() -> {
            mNotesTextView.setText(getString(R.string.Tap_on_a_color));
            mNotesTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        }, 700);
    }

    @Override
    public void addColorToRack(String color, int position) {
        if (position != mColorsAdapter.getItemCount() - 1) {
            mColorsAdapter.remove(position);
        }
        mColorsAdapter.insert(color.contains("#") ? color.split("#")[1] : color, position);
        mColorsMap.put(color, color);
        saveColors();
        if (mContainerCallback != null) mContainerCallback.addColor(color, position);
    }

    @Override
    public boolean isColorExistsInRack(String color) {
        return mColorsMap.containsValue(color);
    }

    @Override
    public void onBackClicked() {

    }

    private void saveColors() {
        List<String> colors = new ArrayList<>(mColorsAdapter.getData());
        if (!mColorsAdapter.mIsInDeleteMode) {
            colors.remove(colors.size() - 1);
        }
        mPenRack.updateColors(forRackType, colors);
    }

    public interface FTEditColorsContainerCallback {
        void addColor(String color, int position);

        void fetchColors();

        void removeColor(String removedColor);

        void reorder(int fromPos, int toPos);
    }

    private class SeparatorDecorator extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mNo_of_col;
        private int offset;

        SeparatorDecorator(int mNo_of_col, int offset) {
            mDivider = getResources().getDrawableForDensity(R.drawable.colors_divider, DisplayMetrics.DENSITY_XHIGH);
            this.mNo_of_col = mNo_of_col;
            this.offset = offset;
        }

        @Override
        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            drawVertical(canvas, parent);
        }

        /**
         * Draw dividers at each expected grid interval
         */
        private void drawVertical(Canvas canvas, RecyclerView parent) {
            if (parent.getChildCount() == 0) return;

            final int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin;
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, ScreenUtil.getScreenWidth(getContext()), bottom);
                mDivider.setAlpha(250);
                mDivider.draw(canvas);
            }
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.set(0, ScreenUtil.convertDpToPx(view.getContext(), offset), 0, 0);
        }
    }

    public interface EditColorsCallback {
        void onColorSelected(String color);
    }
}