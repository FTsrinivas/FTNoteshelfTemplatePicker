package com.fluidtouch.noteshelf.whatsnew.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.whatsnew.ui.FTWhatsNewViewModel;
import com.fluidtouch.noteshelf.whatsnew.ui.model.FTWhatsNewModel;
import com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession;
import com.fluidtouch.noteshelf.whatsnew.util.FTPreviewModes;
import com.fluidtouch.noteshelf.whatsnew.util.FTWhatsNewSlides;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTWhatsNewBaseFragment extends DialogFragment {
    private ViewPager viewPager;
    private WhatsNewPagerAdapter whatsNewPagerAdapter;
    private TextView[] dots;
    private LinearLayout layout;
    private com.fluidtouch.noteshelf.whatsnew.ui.sharedpref.FTWhatsNewSession FTWhatsNewSession;
    private List<FTWhatsNewModel> FTWhatsNewModelsList;
    private String mode = FTPreviewModes.AUTOMATIC.name();
    private FTWhatsNewViewModel viewModel;

    public FTWhatsNewBaseFragment() {
        this.mode = mode;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_whats_new, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setWindowAnimations(
                R.style.dialog_animation_fade);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    public void updateViewPagerData(String mode1) {
        mode = mode1;
        FTWhatsNewModelsList = new ArrayList<>();
        FTWhatsNewModelsList = FTWhatsNewSlides.getInstance().getSlidesList(mode, FTWhatsNewSession);
        whatsNewPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view != null) {
            viewPager = view.findViewById(R.id.view_pager);
            layout = view.findViewById(R.id.dots_container);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(FTWhatsNewViewModel.class);
        FTWhatsNewSession = new FTWhatsNewSession(getActivity());
        FTWhatsNewModelsList = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mode = bundle.getString("mode");
        }

        //  FTWhatsNewSlides.getInstance().setWhatsNewModelsList(FTWhatsNewSession);
        viewModel.getClickEvents().observe(getViewLifecycleOwner(), view1 -> {
            switch (view1.getId()) {
                case R.id.btnSeeWhatsNew:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.btnFollow:
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://linktr.ee/FollowNoteshelf"));
                    startActivity(i);
                    break;
            }
        });
        FTWhatsNewModelsList = FTWhatsNewSlides.getInstance().getSlidesList(mode, FTWhatsNewSession);
        whatsNewPagerAdapter = new WhatsNewPagerAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(whatsNewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    if (!FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewModelsList.get(position).getSlideType() + "_isViewed")) {
                        FTWhatsNewSession.storeUserStatusForViewDismissed(FTWhatsNewModelsList.get(position).getSlideType() + "_isViewed", true);
                    }
                    if (FTWhatsNewSession.getUserViewedTime(FTWhatsNewModelsList.get(position).getSlideType() + "_viewedTime") == 0L) {
                        FTWhatsNewSession.storeUserSlideViewedTime(FTWhatsNewModelsList.get(position).getSlideType() + "_viewedTime", new Date().getTime());
                    }
                }
                if (FTWhatsNewModelsList.get(position).getSlideType().equals(FTWhatsNewFirstFragment.class.getName())) {
                    layout.setVisibility(View.GONE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageSelected(int position) {

                //updating shared prefs for Selected slide
                if (!FTWhatsNewSession.getSlideViewedDismissedStatus(FTWhatsNewModelsList.get(position).getSlideType() + "_isViewed")) {
                    FTWhatsNewSession.storeUserStatusForViewDismissed(FTWhatsNewModelsList.get(position).getSlideType() + "_isViewed", true);
                }
                if (FTWhatsNewSession.getUserViewedTime(FTWhatsNewModelsList.get(position).getSlideType() + "_viewedTime") == 0L) {
                    FTWhatsNewSession.storeUserSlideViewedTime(FTWhatsNewModelsList.get(position).getSlideType() + "_viewedTime", new Date().getTime());
                }
                if (FTWhatsNewModelsList.get(position).isDataToBeRefreshed()) {
                    Fragment fragment = FTWhatsNewModelsList.get(position).getFragmentType();
                    fragment.onResume();
                }

                if (FTWhatsNewModelsList.size() > 1) {
                    selectedDots(position);
                }

                if (FTWhatsNewModelsList.get(position).getSlideType().equals(FTWhatsNewFirstFragment.class.getName())) {
                    layout.setVisibility(View.GONE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        if (FTWhatsNewModelsList.size() > 1) {
            layout.setVisibility(View.VISIBLE);
            // slide indicators
            dots = new TextView[FTWhatsNewModelsList.size()];
            setIndicators();
            selectedDots(0);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    public void showIfMeetAllConditions(Context context, String mode, FragmentManager fragmentManager, FTWhatsNewSession fTWhatsNewSession) {
        this.mode = mode;
        this.FTWhatsNewSession = fTWhatsNewSession;
        if (!ScreenUtil.isMobile(context)) {
            FTWhatsNewSlides.getInstance().setWhatsNewModelsList(fTWhatsNewSession);
            if (FTWhatsNewSlides.getInstance().getSlidesList(mode, fTWhatsNewSession).size() > 0) {
                this.show(fragmentManager, "MyFragment");
            } else {
                // do nothing
            }
        }
    }

    private void selectedDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setTextColor(getResources().getColor(R.color.black));
            } else {
                dots[i].setTextColor(getResources().getColor(R.color.grey));
            }
        }
    }

    private void setIndicators() {
        for (int i = 0; i < dots.length; i++) {
//            dots[i] = new View(getActivity());
//            int mSize = (int) dpFromPx(getActivity(),36);
//            int mMargin = (int) dpFromPx(getActivity(),8);
//              LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mSize, mSize);
//            layoutParams.rightMargin = mMargin;
//            dots[i].setLayoutParams(layoutParams);
//            layout.addView(dots[i]);
//
            dots[i] = new TextView(getActivity());
            dots[i].setText(Html.fromHtml("&#9679;"));
            dots[i].setWidth((int) dpFromPx(getActivity(), 36));
            dots[i].setTextSize((int) dpFromPx(getActivity(), 12));
            layout.addView(dots[i]);
        }
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    private class WhatsNewPagerAdapter extends FragmentStatePagerAdapter {
        private final Context mContext;

        public WhatsNewPagerAdapter(FragmentActivity activity, FragmentManager fm) {
            super(fm);
            this.mContext = activity;

        }

        @Override
        public Fragment getItem(int pos) {
            return FTWhatsNewModelsList.get(pos).getFragmentType();
        }

        @Override
        public int getCount() {
            return FTWhatsNewModelsList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

}
