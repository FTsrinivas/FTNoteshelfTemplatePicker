package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;
import android.util.Log;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.document.enums.FTCoverOverlayStyle;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTUniqueFileName;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;
import com.fluidtouch.noteshelf2.R;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FTShelfCollectionLocal extends FTShelfCollection {

    private final ArrayList<FTShelfItemCollection> shelfs = new ArrayList<>();

    private static FTUrl userFolderURL() {
        FTUrl noteshelfURL = FTDocumentUtils.noteshelfDocumentsDirectory();
        FTUrl systemURL = FTUrl.withAppendedPath(noteshelfURL, "User Documents");

        File file = new File(systemURL.getPath());
        if (!file.exists()) {
            if (file.mkdirs()) {
                Log.i("Info", "User folder created");
            }
        }
        return systemURL;
    }

    @Override
    public FTShelfItemCollection collectionWithTitle(Context context, String title) {
        String path = userFolderURL().getPath();
        final File directory = new File(path);
        File[] files = directory.listFiles();
        if (files.length == 0) {
            return null;
        } else {
            for (File file : files) {
                FTShelfItemCollection collection = FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(file.getPath()));
                if (collection.getTitle(context).equals(title)) {
                    return collection;
                }
            }
        }
        return null;
    }

    @Override
    public void shelfs(final FTShelfItemCollectionBlock onCompletion) {
        if (shelfs.isEmpty()) {
            String path = userFolderURL().getPath();
            final File directory = new File(path);

            List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
            if (files.size() == 0) {
                this.createDefaultShelfCategory((shelf, error) -> {
                    shelfs.add(FTShelfCollectionLocal.this.newCollectionWithURL(shelf.getFileURL()));

                    if (!FTApp.getPref().isDefaultNotebookCreated()) {
                        copySampleDocument();
                        FTApp.getPref().saveDefaultNotebookCreated(true);
                    }
                });
            } else {
                for (File file : files) {
                    shelfs.add(FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(file.getPath())));
                }
            }
        }
        onCompletion.didFetchCollectionItems(shelfs);
    }

    private void createDefaultShelfCategory(FTItemCollectionAndErrorBlock block) {
        String folderName = FTConstants.DEFAULT_SHELF_NAME;
        String path = userFolderURL().getPath() + "/" + folderName + FTConstants.SHELF_EXTENSION;
        File directory = new File(path);
        if (directory.mkdirs()) {
            FTShelfItemCollection shelfFolder = FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(path));
            block.didFinishForShelfItemCollection(shelfFolder, null);
        } else {
            block.didFinishForShelfItemCollection(null, new Error("Failed to create"));
        }
    }

    public void createShelfWithTitle(Context context, String title, FTItemCollectionAndErrorBlock block) {

        String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(title + FTConstants.SHELF_EXTENSION, FTShelfCollectionLocal.userFolderURL());

        String path = userFolderURL().getPath() + "/" + uniqueFileName;
        File directory = new File(path);
        if (directory.mkdirs()) {
            FTShelfItemCollection shelfFolder = FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(path));
            shelfs.add(shelfFolder);
            block.didFinishForShelfItemCollection(shelfFolder, null);
        }
    }

    private FTShelfItemCollection newCollectionWithURL(FTUrl fileURL) {
        return new FTShelfItemCollectionLocal(fileURL);
    }

    @Override
    public void deleteShelf(Context context, FTShelfItemCollection shelf, FTItemCollectionAndErrorBlock block) {
        String categoryFolderPath = shelf.getFileURL().getPath();
        File categoryFolder = new File(categoryFolderPath);
        shelf.shelfItems(context, FTShelfSortOrder.BY_NAME, null, "", (notebooks, error1) -> {
            if (error1 == null) {
                shelf.removeShelfItems(context, (notebooks1, error2) -> {
                    if (error2 == null) {
                        shelfs.remove(shelf);
                        FTFileManagerUtil.deleteRecursive(categoryFolder);
                        block.didFinishForShelfItemCollection(shelf, null);
                    } else {
                        block.didFinishForShelfItemCollection(shelf, new Error("Unable to remove category"));
                    }
                }, notebooks);
            }
        });
    }

    @Override
    public void renameShelf(Context context, String title, FTItemCollectionAndErrorBlock block, FTShelfItemCollection shelf) {
        String sourcePath = shelf.getFileURL().getPath();
        File sourceFile = new File(sourcePath);

        String uniqueFileName = FTUniqueFileName.uniqueFileNameFor(title + FTConstants.SHELF_EXTENSION, FTShelfCollectionLocal.userFolderURL());
        String destPath = userFolderURL().getPath() + "/" + uniqueFileName;
        File destFile = new File(destPath);

        Error error = null;
        if (sourceFile.renameTo(destFile)) {
            int index = shelfs.indexOf(shelf);
            shelf.setFileURL(FTUrl.parse(destFile.getPath()));
            for (FTShelfItem item : shelf.getChildren()) {
                String oldPath = item.getFileURL().getPath();
                FTUrl updatedUrl = FTUrl.parse(shelf.getFileURL().getPath() + "/" + FTDocumentUtils.getFileName(context, item.getFileURL()));
                item.setFileURL(updatedUrl);
                FTShelfCollectionProvider.getInstance().recentShelfProvider.updatePath(oldPath, updatedUrl.getPath());
                FTShelfCollectionProvider.getInstance().pinnedShelfProvider.updatePath(oldPath, updatedUrl.getPath());
                if (item instanceof FTGroupItem) {
                    for (FTShelfItem notebook : ((FTGroupItem) item).getChildren()) {
                        String oldPath1 = item.getFileURL().getPath();
                        FTUrl updatedUrl1 = FTUrl.parse(item.getFileURL().getPath() + "/" + FTDocumentUtils.getFileName(context, notebook.getFileURL()));
                        notebook.setFileURL(updatedUrl1);
                        FTShelfCollectionProvider.getInstance().recentShelfProvider.updatePath(oldPath1, updatedUrl1.getPath());
                        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.updatePath(oldPath1, updatedUrl1.getPath());
                    }
                }
            }
            if (index > 0) shelfs.set(index, shelf);
        } else {
            error = new Error("Failed");
        }
        block.didFinishForShelfItemCollection(shelf, error);
    }

    private void createSampleNotebook(FTBackgroundTaskProtocols.OnCompletion onCompletion) {
        Context context = FTApp.getInstance().getCurActCtx();
        FTNCoverTheme coverTheme = (FTNCoverTheme) FTNTheme.theme(new FTUrl(FTConstants.COVER_FOLDER_NAME + "/" + FTConstants.DEFAULT_COVER_THEME_NAME));
        FTNTheme paperTheme = FTNTheme.theme(new FTUrl(FTConstants.PAPER_FOLDER_NAME + "/" + FTConstants.DEFAULT_PAPER_THEME_NAME));
        FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        Log.d("TemplatePicker==>"," Sample Notebook FTShelfCollectionLocal createSampleNotebook");
        paperTheme.template(context, (documentInfo, generationError) -> {
            final FTUrl fileUri = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getUDID());
            final FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(fileUri);

            if (coverTheme != null) {
                documentInfo.overlayStyle = FTCoverOverlayStyle.DEFAULT_STYLE;
                documentInfo.setCoverTheme(coverTheme);
            }

            document.createDocument(context, documentInfo, (success, error) -> {
                if (success) {
                    String folderName = FTConstants.DEFAULT_SHELF_NAME;
                    String path = userFolderURL().getPath() + "/" + folderName + FTConstants.SHELF_EXTENSION;
                    FTShelfItemCollection shelfFolder = FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(path));
                    File sampleFolder = new File(path + "/" + context.getString(R.string.sample_notebook) + ".nsa");
                    if (!sampleFolder.exists()) {
                        sampleFolder.mkdirs();
                    }
                    FTFileManagerUtil.copyRecursively(new File(document.getFileURL().getPath()), sampleFolder);
                } else {
                    Log.i(FTShelfCollectionLocal.class.getName(), error.getMessage());
                }
                onCompletion.didFinish();
            });
        });
    }

    private void copySampleDocument() {
        AssetsUtil assetManager = new AssetsUtil();
        String folderName = FTConstants.DEFAULT_SHELF_NAME;
        String path = userFolderURL().getPath() + "/" + folderName + FTConstants.SHELF_EXTENSION;
        FTShelfItemCollection shelfFolder = FTShelfCollectionLocal.this.newCollectionWithURL(FTUrl.parse(path));
        File destinationFile = new File(shelfFolder.getFileURL().getPath() + "/" + FTApp.getInstance().getCurActCtx().getString(R.string.sample_notebook) + FTConstants.NS_EXTENSION);
        String noteRelativePath = "Sample Notebook.nsa";

        try {
            assetManager.copyLocalAsset(noteRelativePath, destinationFile.getPath());

            Context context = FTApp.getInstance().getCurActCtx();
            FTNTheme paperTheme = FTNTheme.theme(new FTUrl(FTConstants.PAPER_FOLDER_NAME + "/" + FTConstants.DEFAULT_PAPER_THEME_NAME));

            boolean typeOfLineSelectedByUser = FTApp.getPref().get(SystemPref.TEMPLATE_LINE_TYPE_SELECTED, false);
            boolean typeOfClrSelectedByUser = FTApp.getPref().get(SystemPref.TEMPLATE_COLOR_SELECTED, false);
            FTTemplateUtil ftTemplateUtil = FTTemplateUtil.getInstance();
            if (!typeOfLineSelectedByUser) {
                ftTemplateUtil.fTTemplateLineTypeSerializedObject(34,"Default",34);
            }

            Log.d("TemplatePicker==>","Basic Template Info FTShelfCollectionLocal typeOfClrSelectedByUser::-"
                    +typeOfClrSelectedByUser+" dynamicId::-"+paperTheme.dynamicId);
            if (!typeOfClrSelectedByUser) {
                ftTemplateUtil.fTTemplateColorsSerializedObject("#F7F7F2-1.0",
                        "White","#000000-0.15","#000000-0.15");
            }

            Log.d("TemplatePicker==>"," Sample Notebook FTShelfCollectionLocal");
            FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
            paperTheme.template(context, (documentInfo, generationError) -> {

                File templateFile = new File(destinationFile, "Templates").listFiles()[0];
                if (templateFile.exists()) {
                    templateFile.delete();
                }
                try {
                    FTFileManagerUtil.createFileFromInputStream(new FileInputStream(documentInfo.inputFileURL.getPath()), templateFile.getAbsolutePath());
                    PDDocument templatePdf = PDDocument.load(new FileInputStream(templateFile), "", MemoryUsageSetting.setupTempFileOnly());
                    PDRectangle rectangle = templatePdf.getPage(0).getCropBox();

                    String documentPath = destinationFile + "/Document.plist";
                    FileInputStream inputStream = null;
                    NSDictionary rootDirectory = new NSDictionary();
                    try {
                        inputStream = new FileInputStream(documentPath);
                        rootDirectory = (NSDictionary) PropertyListParser.parse(inputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (rootDirectory.containsKey("pages")) {
                        NSArray pathsArray = (NSArray) rootDirectory.objectForKey("pages");
                        NSDictionary page = (NSDictionary) pathsArray.objectAtIndex(0);
                        String pageRect = "{{0, 0}, {" + (int) rectangle.getWidth() + ", " + (int) rectangle.getHeight() + "}}";
                        page.put("pdfKitPageRect", pageRect);

                        try {
                            File file = new File(documentPath);
                            FileOutputStream outputStream = null;
                            outputStream = new FileOutputStream(file);
                            outputStream.write(rootDirectory.toXMLPropertyList().getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<FTShelfItem> allLocalShelfItems(Context context) {
        List<FTShelfItem> shelfItems = new ArrayList<>();
        loadLocalShelfItemsRecursively(context, shelfItems, null);
        return shelfItems;
    }

    private synchronized void loadLocalShelfItemsRecursively(Context context, final List<FTShelfItem> allShelfItems, Object parent) {
        if (parent == null) {
            shelfs(shelfs -> {
                for (FTShelfItemCollection shelfItemCollection : shelfs) {
                    if (!shelfItemCollection.isTrash(context))
                        loadLocalShelfItemsRecursively(context, allShelfItems, shelfItemCollection);
                }
            });
        } else if (parent instanceof FTShelfItemCollection) {
            FTShelfItemCollection shelfItemCollection = (FTShelfItemCollection) parent;
            shelfItemCollection.shelfItems(context, FTShelfSortOrder.BY_NAME, null, "", (shelfItems, error) -> {
                for (FTShelfItem shelfItem : shelfItems) {
                    loadLocalShelfItemsRecursively(context, allShelfItems, shelfItem);
                }
            });
        } else if (parent instanceof FTGroupItem) {
            FTGroupItem groupItem = (FTGroupItem) parent;
            ((FTGroupItem) parent).getShelfCollection().shelfItems(context, FTShelfSortOrder.BY_NAME, groupItem, "", (shelfItems, error) -> {
                for (FTShelfItem shelfItem : shelfItems) {
                    loadLocalShelfItemsRecursively(context, allShelfItems, shelfItem);
                }
            });
        } else {
            allShelfItems.add((FTShelfItem) parent);
        }
    }
}