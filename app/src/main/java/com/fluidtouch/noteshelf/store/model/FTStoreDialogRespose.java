package com.fluidtouch.noteshelf.store.model;

import java.util.ArrayList;

public class FTStoreDialogRespose {

    String pack_info = "";
    String updates_info = "";
    ArrayList<String> preview_items = new ArrayList<>();
    ArrayList<String> preview_tokens = new ArrayList<>();

    public String getPack_info() {
        return pack_info;
    }

    public void setPack_info(String pack_info) {
        this.pack_info = pack_info;
    }

    public String getUpdates_info() {
        return updates_info;
    }

    public void setUpdates_info(String updates_info) {
        this.updates_info = updates_info;
    }

    public ArrayList<String> getPreview_items() {
        return preview_items;
    }

    public void setPreview_items(ArrayList<String> preview_items) {
        this.preview_items = preview_items;
    }

    public ArrayList<String> getPreview_tokens() {
        return preview_tokens;
    }

    public void setPreview_tokens(ArrayList<String> preview_tokens) {
        this.preview_tokens = preview_tokens;
    }
}
