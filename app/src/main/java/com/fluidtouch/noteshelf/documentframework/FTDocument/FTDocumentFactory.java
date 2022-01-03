package com.fluidtouch.noteshelf.documentframework.FTDocument;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SizeF;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTPageThumbnail;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FTDocumentFactory {

    public static FTUrl tempDocumentPath(String name) {
        String fileName = name;
        String tempPath = FTConstants.DOCUMENTS_ROOT_PATH;
        tempPath = tempPath + "/Temp";
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        tempPath = tempPath + "/" + name + FTConstants.NS_EXTENSION;
        return FTUrl.parse(tempPath);
    }

    public static FTNoteshelfDocument documentForItemAtURL(FTUrl url) {
        FTNoteshelfDocument document = new FTNoteshelfDocument(url);
        return document;
    }

    public static void duplicateDocumentAtURL(final Context context, final FTUrl url, final FTDocumentFactory.FTDocumentItemAndErrorBlock onCompletion) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File sourceFile = new File(url.getPath());
                FTUrl tempPath = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getUDID());
                File destFile = new File(tempPath.getPath());

                try {
                    FTDocumentUtils.copyDirectory(sourceFile, destFile);
                    final FTDocumentItem document = FTDocumentFactory.documentForItemAtURL(tempPath);
                    ((FTNoteshelfDocument) document).prepareForImporting(context, new FTNoteshelfDocument.SuccessErrorBlock() {
                        @Override
                        public void didFinishWithStatus(final boolean success, final Error error) {
                            ((FTBaseActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (success) {
                                        onCompletion.didFinishWithDocument(document, null);
                                    } else {
                                        onCompletion.didFinishWithDocument(null, error);
                                    }
                                }
                            });

                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onCompletion.didFinishWithDocument(null, new Error(e.getMessage()));
                        }
                    });
                }
            }
        });

    }

    private static void duplicateDocuments(final Context context, final List<FTShelfItem> originalDocuments, final List<FTShelfItem> duplicatedDocuments, final Integer index, final FTShelfItemCollection shelfItemCollection, final FTGroupItem group, final FTBaseShelfActivity.FTDuplicateDocumentsBlock onCompletion) {
        if (index < originalDocuments.size()) {
            duplicateDocumentAtURL(context, originalDocuments.get(index).getFileURL(), new FTDocumentFactory.FTDocumentItemAndErrorBlock() {
                @Override
                public void didFinishWithDocument(final FTDocumentItem document, Error error) {
                    if (document != null) {
                        shelfItemCollection.addShelfItemForDocument(context, originalDocuments.get(index).getTitle(context), group, new FTShelfItemCollection.FTDocumentItemAndErrorBlock() {
                            @Override
                            public void didFinishAddingItem(FTDocumentItem documentItem, Error error) {
                                duplicatedDocuments.add(documentItem);
                                duplicateDocuments(context, originalDocuments, duplicatedDocuments, index + 1, shelfItemCollection, group, onCompletion);
                            }
                        }, document.getFileURL());
                    }
                }
            });
        } else {
            onCompletion.didFinishWithWithDocuments(duplicatedDocuments, group);
        }
    }

    public static void duplicateDocuments(Context context, List<FTShelfItem> documents, final FTBaseShelfActivity.FTDuplicateDocumentsBlock onCompletion) {
        int index = 0;
        FTShelfItemCollection shelfItemCollection = documents.get(index).getShelfCollection();
        FTGroupItem group = documents.get(index).getParent();
        duplicateDocuments(context, documents, new ArrayList<FTShelfItem>(), index, shelfItemCollection, group, onCompletion);
    }

    private static void updateCoversForItems(final Context context, final List<FTShelfItem> shelfItems, final FTNTheme theme, final Integer index, final FTBaseShelfActivity.FTCoversUpdatedBlock onCompletion) {
        Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems index::-"+index+" shelfItems size::-"+shelfItems.size());
        if (index >= shelfItems.size()) {
            onCompletion.didFinishWithStatus(true, null);
            return;
        }

        final FTNoteshelfDocument document = new FTNoteshelfDocument(shelfItems.get(index).getFileURL());
        Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems document::-"+document);
        document.openDocument(context, (success, error) -> ((AppCompatActivity) context).runOnUiThread(() -> {
            Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems success::-"+success);
            if (!success) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems theme.overlayType::-"+theme.overlayType+
                    " document.pages(context).size()::-"+document.pages(context).size()+" Status::-"+(theme.overlayType == 1 && document.pages(context).size() > 0));

            if (theme.overlayType == 1 && document.pages(context).size() > 0) {
                FTNoteshelfPage noteshelfPage = document.pages(context).get(0);
                ObservingService.getInstance().addObserver(FTPageThumbnail.strObserver + noteshelfPage.uuid, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        if (arg instanceof FTPageThumbnail.FTThumbnail) {
                            FTPageThumbnail.FTThumbnail ftThumbnail = (FTPageThumbnail.FTThumbnail) arg;
                            Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems getThumbImage::-"+ftThumbnail.getThumbImage());
                            if (ftThumbnail.getThumbImage() != null) {
                                Bitmap overlayBitmap = ((FTNCoverTheme) theme).themeOverlay(context);
                                Bitmap thumbImage = ftThumbnail.getThumbImage();
                                SizeF thumbSize = new SizeF(overlayBitmap.getWidth(), ((float) overlayBitmap.getWidth() / thumbImage.getWidth()) * thumbImage.getHeight());
                                theme.bitmap = BitmapUtil.getMergedBitmap(context, BitmapUtil.getResizedBitmap(thumbImage, (int) thumbSize.getWidth(), (int) thumbSize.getHeight()), overlayBitmap, 0);
                                document.setShelfImage(context, theme);
                                document.saveNoteshelfDocument(context, (success1, error1) -> updateCoversForItems(context, shelfItems, theme, index + 1, onCompletion));
                                ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + ftThumbnail.getPageUUID(), this);
                            }
                            //ObservingService.getInstance().removeObserver(FTPageThumbnail.strObserver + ftThumbnail.getPageUUID(), this);
                        }
                    }
                });
                noteshelfPage.thumbnail().thumbnailImage(context);
            } else {
                Log.d("TemplatePicker==>"," FTDocumentActivity updateCoversForItems Else::-");
                document.setShelfImage(context, theme);
                document.saveNoteshelfDocument(context, (success1, error1) -> updateCoversForItems(context, shelfItems, theme, index + 1, onCompletion));
            }
        }));
    }

    public static void updateShelfItemsCover(Context context, List<FTShelfItem> shelfItems, FTNCoverTheme coverTheme, FTBaseShelfActivity.FTCoversUpdatedBlock ftCoversUpdatedBlock) {
        int index = 0;
        updateCoversForItems(context, shelfItems, coverTheme, index, ftCoversUpdatedBlock);
    }

    public interface FTDocumentItemAndErrorBlock {
        void didFinishWithDocument(FTDocumentItem document, Error error);
    }
}
