package com.fluidtouch.noteshelf.shelf.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu on 06/08/20.
 */
public class FTShelfNotebookOptionsTabletFragment extends FTShelfNotebookOptionsFragment {
    @BindView(R.id.frag_shelf_bottom_options_share_cover_text_view)
    TextView coverStyleTextView;
    @BindView(R.id.frag_shelf_bottom_options_rename_text_view)
    TextView renameTextView;
    @BindView(R.id.frag_shelf_bottom_options_group_text_view)
    TextView groupTextView;

    public static FTShelfNotebookOptionsTabletFragment newInstance(int count, boolean isInsideGroup) {
        FTShelfNotebookOptionsTabletFragment fragment = new FTShelfNotebookOptionsTabletFragment();
        fragment.isInsideGroup = isInsideGroup;
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_COUNT, count);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        updateLayout(mCount);
    }

    @OnClick(R.id.frag_shelf_bottom_options_share_cover_text_view)
    public void showCoverPickerDialog() {
        super.showCoverPickerDialog();
    }

    @OnClick(R.id.frag_shelf_bottom_options_rename_text_view)
    void renameSelectedItems() {
        super.renameItems();
    }

    @OnClick(R.id.frag_shelf_bottom_options_group_text_view)
    void groupSelectedItems() {
        super.groupItems();
    }

    @Override
    void updateActions(float alpha, boolean isClickable) {
        super.updateActions(alpha, isClickable);

        if (FTApp.getPref().getRecentCollectionName().equalsIgnoreCase(getString(R.string.trash))) {
            coverStyleTextView.setAlpha(0.2f);
            coverStyleTextView.setClickable(false);
        } else {
            coverStyleTextView.setAlpha(alpha);
            coverStyleTextView.setClickable(isClickable);
        }
        if (FTApp.getPref().getRecentCollectionName().equalsIgnoreCase(getString(R.string.trash))) {
            groupTextView.setAlpha(0.2f);
            groupTextView.setClickable(false);
        } else {
            if (isInsideGroup) {
                groupTextView.setAlpha(0.2f);
            } else {
                groupTextView.setAlpha(alpha);
                groupTextView.setClickable(isClickable);
            }
        }

        renameTextView.setAlpha(alpha);
        renameTextView.setClickable(isClickable);
    }
}
