package com.fluidtouch.noteshelf.commons.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class BaseRecyclerAdapter<O, V extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<V> {
    protected List<O> items = new ArrayList<>();

    protected BaseRecyclerAdapter() {
        setHasStableIds(true);
    }

    public View getView(ViewGroup parent, int layout) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
    }

    public void add(O object) {
        this.items.add(object);
        notifyDataSetChanged();
    }

    public void add(int index, O object) {
        this.items.add(index, object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends O> collection) {
        if (collection != null) {
            items.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public void addAll(O... items) {
        addAll(Arrays.asList(items));
    }

    public void clear() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void remove(int position) {
        this.items.remove(position);
        notifyItemRemoved(position);
    }

    public void remove(O object) {
        items.remove(object);
        notifyDataSetChanged();
    }

    public void remove(List<O> objects) {
        items.removeAll(objects);
        notifyDataSetChanged();
    }

    public O getItem(int position) {
        //Temporary fix for position is becoming -1
        if (position < 0) {
            return this.items.get(0);
        }
        return this.items.get(position);
    }

    public List<O> getAll() {
        return this.items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void update(int position, O object) {
        this.items.set(position, object);
        notifyItemChanged(position);
    }

    public void updateAll(List<O> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
