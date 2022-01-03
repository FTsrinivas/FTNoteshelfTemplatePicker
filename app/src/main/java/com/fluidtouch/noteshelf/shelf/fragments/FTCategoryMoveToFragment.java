package com.fluidtouch.noteshelf.shelf.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryMoveToAdapter;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTCategoryMoveToFragment extends Fragment implements FTCategoryMoveToAdapter.FTCategoryMoveToAdapterCallback {
    @BindView(R.id.shelf_move_recycler_view)
    RecyclerView mCategoryRecyclerView;
    @BindView(R.id.shelf_move_panel_one_child_fragment)
    RelativeLayout mChildFragmentLayout;
    @BindView(R.id.category_move_new_category_edit_text)
    EditText mNewCategoryEditText;
    @BindView(R.id.category_move_text_view)
    TextView mMoveTextView;

    private List<FTShelfItem> selectedItems;
    private OnMovingItemsListener listener;
    private FTShelfCollectionProvider collectionProvider;
    private FTCategoryMoveToAdapter moveToCategoryAdapter;
    private FTShelfItemCollection selectedCategory;
    private String mUserInputText;

    private final FTSmartDialog smartDialog = new FTSmartDialog();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.listener = (OnMovingItemsListener) getActivity();
    }

    public static FTCategoryMoveToFragment newInstance(FTShelfCollectionProvider collectionProvider, List<FTShelfItem> selectedItems) {
        FTCategoryMoveToFragment ftCategoryMoveToFragment = new FTCategoryMoveToFragment();
        ftCategoryMoveToFragment.collectionProvider = collectionProvider;
        ftCategoryMoveToFragment.selectedItems = selectedItems;
        return ftCategoryMoveToFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moveto_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        mMoveTextView.setVisibility(View.INVISIBLE);

        mCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moveToCategoryAdapter = new FTCategoryMoveToAdapter(this, selectedItems.get(0).getShelfCollection(), false);
        collectionProvider.shelfs(shelfs -> {
            if (!shelfs.isEmpty()) {
                shelfs.remove(shelfs.size() - 1);
            }
            Collections.sort(shelfs, (first, second) -> first.getDisplayTitle(getContext()).compareToIgnoreCase(second.getDisplayTitle(getContext())));
            moveToCategoryAdapter.addAll(shelfs);
        });
        mCategoryRecyclerView.setAdapter(moveToCategoryAdapter);

        mNewCategoryEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(mUserInputText)) {
                handleUserInput();
                return true;
            }
            return false;
        });
        mNewCategoryEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserInputText = s.toString();
                mMoveTextView.setVisibility(TextUtils.isEmpty(mUserInputText) ? View.INVISIBLE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick(R.id.category_move_back_button)
    protected void onBackButtonClicked() {
        listener.onSelectedItemsMoved(new ArrayList<>());
    }

    @OnClick(R.id.category_move_text_view)
    void onMoveClicked() {
        handleUserInput();
    }

    @Override
    public void showInCategoryPanel(FTShelfItemCollection ftShelfItemCollection) {
        getChildFragmentManager().beginTransaction().addToBackStack(getTag())
                .replace(R.id.shelf_move_panel_one_child_fragment, FTShelfMoveToFragment.newInstance(null, ftShelfItemCollection, selectedItems, listener))
                .commitAllowingStateLoss();
    }

    private void handleUserInput() {
        if (!TextUtils.isEmpty(mUserInputText)) {
            if (mUserInputText.equalsIgnoreCase(getString(R.string.trash))) {
                FTDialogFactory.showAlertDialog(getContext(), "", getString(R.string.cannot_use_trash_as_it_is_reserved_by_the_app), "", getString(R.string.ok), null);
            } else {
                collectionProvider.currentProvider().createShelfWithTitle(getContext(), mUserInputText, (shelf, error) -> {
                    if (error == null) {
                        moveToCategoryAdapter.add(shelf);
                        selectedCategory = shelf;
                        moveItems(new ArrayList<>(selectedItems));
                    }
                });
            }
        }
    }

    private void moveItems(final ArrayList<FTShelfItem> items) {
        final int position = 0;
        if (items.size() > 0) {
            smartDialog.setMessage(getString(R.string.moving));
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
            smartDialog.show(getChildFragmentManager());
            selectedCategory.moveShelfItem(items.get(position), null, (groupItem, error) -> {
                if (error == null) {
                    items.remove(position);
                    moveItems(items);
                }
            }, getContext());
        } else {
            if (smartDialog.isAdded()) smartDialog.dismiss();
            listener.onSelectedItemsMoved(selectedItems);
        }
    }

    public interface OnMovingItemsListener {
        void onSelectedItemsMoved(List<FTShelfItem> selectedItems);
    }
}