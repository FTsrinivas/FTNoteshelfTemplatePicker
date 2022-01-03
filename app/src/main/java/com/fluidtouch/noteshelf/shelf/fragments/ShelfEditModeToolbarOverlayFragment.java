package com.fluidtouch.noteshelf.shelf.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShelfEditModeToolbarOverlayFragment.OnShelfEditModeToolbarFragmentInteractionListener} interface to handle interaction events.
 */
public class ShelfEditModeToolbarOverlayFragment extends Fragment {
    private static final String ARG_COUNT = "count";
    //region Binding variables
    @BindView(R.id.shelf_item_edit_mode_title_text_view)
    TextView titleTextView;
    @BindView(R.id.shelf_item_edit_mode_select_all_text_View)
    TextView selectAllTextView;
    @BindView(R.id.shelf_item_edit_mode_layout)
    LinearLayout layout;

    //endregion
    //region Class variables
    private OnShelfEditModeToolbarFragmentInteractionListener mFragmentListener;
    private int mCount;
    //endregion

    //region constructor
    public ShelfEditModeToolbarOverlayFragment() {
        // Required empty public constructor
    }
    //endregion

    public static ShelfEditModeToolbarOverlayFragment newInstance(int count) {
        ShelfEditModeToolbarOverlayFragment fragment = new ShelfEditModeToolbarOverlayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_COUNT, count);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCount = getArguments().getInt(ARG_COUNT);
        }
    }

    //endregion

    //region Fragment callback methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shelf_item_edit_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        titleTextView.setVisibility(getResources().getBoolean(R.bool.isTablet) ? View.VISIBLE : View.INVISIBLE);
        updateToolbarTheme();
        updateLayout(mCount);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof OnShelfEditModeToolbarFragmentInteractionListener) {
            mFragmentListener = (OnShelfEditModeToolbarFragmentInteractionListener) context;
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
    @OnClick(R.id.shelf_item_edit_mode_done_text_view)
    void closeLayout() {
        mFragmentListener.disableEditMode();
    }

    @OnClick(R.id.shelf_item_edit_mode_select_all_text_View)
    void selectAll() {
        if (mCount == mFragmentListener.getAllNotebooksCount()) {
            mFragmentListener.selectNone();
            selectAllTextView.setText(getString(R.string.select_all));
        } else {
            mFragmentListener.selectAll();
            selectAllTextView.setText(getString(R.string.select_none));
        }
    }

    //region Helper methods
    public void updateLayout(int count) {
        mCount = count;
        titleTextView.setText(count == 0 ? getString(R.string.select_a_document) : getString(R.string.num_selected, count));
        requireView().setOnClickListener(null);

        if (mCount != 0) {
            selectAllTextView.setText(mCount == mFragmentListener.getAllNotebooksCount() ? getString(R.string.select_none) : getString(R.string.select_all));
        }
    }
    //endregion


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getBoolean(R.bool.isTablet)) {
            titleTextView.setVisibility(View.VISIBLE);
        } else {
            titleTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void updateToolbarTheme() {
        String toolbarColor = FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR);
        layout.setBackgroundColor(Color.parseColor(toolbarColor));
    }

    //region delegates
    public interface OnShelfEditModeToolbarFragmentInteractionListener {
        void disableEditMode();

        void selectAll();

        void selectNone();

        int getAllNotebooksCount();
    }
    //endregion
}
