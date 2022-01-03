package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.penracks.FTCustomColorPickerFragment;
import com.fluidtouch.noteshelf.document.penracks.FTEditColorsFragment;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.penrack.FTNPenRack;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTColorPickerFragment extends Fragment implements FTCustomColorPickerFragment.ColorPickerContainerCallback {
    @BindView(R.id.layColorScroll)
    protected HorizontalScrollView mScrollView;
    @BindView(R.id.colorSelectionColorsLayout)
    protected LinearLayout mLayColors;
    //region Member Variables
    View view;
    private ColorPickerListener mColorPickerListener;
    private Context mContext;
    private ArrayList<Object> mColorStrings = new ArrayList<>();
    private View mSelectedColorView;
    private String mSelectedColor;
    private ArrayList<String> mCustomColors = new ArrayList<>();
    private Drawable addDrawable;
    private FTNPenRack ftnPenRack;

    //region Factory Method
    public static FTColorPickerFragment newInstance(ColorPickerListener mColorPickerListener) {
        FTColorPickerFragment f = new FTColorPickerFragment();
        f.mColorPickerListener = mColorPickerListener;
        return f;
    }

    //endregion

    //region Lifecycle Events
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.color_selection_layout, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addDrawable = mContext.getDrawable(R.drawable.plus_blue);
        this.mSelectedColor = FTApp.getPref().get(SystemPref.RECENT_INPUT_TEXT_COLOR, FTConstants.DEFAULT_INPUT_TEXT_COLOR);
        loadColors();
        /*String[] customColors = FTApp.getPref().get(SystemPref.CUSTOM_TEXT_COLORS, "").split(",");
        for (String customColor : customColors) {
            if (!TextUtils.isEmpty(customColor)) mCustomColors.add(customColor);
        }
        try {
            Object[] colors = (Object[]) PropertyListParser.parse(FTFileManagerUtil.getFileInputStream(FTFileManagerUtil.copyFileFromAssets(mContext, FTConstants.DEFAULT_COLORS_PLIST_RELATIVE_PATH))).toJavaObject();
            Collections.addAll(mColorStrings, colors);
            mColorStrings.addAll(mCustomColors);
            mColorStrings.add("Add");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        setAdapter();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //region Set Data to UI
    private void setAdapter() {
        mLayColors.removeAllViews();

        if (mColorStrings != null) {
            for (int i = 0; i < mColorStrings.size(); i++) {
                mLayColors.addView(getView(i), i);
            }
            mLayColors.addView(getView(mLayColors.getChildCount()), mLayColors.getChildCount());
        }
    }
    //endregion vbn


    private View getView(int position) {
        final View view = new View(mContext);
        String color = "";
        if (position < mColorStrings.size())
            color = (String) mColorStrings.get(position);
        int size = ScreenUtil.convertDpToPx(mContext, getResources().getInteger(R.integer.thirty_six));
        int margin = ScreenUtil.convertDpToPx(mContext, getResources().getInteger(R.integer.fourteen));

        view.setLayoutParams(getLayoutParams(size, margin));

        if (mSelectedColor.equalsIgnoreCase(color)) {
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.scrollTo((int) view.getX(), 0);
                }
            }, 10);

            mSelectedColorView = view;
            setUpSelectedView(view);
        } else {
            setUpUnSelectedView(view, color);
        }

        if (position == mColorStrings.size()) {
            view.setBackground(addDrawable);
        }

        view.setId(100 + position);
        view.setTag(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getBackground() != addDrawable) {
                    if (mSelectedColorView != null) {
                        setUpUnSelectedView(mSelectedColorView, mSelectedColor);
                    }
                    mSelectedColor = (String) mColorStrings.get((int) v.getTag());
                    mSelectedColorView = v;
                    FTApp.getPref().save(SystemPref.RECENT_INPUT_TEXT_COLOR, mSelectedColor);
                    setUpSelectedView(view);
                    mColorPickerListener.onColorSelected(mSelectedColor);
                } else {
                    //hideKeyboard(view.getWindowToken());
                    FTEditColorsFragment.newInstance("FTDefaultPenRack", new FTEditColorsFragment.FTEditColorsContainerCallback() {
                        @Override
                        public synchronized void addColor(String color, int position) {
                            if (position != mColorStrings.size() && mLayColors.getChildCount() >= position && mLayColors.getChildAt(position) != null) {
                                mLayColors.removeViewAt(position);
                            }
                            if (position > mColorStrings.size()) {
                                position = mColorStrings.size();
                            }
                            mColorStrings.add(position, color.contains("#") ? color.split("#")[1] : color);
                            mLayColors.addView(getView(position), position);
                        }

                        @Override
                        public synchronized void fetchColors() {
                            loadColors();
                            setAdapter();
                        }

                        @Override
                        public synchronized void removeColor(String removedColor) {
                            int position = mColorStrings.indexOf(removedColor);
                            if (position >= 0) {
                                mColorStrings.remove(position);
                                mLayColors.removeViewAt(position);
                            }
                        }

                        @Override
                        public synchronized void reorder(int fromPos, int toPos) {
                            View view = mLayColors.getChildAt(fromPos);
                            mLayColors.removeViewAt(fromPos);
                            mLayColors.addView(view, toPos);
                        }
                    }).show(view, getChildFragmentManager());
                }
            }
        });

        return view;
    }

    private void loadColors() {
        if (this.ftnPenRack == null) this.ftnPenRack = FTNPenRack.getInstance();
        HashMap<String, Object> mPenrackData = this.ftnPenRack.getPenRackData();
        mColorStrings.clear();
        final Object[] mColors = (Object[]) ((HashMap<String, Object>) mPenrackData.get("FTDefaultPenRack")).get("currentColors");
        for (Object mColor : mColors) {
            mColorStrings.add(mColor.toString());
        }
    }

    private LinearLayout.LayoutParams getLayoutParams(int size, int margin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.rightMargin = margin;
        return layoutParams;
    }

    //region Update Data
    private void setUpSelectedView(View view) {
        updateView(view, R.drawable.selected_color_bg, mSelectedColor, 0);
    }
    //endregion

    private void setUpUnSelectedView(View view, String color) {
        updateView(view, R.drawable.circular_grey_bg, color, 0);
    }

    private void updateView(View view, int bgDrawableId, String color, int position) {
        view.setBackground(mContext.getDrawable(bgDrawableId));
        DrawableUtil.setGradientDrawableColor(view, mContext.getString(R.string.set_color, color), position);
    }

    private void hideKeyboard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    @Override
    public void addColorToRack(String color, int position) {
        if (color.length() > 0 && !mColorStrings.contains(color.substring(1))) {
            mColorStrings.add(mColorStrings.size() - 1, color.substring(1));
            mCustomColors.add(color.substring(1));
            if (mColorStrings != null) {
                mLayColors.addView(getView(mColorStrings.size() - 2), mColorStrings.size() - 2);
            }
        }
    }

    @Override
    public boolean isColorExistsInRack(String color) {
        return false;
    }

    @Override
    public void onBackClicked() {
        FTApp.getPref().save(SystemPref.CUSTOM_TEXT_COLORS, TextUtils.join(",", mCustomColors));
    }

    public interface ColorPickerListener {
        void onColorSelected(String color);
    }
    //endregion
}