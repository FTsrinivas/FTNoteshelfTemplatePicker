package com.fluidtouch.noteshelf.welcome;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FTWelcomeScreenAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private FragmentManager fragmentManager;

    public FTWelcomeScreenAdapter(List<Fragment> fragments, @NonNull FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void destroyItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
        fragmentManager.beginTransaction().remove((Fragment) object);
    }
}