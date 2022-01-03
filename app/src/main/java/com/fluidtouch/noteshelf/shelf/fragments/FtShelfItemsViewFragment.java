package com.fluidtouch.noteshelf.shelf.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu on 13/08/20.
 */
public class FtShelfItemsViewFragment extends Fragment {
    @BindView(R.id.shelf_items_view_title_view)
    TextView mTitleTextView;
    @BindView(R.id.shelf_items_view_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.shelf_items_view_title_layout)
    RelativeLayout mTitleLayout;

    private FTShelfMoveToAdapter mAdapter;
    private FTShelfItemCollection collection;
    private FTShelfItem currentShelfItem;
    private FTGroupItem groupItem;
    private FTShelfItemsViewContainerListener listener;

    public static FtShelfItemsViewFragment newInstance(FTShelfItemCollection collection, FTShelfItem currentShelfItem, FTGroupItem groupItem, FTShelfItemsViewContainerListener ftShelfItemsViewContainerListener) {
        FtShelfItemsViewFragment fragment = new FtShelfItemsViewFragment();
        fragment.collection = collection;
        fragment.currentShelfItem = currentShelfItem;
        fragment.groupItem = groupItem;
        fragment.listener = ftShelfItemsViewContainerListener;
        return fragment;
    }

    private FTShelfMoveToAdapter.FTShelfMoveToAdapterCallback mAdapterListener = new FTShelfMoveToAdapter.FTShelfMoveToAdapterCallback() {
        @Override
        public void showInGroupPanel(FTGroupItem ftGroupItem) {
            getChildFragmentManager().beginTransaction().addToBackStack(getTag())
                    .replace(R.id.shelf_move_panel_two_child_fragment, FtShelfItemsViewFragment.newInstance(collection, currentShelfItem, ftGroupItem, listener))
                    .commit();
        }

        @Override
        public void onNotebookClicked(FTShelfItem document) {
            listener.onNotebookSelected(document.getFileURL());
            navigateBack();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shelf_items_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        FTAnimationUtils.showEndPanelAnimation(getContext(), view, true, null);
        String toolbarColor = FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR);
        mTitleLayout.setBackgroundColor(Color.parseColor(toolbarColor));
        if (groupItem == null) {
            mTitleTextView.setText(collection.getDisplayTitle(getContext()));
            collection.shelfItems(getContext(), FTShelfSortOrder.BY_DATE, null, "", new FTShelfItemCollection.ShelfNotebookItemsAndErrorBlock() {
                @Override
                public void didFinishWithNotebookItems(List<FTShelfItem> notebooks, Error error) {
                    mAdapter = new FTShelfMoveToAdapter(mAdapterListener, currentShelfItem, false);
                    mAdapter.addAll(collection.getChildren());
                    mRecyclerView.setAdapter(mAdapter);
                }
            });
        } else {
            mTitleTextView.setText(groupItem.getDisplayTitle(getContext()));
            mAdapter = new FTShelfMoveToAdapter(mAdapterListener, currentShelfItem, false);
            mAdapter.addAll(groupItem.getChildren());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @OnClick(R.id.shelf_items_view_back_image_view)
    void navigateBack() {
        FTAnimationUtils.showEndPanelAnimation(getContext(), getView(), false, () -> {
            if (getActivity() != null) {
                if (groupItem != null) {
                    getFragmentManager().beginTransaction().remove(this).commit();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                }
            }
        });
    }

    public interface FTShelfItemsViewContainerListener {
        void onNotebookSelected(FTUrl fileURL);
    }
}
