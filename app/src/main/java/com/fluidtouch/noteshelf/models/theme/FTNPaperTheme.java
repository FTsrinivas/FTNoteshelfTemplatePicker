package com.fluidtouch.noteshelf.models.theme;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.generator.FTAutoTemplateGenerationCallback;
import com.fluidtouch.noteshelf.generator.FTAutoTemplateGenerator;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplateModelClassNew;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class FTNPaperTheme extends FTNTheme {

    transient String cachePath = FTConstants.TEMP_FOLDER_PATH + "TemplatesCache/";
    transient FTTemplateDetailedInfoAdapter mCallBack;
    transient FTTemplateDetailedInfoAdapter.ThemeViewHolder mChildViewHolder;

    public static FTNTheme theme(FTUrl url) {
        NSDictionary metadataDict = new NSDictionary();
        String metadataUrl = url.withAppendedPath("metadata.plist").getPath();

        if (FTNTheme.isTheThemeExists(metadataUrl)) {
            try {
                InputStream inputStream = null;
                if (metadataUrl.contains("download")) {
                    inputStream = new FileInputStream(metadataUrl);
                } else {
                    inputStream = FTApp.getInstance().getCurActCtx().getAssets().open(metadataUrl);
                }
                //InputStream inputStream = FTApp.getInstance().getCurActCtx().getAssets().open(metadataUrl);
                metadataDict = (NSDictionary) PropertyListParser.parse(inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //DUMMY ELSE
        }

        return theme(metadataDict);
    }

    public static FTNTheme theme(NSDictionary metadata) {
        if (metadata.containsKey(FTNTheme.DYNAMIC_ID)) {
            int dynamicId = Integer.parseInt(metadata.objectForKey(FTNTheme.DYNAMIC_ID).toString());
            if (dynamicId == 2 || dynamicId == 3) {
                return new FTNDynamicTemplateTheme(metadata);
            } else if (dynamicId == 1 || dynamicId == 4) {
                return new FTNAutoTemlpateDiaryTheme(metadata);
            }
        }
        return new FTNPaperTheme();
    }

    @Override
    public void template(Context mContext, FTAutoTemplateGenerationCallback callback) {

        //TODO:
        //Check if template is generated for ftSelectedDeviceInfo
        // If generated return cached URL
        // If not generated FTAutoTemplateGenerator.autoTemplateGenerator()
        //callback.onGenerated();
        //if theme is custom, directly return theme template URL
        FTUrl url = this.themeTemplateURL();

        //Check if template is generated for ftSelectedDeviceInfo
        // If generated return cached URL
        if (new File(url.getPath()).exists()) {
            FTDocumentInputInfo documentInfo = new FTDocumentInputInfo();
            documentInfo.inputFileURL = url;
            callback.onGenerated(documentInfo, null);
            return;
        }

            if (this.dynamicId == 2) {
                FTAutoTemplateGenerator.autoTemplateGenerator(this).generate(mContext,
                        (documentInfo, generationError) -> {
                            Log.d("TemplatePickerV2","dynamicId 2 documentInfo.inputFileURL.getPath():: "+documentInfo.inputFileURL.getPath() +" url.getPath:: "+url.getPath());
                            callback.onGenerated(documentInfo, generationError);
                            basicTemplatePDFGenerated(documentInfo.inputFileURL, mContext);
                        });
            }  else {
                AsyncTask.execute(()->{
                        FTAutoTemplateGenerator.autoTemplateGenerator(this).generate(mContext,
                        (documentInfo, generationError) -> {
                            Log.d("TemplatePickerV2","dynamicId NOT 2 documentInfo.inputFileURL.getPath():: "+documentInfo.inputFileURL.getPath() +" url.getPath:: "+url.getPath());
                            callback.onGenerated(documentInfo, generationError);
                        });
                });
            }

    }

    @Override
    public FTUrl themeTemplateURL() {
        /*String templateName = "/template";
        int device = FTApp.getInstance().getCurActCtx().getResources().getInteger(R.integer.device);
        if (device == 600) {
            templateName += "_" + device;
        } else if (device == 360 || device == 300) {
            templateName += "_360";
        }
        templateName += ".pdf";
        FTUrl templateURL = null;
        if (isDownloadTheme || isCustomTheme) {
            String prefix = (isDownloadTheme ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.CUSTOM_PAPERS_PATH) + this.packName;
            if (new File(prefix + templateName).exists()) {
                return FTUrl.parse(prefix + templateName);
            } else {
                return FTUrl.parse(prefix + "/template.pdf");
            }
        } else {
            if (this.themeFileURL.getPath().contains("stockPapers")) {
                if (this.isLandscape) {
                    if (this.packName.contains("Portrait")) {
                        String _packName = this.packName;
                        _packName = _packName.replaceAll("Portrait","Landscape");
                        return this.themeFileURL = FTUrl.parse("stockPapers/" + _packName +"/template.pdf");
                    } else if (this.packName.contains("Land")) {
                        return this.themeFileURL = FTUrl.parse("stockPapers/" + this.packName + "/template.pdf");
                    } else {
                        return this.themeFileURL = FTUrl.parse("stockPapers/" + this.packName + "/template_land.pdf");
                    }
                }
            }
            return FTUrl.parse("stockPapers/" + this.packName +templateName);
        }*/

        if (isDownloadTheme || isCustomTheme) {
            File file = null;
            if (isDownloadTheme) {
                if (isLandscape) {
                    return this.themeFileURL = FTUrl.parse(FTConstants.DOWNLOADED_PAPERS_PATH2 + this.packName +"/template_land.pdf");
                } else {
                    return this.themeFileURL = FTUrl.parse(FTConstants.DOWNLOADED_PAPERS_PATH2 + this.packName +"/template_port.pdf");
                }

            } else {
                return this.themeFileURL = FTUrl.parse((FTConstants.CUSTOM_PAPERS_PATH) + this.packName + "/template.pdf");
            }
        } else {
            if (this.isLandscape) {
                if (this.packName.contains("Portrait")) {
                    String _packName = this.packName;
                    _packName = _packName.replaceAll("Portrait","Landscape");
                    return this.themeFileURL = FTUrl.parse("stockPapers/" + _packName +"/template.pdf");
                } else if (this.packName.contains("Land")) {
                    return this.themeFileURL = FTUrl.parse("stockPapers/" + this.packName + "/template.pdf");
                } else {
                    return this.themeFileURL = FTUrl.parse("stockPapers/" + this.packName + "/template_land.pdf");
                }
            }
        }
        return FTUrl.parse("stockPapers/" + this.packName + "/template.pdf");

    }

    @Override
    public void themeThumbnailOnCallBack(Context mContext,
                                         FTNTheme ftnTheme,
                                         FTTemplateDetailedInfoAdapter callBack,
                                         FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder) {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        Log.d("TemplatePicker==>", "FTNPaperTheme themeThumbnailOnCallBack Before::-");
        Log.d("TemplatePicker==>", "ThumbnailGen FTNPaperTheme checkThumbnail categoryName::-" + ftnTheme.categoryName +
                " checkThumbnail status::-" + checkThumbnail(ftnTheme) +
                " isDefaultTheme::-" + ftnTheme.isDefaultTheme
                + " thumbnailURLPath::-" + ftnTheme.thumbnailURLPath
                + " File Exists" + new File(ftnTheme.thumbnailURLPath).exists()
                +" mCallBack:: "+mCallBack);
        mCallBack = callBack;
        this.mChildViewHolder = childViewHolder;
        FTUrl abc;
         /*template(mContext,(documentInfo, generationError) -> {
            //TODO::
            //save generated PDf in proper location
            // MOve Temp/Pages to Library/papers_themes_v2 path/Pages
            //basicTemplatePDFGenerated(documentInfo.inputFileURL,mContext);
             abc = documentInfo.inputFileURL;
        });*/

        if (ftnTheme instanceof FTNPaperTheme) {

            TemplateModelClassNew templateModelClassNew = new TemplateModelClassNew();
            templateModelClassNew.setFtnTheme(ftnTheme);
            templateModelClassNew.setmContext(mContext);
            templateModelClassNew.setFtTemplateDetailedInfoAdapter(callBack);
            templateModelClassNew.setChildViewHolder(childViewHolder);

            templateModelClassNew.getChildViewHolder().progressbarFrmLyt.setVisibility(View.VISIBLE);
            templateModelClassNew.getChildViewHolder().template_itemIV.setVisibility(View.GONE);

            AsyncTaskRunner aTask = new AsyncTaskRunner();
            aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, templateModelClassNew);
        }

        if (dynamicId == 2) {
            //PDF Generation
        } else if (theme().getCategoryName() != rencents || custom) {
            //Not basic, Not recent, Not custom
            theme if assets lo ?
        }

    }

    protected Bitmap pdfToBitmap(FTNPaperTheme theme, File pdfFile, String fileName, Context mContext) {
        /*
         * Conversion of PDF to Image format
         * */
        Bitmap bitmap = null;
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));


            PdfRenderer.Page page = renderer.openPage(0);
            FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
            int height = isLandscape? 188 : 340;
            int width  = aspectSize((int) ftSelectedDeviceInfo.getPageWidth(),(int) ftSelectedDeviceInfo.getPageHeight(),height);
        /*    int width  = 274;
            int height = 188;*/
          /*  int width = (int) ftSelectedDeviceInfo.getPageWidth();*/



//            int height = (int) ftSelectedDeviceInfo.getPageHeight();
            Log.d("TemplatePicker==>", "FTDynamicTemplateFormat storeImage getPageWidth::-" + width
                    + " getPageHeight::-" + height);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            bitmaps.add(bitmap);
            page.close();
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (bitmaps != null && !bitmaps.isEmpty()) {
            storeImage(theme, bitmaps.get(0), fileName, mContext);
        }

        return bitmap;
    }

    private int aspectSize(int originalWidth,int originalHeight,int bitmapHeight){
        return (bitmapHeight * originalWidth)/originalHeight;
    }

    private void storeImage(FTNPaperTheme theme, Bitmap image, String fileName,
                            Context mContext) {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        String thmumbNailName = null;
        String tabSelected = ftSelectedDeviceInfo.getLayoutType();

        Log.d("TemplatePicker==>", "FTDynamicTemplateFormat storeImage getPageWidth::-" + ftSelectedDeviceInfo.getPageWidth()
                + " getPageHeight::-" + ftSelectedDeviceInfo.getPageHeight() +
                " tabSelected::-" + tabSelected + " theme.categoryName::-" + theme.categoryName +
                " getThemeBgClrName::-" + ftSelectedDeviceInfo.getThemeBgClrName() +
                " getLineType::-" + ftSelectedDeviceInfo.getLineType() +
                " getLayoutType::-" + ftSelectedDeviceInfo.getLayoutType());


        Log.d("TemplatePicker==>", " storeImage categoryName:-" + theme.categoryName + " isDefaultTheme::-" + theme.isDefaultTheme);

        if (theme.categoryName.contains("Basic")) {
            thmumbNailName = "thumbnail"
                    + "_" + fileName
                    + "_" + ftSelectedDeviceInfo.getPageWidth()
                    + "_" + ftSelectedDeviceInfo.getPageHeight()
                    + "_" + ftSelectedDeviceInfo.getThemeBgClrName()
                    + "_" + ftSelectedDeviceInfo.getLineType()
                    + "_" + ftSelectedDeviceInfo.getLayoutType() + ".jpg";
        }

        saveInCache(thmumbNailName, image, mContext);
    }

    private void saveInCache(String thmumbNailName, Bitmap image, Context mContext) {
        /*
         * Saving bitmap to internal storage
         * */
        File tempCacheFiles = new File(cachePath);
        if (!tempCacheFiles.exists()) {
            tempCacheFiles.mkdir();
        }

        File pictureFile = new File(cachePath + thmumbNailName);
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
        /*Log.d("TemplatePickerV2", " basicTemplatePDFGenerated "+
                " mCallBack:: "+mCallBack);*/
        ((Activity) mContext).runOnUiThread(() -> {
            if (mCallBack != null) {
               /* Log.d("TemplatePickerV2", " basicTemplatePDFGenerated "+
                        " mCallBack inside:: "+mCallBack);*/

                mChildViewHolder.progressbar.setVisibility(View.GONE);
                mChildViewHolder.template_itemIV.setVisibility(View.VISIBLE);
                Log.d("TemplatePickerV2", " basicTemplatePDFGenerated "+
                        " thumbnailURLPath:: "+thumbnailURLPath);
                mCallBack.thumbnailsGeneration(true, mContext, mChildViewHolder, this, this.themeThumbnail(mContext));
            }
        });

    }

    private boolean checkThumbnail(FTNTheme ftnTheme) {
        if (new File(ftnTheme.thumbnailURLPath).exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Bitmap themeThumbnail(Context context) {
        //TODO::
        //Create new method thumbnailOncallBack
        //make above method as call back method
        //If thumbnail exists, call the callback immediately
        //If not exists, call the template methods to generate the template and generate the thumbnail

        /*AssetManager assetmanager = context.getAssets();
        InputStream is = null;

        try {
            String tabName = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");
            String imageName = null;
            if (tabName.contains("port")) {
                imageName = "thumbnail_port.png";
            } else {
                imageName = "thumbnail_land@2x.png";
            }

            if (isDownloadTheme) {
                File file = new File((FTConstants.DOWNLOADED_PAPERS_PATH2) + this.packName + "/" + imageName);
                is = new FileInputStream(file);
            } else if (isCustomTheme) {
                File file = new File((FTConstants.CUSTOM_PAPERS_PATH) + this.packName + "/thumbnail@2x.png");
                is = new FileInputStream(file);
            } else if (this.categoryName.contains("Recent")) {
                if (this.dynamicId == 2) {
                    File file = new File(this.thumbnailURLPath);
                    is = new FileInputStream(file);
                } else {
                    if (AssetsUtil.isAssetExists(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName)) {
                        is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName);
                    } else {
                        is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + "thumbnail@2x.png");
                    }
                }
            } else if (this.categoryName.contains("Basic")) {
                Log.d("TemplatePicker==>", "themeThumbnail themeName::-" + this.themeName + " this.thumbnailURLPath::-" + this.thumbnailURLPath);
                File file = new File(this.thumbnailURLPath);
                is = new FileInputStream(file);
            } else {
                if (AssetsUtil.isAssetExists(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName)) {
                    is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName);
                } else {
                    is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + "thumbnail@2x.png");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(is);*/

        AssetManager assetmanager = context.getAssets();
        InputStream is = null;
        String path = null;
        String imageName = null;

        String tabName = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");
        if (tabName.contains("port")) {
            imageName = "thumbnail_port@2x.png";
        } else {
            imageName = "thumbnail_land@2x.png";
        }

        try {
            if (isDownloadTheme || isCustomTheme) {
                File file = null;
                if (isDownloadTheme) {
                    if (isLandscape) {
                        file = new File((FTConstants.DOWNLOADED_PAPERS_PATH2) + this.packName + "/thumbnail_land@2x.png");

                    } else {
                        file = new File((FTConstants.DOWNLOADED_PAPERS_PATH2) + this.packName + "/thumbnail_port@2x.png");

                    }

                } else {
                    file = new File((FTConstants.CUSTOM_PAPERS_PATH) + this.packName + "/thumbnail@2x.png");
                }
                is = new FileInputStream(file);

            } else if (this.categoryName.contains("Recent")) {
                //Log.d("TemplatePickerV2:::::","Recents Theme thumbnailURLPath:: "+this.thumbnailURLPath + " dynamicId:: "+this.dynamicId);

                if (this.dynamicId == 2) {
                    File file = new File(this.thumbnailURLPath);
                    if (!file.exists()) {
                        is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + "Plain.nsp" + "/thumbnail_port@2x.png");
                    } else {
                        is = new FileInputStream(file);
                    }

                } else {
                    if (AssetsUtil.isAssetExists(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName)) {
                        is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + imageName);
                    } else {
                        is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/" + "thumbnail@2x.png");
                    }
                }
            } else {
                /*if (this.packName.toLowerCase().contains("land")) {
                    is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/thumbnail_land@2x.png");
                } else {
                    is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/thumbnail@2x.png");
                }*/

                Log.d("TemplatePickerV2","FTNpaperTheme Basic "+this.isLandscape+" themeName:: "+this.themeName);
                if (this.isLandscape) {
                    path = FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/thumbnail_land@2x.png";
                    is = assetmanager.open(path);
                } else {
                    path = FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/thumbnail_port@2x.png";
                    is = assetmanager.open(FTConstants.PAPER_FOLDER_NAME + "/" + this.packName + "/thumbnail_port@2x.png");
                }
            }

            Log.d("TemplatePickerV2","FTNpaperTheme isLandscape "+this.isLandscape+" path:: "+path +" isLandscape"+isLandscape);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(is);
    }

    @Override
    public FTNPaperTheme basicTemplatePDFGenerated(FTUrl pdfFileURL, Context mContext) {
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        //this.themeName = FTTemplatesInfoSingleton.getInstance().getNSPFileNameWithoutExtn(this);
       /* Log.d("TemplatePickerV2", " basicTemplatePDFGenerated this.themeName::-" + this.themeName
                + " isDefaultTheme:: " + this.isDefaultTheme + " pdfFileURL.getPath()::-" + pdfFileURL.getPath()+
                " thumbnailURLPath:: "+thumbnailURLPath);
*/
        this.bitmap = pdfToBitmap(this, new File(pdfFileURL.getPath()), this.themeName, mContext);
        Log.d("TemplatePickerV2", " basicTemplatePDFGenerated this.bitmap::-" +this.bitmap);
        return this;
    }

    private class AsyncTaskRunner extends AsyncTask<TemplateModelClassNew, String, FTNPaperTheme> {
        TemplateModelClassNew templateModelClassNew;

        @Override
        protected FTNPaperTheme doInBackground(TemplateModelClassNew... params) {
            templateModelClassNew = params[0];
            FTNPaperTheme paperTheme = null;
            //Log.d("TemplatePickerV2", "FTTemplateDetailedInfoAdapter doInBackground" + templateModelClassNew);
            AtomicReference<FTUrl> kdn = new AtomicReference<>();
            templateModelClassNew.getFtnTheme().template(templateModelClassNew.getmContext(),
                    (documentInfo, generationError) -> {
                        if (documentInfo != null) {
                            kdn.set(documentInfo.inputFileURL);
                        }
                    });

            if (kdn.get() != null) {
                paperTheme = basicTemplatePDFGenerated(kdn.get(), templateModelClassNew.getmContext());
            }

            return paperTheme;
        }

        @Override
        protected void onPostExecute(FTNPaperTheme bitmap) {
            templateModelClassNew.getChildViewHolder().progressbarFrmLyt.setVisibility(View.GONE);
            templateModelClassNew.getChildViewHolder().template_itemIV.setVisibility(View.VISIBLE);

            if (bitmap != null) {
                //templateModelClassNew.getFtTemplateDetailedInfoAdapter().basicTemplatesAliggnment(bitmap,templateModelClassNew.getmContext(),templateModelClassNew.getChildViewHolder());
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            //updateProgressUI();
        }
    }

}
