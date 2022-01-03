package com.fluidtouch.noteshelf.whatsnew.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.whatsnew.ui.FTWhatsNewViewModel;
import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;
import com.fluidtouch.noteshelf2.R;

public class FTWhatsFourthFragment extends Fragment {
    //private int[] followUs = {R.drawable.follow1, R.drawable.follow2, R.drawable.follow3, R.drawable.follow4, R.drawable.follow5, R.drawable.follow6};
    private RecyclerView followUsRecyclerView;
    private FTWhatsNewSession whatsNewSession;
    private ImageView imageViewClose;
    private boolean isAlreadyDoing = false;
    private Button btnFollow;
    private FTWhatsNewViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_whats_new_fourth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        followUsRecyclerView = view.findViewById(R.id.rview_follow_items);
        imageViewClose = view.findViewById(R.id.iv_close);
        btnFollow = view.findViewById(R.id.btnFollow);
        viewModel = new ViewModelProvider(requireActivity()).get(FTWhatsNewViewModel.class);
        whatsNewSession = new FTWhatsNewSession(getActivity());
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsNewSession.storeUserStatusForViewDismissed(FTWhatsFourthFragment.class.getName() + "_isDismissed", true);
                ((DialogFragment) getParentFragment()).dismiss();
            }
        });
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.clickEvents(btnFollow);
            }
        });

    }

}