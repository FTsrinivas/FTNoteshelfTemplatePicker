package com.fluidtouch.noteshelf.document.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.DateUtil;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTGetInfoPopup extends FTBaseDialog.Popup {
    @BindView(R.id.tvTitle)
    TextView mTitleTextView;
    @BindView(R.id.tvCreated)
    TextView mCreatedTextView;
    @BindView(R.id.tvUpdated)
    TextView mUpdatedTextView;
    @BindView(R.id.tvCategory)
    TextView mCategoryTextView;
    @BindView(R.id.tvPage)
    TextView mPageTextView;
    @BindView(R.id.tvPageCreated)
    TextView mPageCreatedTextView;
    @BindView(R.id.tvPageUpdated)
    TextView mPageUpdatedTextView;
    private static String dateFormat = "dd/MM/yyyy hh:mm a";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.END);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_get_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        FTNoteshelfPage page = getCurrentPage();
        if (page != null) {
            FTNoteshelfDocument document = page.getParentDocument();
            mTitleTextView.setText(document.getDisplayTitle(getContext()));
            Date creationDate = document.getFileCreationDate();
            if (creationDate != null) {
                mCreatedTextView.setVisibility(View.VISIBLE);
                mCreatedTextView.setText(new SimpleDateFormat(dateFormat).format(creationDate));
            } else {
                mCreatedTextView.setVisibility(View.GONE);
            }
            mUpdatedTextView.setText(new SimpleDateFormat(dateFormat).format(document.getFileModificationDate()));
            mCategoryTextView.setText("- -");
            File documentFile = new File(document.getFileURL().getPath());
            if (documentFile.exists()) {
                if (documentFile.getParent().endsWith(FTConstants.GROUP_EXTENSION)) {
                    documentFile = new File(documentFile.getParent());
                }
                String categoryName = FTDocumentUtils.getFileName(getContext(), FTUrl.parse(documentFile.getParent()))
                        .replace(FTConstants.SHELF_EXTENSION, "");
                mCategoryTextView.setText(categoryName);
            }
            mPageTextView.setText(String.valueOf(page.pageIndex() + 1));
            mPageCreatedTextView.setText(DateUtil.getDateAndTime(dateFormat, page.creationDate));
            mPageUpdatedTextView.setText(DateUtil.getDateAndTime(dateFormat, page.lastUpdated));
        }
    }

    @OnClick(R.id.dialog_title)
    void onBackClicked() {
        dismiss();
    }

    public FTNoteshelfPage getCurrentPage() {
        FTNoteshelfPage currentPage = null;
        if (getActivity() != null) {
            currentPage = ((FTDocumentActivity) getActivity()).getCurrentPage();
        }
        return currentPage;
    }
}