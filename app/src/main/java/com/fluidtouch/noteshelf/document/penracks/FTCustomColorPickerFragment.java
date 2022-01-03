package com.fluidtouch.noteshelf.document.penracks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ColorUtil;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 06/05/19
 */
public class FTCustomColorPickerFragment extends FTBaseDialog.Popup implements ColorPickerView.OnColorChangedListener {
    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6})$";
    @BindView(R.id.custom_color_picker_back_image_view)
    protected ImageView image_back;
    @BindView(R.id.custom_color_picker_selected_hex_color_view)
    protected View mSelectedColorView;
    @BindView(R.id.custom_color_picker_selected_hex_color_text_view)
    protected EditText mSelectedColorEditText;
    @BindView(R.id.custom_color_picker_add_text_view)
    protected TextView mAddColorTextView;
    @BindView(R.id.custom_color_picker_picker_view)
    protected ColorPickerView mPickerView;
    @BindView(R.id.txtColorError)
    protected TextView txtColorError;
    private boolean mIsNew;
    private int mPosition;
    private String mLastSelectedColor;
    private FTCustomColorPickerFragment.ColorPickerContainerCallback mContainerCallback;
    private boolean isPenrack;

    public static FTCustomColorPickerFragment newInstance(String lastSelectedColor, int position, boolean isNew, boolean isPenrack, FTCustomColorPickerFragment.ColorPickerContainerCallback containerCallback) {
        FTCustomColorPickerFragment fragment = new FTCustomColorPickerFragment();
        fragment.mLastSelectedColor = lastSelectedColor;
        fragment.mContainerCallback = containerCallback;
        fragment.mPosition = position;
        fragment.mIsNew = isNew;
        fragment.isPenrack = isPenrack;
        return fragment;
    }

    int prevDialogPos;
    private final Observer onKeyboardHeightChangedObserver = (observable, o) -> {
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            if (window != null) {
                int keyboardHeight = (int) o;
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                if (keyboardHeight == 0) {
                    layoutParams.y = prevDialogPos;
                } else {
                    prevDialogPos = layoutParams.y;
                    layoutParams.y = 0;
                }
                window.setAttributes(layoutParams);
            }
        }
    };

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
                if (!isPenrack) {
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
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
        return inflater.inflate(R.layout.custom_color_picker_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        LinearLayout dialogLayout = view.findViewById(R.id.custom_color_dialog_layout);
        ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) dialogLayout.getLayoutParams();
        if (!isMobile() && !isPenrack) {
            layoutParams.width = ScreenUtil.convertDpToPx(getContext(), 376);
            layoutParams.height = ScreenUtil.convertDpToPx(getContext(), 427);
        }
        if (isMobile() || !isPenrack) {
            layoutParams.height = ScreenUtil.convertDpToPx(getContext(), 427);
        }
        dialogLayout.setLayoutParams(layoutParams);

        ObservingService.getInstance().addObserver("onKeyboardHeightChanged", onKeyboardHeightChangedObserver);

        //Initialization
        mPickerView.setColor(Color.parseColor(mLastSelectedColor));
        mSelectedColorEditText.setText(mLastSelectedColor);
        setUpSelectedColorView(Color.parseColor(mLastSelectedColor));
        setUpAddColorTextView(R.drawable.single_line_corner, Color.parseColor("#4aa1ff"),
                mContainerCallback.isColorExistsInRack(mLastSelectedColor) ? getString(R.string.tickmark) : getString(R.string.add));

        mPickerView.setOnColorChangedListener(this);

        mSelectedColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String color = mSelectedColorEditText.getText().toString();
                Pattern colorPattern = Pattern.compile(HEX_PATTERN);
                Matcher m = colorPattern.matcher(color);
                boolean isColor = m.matches();
                txtColorError.setVisibility(View.INVISIBLE);
                if (isColor) {
                    FTFirebaseAnalytics.logEvent("ColorPicker_Code");
                    mPickerView.setColor(Color.parseColor(color));
                    setUpSelectedColorView(Color.parseColor(color));
                    mAddColorTextView.setEnabled(true);
                    setUpAddColorTextView(R.drawable.single_line_corner, Color.parseColor("#4aa1ff"),
                            mContainerCallback.isColorExistsInRack(color) ? getString(R.string.tickmark) : getString(R.string.add));
                } else {
                    mAddColorTextView.setEnabled(false);
                    txtColorError.setVisibility(View.VISIBLE);
                }
            }
        });
        if (isChildFragment() && !isMobile()) {
            FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), true, () -> getParentFragment().getView().setVisibility(View.GONE));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSelectedColorEditText.getWindowToken(), 0);
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
            if (!isPenrack && atView != null) {
                int[] location = new int[2];
                atView.getLocationOnScreen(location);
                int sourceX = location[0];
                int sourceY = location[1];
                window.setGravity(Gravity.TOP | Gravity.START);
                layoutParams.x = sourceX + atView.getWidth() / 2;
                layoutParams.y = sourceY + atView.getHeight() / 2;
            } else {
                Dialog parentDialog = getParentDialog();
                if (parentDialog != null) {
                    layoutParams.x = parentDialog.getWindow().getAttributes().x;
                    layoutParams.y = parentDialog.getWindow().getAttributes().y;
                }
            }
        }
        window.setAttributes(layoutParams);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        getParentDialog().dismiss();
    }

    @OnClick(R.id.custom_color_picker_back_image_view)
    void closeDialog() {
        dismiss();
    }

    @OnClick(R.id.custom_color_picker_add_text_view)
    void addColorToPenRack() {
        if (txtColorError.getVisibility() == View.INVISIBLE) {
            String color = mSelectedColorEditText.getText().toString();
            if (mContainerCallback.isColorExistsInRack(color)) {
                return;
            }
            FTFirebaseAnalytics.logEvent("ColorPicker_Add");
            setUpAddColorTextView(R.drawable.custom_color_add_selected_bg, Color.parseColor("#FFFFFF"), getString(R.string.tickmark));
            mContainerCallback.addColorToRack(color, mIsNew ? mPosition++ : mPosition);
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        String color = getString(R.string.set_color, String.format("%06X", (0xFFFFFF & newColor)));
        mSelectedColorEditText.setText(color);
    }

    private void setUpAddColorTextView(int background, int textColor, String text) {
        mAddColorTextView.setBackgroundResource(background);
        mAddColorTextView.setTextColor(textColor);
        mAddColorTextView.setText(text);
    }

    private void setUpSelectedColorView(int color) {
        GradientDrawable background = (GradientDrawable) mSelectedColorView.getBackground();
        background.setColor(color);
        background.setStroke(ColorUtil.isLightColor(color) ? 1 : 0, Color.parseColor("#383838"));
    }

    public interface ColorPickerContainerCallback {
        void addColorToRack(String color, int position);

        boolean isColorExistsInRack(String color);

        void onBackClicked();
    }
}
