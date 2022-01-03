package com.fluidtouch.noteshelf.templatepicker.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;

import com.fluidtouch.dynamicgeneration.FTDynamicTemplateGenerator;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplateModelClass;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;

public class PDFGenAsyncTask extends AsyncTask<TemplateModelClass, String, FTUrl> {
    /*private WeakReference<TemplateModelClass> myObjectWeakReference;
    private TemplateModelClass templateModelClassAT;*/
    private Context mContext;
    FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder;
    FTSelectedDeviceInfo ftSelectedDeviceInfo;
    FTNPaperTheme ftnPaperTheme;
    FTDocumentInputInfo documentInfo;
    Error generationError;
    FTTemplateDetailedInfoAdapter callBack;

    public PDFGenAsyncTask(Context context,
                           FTTemplateDetailedInfoAdapter callBack,
                           FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder,
                           FTSelectedDeviceInfo ftSelectedDeviceInfo,FTNPaperTheme ftnPaperTheme){
        this.mContext = context;
        this.childViewHolder = childViewHolder;
        this.ftSelectedDeviceInfo = ftSelectedDeviceInfo;
        this.ftnPaperTheme = ftnPaperTheme;
        this.callBack = callBack;
    }

    @Override
    protected FTUrl doInBackground(TemplateModelClass... params) {

        if (ftnPaperTheme.dynamicId == 2) {

            Size mSize = new Size(ftSelectedDeviceInfo.getPageWidth(), ftSelectedDeviceInfo.getPageHeight());
            Log.d("TemplatePicker==>","FTNPaperTheme themeThumbnailOnCallBack Theme getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                    " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight()+
                    " getPageThemeBgClr::-"+ftSelectedDeviceInfo.getThemeBgClrName()+
                    " ftnTheme.thumbnailURLPath::-"+ftnPaperTheme.thumbnailURLPath);
            FTNDynamicTemplateTheme inTheme =  (FTNDynamicTemplateTheme) ftnPaperTheme;
            TemplateModelClass templateModelClass = new TemplateModelClass();
            templateModelClass.setFtnTheme(inTheme);
            templateModelClass.setLandscape(inTheme.isLandscape);
            templateModelClass.setmContext(mContext);
            templateModelClass.setTemplateSize(mSize);

            FTDynamicTemplateGenerator dynamicTemplateGenerator = new FTDynamicTemplateGenerator(
                    templateModelClass.getFtnTheme().getTemplateInfoDict() ,
                    templateModelClass.isLandscape(),
                    templateModelClass.getTemplateSize(),
                    templateModelClass.getmContext());
            Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter doInBackground getFtnTheme::-"+templateModelClass.getFtnTheme().themeClassName);
            FTUrl pdfFileURL = dynamicTemplateGenerator.generate(templateModelClass.getFtnTheme());
            Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter doInBackground thumbnailURLPath::-"+pdfFileURL.getPath());
            return pdfFileURL;
        }
        return null;
    }

    @Override
    protected void onPostExecute(FTUrl pdfFileURL) {
        Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter onPostExecute ftUrl::-"+pdfFileURL);
        if (pdfFileURL != null) {
            Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter onPostExecute ftUrl getPath::-"+pdfFileURL.getPath());
            /*FTUrl pdfFileURL, Context mContext,
                    FTTemplateDetailedInfoAdapter callBack,
                    FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder*/
            //ftnPaperTheme.basicTemplatePDFGenerated(pdfFileURL,mContext,this.callBack,this.childViewHolder);
            //ftAutoTemplateGenerationCallback.onGenerated(this.documentInfo,this.generationError);
            /*FTDocumentInputInfo documentInfo = new FTDocumentInputInfo();
            documentInfo.inputFileURL = pdfFileURL;*/
            //callback.onGenerated(documentInfo,null);
        }

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // display a progress dialog for good user experience
        /*childViewHolder.progressbarFrmLyt.setVisibility(View.VISIBLE);
        childViewHolder.template_itemIV.setVisibility(View.GONE);*/
    }

}
