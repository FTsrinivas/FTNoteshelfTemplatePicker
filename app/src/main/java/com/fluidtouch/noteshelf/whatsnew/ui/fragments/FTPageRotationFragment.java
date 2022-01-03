package com.fluidtouch.noteshelf.whatsnew.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;
import com.fluidtouch.noteshelf2.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FTPageRotationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FTPageRotationFragment extends Fragment {
    private ImageView pageRotationImageView, imageViewClose;
    private FTWhatsNewSession whatsNewSession;

    public static FTPageRotationFragment newInstance() {
        FTPageRotationFragment fragment = new FTPageRotationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_rotation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pageRotationImageView = view.findViewById(R.id.paper);

        imageViewClose = view.findViewById(R.id.iv_close);
        whatsNewSession = new FTWhatsNewSession(getActivity());
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsNewSession.storeUserStatusForViewDismissed(FTPageRotationFragment.class.getName() + "_isDismissed", true);
                ((DialogFragment)getParentFragment()).dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Animation an = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        pageRotationImageView.setVisibility(View.VISIBLE);
        pageRotationImageView.startAnimation(an);
    }

}