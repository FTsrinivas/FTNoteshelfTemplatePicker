package com.fluidtouch.noteshelf.document.dialogs.addnew;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.document.dialogs.FTBookmarkFragment;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddNewTagFragment extends Fragment {
    @BindView(R.id.child_container_layout)
    RelativeLayout mChildContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_tag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.bookmark)
    void onBookmarkClicked() {
        FTFirebaseAnalytics.logEvent("document_activity", "add_new_tag", "bookmark");
        FTLog.crashlyticsLog("AddNew UI: Bookmark");

        getChildFragmentManager().beginTransaction().replace(R.id.child_container_layout, new FTBookmarkFragment()).commit();
        FTAnimationUtils.showEndPanelAnimation(getContext(), mChildContainer, true, null);
    }
}