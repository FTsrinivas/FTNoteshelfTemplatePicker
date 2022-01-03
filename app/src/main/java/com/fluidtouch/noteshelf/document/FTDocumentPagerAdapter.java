package com.fluidtouch.noteshelf.document;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sreenu on 2019-09-13
 */
//changed FragmentStatePagerAdapter to FragmentPagerAdapter to avoid crash
public class FTDocumentPagerAdapter extends FragmentStatePagerAdapter {
    private final Context context;
    private final FTNoteshelfDocument currentDocument;
    private FTDocumentPageFragment.PagerToActivityCallBack listener;
    private List<Fragment> pageControllers = new ArrayList<>();
    //private ObservingService searchObserver;
    private int noOfPagesAdded = 0;
    private String searchKey = "";

    FTDocumentPagerAdapter(FragmentManager fm, Context context, FTNoteshelfDocument currentDocument,
                           FTDocumentPageFragment.PagerToActivityCallBack listener, ObservingService searchObserver) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
        this.currentDocument = currentDocument;
        this.listener = listener;
        //this.searchObserver = searchObserver;
        this.pageControllers.addAll(Arrays.asList(new FTDocumentPageFragment[currentDocument.pages(context).size() + 2]));
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    @Override
    public int getCount() {
        return pageControllers.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return getFragmentAtIndex(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (listener.getCurrentItemPosition() != position) {
            super.destroyItem(container, position, object);
            removeFragmentAtIndex(position);
        }
    }

    private Fragment prepareItem(int position) {
        if (position == 0 || position == getCount() - 1) {
            return FTRefreshFragment.newInstance(position);
        } else {
            position = position - 1;

            Bundle args = new Bundle();
            args.putInt("num", position);
            args.putString("searchKey", searchKey);
            return FTDocumentPageFragment.newInstance(currentDocument.pages(context).get(position), args);
        }
    }

    void addItem(int position) {
        noOfPagesAdded = currentDocument.pages(context).size() + 2 - pageControllers.size();
        this.pageControllers.addAll(position, Arrays.asList(new FTDocumentPageFragment[noOfPagesAdded]));
        pageControllers.set(position, prepareItem(position));
    }

    Fragment getFragmentAtIndex(int position) {
        if (pageControllers.get(position) == null) {
            pageControllers.set(position, prepareItem(position));
        }
        return pageControllers.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    void removeFragmentAtIndex(int position) {
        if (position + noOfPagesAdded < pageControllers.size()) {
            pageControllers.set(position + noOfPagesAdded, null);
            noOfPagesAdded = 0;
        }
    }
}
