package com.fluidtouch.noteshelf.document.penracks.favorites;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class FTFavoritesProvider {
    private Context mContext;
    private Gson gson = new Gson();

    public FTFavoritesProvider(Context context) {
        this.mContext = context;
    }

    public void saveFavorite(Favorite favorite) {
        if (favorite != null) {
            FavoritesDiskItem favoritesDiskItem = getFavoriteDiskItem();
            List<Favorite> favorites = favoritesDiskItem.getFavorites();
            if (!favorites.contains(favorite)) {
                favorites.add(favorite);
                favoritesDiskItem.setFavorites(favorites);
                writeFavoritesDiskItem(favoritesDiskItem);
            }
        }
    }

    public void saveAllFavorites(List<Favorite> favorites) {
        if (favorites != null && !favorites.isEmpty()) {
            FavoritesDiskItem favoritesDiskItem = getFavoriteDiskItem();
            favoritesDiskItem.setFavorites(favorites);
            writeFavoritesDiskItem(favoritesDiskItem);
        }
    }

    public void removeFavorite(Favorite favorite) {
        if (favorite != null) {
            FavoritesDiskItem favoritesDiskItem = getFavoriteDiskItem();
            List<Favorite> favorites = favoritesDiskItem.getFavorites();
            if (!favorites.isEmpty()) {
                Iterator iterator = favorites.iterator();
                while (iterator.hasNext()) {
                    Favorite existingFavorite = (Favorite) iterator.next();
                    if (favorite.getPenColor().equals(existingFavorite.getPenColor()) && favorite.getPenType().equals(existingFavorite.getPenType())) {
                        iterator.remove();
                        favoritesDiskItem.setFavorites(favorites);
                        writeFavoritesDiskItem(favoritesDiskItem);
                        break;
                    }
                }
            }
        }
    }

    public List<Favorite> getFavorites() {
        return getFavoriteDiskItem().getFavorites();
    }

    private FavoritesDiskItem getFavoriteDiskItem() {
        FavoritesDiskItem favoritesDiskItem = new FavoritesDiskItem();
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(getFavoritesFilePath()));
            FavoritesDiskItem favoritesDiskItemx = gson.fromJson(jsonReader, FavoritesDiskItem.class);
            if (favoritesDiskItemx != null) {
                favoritesDiskItem = favoritesDiskItemx;
            }
            jsonReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return favoritesDiskItem;
    }

    private void writeFavoritesDiskItem(FavoritesDiskItem favoritesDiskItem) {
        try {
            String favoritesJson = gson.toJson(favoritesDiskItem);
            JsonWriter jsonWriter = new JsonWriter(new FileWriter(getFavoritesFilePath()));
            jsonWriter.jsonValue(favoritesJson);
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFavoritesFilePath() {
        File clipartDir = new File(ContextCompat.getDataDir(mContext).getPath() + "/favorites");
        if (!clipartDir.exists())
            clipartDir.mkdirs();
        File recentJson = new File(clipartDir, "favorites.json");
        if (!recentJson.exists()) {
            try {
                recentJson.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recentJson.getPath();
    }

    public boolean isFavorite(Favorite favoriteItem) {
        boolean isFavorite = false;
        List<Favorite> favorites = getFavorites();
        if (!favorites.isEmpty() && favoriteItem != null) {
            for (Favorite favorite : favorites) {
                if (favorite.getPenColor().equals(favoriteItem.getPenColor()) &&
                        favorite.getPenType().equals(favoriteItem.getPenType()) &&
                        favorite.getPenSize() == favoriteItem.getPenSize()) {
                    isFavorite = true;
                    break;
                }
            }
        }
        return isFavorite;
    }
}