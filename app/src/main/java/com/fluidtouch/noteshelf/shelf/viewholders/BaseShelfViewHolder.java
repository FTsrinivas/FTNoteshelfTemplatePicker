package com.fluidtouch.noteshelf.shelf.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseShelfViewHolder extends RecyclerView.ViewHolder {
    BaseShelfViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void updateGroupingMode(int isItemBackgroundViewVisible);

    public abstract void setView(int position);
}
