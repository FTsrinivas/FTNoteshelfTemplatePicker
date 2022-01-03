package com.fluidtouch.noteshelf.document.penracks.favorites;

import java.util.ArrayList;
import java.util.List;

public class FavoritesDiskItem {
    private List<Favorite> favorites = new ArrayList<>();

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}