package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.print.PrinterInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNAutoTemlpateDiaryTheme;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.store.ui.FTDiaryDatePickerPopup;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplateModelClassNew;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateMoreDetailsInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.interfaces.AddCustomThemeListener;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumbnailGenCallBack;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumbnailsGenerationListener;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTTemplateDetailedInfoAdapter extends
        BaseRecyclerAdapter<FTNTheme, FTTemplateDetailedInfoAdapter.ThemeViewHolder>
        implements ThumbnailsGenerationListener, FTDiaryDatePickerPopup.DatePickerListener {

    private ArrayList<FTNTheme> ftnThemeArrayList;
    AddCustomThemeListener addCustomThemeListener;
    FTSelectedDeviceInfo ftSelectedDeviceInfo;
    FTTemplateColors ftTemplateColorsInfo;
    FTTemplateUtil ftTemplateUtil;
    FTLineTypes ftLineTypesInfo;
    String fileName;


    String cachePath = FTConstants.TEMP_FOLDER_PATH + "TemplatesCache/";
    public boolean isEditMode = false;
    ThumbnailGenCallBack mthumbnailGenCallBack;

    FragmentManager childFragmentManager;
    FTTemplateMoreDetailsInfo ftTemplateMoreDetailsInfo = new FTTemplateMoreDetailsInfo();
    FTNThemeCategory.FTThemeType _themeType;
    public FTTemplateDetailedInfoAdapter(){}
    public FTTemplateDetailedInfoAdapter(AddCustomThemeListener addCustomThemeListener){
        this.addCustomThemeListener = addCustomThemeListener;
    }

    public void setAddCustomThemeListener(AddCustomThemeListener addCustomThemeListener) {
        this.addCustomThemeListener = addCustomThemeListener;
    }

    FTTemplateDetailedInfoAdapter(ArrayList<FTNTheme> ftnThemeArrayList,
                                  String typeOfLayout, AddCustomThemeListener addCustomThemeListener,
                                  ThumbnailGenCallBack mthumbnailGenCallBack, FragmentManager childFragmentManager,FTNThemeCategory.FTThemeType _themeType,String catName) {
        this.ftnThemeArrayList      = ftnThemeArrayList;
        this.addCustomThemeListener = addCustomThemeListener;
        this.mthumbnailGenCallBack  = mthumbnailGenCallBack;
        this.childFragmentManager   = childFragmentManager;
        ftTemplateUtil              = FTTemplateUtil.getInstance();
        ftSelectedDeviceInfo        = FTSelectedDeviceInfo.selectedDeviceInfo();
        ftTemplateColorsInfo        = ftTemplateUtil.getFtTemplateColorsObj();
        ftLineTypesInfo             = ftTemplateUtil.getFtTemplateLineInfoObj();
        this._themeType             = _themeType;
    }


    public void notifyChildAdapter(){
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ThemeViewHolder(viewGroup.getContext(), getView(viewGroup, R.layout.template_category_items_lyt_new));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder childViewHolder, int position) {

        FTNTheme ftnTheme = ftnThemeArrayList.get(position);
        Context context = childViewHolder.itemView.getContext();
        String themeName = ftnThemeArrayList.get(position).themeName;

        Log.d("TemplatePickerV2", "FTTemplateDetailedInfoAdapter onBindViewHolder categoryName::-"
                + ftnTheme.getCategoryName());

        childViewHolder.themeName.setText(themeName);

        childViewHolder.progressbarFrmLyt.setVisibility(View.VISIBLE);
        childViewHolder.template_itemIV.setVisibility(View.GONE);

        //Basic
        if (ftnTheme.getCategoryName().toLowerCase().equalsIgnoreCase("basic")) {

            if (ftnTheme instanceof FTNPaperTheme) {
                TemplateModelClassNew templateModelClassNew = new TemplateModelClassNew();
                templateModelClassNew.setFtnTheme(ftnTheme);
                templateModelClassNew.setmContext(context);
                templateModelClassNew.setFtTemplateDetailedInfoAdapter(this);
                templateModelClassNew.setChildViewHolder(childViewHolder);

                templateModelClassNew.getChildViewHolder().progressbarFrmLyt.setVisibility(View.VISIBLE);
                templateModelClassNew.getChildViewHolder().template_itemIV.setVisibility(View.GONE);

                AsyncTaskRunner aTask = new AsyncTaskRunner();
                aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, templateModelClassNew);
            }
        }

        //Not Basic/Recent/Custom
        if (!ftnTheme.getCategoryName().toLowerCase().equalsIgnoreCase("Basic") &&
                !ftnTheme.getCategoryName().toLowerCase().equalsIgnoreCase("Recent") &&
                !ftnTheme.getCategoryName().toLowerCase().equalsIgnoreCase("custom")) {
            Bitmap bitmap = null;
            childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
            childViewHolder.template_itemIV.setVisibility(View.VISIBLE);

            if (ftnTheme instanceof FTNPaperTheme) {
                bitmap = ftnTheme.themeThumbnail(context);
            } else if (ftnTheme instanceof FTNCoverTheme){
                bitmap = ftnTheme.themeThumbnail(context);
            }

            ftnTheme.bitmap = bitmap;

            Log.d("TemplatePicker==>", "ccategoryName::-" + ftnTheme.getCategoryName() +
                    " FTNCoverTheme instance::-" + (ftnTheme instanceof FTNCoverTheme) +
                    " FTNPaperTheme instance::-" + (ftnTheme instanceof FTNPaperTheme));

            Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter onBindViewHolder thumbnailURLPath::-"
                    + ftnTheme.thumbnailURLPath + " isLandscape::-" + ftnTheme.isLandscape + " bitmap::-" + bitmap +
                    " FTNCoverTheme instance::-" + (ftnTheme instanceof FTNCoverTheme) +
                    " FTNPaperTheme instance::-" + (ftnTheme instanceof FTNPaperTheme)+" themeFileURL:: "+ftnTheme.themeFileURL.getPath());
            if (bitmap != null) {
                if (this._themeType == FTNThemeCategory.FTThemeType.COVER) {
                    //
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);
                    Bitmap borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                    BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                    childViewHolder.template_itemIV.setBackground(ob);
                } else {
                    if (ftnTheme.isLandscape) {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 152),
                                (int) dipToPixels(context, 106),
                                false);
                        Bitmap borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                        BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                        childViewHolder.template_itemIV.setBackground(ob);
                    } else {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 116),
                                (int) dipToPixels(context, 143),
                                false);
                        Bitmap borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                        BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                        childViewHolder.template_itemIV.setBackground(ob);
                    }
                }
            }
        }

        //Custom
        if (ftnTheme.getCategoryName().toLowerCase().contains("custom")) {
            if (!ftnTheme.themeName.contains(context.getResources().
                    getString(R.string.template_custom_theme))) {
                Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter Custom categoryName::-" + ftnTheme.getCategoryName() +
                        " FTNCoverTheme instance::-" + (ftnTheme instanceof FTNCoverTheme) +
                        " FTNPaperTheme instance::-" + (ftnTheme instanceof FTNPaperTheme));
                Bitmap bitmap = ftnTheme.themeThumbnail(context);
                ftnTheme.bitmap = bitmap;
                childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
                childViewHolder.template_itemIV.setVisibility(View.VISIBLE);
                if (bitmap != null) {
                    Bitmap borderedBitmap;
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);
                    borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                    BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                    childViewHolder.template_itemIV.setBackground(ob);
                }
            } else {
                childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
                childViewHolder.template_itemIV.setVisibility(View.VISIBLE);
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.new_custom_template_bg);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(icon,
                        (int) dipToPixels(context, 116),
                        (int) dipToPixels(context, 143),
                        false);
                BitmapDrawable ob = new BitmapDrawable(context.getResources(), scaledBitmap);
                childViewHolder.template_itemIV.setBackground(ob);
            }
        }

        //Recent
        if (ftnTheme.getCategoryName().contains("Recent")) {

            childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
            childViewHolder.template_itemIV.setVisibility(View.VISIBLE);
            Bitmap bitmapRecent = null;
           Log.d("::TemplatePickerV2", " recentFTNTheme FTTemplateDetailedInfoAdapter FTNPaperTheme instance::-::-"
                    + (ftnTheme instanceof FTNPaperTheme) + " FTNCoverTheme instance::-" + (ftnTheme instanceof FTNCoverTheme));
            bitmapRecent = ftnTheme.themeThumbnail(context);

            Log.d("TemplatePickerV2", " recentFTNTheme FTTemplateDetailedInfoAdapter recentlySelectedThemeURLe::-"
                    + ftnTheme.thumbnailURLPath + " ftThemeType::-" + ftnTheme.ftThemeType + " bitmapRecent::-" + bitmapRecent);

            if (bitmapRecent != null) {
                saveImageInDummy(bitmapRecent, context);
                Bitmap borderedBitmap;
                if (ftnTheme.isCustomTheme) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRecent,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);
                    if (ftnTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                            (ftnTheme.thumbnailURLPath.toLowerCase())) {
                        Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                        borderedBitmap = addBlueBorder(scaledBitmap, 4);
                    } else {
                        Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                        borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                    }
                    BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                    childViewHolder.template_itemIV.setBackground(ob);
                } else {
                    if (ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRecent,
                                (int) dipToPixels(context, 116),
                                (int) dipToPixels(context, 143),
                                false);
                        if (ftnTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                                (ftnTheme.thumbnailURLPath.toLowerCase())) {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                            borderedBitmap = addBlueBorder(scaledBitmap, 4);
                        } else {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                            borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                        }
                        BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                        childViewHolder.template_itemIV.setBackground(ob);
                    } else {

                        if (ftnTheme.thumbnailURLPath.contains("land")) {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRecent,
                                    (int) dipToPixels(context, 152),
                                    (int) dipToPixels(context, 106),
                                    false);
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 80, 0, 0);
                            childViewHolder.template_itemIV.setLayoutParams(lp);

                            if (ftnTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                                    (ftnTheme.thumbnailURLPath.toLowerCase())) {
                                Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                                borderedBitmap = addBlueBorder(scaledBitmap, 4);
                            } else {
                                Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                                borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                            }

                            BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                            childViewHolder.template_itemIV.setBackground(ob);
                        } else {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRecent,
                                    (int) dipToPixels(context, 116),
                                    (int) dipToPixels(context, 143),
                                    false);
                            if (ftnTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                                    (ftnTheme.thumbnailURLPath.toLowerCase())) {
                                Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                                borderedBitmap = addBlueBorder(scaledBitmap, 4);
                            } else {
                                Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                                borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                            }
                            BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                            childViewHolder.template_itemIV.setBackground(ob);
                        }
                    }
                }

            }
        }

        //Hide and show delete icon
        if ((ftnTheme.isDownloadTheme ||
                ftnTheme.isCustomTheme ||
                ftnTheme.isBasicTheme || ftnTheme.getCategoryName().contains("Recent")) &&
                !ftnTheme.themeName.contains(context.getResources().
                        getString(R.string.template_custom_theme))) {
            Log.d("TemplatePicker==>", "Vangala Code to show delete button on top of thumbnails start ftnTheme.categoryName::"
                    + ftnTheme.getCategoryName() + " isEditMode::-" + isEditMode);
            if (isEditMode) {
                if (!ftnTheme.getCategoryName().equalsIgnoreCase(context.getResources().
                        getString(R.string.template_custom_theme)) &&
                        !ftnTheme.getCategoryName().equalsIgnoreCase("Basic")) {
                    childViewHolder.deleteTemplate.setVisibility(View.VISIBLE);
                } else {
                    childViewHolder.deleteTemplate.setVisibility(View.GONE);
                }
            } else {
                childViewHolder.deleteTemplate.setVisibility(View.GONE);
            }
        } else {
            childViewHolder.deleteTemplate.setVisibility(View.GONE);
        }

        childViewHolder.deleteTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TemplatePicker==>", " deleteTemplate themeName::-" + ftnTheme.themeName);
                addCustomThemeListener.onTemplateDelete(ftnTheme);
                isEditMode = false;
                notifyDataSetChanged();
            }
        });

        childViewHolder.template_itemIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tabSelected = ftSelectedDeviceInfo.getLayoutType();
                if (isEditMode) {
                    isEditMode = false;
                    notifyDataSetChanged();
                } else {
                    if (ftnTheme.dynamicId == 1 || ftnTheme.dynamicId == 4) {
                        new FTDiaryDatePickerPopup(ftnTheme,
                                FTTemplateDetailedInfoAdapter.this).show(childFragmentManager);
                    } else if (ftnTheme.themeName.contains(context.getResources().
                            getString(R.string.template_custom_theme))) {
                        addCustomThemeListener.onTemplateSelect(ftnTheme, ftnTheme.isLandscape);
                        ObservingService.getInstance().postNotification("addCustomTheme", ftnTheme);
                    }
                    else{
                        Log.d("TemplatePickerV2", "TemplatePickerV2 ThemeClicked Selected action FTDetailedInfoAdapter ftnTheme themeFileURL Before::-" +
                                ftnTheme.themeFileURL.getPath()+" isLandscape:: "+
                                ftnTheme.isLandscape+" themeName:: "+
                                ftnTheme.themeName+" packName:: "+
                                ftnTheme.packName+" thumbnailURLPath:: "+
                                ftnTheme.thumbnailURLPath);


                        if (ftnTheme.categoryName.toLowerCase().equalsIgnoreCase("Recent")) {
                            Log.d("TemplatePicker==>", "Template Selected action FTDetailedInfoAdapter ftnTheme packName " +
                                    ftnTheme.packName+" isLandscape:: "+
                                    ftnTheme.isLandscape+" thumbnailURLPath:: "+
                                    ftnTheme.thumbnailURLPath+" themeBgClrName:: "+
                                    ftnTheme.themeBgClrName);
                        }
                        addCustomThemeListener.onTemplateSelect(ftnTheme, ftnTheme.isLandscape);
                    }
                }
            }
        });

        childViewHolder.template_itemIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!ftnTheme.getCategoryName().toLowerCase().contains("basic")) {
                    Log.d("TemplatePicker==>", "template_itemIV setOnLongClickListener::");
                    isEditMode = true;
                    notifyDataSetChanged();
                    return true;
                } else {
                    return false;
                }

            }
        });

        childViewHolder.tempItemLyt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!ftnTheme.getCategoryName().toLowerCase().contains("basic")) {
                    Log.d("TemplatePicker==>", "tempItemLyt setOnLongClickListener::");
                    isEditMode = true;
                    notifyDataSetChanged();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void saveImageInDummy(Bitmap image, Context mContext) {
        /*
         * Saving bitmap to internal storage
         * */
        File tempCacheFiles = new File(cachePath);
        if (!tempCacheFiles.exists()) {
            tempCacheFiles.mkdir();
        }

        File pictureFile = new File(cachePath + "bitmapRecent.jpg");
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public void thumbnailCreationCompleted(FTNTheme ftnTheme, Context context, ThemeViewHolder childViewHolder, Bitmap bitmap) {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        FTNTheme ftRecentTheme = FTTemplateUtil.getInstance().getRecentPaperTheme();

        if (ftnTheme.dynamicId == 2) {
            childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
            childViewHolder.template_itemIV.setVisibility(View.VISIBLE);
        }

        Log.d("TemplatePicker==>", "ThumbnailGen FTTemplateDetailedInfoAdapter thumbnailCreationCompleted categoryName::-" + ftnTheme.getCategoryName() +
                " ftnTheme.thumbnailURLPath::-" + ftnTheme.thumbnailURLPath + " bitmap::-" + bitmap);

        if (ftnTheme.getCategoryName().contains("Recent")) {
            Log.d("TemplatePicker==>", " recentFTNTheme FTTemplateDetailedInfoAdapter recentlySelectedThemeURLe::-"
                    + ftRecentTheme.thumbnailURLPath + " ftThemeType::-" + ftnTheme.ftThemeType);

            if (bitmap != null) {
                Bitmap borderedBitmap;

                if (ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                    //
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);
                    if (ftRecentTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                            (ftnTheme.thumbnailURLPath.toLowerCase())) {
                        Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                        borderedBitmap = addBlueBorder(scaledBitmap, 4);
                    } else {
                        Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                        borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                    }
                    BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                    childViewHolder.template_itemIV.setBackground(ob);

                } else {
                    if (ftnTheme.isLandscape) {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 152),
                                (int) dipToPixels(context, 106),
                                false);
                        if (ftRecentTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                                (ftnTheme.thumbnailURLPath.toLowerCase())) {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                            borderedBitmap = addBlueBorder(scaledBitmap, 4);
                        } else {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                            borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                        }

                        if (ftnTheme.getCategoryName().contains("Recent")) {
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(0, 85, 0, 0);
                            childViewHolder.template_itemIV.setLayoutParams(lp);
                        }

                        BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                        childViewHolder.template_itemIV.setBackground(ob);
                    } else {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 116),
                                (int) dipToPixels(context, 143),
                                false);
                        if (ftRecentTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                                (ftnTheme.thumbnailURLPath.toLowerCase())) {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                            borderedBitmap = addBlueBorder(scaledBitmap, 4);
                        } else {
                            Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                            borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                        }
                        BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                        childViewHolder.template_itemIV.setBackground(ob);
                    }
                }
            } else {
                Bitmap borderedBitmap;
                Bitmap myBitmap = null;
                try {
                    myBitmap = BitmapFactory.decodeStream(FTApp.getInstance().getAssets().open(FTConstants.PAPER_FOLDER_NAME + "/" + ftRecentTheme.packName + "/" + "thumbnail_port@2x.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter myBitmap::-" + myBitmap +
                        " Path::-" + FTConstants.PAPER_FOLDER_NAME + "/" + ftRecentTheme.packName + "/" + "thumbnail_port@2x.png");
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap,
                        (int) dipToPixels(context, 116),
                        (int) dipToPixels(context, 143),
                        false);
                if (ftRecentTheme.thumbnailURLPath.toLowerCase().equalsIgnoreCase
                        (ftnTheme.thumbnailURLPath.toLowerCase())) {
                    Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL TRUE::-");
                    borderedBitmap = addBlueBorder(scaledBitmap, 4);
                } else {
                    Log.d("TemplatePicker==>", " FTTemplateDetailedInfoAdapter RECENT_PAPER_THEME_URL ELSE::-");
                    borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                }

                if (ftnTheme.getCategoryName().contains("Recent")) {
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 85, 0, 0);
                    childViewHolder.template_itemIV.setLayoutParams(lp);
                }

                BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                childViewHolder.template_itemIV.setBackground(ob);
            }
        } else {
            if (bitmap != null) {
                Bitmap scaledBitmap = null;
                if (ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                    scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);
                } else {
                    if (ftSelectedDeviceInfo.getLayoutType().contains("port")) {
                        Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter ftSelectedDeviceInfo PORT");
                        scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 116),
                                (int) dipToPixels(context, 143),
                                false);

                    } else {
                        Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter ftSelectedDeviceInfo LAND");
                        scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                                (int) dipToPixels(context, 152),
                                (int) dipToPixels(context, 106),
                                false);
                    }
                }

                Bitmap borderedBitmap = addWhiteBorder(scaledBitmap, 2);
                BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
                childViewHolder.template_itemIV.setBackground(ob);
            }
        }


    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.parseColor("#1C000000"));
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    private Bitmap addBlueBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.parseColor("#5377F8"));
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    @Override
    public void thumbnailsGeneration(boolean status, Context mContext, ThemeViewHolder childViewHolder,
                                     FTNPaperTheme ftnPaperTheme, Bitmap bitmap) {
        /*Log.d("TemplatePickerV2", "FTTemplateDetailedInfoAdapter thumbnailsGeneration::-" + status
        +" thumbnailURLPath"+ftnPaperTheme.thumbnailURLPath);*/
        thumbnailCreationCompleted(ftnPaperTheme, mContext,
                childViewHolder, bitmap);

    }

    @Override
    public void onDatesSelected(FTNTheme theme, Date startDate, Date endDate) {
        FTNAutoTemlpateDiaryTheme diaryTheme = (FTNAutoTemlpateDiaryTheme) theme;
        diaryTheme.startDate = startDate;
        diaryTheme.endDate = endDate;
        diaryTheme.dynamicId = theme.dynamicId;
        addCustomThemeListener.onTemplateSelect(theme, theme.isLandscape);
    }

    @Override
    public int getItemCount() {
        return ftnThemeArrayList.size();
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void setLinearLayoutSpec(RelativeLayout progressbarLayout, Context mContext, String layoutType) {
        ViewGroup.LayoutParams params = progressbarLayout.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        if (layoutType.toLowerCase().equalsIgnoreCase("portrait")) {
            params.height = (int) dipToPixels(mContext, 143);
            params.width = (int) dipToPixels(mContext, 116);
        } else {
            params.height = (int) dipToPixels(mContext, 106);
            params.width = (int) dipToPixels(mContext, 152);
        }

        progressbarLayout.setLayoutParams(params);
    }

    public class ThemeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.templateNameTv)
        TextView themeName;

        @BindView(R.id.template_itemIV)
        public ShapeableImageView template_itemIV;

        @BindView(R.id.tempItemLyt)
        ConstraintLayout tempItemLyt;

        @BindView(R.id.progressbar)
        public ProgressBar progressbar;

        @BindView(R.id.progressbarFrmLyt)
        public RelativeLayout progressbarFrmLyt;

        @BindView(R.id.item_choose_cover_paper_delete_image_view)
        ImageButton deleteTemplate;

        ThemeViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (itemView.getId() == R.id.customCreateRelLyt) {
                deleteTemplate.setVisibility(View.GONE);
            }

            float radius = context.getResources().getDimension(R.dimen.notebook_template_corner_radius);
            template_itemIV.setShapeAppearanceModel(template_itemIV
                    .getShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, radius)
                    .build());

            setLinearLayoutSpec(progressbarFrmLyt, context, FTSelectedDeviceInfo.selectedDeviceInfo().getLayoutType());


        }

    }

    private class AsyncTaskRunner extends AsyncTask<TemplateModelClassNew, String, FTNPaperTheme> {
        TemplateModelClassNew templateModelClassNew;

        @Override
        protected FTNPaperTheme doInBackground(TemplateModelClassNew... params) {
            templateModelClassNew = params[0];
            FTNPaperTheme paperTheme = null;
            AtomicReference<FTUrl> kdn = new AtomicReference<>();
            templateModelClassNew.getFtnTheme().template(templateModelClassNew.getmContext(),
                    (documentInfo, generationError) -> {
                        if (documentInfo != null) {
                            kdn.set(documentInfo.inputFileURL);
                        }
                    });

            if (kdn.get() != null) {
                // Log.d("TemplatePickerV2", "FTTemplateDetailedInfoAdapter AsyncTaskRunner doInBackground::-");
                paperTheme = templateModelClassNew.getFtnTheme().basicTemplatePDFGenerated(kdn.get(), templateModelClassNew.getmContext());
            }

            return paperTheme;
        }

        @Override
        protected void onPostExecute(FTNPaperTheme paperTheme) {
            // Log.d("TemplatePickerV2", "FTTemplateDetailedInfoAdapter AsyncTaskRunner onPostExecute paperTheme::-"+paperTheme);

            if (paperTheme != null) {
                //notifyItemChanged(0);
                //Log.d("TemplatePickerV2", "basicTemplatesAliggnment AsyncTaskRunner onPostExecute thumbnailURLPath:: "+paperTheme.thumbnailURLPath);
                basicTemplatesAliggnment(paperTheme, templateModelClassNew.getmContext(), templateModelClassNew.getChildViewHolder(), templateModelClassNew);
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            //updateProgressUI();
        }
    }

    public void basicTemplatesAliggnment(FTNPaperTheme paperTheme, Context context, ThemeViewHolder childViewHolder, TemplateModelClassNew templateModelClassNew) {
        // Log.d("TemplatePickerV2", "basicTemplatesAliggnment thumbnailURLPath:: "+paperTheme.thumbnailURLPath);

        childViewHolder.progressbarFrmLyt.setVisibility(View.GONE);
        childViewHolder.template_itemIV.setVisibility(View.VISIBLE);
        childViewHolder.themeName.setText(templateModelClassNew.getFtnTheme().themeName);
        if (paperTheme.bitmap != null) {
            Bitmap scaledBitmap = null;
            if (templateModelClassNew.ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                scaledBitmap = Bitmap.createScaledBitmap(paperTheme.bitmap,
                        (int) dipToPixels(context, 116),
                        (int) dipToPixels(context, 143),
                        false);
            } else {
                if (FTSelectedDeviceInfo.selectedDeviceInfo().getLayoutType().toLowerCase().contains("port")) {
                    Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter ftSelectedDeviceInfo PORT");
                    scaledBitmap = Bitmap.createScaledBitmap(paperTheme.bitmap,
                            (int) dipToPixels(context, 116),
                            (int) dipToPixels(context, 143),
                            false);

                } else {
                    Log.d("TemplatePicker==>", "FTTemplateDetailedInfoAdapter ftSelectedDeviceInfo LAND");
                    scaledBitmap = Bitmap.createScaledBitmap(paperTheme.bitmap,
                            (int) dipToPixels(context, 152),
                            (int) dipToPixels(context, 106),
                            false);
                }
            }

            Bitmap borderedBitmap = addWhiteBorder(scaledBitmap, 2);
            BitmapDrawable ob = new BitmapDrawable(context.getResources(), borderedBitmap);
            childViewHolder.template_itemIV.setBackground(ob);

        }
    }

}
