package com.fluidtouch.noteshelf.shelf.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.shelf.FTShelfNotebookMoreOptionsDialog;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTShelfNotebookOptionsFragment extends Fragment implements FTChooseCoverPaperDialog.CoverChooseListener,
        FTShelfNotebookMoreOptionsDialog.FTShelfNotebookMoreOptionsParentListener, FTChoosePaperTemplate.TemplatePaperChooseListener {
    protected static final String ARG_COUNT = "count";

    @BindView(R.id.frag_shelf_bottom_options_share_text_view)
    TextView shareTextView;
    @BindView(R.id.frag_shelf_bottom_options_duplicate_text_view)
    TextView duplicateTextView;
    @BindView(R.id.frag_shelf_bottom_options_move_text_view)
    TextView moveTextView;
    @BindView(R.id.frag_shelf_bottom_options_delete_text_view)
    TextView deleteTextView;
    @BindView(R.id.frag_shelf_bottom_options_more_text_view)
    TextView moreTextView;

    protected boolean isInsideGroup;

    private ShelfNotbookOptionsFragmentInteractionListener mFragmentListener;
    int mCount;

    public static FTShelfNotebookOptionsFragment newInstance(boolean isTablet, int count, boolean isInsideGroup) {
        if (isTablet) {
            return FTShelfNotebookOptionsTabletFragment.newInstance(count, isInsideGroup);
        }
        FTShelfNotebookOptionsFragment fragment = new FTShelfNotebookOptionsFragment();
        fragment.isInsideGroup = isInsideGroup;
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_COUNT, count);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCount = getArguments().getInt(ARG_COUNT);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shelf_bottom_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        updateLayout(mCount);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof ShelfEditModeToolbarOverlayFragment.OnShelfEditModeToolbarFragmentInteractionListener) {
            mFragmentListener = (ShelfNotbookOptionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnShelfSearchFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentListener = null;
    }

    //region View On action callbacks
    @OnClick(R.id.frag_shelf_bottom_options_delete_text_view)
    void deleteSelectedShelfItems(View view) {
        FTLog.crashlyticsLog("UI: Clicked delete notebooks");
        mFragmentListener.deleteInEditMode(view);
    }
    //endregion

    @OnClick(R.id.frag_shelf_bottom_options_duplicate_text_view)
    void duplicateSelectedShelfItems() {
        FTLog.crashlyticsLog("UI: Clicked duplicate notebooks");
        mFragmentListener.duplicateInEditMode();
    }

    @OnClick(R.id.frag_shelf_bottom_options_move_text_view)
    void moveSelectedShelfItems() {
        FTLog.crashlyticsLog("UI: Clicked move notebooks");
        mFragmentListener.moveInEditMode();
    }

    @OnClick(R.id.frag_shelf_bottom_options_share_text_view)
    void shareSelectedShelfItems(View view) {
        FTLog.crashlyticsLog("UI: Clicked share notebook");
        if (mCount == 1) {
            mFragmentListener.shareInEditMode(view);
        } else if (mCount > 1) {
            Toast.makeText(getContext(), R.string.share_more_items_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showCoverPickerDialog() {
        FTLog.crashlyticsLog("UI: Clicked choose cover for notebook");
        //FTChooseCoverPaperDialog.newInstance(FTNThemeCategory.FTThemeType.COVER).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());
        //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.COVER).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

        FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
        _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
        _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(null);
        _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.COVER);
        _ftTemplatepickerInputInfo.set_notebookTitle(null);
         FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

    }

    @Override
    public void renameItems() {
        FTRenameDialog.newInstance(FTRenameDialog.RenameType.RENAME_NOTEBOOK,
                "", 0, new FTRenameDialog.RenameListener() {
                    @Override
                    public void renameShelfItem(String updatedName, int position, DialogFragment dialogFragment) {
                        dialogFragment.dismissAllowingStateLoss();
                        mFragmentListener.renameSelectedItems(updatedName);
                    }

                    @Override
                    public void dialogActionCancel() {
                    }
                }).show(getActivity().getSupportFragmentManager(), FTRenameDialog.class.getName());
        showKeyboard();
    }

    @Override
    public void groupItems() {
        if (isInsideGroup) {
            Toast.makeText(getContext(), R.string.group_inside_group_error, Toast.LENGTH_LONG).show();
            return;
        } else if (mCount > 0) {
            FTRenameDialog.newInstance(FTRenameDialog.RenameType.NEW_GROUP,
                    "", 0, new FTRenameDialog.RenameListener() {
                        @Override
                        public void renameShelfItem(String groupName, int position, DialogFragment dialogFragment) {
                            mFragmentListener.groupSelectedItems(groupName);
                        }

                        @Override
                        public void dialogActionCancel() {
                        }
                    }).show(getActivity().getSupportFragmentManager(), "FTRenameDialog");
            showKeyboard();
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @OnClick(R.id.frag_shelf_bottom_options_more_text_view)
    void onMoreClicked() {
        boolean isTrash = FTApp.getPref().getRecentCollectionName().equalsIgnoreCase(getString(R.string.trash));
        FTShelfNotebookMoreOptionsDialog.newInstance(this, isInsideGroup, isTrash).show(getActivity().getSupportFragmentManager());
    }

    //region Helper methods
    public void updateLayout(int count) {
        mCount = count;
        updateActions(count == 0 ? 0.2f : 1.0f, count != 0);

        if (count == 1) {
            shareTextView.setAlpha(1.0f);
        } else if (count > 1) {
            shareTextView.setAlpha(0.2f);
        }

        requireView().setOnClickListener(null);
    }
    //endregion

    void updateActions(float alpha, boolean isClickable) {
        if (FTApp.getPref().getRecentCollectionName().equalsIgnoreCase(getString(R.string.trash))) {
            deleteTextView.setAlpha(0.2f);
            duplicateTextView.setAlpha(0.2f);
            deleteTextView.setClickable(false);
            duplicateTextView.setClickable(false);
        } else {
            deleteTextView.setAlpha(alpha);
            duplicateTextView.setAlpha(alpha);
            deleteTextView.setClickable(isClickable);
            duplicateTextView.setClickable(isClickable);
        }

        shareTextView.setAlpha(alpha);
        moveTextView.setAlpha(alpha);
        moreTextView.setAlpha(alpha);

        moveTextView.setClickable(isClickable);
        moreTextView.setClickable(isClickable);
    }

    //region Custom listeners call back methods
    @Override
    public void onThemeChosen(FTNTheme theme, boolean isCurrentPage,boolean isLandscapeStatus) {
        Log.d("TemplatePicker==>","Mani onThemeChosen Template Selected action FTShelfNotebookOptionsFragment isLandscape Status::-"+isLandscapeStatus+" theme.isLandscape::-"+theme.isLandscape);
        //mFragmentListener.coverStyleInEditMode((FTNCoverTheme) theme);
        mFragmentListener.coverStyleInEditMode(theme);
    }
    //endregion

    @Override
    public void addCustomTheme(FTNTheme theme) {
        mFragmentListener.addCustomTheme(theme);
    }

    @Override
    public void onClose() {

    }

    @Override
    public boolean isCurrentTheme() {
        return false;
    }

    public interface ShelfNotbookOptionsFragmentInteractionListener {
        void disableEditMode();

        void duplicateInEditMode();

        void moveInEditMode();

        void deleteInEditMode(View view);

        void shareInEditMode(View view);

        //void coverStyleInEditMode(FTNCoverTheme selectedTheme);
        void coverStyleInEditMode(FTNTheme selectedTheme);

        void addCustomTheme(FTNTheme theme);

        void renameSelectedItems(String updatedName);

        void groupSelectedItems(String groupName);
    }
}
