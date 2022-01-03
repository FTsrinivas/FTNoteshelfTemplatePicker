package com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.SizeF;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.fluidtouch.noteshelf.annotation.FTAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.document.textedit.FTStyledTextBitmapGenerator;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItem;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTFileItemPDF;
import com.fluidtouch.noteshelf.documentframework.FileItems.FTPdfDocumentRef;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.textrecognition.annotation.FTImageRecognitionResult;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionResult;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecogCachePlistItem;
import com.fluidtouch.noteshelf.textrecognition.scandocument.FTScannedTextRecognitionResult;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;
import com.fluidtouch.renderingengine.renderer.FTRenderManager;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import org.benjinus.pdfium.Link;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FTNoteshelfPage implements Serializable {
    public final Object pauseWatcher = new Object();
    public String uuid;
    public Integer lineHeight;
    public Integer associatedPageIndex = 0;
    public Integer associatedPDFKitPageIndex = 0;
    public String associatedPDFFileName;
    public long lastUpdated;
    public boolean isBookmarked = false;
    public String bookmarkColor = "8ACCEA";
    public String bookmarkTitle = "";

    public RectF getPdfPageRect() {
        if (pdfPageRect == null || pdfPageRect.width() == 0) {
            pdfPageRect = getFTPdfDocumentRef().getPageRectAtIndex(associatedPDFKitPageIndex - 1);
            setPageDirty(true);
        }
        return pdfPageRect;
    }

    public void setPdfPageRect(RectF pdfPageRect) {
        this.pdfPageRect = pdfPageRect;
    }

    //PDFPage pdfPageRef;
    private RectF pdfPageRect;
    boolean isDirty;
    String deviceModel;
    public long creationDate;
    ArrayList<String> tags = new ArrayList<>();
    FTPageThumbnail pageThumbnail;
    FTPdfDocumentRef pdfDocumentRef;
    private Context mContext;
    private FTNoteshelfDocument parentDocument;
    private boolean isinUse = false;
    private FTNSqliteAnnotationFileItem pageSqliteFileItem;
    private ArrayList<FTSearchableItem> searchableItems = new ArrayList<>();
    private List<Link> hyperLinks = null;
    private FTFileItemPDF fileItemPDF;
    public boolean isPageEdited = false;
//    public int[] bgTextures = new int[3];

    public FTNoteshelfPage(Context context) {
        super();
        this.mContext = context;
    }

    public FTNoteshelfPage(Context context, NSDictionary dict) {
        super();
        this.mContext = context;
        uuid = dict.objectForKey("uuid").toString();
        associatedPDFFileName = dict.objectForKey("associatedPDFFileName").toString();
        if (dict.objectForKey("associatedPDFKitPageIndex") != null) {
            associatedPDFKitPageIndex = (Integer) dict.objectForKey("associatedPDFKitPageIndex").toJavaObject();
        } else {
            associatedPDFKitPageIndex = (Integer) dict.objectForKey("associatedPageIndex").toJavaObject();
        }
        associatedPageIndex = (Integer) dict.objectForKey("associatedPageIndex").toJavaObject();
        bookmarkColor = dict.objectForKey("bookmarkColor").toString();
        bookmarkTitle = dict.objectForKey("bookmarkTitle").toString();
        deviceModel = dict.objectForKey("deviceModel").toString();
        isBookmarked = (Boolean) dict.objectForKey("isBookmarked").toJavaObject();
        lineHeight = (Integer) dict.objectForKey("lineHeight").toJavaObject();
        if (dict.objectForKey("creationDate") != null) {
            creationDate = ((NSNumber) dict.objectForKey("creationDate")).longValue();
        }
        if (dict.objectForKey("lastUpdated") != null) {
            lastUpdated = ((NSNumber) dict.objectForKey("lastUpdated")).longValue();
        }
        if (dict.objectForKey("pdfKitPageRect") != null) {
            String pageRectStr = dict.objectForKey("pdfKitPageRect").toString();
            pageRectStr = pageRectStr.replace("{", "");
            pageRectStr = pageRectStr.replace("}", "");
            pageRectStr = pageRectStr.replace(" ", "");
            String[] items = pageRectStr.split(",");
            if (items.length == 4) {
                pdfPageRect = new RectF();
                pdfPageRect.left = Integer.parseInt(items[0]);
                pdfPageRect.top = Integer.parseInt(items[1]);
                pdfPageRect.right = Integer.parseInt(items[2]) - Integer.parseInt(items[0]);
                pdfPageRect.bottom = Integer.parseInt(items[3]) - Integer.parseInt(items[1]);

            }
        }
    }

    public FTNoteshelfDocument getParentDocument() {
        return parentDocument;
    }

    public void setParentDocument(FTNoteshelfDocument parentDocument) {
        this.parentDocument = parentDocument;
        fileItemPDF = (FTFileItemPDF) this.parentDocument.templateFolderItem().childFileItemWithName(this.associatedPDFFileName);
    }

    NSDictionary dictionaryRepresentation() {
        NSDictionary dict = new NSDictionary();
        dict.put("uuid", this.uuid);
        dict.put("associatedPDFFileName", this.associatedPDFFileName);
        dict.put("associatedPageIndex", this.associatedPageIndex);
        dict.put("associatedPDFKitPageIndex", this.associatedPDFKitPageIndex);
        dict.put("bookmarkColor", this.bookmarkColor);
        dict.put("bookmarkTitle", this.bookmarkTitle);
        dict.put("deviceModel", this.deviceModel);
        dict.put("isBookmarked", this.isBookmarked);
        dict.put("creationDate", new NSNumber(this.creationDate));
        dict.put("lastUpdated", new NSNumber(this.lastUpdated));
        dict.put("lineHeight", this.lineHeight);
        dict.put("tags", this.tags);
        if (this.pdfPageRect != null) {
            dict.put("pdfKitPageRect", this.getRectStringFor(this.pdfPageRect));
        }
        return dict;
    }

    public FTHandwritingRecognitionResult getRecognitionInfo(Context context) {
        FTNoteshelfDocument recognitionDocument = this.parentDocument;
        FTHandwritingRecognitionCachePlistItem fileItem = recognitionDocument.recognitionCache(context).recognitionCachePlist();
        if (fileItem != null) {
            return fileItem.getRecognitionInfo(getContext(), this);
        }
        return null;
    }

    public void setRecognitionInfo(Context context, FTHandwritingRecognitionResult recognitionInfo) {
        FTNoteshelfDocument recognitionDocument = this.parentDocument;
        FTHandwritingRecognitionCachePlistItem fileItem = recognitionDocument.recognitionCache(getContext()).recognitionCachePlist();
        if (fileItem != null) {
            fileItem.setRecognitionInfo(getContext(), this.uuid, recognitionInfo);
        }
        recognitionDocument.recognitionCache(context).saveRecognitionInfoToDisk();
    }

    public FTScannedTextRecognitionResult getVisionRecognitionInfo() {
        FTNoteshelfDocument recognitionDocument = this.parentDocument;
        FTScannedTextRecogCachePlistItem fileItem = recognitionDocument.recognitionCache(getContext()).visionRecognitionCachePlist();
        if (fileItem != null) {
            return fileItem.getRecognitionInfo(getContext(), this);
        }
        return null;
    }

    public void setVisionRecognitionInfo(FTScannedTextRecognitionResult recognitionInfo) {
        FTNoteshelfDocument recognitionDocument = this.parentDocument;
        FTScannedTextRecogCachePlistItem fileItem = recognitionDocument.recognitionCache(getContext()).visionRecognitionCachePlist();
        if (fileItem != null) {
            fileItem.setRecognitionInfo(getContext(), recognitionInfo, this);
        }
        recognitionDocument.recognitionCache(getContext()).saveRecognitionInfoToDisk();
    }

    public boolean canRecognizeHandwriting() {
        boolean canRecognize = false;
        FTHandwritingRecognitionResult recognitionInfo = this.getRecognitionInfo(getContext());
        if (null == recognitionInfo) {
            canRecognize = true;
        } else if (!recognitionInfo.languageCode.equals(FTLanguageResourceManager.getInstance().getCurrentLanguageCode())) {
            canRecognize = true;
        } else if (recognitionInfo.lastUpdated < lastUpdated) {
            canRecognize = true;
        }
        return canRecognize;
    }

    public boolean canRecognizeVisionText() {
        boolean canRecognize = false;
        FTScannedTextRecognitionResult recognitionInfo = getVisionRecognitionInfo();
        if (getFTPdfDocumentRef() != null && (getFTPdfDocumentRef().getPdfTexts().isEmpty() || TextUtils.isEmpty(getFTPdfDocumentRef().getPdfTexts().get(associatedPageIndex - 1)))) {
            if (null == recognitionInfo) {
                canRecognize = true;
            }/*else if(visionRecognitionInfo!.languageCode != FTVisionLanguageMapper.currentISOLanguageCode()) {
                canRecognize = true;
            }*/ else if (recognitionInfo.lastUpdated < lastUpdated) {
                canRecognize = true;
            }
        }
        return canRecognize;
    }

    public int pageIndex() {
        int index = parentDocument.pages(getContext()).indexOf(this);
        if (index == -1) {
            index = 0;
        }
        return index;
    }

    public FTPageThumbnail thumbnail() {
        synchronized (this) {
            if (null == this.pageThumbnail) {
                this.pageThumbnail = new FTPageThumbnail(this);
            }
        }
        return this.pageThumbnail;
    }

    private String getRectStringFor(RectF rect) {
        String rectString = "{{0, 0}, {0, 0}}";
        rectString = String.format("{{%d, %d}, {%d, %d}}", (int) this.pdfPageRect.left, (int) this.pdfPageRect.top, (int) this.pdfPageRect.width(), (int) this.pdfPageRect.height());
        return rectString;
    }

    public ArrayList<FTAnnotation> getPageAnnotations() {
        return this.sqliteFileItem().getAnnotationsArray(getContext());
    }

    public FTNSqliteAnnotationFileItem sqliteFileItem() {
        synchronized (this) {
            FTFileItem annotationFolder = this.parentDocument.annotationFolderItem();
            if (null == this.parentDocument || null == annotationFolder) {
                return null;
            }
            if (null != this.pageSqliteFileItem && null == this.pageSqliteFileItem.parent) {
                this.pageSqliteFileItem = null;
            }

            if (null == this.pageSqliteFileItem) {
                FTNSqliteAnnotationFileItem annotationsFileItem = (FTNSqliteAnnotationFileItem) annotationFolder.childFileItemWithName(this.sqliteFileName());
                if (null == annotationsFileItem) {
                    annotationsFileItem = new FTNSqliteAnnotationFileItem(getContext(), this.sqliteFileName(), false);
                    annotationFolder.addChildItem(annotationsFileItem);
                }
                annotationsFileItem.associatedPage = this;
                this.pageSqliteFileItem = annotationsFileItem;
            }
            return this.pageSqliteFileItem;
        }
    }

    private String sqliteFileName() {
        return this.uuid;
    }

    public void addAnnotations(List<FTAnnotation> annotations) {
        addAnnotations(annotations, new ArrayList<>());
    }

    public void addAnnotations(List<FTAnnotation> annotations, List<Integer> annotationsIndexes) {
        for (int i = 0; i < annotations.size(); i++) {
            if (annotationsIndexes.size() > 0)
                this.sqliteFileItem().addAnnotation(getContext(), annotations.get(i), annotationsIndexes.get(i));
            else
                this.sqliteFileItem().addAnnotation(getContext(), annotations.get(i));
        }
        this.isDirty = true;
        this.lastUpdated = FTDeviceUtils.getTimeStamp();
    }

    public void removeAnnotations(List<FTAnnotation> annotations) {
        FTNSqliteAnnotationFileItem annotationsFileItem = this.sqliteFileItem();
        if (null != annotationsFileItem) {
            for (FTAnnotation eachAnnotation : annotations) {
                annotationsFileItem.removeAnnotation(getContext(), eachAnnotation);
            }
            this.isDirty = true;
            this.lastUpdated = FTDeviceUtils.getTimeStamp();
            this.parentDocument.documentInfoPlist().forceSave = true;
        }
    }

    public void savePage() {
        parentDocument.saveNoteshelfDocument(mContext, null);
    }

    public boolean isPageDirty() {
        return this.isDirty;
    }

    public void setPageDirty(boolean isDirty) {
        if (this.isDirty && !isDirty) {
            FTENSyncRecordUtil.addPageToEvernoteSyncRecord(this);
        }
        this.isDirty = isDirty;
        if (isDirty) {
            isPageEdited = true;
            this.lastUpdated = FTDeviceUtils.getTimeStamp();
            this.parentDocument.documentInfoPlist().forceSave = true;
            this.sqliteFileItem().setAnnotationsArray(this.getPageAnnotations());
            thumbnail().setPageChanged(getContext());
        }
    }

    public float getDeviceScale() {
        if (getFTPdfDocumentRef() == null)
            return 1;
        SizeF pageSizeF = getFTPdfDocumentRef().getPageSize(associatedPDFKitPageIndex - 1);
        SizeF screenSize = ScreenUtil.setScreenSize(mContext);
        if (pdfPageRect.width() != pageSizeF.getWidth() || pdfPageRect.width() == screenSize.getWidth()) {
            return 1;
        } else {
            return 2;
        }
    }

    public RectF getPageRect() {
        RectF pageRect = new RectF(0, 0, 0, 0);
        if (null == pdfPageRect) {
            pdfPageRect = getFTPdfDocumentRef().getPageRectAtIndex(associatedPDFKitPageIndex - 1);
        }
        pageRect.set(pdfPageRect.left, pdfPageRect.top, pdfPageRect.width(), pdfPageRect.height());
        return pageRect;
    }

    public String documentVersion() {
        String documentVersion = "4.0";
        if (parentDocument != null) {
            documentVersion = parentDocument.propertyInfoPlist().objectForKey(mContext, FTConstants.DOCUMENT_VERSION_KEY).toString();
            if (null == documentVersion) {
                return documentVersion;
            }
        }
        return documentVersion;
    }

    //Finder Operations
    void deepCopyPage(FTNoteshelfDocument toDocument, PageCopyCompletion onCompletion) {
        deepCopyPage(toDocument, true, onCompletion);
    }

    void deepCopyPage(FTNoteshelfDocument toDocument, boolean withAnnotations, PageCopyCompletion onCompletion) {
        FTNoteshelfPage newPage = new FTNoteshelfPage(getContext(), this.dictionaryRepresentation());
        //newPage.addAnnotations(this.getPageAnnotations());
        newPage.parentDocument = toDocument;
        newPage.mContext = this.mContext;
        newPage.associatedPDFFileName = this.associatedPDFFileName;

        if (this.associatedPDFKitPageIndex != null) {
            newPage.associatedPDFKitPageIndex = this.associatedPDFKitPageIndex;
        } else {
            newPage.associatedPDFKitPageIndex = this.associatedPageIndex;
        }

        newPage.associatedPageIndex = this.associatedPageIndex;
        newPage.bookmarkColor = this.bookmarkColor;
        newPage.bookmarkTitle = this.bookmarkTitle;
        newPage.deviceModel = this.deviceModel;
        newPage.isBookmarked = this.isBookmarked;
        newPage.lineHeight = this.lineHeight;
        newPage.pdfPageRect = new RectF(this.pdfPageRect);

        newPage.creationDate = FTDeviceUtils.getTimeStamp();
        newPage.lastUpdated = FTDeviceUtils.getTimeStamp();

        newPage.uuid = FTDocumentUtils.getUDID();
        ArrayList<FTAnnotation> pageAnnotations = this.getPageAnnotations();
        ArrayList<FTAnnotation> pageAnnotationsCopied = new ArrayList<>();
        if (withAnnotations) {
            for (FTAnnotation annotation : pageAnnotations) {
                pageAnnotationsCopied.add(((FTAnnotationV1.FTAnnotationProtocol) annotation).deepCopyAnnotation(newPage));
            }
            newPage.addAnnotations(pageAnnotationsCopied);
        }

        //if template object does not exists in new document, create new template object
        if (newPage.fileItemPDF == null) {
            newPage.fileItemPDF = new FTFileItemPDF(this.associatedPDFFileName, false);
            newPage.parentDocument.templateFolderItem().addChildItem(newPage.fileItemPDF);
        }
        //if template file does not exists in new document, create new template file
        File newPdfFileItem = new File(newPage.fileItemPDF.getFileItemURL().getPath());
        try {
            if (!newPdfFileItem.exists() && newPdfFileItem.createNewFile()) {
                FTDocumentUtils.copyFile(new File(this.parentDocument.templateFolderItem()
                                .childFileItemWithName(this.associatedPDFFileName).getFileItemURL().getPath()),
                        newPdfFileItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileItemPDF != null && fileItemPDF.pageCount(getContext()) == 0) {
            fileItemPDF.deleteFileItem();
            this.parentDocument.templateFolderItem().deleteChildItem(fileItemPDF);
        }

        //Copy currentPage recognitionInfo to newPage
        FTHandwritingRecognitionResult currentRecogInfo = this.getRecognitionInfo(getContext());
        if (currentRecogInfo != null) {
            FTHandwritingRecognitionResult recogInfo = new FTHandwritingRecognitionResult(currentRecogInfo.dictionaryRepresentation());
            if (currentRecogInfo.lastUpdated == this.lastUpdated) {
                recogInfo.lastUpdated = newPage.lastUpdated;
            }
            newPage.setRecognitionInfo(getContext(), recogInfo);
        }
        FTScannedTextRecognitionResult currentVisionRecogInfo = this.getVisionRecognitionInfo();
        if (currentVisionRecogInfo != null) {
            FTScannedTextRecognitionResult visionRecogInfo = new FTScannedTextRecognitionResult(currentVisionRecogInfo.dictionaryRepresentation());
            if (currentVisionRecogInfo.lastUpdated == this.lastUpdated) {
                visionRecogInfo.lastUpdated = newPage.lastUpdated;
            }
            newPage.setVisionRecognitionInfo(visionRecogInfo);
        }
        onCompletion.didFinishWithPage(newPage);
    }

    void willDelete() {
        ArrayList<FTAnnotation> filteredAnnotations = this.getPageAnnotations();
        for (FTAnnotation annotation : filteredAnnotations) {
            if (annotation instanceof FTImageAnnotation) {
                ((FTImageAnnotationV1) annotation).delete();
            }

            if (annotation instanceof FTAudioAnnotation) {
                ((FTAudioAnnotationV1) annotation).delete();
            }
        }

        FTNSqliteAnnotationFileItem annotationsFileItem = this.sqliteFileItem();
        if (null != annotationsFileItem) {
            annotationsFileItem.deleteContent();
        }
        this.thumbnail().removeThumbnail();
        this.thumbnail().delete();
        this.setRecognitionInfo(getContext(), null); // To remove record from recognition plist
        this.setVisionRecognitionInfo(null); // To remove record from vision recognition plist
    }

    FTNoteshelfPage copyPageAttributes(Context context) {
        FTNoteshelfPage newPage = new FTNoteshelfPage(context);
//        newPage.isInitializationInprogress = true;
        newPage.associatedPDFFileName = this.associatedPDFFileName;
        newPage.associatedPDFKitPageIndex = this.associatedPDFKitPageIndex;
        newPage.associatedPageIndex = this.associatedPageIndex;
        newPage.lineHeight = this.lineHeight;
        newPage.pdfPageRect = this.pdfPageRect;
//        newPage.isInitializationInprogress = false;
        return newPage;
    }

    public boolean isTemplate() {
        FTNoteshelfDocument doc = this.parentDocument;
        if (null != doc) {
            NSDictionary templateInfo = doc.templateValues(getContext(), this.associatedPDFFileName);
            if (null != templateInfo) {
                NSNumber sourcedFromImage = (NSNumber) templateInfo.objectForKey("isTemplate");
                if (null != sourcedFromImage) {
                    return sourcedFromImage.boolValue();
                }
            }
        }
        return false;
    }

    public FTPdfDocumentRef getFTPdfDocumentRef() {
        if (pdfDocumentRef == null) {
            if (fileItemPDF == null)
                fileItemPDF = (FTFileItemPDF) this.parentDocument.templateFolderItem().childFileItemWithName(this.associatedPDFFileName);
            if (fileItemPDF == null) {
                return null;
            }
            pdfDocumentRef = fileItemPDF.pageDocumentRef(mContext);
            parentDocument.addPdfDocumentRef(fileItemPDF);
        }
        return pdfDocumentRef;
    }

    public boolean searchForKey(String searchKey) {
        searchKey = searchKey.trim().toLowerCase();
        boolean isFound = false;
        if (searchKey.isEmpty()) {
            return searchableItems.size() > 0;
        }
        this.searchableItems.clear();
        //Typed text
        ArrayList<FTTextAnnotation> textAnnotations = this.sqliteFileItem().getTextAnnotationsForKey(getContext(), searchKey);
        if (!textAnnotations.isEmpty()) {
            isFound = true;
            FTStyledTextBitmapGenerator generator = new FTStyledTextBitmapGenerator();
            for (FTTextAnnotation textAnnotation : textAnnotations) {
                int padding = ScreenUtil.convertDpToPx(getContext(), 10);
                RectF boundingRect = textAnnotation.getBoundingRect();
                int startPos = 0;
                int endPos = 0;
                if (boundingRect.width() > 0) {
                    StaticLayout layout = generator.getStaticLayout(((FTTextAnnotationV1) textAnnotation).getTextInputInfo(), getContext(),
                            (int) boundingRect.width() - (2 * padding), 1, getContext().getResources().getDisplayMetrics().density);

                    String title = ((FTTextAnnotationV1) textAnnotation).getTextInputInfo().getPlainText();
                    int fromIndex = 0;
                    do {
                        startPos = title.toLowerCase(Locale.US).indexOf(searchKey.toLowerCase(Locale.US), fromIndex);
                        endPos = startPos + searchKey.length();
                        int startLine = layout.getLineForOffset(startPos);
                        int endLine = layout.getLineForOffset(endPos);
                        for (int i = startLine; i <= endLine; i++) {
                            if (startPos != -1) {
                                int lineNo = layout.getLineForOffset(startPos);
                                int lineEnd = layout.getLineVisibleEnd(i);
                                int newEndPos = endPos > lineEnd ? lineEnd : endPos;
                                RectF rectF = new RectF();
                                rectF.left = (boundingRect.left + padding) + layout.getPrimaryHorizontal(startPos);
                                rectF.right = (boundingRect.left + padding) + layout.getPrimaryHorizontal(newEndPos);
                                rectF.top = (boundingRect.top + padding) + layout.getLineTop(lineNo);
                                rectF.bottom = (boundingRect.top + padding) + layout.getLineBottom(lineNo);
                                fromIndex = endPos;
                                FTSearchableItem searchableItem = new FTSearchableItem();
                                searchableItem.setText(searchKey);
                                searchableItem.setRotated(1);
                                searchableItem.setBoundingRect(rectF);
                                this.searchableItems.add(searchableItem);
                                if (layout.getLineCount() > i) {
                                    startPos = layout.getLineStart(i + 1);
                                }
                            }
                        }
                    } while (startPos != -1);
                }
            }
        }

        //PDF text
        FTFileItemPDF fileItemPDF = (FTFileItemPDF) this.parentDocument.templateFolderItem().childFileItemWithName(this.associatedPDFFileName);
        this.searchableItems.addAll(fileItemPDF.pageDocumentRef(getContext()).searchRects(searchKey, associatedPageIndex - 1, new SizeF(getPageRect().width(), getPageRect().height())));
        if (!this.searchableItems.isEmpty()) {
            isFound = true;
        }

        //Handwritten text recognition
        if (getRecognitionInfo(getContext()) != null && !FTLanguageResourceManager.getInstance().getCurrentLanguageCode().equals(FTLanguageResourceManager.languageCodeNone)) {
            String recogString = this.getRecognitionInfo(getContext()).recognisedString.toLowerCase().trim();
            if (recogString.contains(searchKey)) {
                //Get matching indices
                List<Integer> indices = new ArrayList<>();
                int index;
                int initialIndex = 0;
                while (true) {
                    index = recogString.indexOf(searchKey, initialIndex);
                    if (index == -1) {
                        break;
                    }
                    indices.add(index);
                    initialIndex = index + searchKey.length();
                }
                //Loop through each index
                for (int startIndex : indices) {
                    RectF wordRect = null;
                    for (int i = startIndex; i < (startIndex + searchKey.length()) && i < getRecognitionInfo(getContext()).characterRects.size(); i++) {
                        RectF charRect = getRecognitionInfo(getContext()).characterRects.get(i).cgRectValue();
                        if (!charRect.isEmpty()) {
                            if (i == startIndex) {
                                wordRect = charRect;
                            } else if (wordRect != null) {
                                wordRect.union(charRect);
                            }
                        }
                    }
                    if (wordRect != null && !wordRect.isEmpty()) {
                        FTSearchableItem searchableItem = new FTSearchableItem();
                        searchableItem.setBoundingRect(wordRect);
                        this.searchableItems.add(searchableItem);
                        isFound = true;
                    }
                }
            }
        }

        //Image text recognition
        FTScannedTextRecognitionResult visionRecogInfo = getVisionRecognitionInfo();
        if (visionRecogInfo != null) {
            String recogString = visionRecogInfo.recognisedString.toLowerCase().trim();
            FTLog.debug(FTLog.VISION_RECOGNITION, "Search Key = " + searchKey + "\nRecognized String = " + recogString);
            if (recogString.contains(searchKey)) {
                //Get matching indices
                List<Integer> indices = new ArrayList<>();
                int index;
                int initialIndex = 0;
                while (true) {
                    index = recogString.indexOf(searchKey, initialIndex);
                    if (index == -1) {
                        break;
                    }
                    indices.add(index);
                    initialIndex = index + searchKey.length();
                }
                //Loop through each index
                for (int startIndex : indices) {
                    RectF wordRect = null;
                    for (int i = startIndex; i < (startIndex + searchKey.length()) && i < visionRecogInfo.characterRects.size(); i++) {
                        RectF charRect = visionRecogInfo.characterRects.get(i).cgRectValue();
                        if (!charRect.isEmpty()) {
                            if (i == startIndex) {
                                wordRect = charRect;
                            } else if (wordRect != null) {
                                wordRect.union(charRect);
                            }
                        }
                    }
                    if (wordRect != null && !wordRect.isEmpty()) {
                        FTSearchableItem searchableItem = new FTSearchableItem();
                        searchableItem.setBoundingRect(wordRect);
                        this.searchableItems.add(searchableItem);
                        isFound = true;
                    }
                }
            }
        }

        //Image Annotations text recognition
        for (FTImageAnnotation imageAnnotation : getImageAnnotations()) {
            FTImageRecognitionResult imageRecognitionInfo = ((FTImageAnnotationV1) imageAnnotation).getImageTextRecognitionInfo();
            if (imageRecognitionInfo != null) {
                FTLog.debug(FTLog.IMAGE_RECOGNITION, "Image Annotation Rect: " + imageAnnotation.getBoundingRect().toString());
                String recogString = imageRecognitionInfo.recognisedString.toLowerCase().trim();
                if (recogString.contains(searchKey)) {
                    //Get matching indices
                    List<Integer> indices = new ArrayList<>();
                    int index;
                    int initialIndex = 0;
                    while (true) {
                        index = recogString.indexOf(searchKey, initialIndex);
                        if (index == -1) {
                            break;
                        }
                        indices.add(index);
                        initialIndex = index + searchKey.length();
                    }
                    //Loop through each index
                    for (int startIndex : indices) {
                        RectF wordRect = null;
                        for (int i = startIndex; i < (startIndex + searchKey.length()) && i < imageRecognitionInfo.characterRects.size(); i++) {
                            RectF charRect = imageRecognitionInfo.characterRects.get(i).cgRectValue();
                            if (!charRect.isEmpty()) {
                                if (i == startIndex) {
                                    wordRect = charRect;
                                } else if (wordRect != null) {
                                    wordRect.union(charRect);
                                }
                            }
                        }
                        if (wordRect != null && !wordRect.isEmpty() && imageAnnotation.getImage() != null) {
                            //Move wordRect from respect to image to respect to boundingRect of imageAnnotation
                            float scale = imageAnnotation.getImage().getWidth() / imageAnnotation.getBoundingRect().width();
                            wordRect = FTGeometryUtils.scaleRect(wordRect, 1 / scale);
                            float newLeft = wordRect.left + imageAnnotation.getBoundingRect().left;
                            float newTop = wordRect.top + imageAnnotation.getBoundingRect().top;
                            wordRect.offsetTo(newLeft, newTop);

                            FTSearchableItem searchableItem = new FTSearchableItem();
                            searchableItem.setBoundingRect(wordRect);
                            this.searchableItems.add(searchableItem);
                            isFound = true;
                        }
                    }
                }
            }
        }
        return isFound;
    }

    public ArrayList<FTSearchableItem> getSearchableItems() {
        return searchableItems;
    }

    public void clearSearchableItems() {
        searchableItems.clear();
    }

    public synchronized List<Link> getHyperlinks() {
        if (hyperLinks == null && getFTPdfDocumentRef() != null) {
//            FTFileItemPDF fileItemPDF = (FTFileItemPDF) this.parentDocument.templateFolderItem().childFileItemWithName(this.associatedPDFFileName);
            this.hyperLinks = getFTPdfDocumentRef().getPageLinks(associatedPageIndex - 1, new SizeF(getPageRect().width(), getPageRect().height()));
        }
        return this.hyperLinks;
    }

    public void unloadContents() {
        if (isDirty) {
            return;
        }
        int currentPageDisplayed = -1;
        int index = this.pageIndex();
        if (null != this.parentDocument.delegate) {
            currentPageDisplayed = parentDocument.delegate.currentPageDisplayed();
        }
        if (-1 != currentPageDisplayed && index != (currentPageDisplayed - 1) && index != currentPageDisplayed && index != (currentPageDisplayed + 1)) {
            ArrayList<FTAnnotation> annotations = this.getPageAnnotations();
            boolean unloadAnnotationsFile = true;
            if (annotations.size() > 0) {
                for (FTAnnotation eachAnnotation : annotations) {
                    eachAnnotation.unloadContents();
                }
            }
            if (unloadAnnotationsFile) {
                this.sqliteFileItem().unloadContentsOfFileItem();
            }
            getFTPdfDocumentRef().closeDocument();
        }
    }

    private Context getContext() {
        return mContext;
    }

    public synchronized void destroy() {
        //pageSqliteFileItem = null;
        thumbnail().removeThumbnail();
        pageThumbnail = null;
        //_recognitionInfo = null;
    }

    public List<FTAudioAnnotation> getAudioAnnotations() {
        ArrayList<FTAudioAnnotation> annotations = new ArrayList<>();
        for (FTAnnotation annotation : getPageAnnotations()) {
            if (annotation.annotationType() == FTAnnotationType.audio) {
                annotations.add((FTAudioAnnotation) annotation);
            }
        }
        return annotations;
    }

    public List<FTImageAnnotation> getImageAnnotations() {
        List<FTImageAnnotation> imgAnnotations = new ArrayList<>();
        List<FTAnnotation> annotations = getPageAnnotations();
        for (int i = 0; i < annotations.size(); i++) {
            try {
                FTAnnotation annotation = annotations.get(i);
                if (annotation != null && annotation.annotationType() == FTAnnotationType.image) {
                    imgAnnotations.add((FTImageAnnotationV1) annotation);
                }
            } catch (Exception e) {
                FTLog.logCrashException(e);
            }
        }
        return imgAnnotations;
    }

    public boolean getIsinUse() {
        return isinUse;
    }

    public synchronized void setIsinUse(boolean isinUse) {
        this.isinUse = isinUse;
    }

    interface PageCopyCompletion {
        void didFinishWithPage(FTNoteshelfPage copiedPage);
    }
}