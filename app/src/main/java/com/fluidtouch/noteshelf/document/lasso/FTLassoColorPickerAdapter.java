package com.fluidtouch.noteshelf.document.lasso;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 21/03/19
 */
public class FTLassoColorPickerAdapter extends BaseRecyclerAdapter<Object, FTLassoColorPickerAdapter.LassoColorPickerViewHolder> {
    private FTLassoColorPickerContainerCallback mParentCallback;

    public FTLassoColorPickerAdapter(FTLassoColorPickerContainerCallback parentCallback) {
        mParentCallback = parentCallback;
    }

    @NonNull
    @Override
    public LassoColorPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LassoColorPickerViewHolder(getView(parent, R.layout.item_lasso_color_picker_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull LassoColorPickerViewHolder holder, int position) {
        if (!((String) getItem(position)).equalsIgnoreCase("add")) {
            holder.itemView.setBackgroundResource(R.drawable.circular_grey_bg);
            DrawableUtil.setGradientDrawableColor(holder.mView,
                    holder.itemView.getContext().getString(R.string.set_color, (String) getItem(position)), position);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.plus_blue);
        }
    }

    public interface FTLassoColorPickerContainerCallback {
        void onColorSelected(String string);
    }

    class LassoColorPickerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_lasso_color_picker_view)
        protected View mView;

        public LassoColorPickerViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_lasso_color_picker_view)
        protected void selectColor() {
            mParentCallback.onColorSelected(itemView.getContext().getString(R.string.set_color, (String) getItem(getAdapterPosition())));
        }
    }
}
