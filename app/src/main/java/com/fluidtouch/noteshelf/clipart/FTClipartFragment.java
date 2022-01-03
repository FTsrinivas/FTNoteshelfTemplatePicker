package com.fluidtouch.noteshelf.clipart;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.clipart.pixabay.dialog.FTPixabayClipartFragment;
import com.fluidtouch.noteshelf.clipart.unsplash.dialog.FTUnsplashClipartFragment;
import com.fluidtouch.noteshelf2.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FTClipartFragment extends Fragment {
    public static final String CLIPART_RECENT = "Recent";

    private final HashMap<String, String> mCategoryStrings = new LinkedHashMap<>();

    private TextView mPrevSelected;

    @Override

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this instanceof FTUnsplashClipartFragment) {
            mCategoryStrings.put(getString(R.string.recent), "Recent");
            mCategoryStrings.put(getString(R.string.featured), "Featured");
            mCategoryStrings.put(getString(R.string.animals), "Animals");
            mCategoryStrings.put(getString(R.string.animals_and_nature), "Nature");
            mCategoryStrings.put(getString(R.string.arts), "Arts");
            mCategoryStrings.put(getString(R.string.athletics), "Athletics");
            mCategoryStrings.put(getString(R.string.business), "Business");
            mCategoryStrings.put(getString(R.string.fashion), "Fashion");
            mCategoryStrings.put(getString(R.string.film), "Film");
            mCategoryStrings.put(getString(R.string.food_and_drink), "Food & Drink");
            mCategoryStrings.put(getString(R.string.health), "Health");
            mCategoryStrings.put(getString(R.string.history), "History");
            mCategoryStrings.put(getString(R.string.interiors), "Interiors");
            mCategoryStrings.put(getString(R.string.technology), "Technology");
            mCategoryStrings.put(getString(R.string.textures), "Textures");
            mCategoryStrings.put(getString(R.string.travel_and_places), "Travel & Places");
            mCategoryStrings.put(getString(R.string.wallpapers), "Wallpapers");
        } else if (this instanceof FTPixabayClipartFragment) {
            mCategoryStrings.put(getString(R.string.recent), "Recent");
            mCategoryStrings.put(getString(R.string.photos), "photo");
            mCategoryStrings.put(getString(R.string.vectors), "vector");
            mCategoryStrings.put(getString(R.string.illustrations), "illustration");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayout categoriesLayout = view.findViewById(R.id.clipart_categories_layout);
        if (categoriesLayout != null) {
            for (Map.Entry<String, String> category : mCategoryStrings.entrySet()) {
                TextView textView = new TextView(new ContextThemeWrapper(getContext(), R.style.ClipartCategoryText));
                textView.setText(category.getKey());
                textView.setTextColor(getResources().getColor(R.color.ns_blue, null));
                textView.setBackgroundResource(0);
                textView.setTag(category.getValue());
                textView.setOnClickListener((View v) -> {
                    TextView clickedView = (TextView) v;
                    if (mPrevSelected != null) {
                        mPrevSelected.setBackgroundResource(0);
                        mPrevSelected.setTextColor(getResources().getColor(R.color.ns_blue, null));
                    }
                    clickedView.setBackgroundResource(R.drawable.clipart_category_bg);
                    clickedView.setTextColor(Color.WHITE);
                    mPrevSelected = clickedView;
                    onCategorySelected(clickedView.getTag().toString());
                });
                categoriesLayout.addView(textView);
            }
        }
    }

    public void onCategorySelected(String category) {

    }
}