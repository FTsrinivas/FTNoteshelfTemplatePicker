package com.fluidtouch.noteshelf.whatsnew.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.whatsnew.ui.FTWhatsNewViewModel;
import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;
import com.fluidtouch.noteshelf2.R;

public class FTWhatsNewFirstFragment extends Fragment {

    private ImageView imageViewClose;
    private FTWhatsNewSession whatsNewSession;
    private Button btnSeeWhatsNew;

    private FTWhatsNewViewModel viewModel;

    public FTWhatsNewFirstFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whats_new_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (FTApp.isChineseBuild()) {
            TextView textView = view.findViewById(R.id.webdav_and_);
            if (textView != null) textView.setText(R.string.study_planners);
        }

        imageViewClose = view.findViewById(R.id.iv_close);
        btnSeeWhatsNew = view.findViewById(R.id.btnSeeWhatsNew);
        whatsNewSession = new FTWhatsNewSession(getActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(FTWhatsNewViewModel.class);
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsNewSession.storeUserStatusForViewDismissed(FTWhatsNewFirstFragment.class.getName() + "_isDismissed", true);
                ((DialogFragment) getParentFragment()).dismiss();
            }
        });
        btnSeeWhatsNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clickEvents(btnSeeWhatsNew);
            }
        });
    }
}
