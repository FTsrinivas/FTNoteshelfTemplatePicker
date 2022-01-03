package com.fluidtouch.noteshelf.commons.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTExportFormatPopup extends FTBaseDialog.Popup {
    @BindView(R.id.export_format_png_text_view)
    TextView mPNGTextView;
    @BindView(R.id.export_format_pdf_text_view)
    TextView mPDFTextView;
    @BindView(R.id.export_format_nsa_text_view)
    TextView mNSATextView;
    @BindView(R.id.export_format_nsa_divider)
    View mNSADivider;
    @BindView(R.id.export_format_pdf_divider)
    View mPDFDivider;
    @BindView(R.id.export_format_png_divider)
    View mPNGDivider;
    @BindView(R.id.dialog_back_button)
    ImageView mBackButton;

    private OnShareAsNSAListener nsaListener;
    private OnShareAsPDFListener pdfListener;
    private OnShareAsPNGListener pngListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_export_format, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                if (nsaListener == null) {
                    window.setGravity(Gravity.TOP | Gravity.END);
                } else {
                    window.setGravity(Gravity.BOTTOM | Gravity.START);
                }
            }
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            pngListener = (OnShareAsPNGListener) savedInstanceState.getSerializable("pngListener");
            pdfListener = (OnShareAsPDFListener) savedInstanceState.getSerializable("pdfListener");
            nsaListener = (OnShareAsNSAListener) savedInstanceState.getSerializable("nsaListener");
        }

        mBackButton.setVisibility(nsaListener == null ? View.VISIBLE : View.INVISIBLE);

        mPNGDivider.setVisibility(pngListener == null ? View.GONE : View.VISIBLE);
        mPNGDivider.setVisibility(pngListener == null ? View.GONE : View.VISIBLE);
        mPDFTextView.setVisibility(pdfListener == null ? View.GONE : View.VISIBLE);
        mPDFDivider.setVisibility(pdfListener == null ? View.GONE : View.VISIBLE);
        mNSATextView.setVisibility(nsaListener == null ? View.GONE : View.VISIBLE);
        mNSADivider.setVisibility(nsaListener == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("pngListener", pngListener);
        outState.putSerializable("pdfListener", pdfListener);
        outState.putSerializable("nsaListener", nsaListener);
    }

    public void showNSAOption(OnShareAsNSAListener listener) {
        this.nsaListener = listener;
    }

    public void showPDFOption(OnShareAsPDFListener listener) {
        this.pdfListener = listener;
    }

    public void showPNGOption(OnShareAsPNGListener listener) {
        this.pngListener = listener;
    }

    @OnClick(R.id.export_format_nsa_text_view)
    void onShareAsNSAClicked() {
        if (nsaListener != null && mNSATextView.getVisibility() == View.VISIBLE) {
            FTFirebaseAnalytics.logEvent("ShareFormat_Noteshelf");
            FTLog.crashlyticsLog("UI: Clicked share notebook as .nsa");
            FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.NSA_EXTENSION);

            nsaListener.shareAsNSA();
        }
        dismiss();
    }

    @OnClick(R.id.export_format_pdf_text_view)
    void onShareAsPDFClicked() {
        if (pdfListener != null && mPDFTextView.getVisibility() == View.VISIBLE) {
            FTFirebaseAnalytics.logEvent("ShareFormat_Png");
            FTLog.crashlyticsLog("UI: Clicked share notebook as .png");
            FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.PDF_EXTENSION);

            pdfListener.shareAsPDF();
        }
        dismiss();
    }

    @OnClick(R.id.export_format_png_text_view)
    void onShareAsPNGClicked() {
        if (pngListener != null && mPNGTextView.getVisibility() == View.VISIBLE) {
            FTFirebaseAnalytics.logEvent("ShareFormat_Pdf");
            FTLog.crashlyticsLog("UI: Clicked share notebook as .png");
            FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.PNG_EXTENSION);

            pngListener.shareAsPNG();
        }
        dismiss();
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    public interface OnShareAsNSAListener extends Serializable {
        void shareAsNSA();
    }

    public interface OnShareAsPDFListener extends Serializable {
        void shareAsPDF();
    }

    public interface OnShareAsPNGListener extends Serializable {
        void shareAsPNG();
    }
}