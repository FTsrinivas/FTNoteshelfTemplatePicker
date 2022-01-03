package com.fluidtouch.noteshelf.whatsnew.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;
import com.fluidtouch.noteshelf2.R;

public class FTWhatsNewSecondFragment extends Fragment {
    private ImageView imageViewClose;
    private FTWhatsNewSession whatsNewSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_whats_new_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewClose = view.findViewById(R.id.iv_close);
        whatsNewSession = new FTWhatsNewSession(getActivity());
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whatsNewSession.storeUserStatusForViewDismissed(FTWhatsNewSecondFragment.class.getName() + "_isDismissed", true);
                ((DialogFragment)getParentFragment()).dismiss();
            }
        });
    }
}