package com.fluidtouch.noteshelf.shelf.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;

public class FTShelfMoveToFragment extends Fragment implements FTShelfMoveToAdapter.FTShelfMoveToAdapterCallback {
    @BindView(R.id.shelf_move_recycler_view)
    RecyclerView groupRecyclerView;
    @BindView(R.id.shelf_move_new_group_edit_text)
    EditText mNewGroupEditText;
    @BindView(R.id.category_title_text_view)
    TextView mCategoryTextView;

    private FTShelfItemCollection mSelectedCategory;
    private FTGroupItem mOpenedGroupInPanel;
    private List<FTShelfItem> mSelectedShelfItems;
    private FTCategoryMoveToFragment.OnMovingItemsListener listener;
    private FTShelfMoveToAdapter moveToShelfAdapter;
    private FTShelfItemCollection shelfItemCollection;
    private FTGroupItem groupItem;
    private String mUserInputText;

    private final FTSmartDialog smartDialog = new FTSmartDialog();

    public static FTShelfMoveToFragment newInstance(FTGroupItem groupItem, FTShelfItemCollection ftShelfItemCollection, List<FTShelfItem> selectedItems, FTCategoryMoveToFragment.OnMovingItemsListener listener) {
        FTShelfMoveToFragment moveToShelfFragment = new FTShelfMoveToFragment();
        moveToShelfFragment.shelfItemCollection = ftShelfItemCollection;
        moveToShelfFragment.mSelectedCategory = ftShelfItemCollection;
        moveToShelfFragment.mSelectedShelfItems = selectedItems;
        moveToShelfFragment.listener = listener;
        moveToShelfFragment.groupItem = groupItem;
        moveToShelfFragment.mOpenedGroupInPanel = groupItem;
        return moveToShelfFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moveto_shelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        FTAnimationUtils.showStartPanelAnimation(getContext(), view, true, null);

        if (groupItem != null) {
            mNewGroupEditText.setVisibility(GONE);
            mCategoryTextView.setText(this.groupItem.getDisplayTitle(getContext()));
        } else {
            mCategoryTextView.setText(this.shelfItemCollection.getDisplayTitle(getContext()));
        }

        moveToShelfAdapter = new FTShelfMoveToAdapter(this, mSelectedShelfItems.get(0).parent, false);
        shelfItemCollection.shelfItems(getContext(), FTShelfSortOrder.BY_NAME, groupItem, "", (notebooks, error) -> {
            Collections.sort(notebooks, (first, second) -> first.getDisplayTitle(getContext()).compareToIgnoreCase(second.getDisplayTitle(getContext())));
            moveToShelfAdapter.addAll(notebooks);
        });
        groupRecyclerView.setAdapter(moveToShelfAdapter);

        mNewGroupEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (TextUtils.isEmpty(mUserInputText))
                mUserInputText = mNewGroupEditText.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleUserInput();
                return true;
            }
            return false;
        });
        mNewGroupEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserInputText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mNewGroupEditText.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) mUserInputText = getString(R.string.group);
        });
    }

    @OnClick(R.id.shelf_move_back)
    void onBackButtonClicked() {
        FTAnimationUtils.showStartPanelAnimation(getContext(), getView(), false, () -> {
            if (getFragmentManager() != null) {
                getFragmentManager().beginTransaction().remove(FTShelfMoveToFragment.this).commitAllowingStateLoss();
            }
        });
    }

    @OnClick(R.id.shelf_move_panel_two_move_text_view)
    void onMoveButtonClicked() {
        handleUserInput();
    }

    @Override
    public void showInGroupPanel(FTGroupItem ftGroupItem) {
        getChildFragmentManager().beginTransaction().addToBackStack(getTag())
                .replace(R.id.shelf_move_panel_two_child_fragment, FTShelfMoveToFragment.newInstance(ftGroupItem, shelfItemCollection, mSelectedShelfItems, listener))
                .commitAllowingStateLoss();
    }

    @Override
    public void onNotebookClicked(FTShelfItem document) {
        //This method is not used here.
    }

    /**
     * If new group does not exist and user input is not empty then,
     * creates a new group and moves all selected notebook into it, else,
     * moves them into the opened category in panel.
     **/
    private void handleUserInput() {
        if (!mSelectedShelfItems.isEmpty()) {
            if (!TextUtils.isEmpty(mUserInputText)) {
                mSelectedCategory.createGroupItem(getContext(), mSelectedShelfItems, (groupItem, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), R.string.unexpected_error_occurred_please_try_again, Toast.LENGTH_SHORT).show();
                    } else {
                        moveToShelfAdapter.add(groupItem);
                        listener.onSelectedItemsMoved(mSelectedShelfItems);
                    }
                }, mUserInputText);
            } else {
                if (isTheLocationSame()) {
                    Toast.makeText(getContext(), R.string.cannot_move_to_same_location, Toast.LENGTH_LONG).show();
                } else {
                    moveShelfItems(new ArrayList<>(mSelectedShelfItems));
                }
            }
        }
    }


    /**
     * Checks if the one of the selected notebooks is being moved to the same location.
     **/
    private boolean isTheLocationSame() {
        FTShelfItem selectedNotebook = mSelectedShelfItems.get(0);
        FTGroupItem selectedNotebookGroup = selectedNotebook.getParent();
        //If group does exists, then check if path matches to avoid moving to same location.
        if (selectedNotebookGroup == null) {
            //If no group is opened, then compare the selected notebook path with category path.
            if (mOpenedGroupInPanel == null) {
                return selectedNotebook.getShelfCollection().getFileURL().equals(mSelectedCategory.getFileURL());
            } else {
                return false;
            }
        } else {
            //Check If selected and group paths are equal, to avoid moving to same location.
            if (mOpenedGroupInPanel == null) {
                return false;
            } else {
                return selectedNotebookGroup.getFileURL().equals(mOpenedGroupInPanel.getFileURL());
            }
        }
    }

    /**
     * Moves the selected notebooks to the selected location.
     *
     * @param shelfItems array of user selected notebooks.
     **/
    private void moveShelfItems(final List<FTShelfItem> shelfItems) {
        final int position = 0;
        if (shelfItems.size() > 0) {
            smartDialog.setMessage(getString(R.string.moving));
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
            smartDialog.show(getChildFragmentManager());
            mSelectedCategory.moveShelfItem(shelfItems.get(position), mOpenedGroupInPanel, (groupItem, error) -> {
                if (error == null) {
                    shelfItems.remove(position);
                    moveShelfItems(shelfItems);
                }
            }, getContext());
        } else {
            if (smartDialog.isAdded()) smartDialog.dismiss();
            listener.onSelectedItemsMoved(mSelectedShelfItems);
        }
    }
}