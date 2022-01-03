package com.fluidtouch.noteshelf.document.lasso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sreenu on 21/03/19
 */
public class FTLassoColorPickerFragment extends Fragment {
    @BindView(R.id.lasso_color_picker_recycler_view)
    protected RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lasso_color_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

//        FTLassoColorPickerAdapter adapter = new FTLassoColorPickerAdapter();
//        adapter.addAll(getColors());
//        mSearchRecyclerView.setAdapter(adapter);
    }

    private List<String> getColors() {
        try {
            File colorPlist = FTFileManagerUtil.copyFileFromAssets(getContext(), FTConstants.DEFAULT_COLORS_PLIST_RELATIVE_PATH);
            return new ArrayList<>(Arrays.asList((String[]) PropertyListParser.parse(FTFileManagerUtil.getFileInputStream(colorPlist)).toJavaObject()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
