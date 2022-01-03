package com.fluidtouch.noteshelf.pdfexport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItem;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPDF;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.multipdf.Overlay;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.util.Matrix;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;


public class FTPdfCreator {
    private static final String TAG = FTPdfCreator.class.getSimpleName();
    private Context mContext;
    private FTNoteshelfDocument mNoteshelfDocument;
    private List<FTNoteshelfPage> pages;
    private OnCreateResponse mOnCreateResponse;
    private int lastPdfIndex = 0;
    public static PDType0Font pdType0FontAllLanguages = null;

    public FTPdfCreator(Context context) {
        mContext = context;
        PDFBoxResourceLoader.init(context);
    }

    public FTPdfCreator noteshelfDocument(FTNoteshelfDocument noteshelfDocument) {
        mNoteshelfDocument = noteshelfDocument;
        return this;
    }

    public FTPdfCreator pages(List<FTNoteshelfPage> pages) {
        this.pages = pages;
        return this;
    }

    public FTPdfCreator onCreateResponse(OnCreateResponse onCreateResponse) {
        mOnCreateResponse = onCreateResponse;
        return this;
    }

    public void Create() {
        final FTSmartDialog smartDialog = new FTSmartDialog();
        if (!((FTBaseActivity) mContext).isFinishing()) {
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .setMessage(mContext.getString(R.string.exporting))
                    .show(((FTBaseActivity) mContext).getSupportFragmentManager());
        }
        AsyncTask exportToPdfTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String documentName = mNoteshelfDocument.getTitle(mContext);
                File finalOutputFile = new File(mContext.getCacheDir() + "/pdfExport", documentName + FTConstants.PDF_EXTENSION);
                setContentFile(pages, finalOutputFile);
                return finalOutputFile;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (!((AppCompatActivity) mContext).isFinishing()) {
                    smartDialog.dismissAllowingStateLoss();
                }
                File file = (File) o;
                if (null != file) {
                    mOnCreateResponse.onPdfCreated(file);
                } else {
                    mOnCreateResponse.onPdfCreateFailed();
                }
            }
        };

        exportToPdfTask.executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    private void setContentFile(List<FTNoteshelfPage> pages, File contentFile) {
        HashMap<String, PDDocument> hashMap = new HashMap<>();
        PDDocument document = new PDDocument();
        for (int i = 0; i < pages.size(); i++) {
            FTFileItem ftFileItem = mNoteshelfDocument.templateFolderItem();
            FTNoteshelfPage noteshelfPage = pages.get(i);
            ArrayList<FTAnnotation> pageAnnotations = noteshelfPage.getPageAnnotations();
            createPdf(pageAnnotations, document, noteshelfPage, ftFileItem, hashMap);
        }
        try {
            if (!contentFile.getParentFile().exists()) {
                contentFile.getParentFile().mkdirs();
            } else {
                FTFileManagerUtil.deleteFilesInsideFolder(contentFile.getParentFile());
            }
            document.save(contentFile);
            document.close();
            Iterator it = hashMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                hashMap.get(key).close();
            }
            hashMap.clear();
            pdType0FontAllLanguages = null;
        } catch (IOException e) {
            Log.i("FTPdfCreator", Objects.requireNonNull(e.getMessage()));
        }
    }

    private PdfDocument.Page addPage(PdfDocument document, FTNoteshelfPage noteshelfPage, int index, boolean isContainingHighlighter) {
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder((int) noteshelfPage.getPageRect().width(), (int) noteshelfPage.getPageRect().height(), index + 1).create();
        PdfDocument.Page pdfPage = document.startPage(pageInfo);

        //draw page background with TRANSPARENT
        Canvas canvas = pdfPage.getCanvas();
        Paint paintT = new Paint();
        paintT.setColor(Color.TRANSPARENT);
        canvas.drawPaint(paintT);

        //render annotations
        ArrayList<FTAnnotation> pageAnnotations = noteshelfPage.getPageAnnotations();
        FTPdfRenderer renderer = new FTPdfRenderer(mContext);
        for (FTAnnotation annotation : pageAnnotations) {
            if (renderer.render(annotation, canvas)) {
                isContainingHighlighter = true;
            }
        }
        document.finishPage(pdfPage);
        return pdfPage;
    }

    private void setTemplateFile(List<FTNoteshelfPage> pages, File templateFile) {
        PDDocument document = new PDDocument();
        FTFileItem ftFileItem = mNoteshelfDocument.templateFolderItem();
        HashMap<String, PDDocument> hashMap = new HashMap<>();
        if (templateFile.getParentFile() != null && !templateFile.getParentFile().exists()) {
            templateFile.getParentFile().mkdirs();
        }

        try {
            for (FTNoteshelfPage noteshelfPage : pages) {
                addTemplate(document, noteshelfPage, ftFileItem, hashMap);
            }
            document.save(templateFile);
            document.close();
        } catch (IOException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void addTemplate(PDDocument document, FTNoteshelfPage noteshelfPage, FTFileItem ftFileItem, HashMap<String, PDDocument> hashMap) throws IOException {
        PDDocument templatePdf;
        if (hashMap.containsKey(noteshelfPage.associatedPDFFileName)) {
            templatePdf = hashMap.get(noteshelfPage.associatedPDFFileName);
        } else {
            FTFileItem fileItemPdf = ftFileItem.childFileItemWithName(noteshelfPage.associatedPDFFileName);
            File templateFile = new File(fileItemPdf.getFileItemURL().getPath());
            templatePdf = PDDocument.load(new FileInputStream(templateFile));
            hashMap.put(noteshelfPage.associatedPDFFileName, templatePdf);
        }

        PDPage page = templatePdf.getPage(noteshelfPage.associatedPDFKitPageIndex - 1);
        page.setMediaBox(new PDRectangle(noteshelfPage.getPageRect().width(), noteshelfPage.getPageRect().height()));
        document.addPage(page);
    }

    private void overlayDocuments(File contentFile, File templateFile, File outputFile) {
        HashMap<Integer, String> overlayGuide = new HashMap<>();
        Overlay overlay = new Overlay();
        try {
            overlay.setInputFile(contentFile.getAbsolutePath());
            overlay.setAllPagesOverlayFile(templateFile.getAbsolutePath());
            overlay.setOutputFile(outputFile.getAbsolutePath());
            overlay.setOverlayPosition(Overlay.Position.BACKGROUND);
            overlay.overlay(overlayGuide);
        } catch (IOException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public void createPdf(ArrayList<FTAnnotation> annotations, PDDocument document, FTNoteshelfPage noteshelfPage, FTFileItem ftFileItem, HashMap<String, PDDocument> hashMap) {
        try {
            PDDocument templatePdf;
            if (hashMap.containsKey(noteshelfPage.associatedPDFFileName) && noteshelfPage.associatedPDFKitPageIndex == lastPdfIndex + 1) {
                templatePdf = hashMap.get(noteshelfPage.associatedPDFFileName);
            } else {
                FTFileItem fileItemPdf = ftFileItem.childFileItemWithName(noteshelfPage.associatedPDFFileName);
                File templateFile = new File(fileItemPdf.getFileItemURL().getPath());
                templatePdf = PDDocument.load(new FileInputStream(templateFile), ((FTFileItemPDF) fileItemPdf).documentPassword, MemoryUsageSetting.setupTempFileOnly());
                if (hashMap.containsKey(noteshelfPage.associatedPDFFileName))
                    hashMap.put(noteshelfPage.associatedPDFFileName + hashMap.size(), templatePdf);
                else
                    hashMap.put(noteshelfPage.associatedPDFFileName, templatePdf);
            }
            lastPdfIndex = noteshelfPage.associatedPDFKitPageIndex;

            PDPage pdPage = templatePdf.getPage(noteshelfPage.associatedPDFKitPageIndex - 1);
            int rotation = pdPage.getRotation();
            PDPage page = new PDPage(pdPage.getCOSObject());
            page.setResources(pdPage.getResources());
            page.setMediaBox(pdPage.getMediaBox());
            page.setCropBox(pdPage.getCropBox());
            page.setArtBox(pdPage.getArtBox());

            if (annotations.size() > 0) {
                PDPageContentStream contentStream;
                FTPdfStrokeRenderer pdfStrokeRenderer = null;
                FTPdfTextRenderer pdfTextRenderer = null;
                FTPdfImageRenderer pdfImageRenderer = null;
                contentStream = new PDPageContentStream(templatePdf, page, true, false, true);
                switch (rotation) {
                    case 90: {
                        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(90), 0, 0);
                        matrix.translate(0, -page.getMediaBox().getWidth());
                        contentStream.transform(matrix);
                    }
                    break;
                    case 180: {
                        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(180), 0, 0);
                        matrix.translate(-page.getMediaBox().getWidth(), -page.getMediaBox().getHeight());
                        contentStream.transform(matrix);
                    }
                    break;
                    case 270: {
                        Matrix matrix = Matrix.getRotateInstance(Math.toRadians(270), 0, 0);
                        matrix.translate(-page.getMediaBox().getHeight(), 0);
                        contentStream.transform(matrix);
                    }
                    break;
                }

                for (int i = 0; i < annotations.size(); i++) {
                    FTAnnotation annotation = annotations.get(i);
                    if (annotation instanceof FTImageAnnotation) {
                        if (pdfImageRenderer == null)
                            pdfImageRenderer = new FTPdfImageRenderer();
                        pdfImageRenderer.pdfBoxImageRender(annotation, templatePdf, page, contentStream, noteshelfPage);
                    } else if (annotation instanceof FTTextAnnotation) {
                        if (pdfTextRenderer == null)
                            pdfTextRenderer = new FTPdfTextRenderer(mContext);
                        pdfTextRenderer.pdfBoxTextRenderer(templatePdf, annotation, contentStream, page, noteshelfPage);
                    } else if (annotation instanceof FTStroke) {
                        if (pdfStrokeRenderer == null)
                            pdfStrokeRenderer = new FTPdfStrokeRenderer();
                        pdfStrokeRenderer.pdfBoxStokerender(annotation, contentStream, page, noteshelfPage);
                    }
                }

                contentStream.close();
            }
            document.addPage(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnCreateResponse {
        void onPdfCreated(File file);

        void onPdfCreateFailed();
    }
}
