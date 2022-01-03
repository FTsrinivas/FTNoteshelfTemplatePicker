package com.fluidtouch.noteshelf.templatepicker.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Size;

import com.fluidtouch.dynamicgeneration.FTDynamicTemplateGenerator;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.generator.FTAutoTemplateGenerationCallback;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.TemplateModelClass;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumbnailGenCallBack;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumbnailsGenerationListener;

import java.lang.ref.WeakReference;

public class DummyAsyncTask extends AsyncTask<TemplateModelClass, String, FTUrl> {

    private FTTemplateDetailedInfoAdapter mContext;

    FTSelectedDeviceInfo ftSelectedDeviceInfo;
    FTNPaperTheme ftnPaperTheme;
    FTDocumentInputInfo documentInfo;
    Error generationError;
    ThumbnailGenCallBack mthumbnailGenCallBack;
    FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder;
    private ThumbnailsGenerationListener mthumbnailsGenerationListener;

    public DummyAsyncTask(FTTemplateDetailedInfoAdapter mthumbnailsGenerationListener,
                            FTSelectedDeviceInfo ftSelectedDeviceInfo,
                            FTNPaperTheme ftnPaperTheme,
                            ThumbnailGenCallBack mthumbnailGenCallBack,
                            FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder){
        this.mthumbnailsGenerationListener = mthumbnailsGenerationListener;
        this.ftSelectedDeviceInfo = ftSelectedDeviceInfo;
        this.ftnPaperTheme = ftnPaperTheme;
        this.mthumbnailGenCallBack = mthumbnailGenCallBack;
        this.childViewHolder = childViewHolder;
    }

    @Override
    protected FTUrl doInBackground(TemplateModelClass... params) {

        ftnPaperTheme.template(FTApp.getInstance().getApplicationContext(), (documentInfo, generationError) -> {
            //((FTNPaperTheme) ftnTheme).themeThumbnailOnCallBack(context, ftnTheme,this,childViewHolder);
            this.documentInfo = documentInfo;
        });
        /*FTUrl pdfFileURL = dynamicTemplateGenerator.generate(templateModelClass.getFtnTheme());
        Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter doInBackground thumbnailURLPath::-"+pdfFileURL.getPath());
        return pdfFileURL;*/
        return this.documentInfo.inputFileURL;
    }

    @Override
    protected void onPostExecute(FTUrl pdfFileURL) {
        Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter DummyAsyncTask onPostExecute ftUrl::-"+pdfFileURL);
        /*if (pdfFileURL != null) {*/
            Log.d("TemplatePicker==>","FTTemplateDetailedInfoAdapter DummyAsyncTask onPostExecute documentInfo.inputFileURL getPath::-"+documentInfo.inputFileURL.getPath());
            //ftnPaperTheme.basicTemplatePDFGenerated(pdfFileURL,mContext,mCallBack,this.childViewHolder);
            //ftAutoTemplateGenerationCallback.onGenerated(this.documentInfo,this.generationError);
            /*FTDocumentInputInfo documentInfo = new FTDocumentInputInfo();
            documentInfo.inputFileURL = pdfFileURL;
            callback.onGenerated(documentInfo,null);*/
            /*((FTNPaperTheme) ftnPaperTheme).basicTemplatePDFGenerated(documentInfo.inputFileURL,mContext,
                    mthumbnailGenCallBack,childViewHolder);*/
            //mthumbnailsGenerationListener.thumbnailsGeneration(true);
            /*ftnPaperTheme.basicTemplatePDFGenerated(pdfFileURL,FTApp.getInstance().getApplicationContext(),
                    mthumbnailsGenerationListener,childViewHolder);*/
        //}
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // display a progress dialog for good user experience
        /*childViewHolder.progressbarFrmLyt.setVisibility(View.VISIBLE);
        childViewHolder.template_itemIV.setVisibility(View.GONE);*/
    }

}
