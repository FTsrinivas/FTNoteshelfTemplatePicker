package com.fluidtouch.noteshelf.document.penracks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.document.enums.PenOrHighlighterInfo;
import com.fluidtouch.noteshelf.document.penracks.favorites.FTFavoritesAdapter;
import com.fluidtouch.noteshelf.document.penracks.favorites.FTFavoritesProvider;
import com.fluidtouch.noteshelf.document.penracks.favorites.Favorite;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

//@sreenu -- Changed from DialogFragment to Fragment to avoid the onCancel() method deleting this fragment in BaseDialog.popup class
public class FTFavPenRackDialog extends Fragment implements FTFavoritesAdapter.FavoritesAdapterCallback /*,View.OnClickListener */ {
    @BindView(R.id.favoritesRecyclerView)
    RecyclerView favoritesRecyclerView;

    @BindView(R.id.sizeIcon)
    ImageView sizeIcon;

    @BindView(R.id.closeSizeLyt)
    ImageView closeSizeLyt;

    @BindView(R.id.rightFadeLytId)
    ImageView rightFadeLytId;
    @BindView(R.id.leftFadeLytId)
    ImageView leftFadeLytId;

    @BindView(R.id.laySizes)
    LinearLayout mLaySizes;

    @BindView(R.id.sizeIconLayout)
    RelativeLayout sizeIconLayout;

    int selItemBtnPos = 1;
    private String penColor;
    private Object[] mFontSizes;
    private String tempColor = "#c5c5b2", mPrefPenKey = "", mPrefSizeKey = "", mPrefColorKey = "", mPrefCurrentSelection = "mPrefCurrentSelection", DEFAULT_TOOL = FTPenType.pen.toString();
    private int mDialogType = 1, expandMargin = 72, defaultMargin = 100, DEFAULT_SIZE = 3;
    private int[] sizes;
    private Integer[] sizes_drawables;
    private View tempPen, tempPoint, tempSizeView, tempLay, tempView, tempShadow, tempColorView;
    private PenRackPref mPenPref;
    private String penDefaultColor = "#939393";
    private FTFavoritesAdapter favoritesAdapter;
    private FTFavoritesProvider favoritesProvider;
    private boolean isAnimation = false;
    private List<Favorite> favorites;
    private FavWidgetToolBarListener mFavWidgetToolBarListener;
    private FTToolBarTools penType = FTToolBarTools.PEN;
    Favorite selectedFavorite = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favwidget_tool_bar, container, false);
    }

    @SuppressLint("Range")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            isAnimation = true;
            view.setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_in_from_top));
        }

        sizeIcon.setOnClickListener(v -> hideAndShowPenSize());
        closeSizeLyt.setOnClickListener(v -> mFavWidgetToolBarListener.closeFavToolbarWidget());
        sizeIconLayout.setOnClickListener(view1 -> hideAndShowPenSize());

        expandMargin = (int) getResources().getDimension(R.dimen.pen_collapse_margin);
        defaultMargin = (int) getResources().getDimension(R.dimen.pen_expand_margin);
        mPenPref = new PenRackPref().init(PenRackPref.PREF_NAME);

        initFavoritesLayout();
    }

    private void hideAndShowPenSize() {
        if (selItemBtnPos == 0) {
            selItemBtnPos = 1;
            mLaySizes.setVisibility(View.VISIBLE);
            rightFadeLytId.setVisibility(View.GONE);
            leftFadeLytId.setVisibility(View.GONE);
            Drawable image = (Drawable) ContextCompat.getDrawable(getActivity(), (R.mipmap.back_big));
            sizeIconLayout.setBackground(image);
            sizeIcon.setBackground(null);
            favoritesRecyclerView.setVisibility(View.GONE);
            showSizesOfPens();
        } else if (selItemBtnPos == 1) {
            selItemBtnPos = 0;
            mLaySizes.setVisibility(View.GONE);
            rightFadeLytId.setVisibility(View.VISIBLE);
            leftFadeLytId.setVisibility(View.VISIBLE);
            favoritesRecyclerView.setVisibility(View.VISIBLE);
            updateSizeIcon(0);
        }
    }

    private void showSizesOfPens() {
        //GetData from Plist
        FTPenRackModel penRackModel = FTPenRackModel.getDefaultPenRack(penType, requireContext());
        sizes = penRackModel.sizes;
        mFontSizes = penRackModel.fontSizes;
        if (penType == FTToolBarTools.HIGHLIGHTER) {
            sizes_drawables = new Integer[]{R.mipmap.sizehighlighter_1, R.mipmap.sizehighlighter_2, R.mipmap.sizehighlighter_3, R.mipmap.sizehighlighter_4, R.mipmap.sizehighlighter_5, R.mipmap.sizehighlighter_6};
        }
        setSizes(1);
    }

    private void setSizes(final int type) {
        if (mFontSizes != null) {
            selectPrefKey(penType);
            mLaySizes.removeAllViews();
            for (int i = 0; i < mFontSizes.length; i++) {
                View viewMain = getLayoutInflater().inflate(R.layout.penrack_size_view, null);
                ImageView view = (ImageView) viewMain.findViewById(R.id.sizeView);
                int mSize = type == 1 ? convertDpToPx(sizes[i]) : LinearLayout.LayoutParams.WRAP_CONTENT;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mSize, mSize);
                if (i == 0)
                    layoutParams.leftMargin = convertDpToPx(getResources().getInteger(R.integer.ten));
                else if (i == mFontSizes.length - 1)
                    layoutParams.rightMargin = convertDpToPx(getResources().getInteger(R.integer.ten));
                layoutParams.gravity = Gravity.CENTER;

                view.setLayoutParams(layoutParams);
                view.setBackground(getContext().getDrawable(type == 1 ? R.drawable.pen_size_bg : sizes_drawables[i]));
                //Set Selected Size
                if (mPenPref.get(mPrefSizeKey, DEFAULT_SIZE) - 1 == i) {
                    DrawableUtil.setGradientDrawableColor(view, "#000000", 0);
                    tempSizeView = view;
                } else {
                    DrawableUtil.setGradientDrawableColor(view, "#c5c5b2", 0);
                }
                view.setId(i);
                viewMain.setId(i + mFontSizes.length);

                viewMain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String log = "Favorite_Size" + ((int) v.getId() + 1);
                        FTFirebaseAnalytics.logEvent(log);

                        ImageView view = (ImageView) v.findViewById(v.getId() - mFontSizes.length);
                        if (null != tempSizeView) {
                            DrawableUtil.setGradientDrawableColor(tempSizeView, "#c5c5b2", 0);
                        }
                        mPenPref.save(mPrefSizeKey, (view.getId()) + 1);
                        DrawableUtil.setGradientDrawableColor(view, "#000000", 0);
                        tempSizeView = view;

                        int color = mPenPref.get(mPrefColorKey, Color.parseColor(tempColor));
                        penorHighlighterInfoChanged(penType, color,
                                ((view.getId()) + 1), mPenPref.get(mPrefPenKey, DEFAULT_TOOL));
                        favoritesAdapter.setSelectedFavorite(getSelectedFavoritePen());
                    }
                });
                viewMain.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                mLaySizes.addView(viewMain);
            }
        }
    }

    private void updateSizeIcon(int penSize) {
        if (selItemBtnPos != 1) {
            sizeIconLayout.setBackground(getContext().getDrawable(R.drawable.circle));
            sizeIcon.setBackground(getContext().getDrawable(R.drawable.pen_size_bg));
            if (penSize != 0) {
                sizeIcon.getLayoutParams().width = convertDpToPx(sizes[penSize - 1]);
                sizeIcon.getLayoutParams().height = convertDpToPx(sizes[penSize - 1]);
            } else if (tempSizeView != null) {
                sizeIcon.getLayoutParams().width = tempSizeView.getLayoutParams().width;
                sizeIcon.getLayoutParams().height = tempSizeView.getLayoutParams().height;
            }
            DrawableUtil.setGradientDrawableColor(sizeIcon, "#000000", 0);
        }
    }

    public void refreshListView() {
        int currentPenType = mPenPref.get(mFavWidgetToolBarListener.getDocUid() + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
        if (currentPenType == FTToolBarTools.PEN.toInt()) {
            penType = FTToolBarTools.PEN;
        } else if (currentPenType == FTToolBarTools.HIGHLIGHTER.toInt()) {
            penType = FTToolBarTools.HIGHLIGHTER;
        } else {
            penType = FTToolBarTools.ERASER;
        }

        selectPrefKey(penType);
        selItemBtnPos = 1;
        hideAndShowPenSize();

        if (isFavorite() && (penType == FTToolBarTools.PEN || penType == FTToolBarTools.HIGHLIGHTER)) {
            this.selectedFavorite = getSelectedFavoritePen();
            this.favoritesAdapter.setSelectedFavorite(selectedFavorite);
        }

        //Scroll to the selected pen.
        if (selectedFavorite != null) {
            int scrollToPos = 0;
            List<Favorite> data = favoritesAdapter.getData();
            for (int i = 0; i < data.size(); i++) {
                Favorite favorite = data.get(i);
                if (favorite.getPenColor().equals(selectedFavorite.getPenColor())
                        && favorite.getPenType().equals(selectedFavorite.getPenType())
                        && favorite.getPenSize() == selectedFavorite.getPenSize()) {
                    scrollToPos = i;
                    break;
                }
            }
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) favoritesRecyclerView.getLayoutManager();
            if (linearLayoutManager != null) {
                if (scrollToPos < linearLayoutManager.findFirstCompletelyVisibleItemPosition() || scrollToPos > linearLayoutManager.findLastCompletelyVisibleItemPosition()) {
                    favoritesRecyclerView.scrollToPosition(scrollToPos);
                }
            }
        }
    }

    public void removeFavourite(Favorite favorite) {
        favoritesAdapter.remove(favorite);
    }

    public void addFavourite(Favorite favorite) {
        favoritesAdapter.getData().add(0, favorite);
        favoritesAdapter.setSelectedFavorite(favorite);
        favoritesAdapter.notifyDataSetChanged();
    }

    private void initFavoritesLayout() {
        showSizesOfPens();

        this.favoritesProvider = new FTFavoritesProvider(getActivity());
        this.favoritesAdapter = new FTFavoritesAdapter(getContext(), this, true); //,true);
        List<Favorite> favorites = favoritesProvider.getFavorites();
        if (favorites != null && !favorites.isEmpty()) {
            Collections.reverse(favorites);
            favoritesAdapter.setData(favorites);
        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        // And now set it to the RecyclerView
        this.favoritesRecyclerView.setLayoutManager(mLayoutManager);
        this.favoritesRecyclerView.setAdapter(this.favoritesAdapter);

        new GestureManager.Builder(favoritesRecyclerView)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .build();

        favoritesAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<Favorite>() {
            @Override
            public void onItemRemoved(Favorite favorite, int position) {
                //Not working with swiping now.
            }

            @Override
            public void onItemReorder(Favorite favorite, int fromPosition, int toPosition) {
                FTFirebaseAnalytics.logEvent("Favorite_DragToRearrange");
                favoritesProvider.saveAllFavorites(favoritesAdapter.getData());
            }
        });

        favoritesRecyclerView.setOnScrollChangeListener((view, i, i1, i2, i3) -> {
            //Reverse layout is set to true
            if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                rightFadeLytId.setVisibility(View.GONE);
                leftFadeLytId.setVisibility(View.GONE);
            } else if (mLayoutManager.findLastCompletelyVisibleItemPosition() == favoritesAdapter.getItemCount() - 1) {
                leftFadeLytId.setVisibility(View.GONE);
                rightFadeLytId.setVisibility(View.GONE);
            } else {
                leftFadeLytId.setVisibility(View.VISIBLE);
                rightFadeLytId.setVisibility(View.VISIBLE);
            }
        });

        refreshListView();
    }

    /*
     * This interface method will be called from FavouriteAdapter.
     * Whenever user clicks on pen or highliter in FavouriteAdapter then same item needs to be refelcted in Main Toolbar for that
     * "favWidgetListener.selctedItemInFavWidget" will be called.
     * */
    @Override
    public void onPenSelected(Favorite favorite) {
        FTFirebaseAnalytics.logEvent("inside_document", "pen_rack", "favorite_pen");
        if (favorite != null) {
            selectedFavorite = favorite;
            penType = getPenrackGroupType(favorite.getPenType());
            FTFirebaseAnalytics.logEvent(penType == FTToolBarTools.PEN ? "Favorite_TapPen" : "Favorite_TapHighlighter");
            showSizesOfPens();
            penColor = favorite.getPenColor();
            penorHighlighterInfoChanged(penType, Color.parseColor(favorite.getPenColor()), favorite.getPenSize(), favorite.getPenType().toString());
        }
    }

    private void penorHighlighterInfoChanged(FTToolBarTools penRackType, int penColor, int penSize, String penType) {
        mFavWidgetToolBarListener.penOrHighlighterClickedInFavWidget(
                PenOrHighlighterInfo.getInfo(penRackType.toInt() + 1, penColor, penSize, penType), true);
        updateSizeIcon(penSize);
    }

    private void selectPrefKey(FTToolBarTools penType) {
        if (penType == FTToolBarTools.PEN) {
            mPrefPenKey = "selectedPen";
            mPrefSizeKey = "selectedPenSize";
            mPrefColorKey = "selectedPenColor";
            tempColor = PenRackPref.DEFAULT_PEN_COLOR;
            DEFAULT_SIZE = PenRackPref.DEFAULT_SIZE;
            DEFAULT_TOOL = PenRackPref.DEFAULT_PEN_TYPE;
        } else if (penType == FTToolBarTools.HIGHLIGHTER) {
            mPrefPenKey = "selectedPen_h";
            mPrefSizeKey = "selectedPenSize_h";
            mPrefColorKey = "selectedPenColor_h";
            tempColor = PenRackPref.DEFAULT_HIGHLIGHTER_COLOR;
            DEFAULT_SIZE = PenRackPref.DEFAULT_SIZE;
            DEFAULT_TOOL = PenRackPref.DEFAULT_HIGHLIGHTER_TYPE;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mFavWidgetToolBarListener = (FavWidgetToolBarListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + e.getMessage());
        }

    }

    @Override
    public void removeFromFavorites(int position) {
        //If no favorites show message
        if (!favoritesAdapter.getData().isEmpty() && position >= 0) {
            Favorite favorite = favoritesAdapter.getItem(position);
            favoritesAdapter.remove(position);
            favoritesProvider.removeFavorite(favorite);
            if (favoritesAdapter.getData().isEmpty()) {
                favoritesRecyclerView.setVisibility(View.GONE);
            } else {
                favoritesRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            favoritesRecyclerView.setVisibility(View.GONE);
        }
    }

    private Favorite getSelectedFavoritePen() {
        String currentColor = "";
        FTPenType currentPenType = null;
        int currentPenSize = 1;
        if (penType == FTToolBarTools.PEN) {
            currentColor = "#" + Integer.toHexString(mPenPref.get(mPrefColorKey, Color.parseColor(PenRackPref.DEFAULT_PEN_COLOR))).toUpperCase(Locale.ENGLISH);
            currentPenType = FTPenType.valueOf(mPenPref.get(mPrefPenKey, FTPenType.pen.toString()));
            currentPenSize = mPenPref.get(mPrefSizeKey, PenRackPref.DEFAULT_SIZE);
        } else if (penType == FTToolBarTools.HIGHLIGHTER) {
            currentColor = "#" + Integer.toHexString(mPenPref.get(mPrefColorKey, Color.parseColor(PenRackPref.DEFAULT_HIGHLIGHTER_COLOR))).toUpperCase(Locale.ENGLISH);
            currentPenType = FTPenType.valueOf(mPenPref.get(mPrefPenKey, FTPenType.highlighter.toString()));
            currentPenSize = mPenPref.get(mPrefSizeKey, PenRackPref.DEFAULT_SIZE);
        }
        Favorite selectedFavorite = new Favorite(currentColor, currentPenType, currentPenSize);
        return favoritesProvider.isFavorite(selectedFavorite) ? selectedFavorite : null;
    }

    private boolean isFavorite() {
        return favoritesProvider.isFavorite(getSelectedFavoritePen());
    }

    private FTToolBarTools getPenrackGroupType(FTPenType penType) {
        if (penType == FTPenType.pen || penType == FTPenType.pilotPen || penType == FTPenType.caligraphy) {
            return FTToolBarTools.PEN;
        } else if (penType == FTPenType.highlighter || penType == FTPenType.flatHighlighter) {
            return FTToolBarTools.HIGHLIGHTER;
        }
        return FTToolBarTools.PEN;
    }

    private int convertDpToPx(int dpValue) {
        return ScreenUtil.convertDpToPx(getContext(), dpValue);
    }

    public interface FavWidgetToolBarListener {
        void penOrHighlighterClickedInFavWidget(PenOrHighlighterInfo mPenOrHighlighterInfo, boolean isFromFavWidToolbar);

        void closeFavToolbarWidget();

        String getDocUid();
    }
}