package com.fluidtouch.noteshelf.store.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.store.adapter.FTChooseCoverPaperAdapter;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTDiaryDatePickerPopup extends FTBaseDialog.Popup {
    @BindView(R.id.start_date_picker)
    DatePicker startDatePicker;
    @BindView(R.id.end_date_picker)
    DatePicker endDatePicker;

    @BindView(R.id.closeTabsBtn)
    ImageButton closeTabsBtn;

    private FTNTheme mTheme;

    private int mSelectedStartMonth;
    private int mSelectedStartYear;
    private int mSelectedEndMonth;
    private int mSelectedEndYear;

    private DatePickerListener mListener;
    private FTTemplateDetailedInfoAdapter mFTTemplateDetailedInfoAdapterListener;
    private Locale locale;

    public FTDiaryDatePickerPopup(FTNTheme theme, FTChooseCoverPaperAdapter listener) {
        super();
        this.mTheme = theme;
        this.mListener = listener;
    }

    public FTDiaryDatePickerPopup(FTNTheme item, FTTemplateDetailedInfoAdapter ftTemplateDetailedInfoAdapter) {
        super();
        this.mTheme = item;
        this.mFTTemplateDetailedInfoAdapterListener = ftTemplateDetailedInfoAdapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (isMobile()) {
            ((BottomSheetDialog) dialog).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.START);
            }
        }
        super.dismissParent = false;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_diary_date_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        startDatePicker.findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        endDatePicker.findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);

        locale = getContext().getResources().getConfiguration().getLocales().get(0);

        Calendar calendar = new GregorianCalendar(locale);
        mSelectedStartYear = calendar.get(Calendar.YEAR);
        mSelectedStartMonth = calendar.get(Calendar.MONTH);

        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, -1);
        mSelectedEndYear = calendar.get(Calendar.YEAR);
        mSelectedEndMonth = calendar.get(Calendar.MONTH);

        startDatePicker.init(mSelectedStartYear, mSelectedStartMonth, 1, (view12, year, monthOfYear, dayOfMonth) -> {
            mSelectedStartYear = year;
            mSelectedStartMonth = monthOfYear;
        });
        endDatePicker.init(mSelectedEndYear, mSelectedEndMonth, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), (view12, year, monthOfYear, dayOfMonth) -> {
            mSelectedEndYear = year;
            mSelectedEndMonth = monthOfYear;
        });
    }

    @OnClick(R.id.closeTabsBtn)
    void onCloseBtnClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        Locale locale = getResources().getConfiguration().getLocales().get(0);
        Calendar startDate = new GregorianCalendar(locale);
        startDate.set(mSelectedStartYear, mSelectedStartMonth, 1);
        Calendar endDate = new GregorianCalendar(locale);
        endDate.set(mSelectedEndYear, mSelectedEndMonth, 1);
        endDate.set(Calendar.DATE, endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        if (endDate.before(startDate)) {
            showAlertDialog(R.string.start_date_eariler_than_end_date);
        } else if (monthsBetween(startDate.getTime(), endDate.getTime()) > 12) {
            showAlertDialog(R.string.more_than_twelve_months_gap);
        } else {
            if (mListener != null) {
                FTApp.getPref().saveDiaryRecentStartDate(startDate.getTime());
                FTApp.getPref().saveDiaryRecentEndDate(endDate.getTime());
                mListener.onDatesSelected(mTheme, startDate.getTime(), endDate.getTime());
                dismiss();
            }

            if (mFTTemplateDetailedInfoAdapterListener != null) {
                FTApp.getPref().saveDiaryRecentStartDate(startDate.getTime());
                FTApp.getPref().saveDiaryRecentEndDate(endDate.getTime());
                mFTTemplateDetailedInfoAdapterListener.onDatesSelected(mTheme, startDate.getTime(), endDate.getTime());
                dismiss();
            }

            dismiss();
        }
    }

    private void showAlertDialog(int messageResId) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(messageResId);
        alertDialog.setPositiveButton(R.string.ok, (dialog, which) -> {
        });
        alertDialog.create();
        alertDialog.show();
    }

    private int monthsBetween(Date startDate, Date endDate) {
        Calendar start = new GregorianCalendar(locale);
        start.setTime(startDate);
        Calendar end = new GregorianCalendar(locale);
        end.setTime(endDate);

        int year = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int months = (end.get(Calendar.MONTH) + 1) - ((start.get(Calendar.MONTH) + 1));
        if (end.get(Calendar.DAY_OF_MONTH) < (start.get(Calendar.DAY_OF_MONTH))) {
            months--;
        }
        return Math.abs(months + (year * 12) + 1);
    }

    public interface DatePickerListener {
        void onDatesSelected(FTNTheme theme, Date startDate, Date endDate);
    }
}
