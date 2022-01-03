package com.fluidtouch.noteshelf.document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FileExporter.FTFileExporter;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.pdfexport.FTPdfCreator;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 2019-09-16
 */
public class FTShareOptionPopUp extends PopupWindow {
//    @BindView(R.id.share_options_nsa_text_view)
//    protected TextView shareOptionsNsaTextView;
//    @BindView(R.id.share_options_nsa_view)
//    protected View shareOptionsNsaView;

    private Context context;
    private FTNoteshelfDocument currentDocument;
    private int currPosition;
    private ArrayList<FTNoteshelfPage> pagesToExport;

    FTShareOptionPopUp(Context context, FTNoteshelfDocument currentDocument, ArrayList<FTNoteshelfPage> pagesToExport, int currPosition) {
        super(context);
        this.context = context;
        this.currentDocument = currentDocument;
        this.currPosition = currPosition;
        this.pagesToExport = pagesToExport;

        init();
    }

    private void init() {
        setWidth(context.getResources().getDimensionPixelOffset(R.dimen.share_options_width));
        //setHeight(context.getResources().getDimensionPixelOffset(R.dimen.share_options_height));

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.popup_export_format, null);
        setContentView(popUpView);
        setFocusable(true);
        setBackgroundDrawable(null);

        ButterKnife.bind(this, popUpView);
//        shareOptionsNsaTextView.setVisibility(View.GONE);
//        shareOptionsNsaView.setVisibility(View.GONE);
    }

    @OnClick(R.id.export_format_pdf_text_view)
    void exportAsPdf() {
        FTLog.crashlyticsLog("UI: Clicked share notebook as .pdf");
        FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.PDF_EXTENSION);
        ArrayList<FTNoteshelfPage> list = new ArrayList<>();
        list.add(currentDocument.pages(context).get(currPosition));
        new FTPdfCreator(context).noteshelfDocument(currentDocument).pages(list).onCreateResponse(new FTPdfCreator.OnCreateResponse() {
            @Override
            public void onPdfCreated(File file) {
                context.startActivity(getShareFilteredIntent(FileUriUtils.getUriForFile(context, file)));
            }

            @Override
            public void onPdfCreateFailed() {

            }
        }).Create();
        dismiss();
    }

    @OnClick(R.id.export_format_png_text_view)
    void exportAsPng() {
        FTLog.crashlyticsLog("UI: Clicked share notebook as .png");
        FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.PNG_EXTENSION);
        final FTSmartDialog smartDialog = new FTSmartDialog()
                .setMessage(context.getString(R.string.generating)).show(((AppCompatActivity) context).getSupportFragmentManager());

        FTFileExporter fileExporter = new FTFileExporter();
        fileExporter.exportPages(context, pagesToExport, FTConstants.PNG_EXTENSION, (file, error) -> ((Activity) context).runOnUiThread(() -> {
            smartDialog.dismissAllowingStateLoss();
            if (file != null && error == null) {
                context.startActivity(FTShareOptionPopUp.this.getShareFilteredIntent(FileUriUtils.getUriForFile(context, file)));
            } else {
                Toast.makeText(context, R.string.export_failed, Toast.LENGTH_SHORT).show();
            }
        }));
        smartDialog.setCancellable(() -> {
            Toast.makeText(context, R.string.cancelled, Toast.LENGTH_SHORT).show();
            fileExporter.cancelExporting();
        });
        dismiss();
    }

    private Intent getShareFilteredIntent(Uri exportFileUri) {
        String mimeType = FTFileManagerUtil.getFileMimeTypeByUri(context, exportFileUri);
        PackageManager pm = context.getPackageManager();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, exportFileUri);
//        List resInfo = pm.queryIntentActivities(shareIntent, 0);
//        List<LabeledIntent> intentList = new ArrayList<>();
//        for (int i = 0; i < resInfo.size(); i++) {
//            ResolveInfo ri = (ResolveInfo) resInfo.get(i);
//            String packageName = ri.activityInfo.packageName;
//            if (!packageName.contains(context.getResources().getString(R.string.app_name).toLowerCase())) {
//                shareIntent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
//                intentList.add(new LabeledIntent(shareIntent, packageName, ri.loadLabel(pm), ri.icon));
//            }
//        }
//
//        Intent openInChooser = Intent.createChooser(new Intent(), "Export Using");
//        LabeledIntent[] extraIntents = (LabeledIntent[]) intentList
//                .toArray(new LabeledIntent[intentList.size()]);
//        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        return shareIntent;
    }
}
