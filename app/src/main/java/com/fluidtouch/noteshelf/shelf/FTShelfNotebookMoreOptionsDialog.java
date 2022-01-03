package com.fluidtouch.noteshelf.shelf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf2.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu on 07/08/20.
 */
public class FTShelfNotebookMoreOptionsDialog extends FTBaseDialog {
    @BindView(R.id.frag_shelf_bottom_options_group_text_view)
    TextView groupTextView;
    private FTShelfNotebookMoreOptionsParentListener listener;
    private boolean isInsideGroup;
    private boolean isTrash;

    public static FTShelfNotebookMoreOptionsDialog newInstance(FTShelfNotebookMoreOptionsParentListener listener, boolean isInsideGroup, boolean isTrash) {
        FTShelfNotebookMoreOptionsDialog dialog = new FTShelfNotebookMoreOptionsDialog();
        dialog.listener = listener;
        dialog.isInsideGroup = isInsideGroup;
        dialog.isTrash = isTrash;
        File file = new File("kh");
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_shelf_more_bottom_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (isTrash) {
            view.findViewById(R.id.hide_for_trash_layout).setVisibility(View.GONE);
            view.findViewById(R.id.hide_for_trash_layout2).setVisibility(View.GONE);
        }
        groupTextView.setAlpha(isInsideGroup ? 0.2f : 1.0f);
    }

    @OnClick(R.id.frag_shelf_bottom_options_group_text_view)
    void onGroupSelected() {
        dismiss();
        listener.groupItems();
    }

    @OnClick(R.id.frag_shelf_bottom_options_rename_text_view)
    void onRenameSelected() {
        dismiss();
        listener.renameItems();
    }

    @OnClick(R.id.frag_shelf_bottom_options_share_cover_text_view)
    void onCoverSelected() {
        dismiss();
        listener.showCoverPickerDialog();
    }

    @OnClick(R.id.frag_shelf_bottom_options_cancel_text_view)
    void onCancelSelected() {
        dismiss();
    }

    public interface FTShelfNotebookMoreOptionsParentListener {
        void groupItems();

        void renameItems();

        void showCoverPickerDialog();
    }
}
