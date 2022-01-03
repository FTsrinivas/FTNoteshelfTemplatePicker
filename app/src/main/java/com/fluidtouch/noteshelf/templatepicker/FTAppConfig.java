package com.fluidtouch.noteshelf.templatepicker;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.FTMigrationModelClass;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.google.gson.Gson;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

public class FTAppConfig {

    private static FTAppConfig instance;
    int count;
    public static synchronized FTAppConfig getInstance() {
        if (instance == null) {
            instance = new FTAppConfig();
        }
        return instance;
    }

    public int getPlistVersion() {
        int plistVersion = 0;
        File plist = new File(FTConstants.TEMP_FOLDER_PATH +"/"+"themes_v8_en"+FTConstants.PLIST_EXTENSION);
        try {
            FileInputStream inputStream = new FileInputStream(plist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            NSNumber plistVersionNSNumber = (NSNumber) dictionary.objectForKey("version");
            return plistVersionNSNumber.intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plistVersion;
    }

    public static String pListfromServer() {
        String lang = Locale.getDefault().getLanguage();
        if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
            lang = "zh-Hans";
        } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
            lang = "zh-Hant";
        }
        String plistName = "themes_v8_" + lang + ".plist";
        return FTConstants.TEMP_FOLDER_PATH +"/"+plistName;
    }

    protected void deleteExistingFile(String filePath) {
        boolean deleted = false;
        //File file = new File(pListfromServer());
        File file = new File(filePath);
        if (file.exists()) {
            deleted = file.delete();
        }
        Log.d("TemplatePicker==>", "deleteExistingFile status::-"+deleted);
    }

    public void checkFileInAssetsFolder() {
        try {
            File migrationHelperList = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + "ThemesMigrationHelper"+".plist");
            if (migrationHelperList.exists()) {
                FileInputStream inputStream = new FileInputStream(migrationHelperList);
                NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
                NSNumber dynamic_id = (NSNumber) dictionary.objectForKey("dynamic_id");
                NSArray migratablesArrayList = (NSArray) dictionary.objectForKey("migratables");
                ArrayList<FTMigrationModelClass> migratablesDictInfoList = new ArrayList<>();
                for (int i=0;i<migratablesArrayList.count();i++) {
                    FTMigrationModelClass ftMigrationModelClass = new FTMigrationModelClass();
                    NSDictionary devicesArrayobjects = (NSDictionary) migratablesArrayList.objectAtIndex(i);
                    boolean bgColorExists       = devicesArrayobjects.containsKey("bgColor");
                    boolean landscape_padExists = devicesArrayobjects.containsKey("landscape_pad");
                    boolean new_pack_nameExists = devicesArrayobjects.containsKey("new_pack_name");
                    boolean portrait_padExists  = devicesArrayobjects.containsKey("portrait_pad");
                    boolean landscape_phoneExists  = devicesArrayobjects.containsKey("landscape_phone");
                    boolean portrait_phoneExists  = devicesArrayobjects.containsKey("portrait_phone");

                    ftMigrationModelClass.setDynamic_id(dynamic_id.intValue());

                    if (bgColorExists) {
                        NSString bgColor        = (NSString) devicesArrayobjects.objectForKey("bgColor");
                        ftMigrationModelClass.setBgColor(bgColor.toString());
                    }

                    if (landscape_padExists) {
                        NSString landscape_pad  = (NSString) devicesArrayobjects.objectForKey("landscape_pad");
                        ftMigrationModelClass.setLandscape_pad(landscape_pad.toString());
                    } else {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey landscape_pad::- FALSE");
                    }

                    if (new_pack_nameExists) {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey new_pack_name::- TRUE position::-"+i);
                        NSString new_pack_name  = (NSString) devicesArrayobjects.objectForKey("new_pack_name");
                        ftMigrationModelClass.setNew_pack_name(new_pack_name.toString());
                    } else {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey new_pack_name::- False position::-"+i);
                    }

                    if (portrait_padExists) {
                        NSString portrait_pad   = (NSString) devicesArrayobjects.objectForKey("portrait_pad");
                        ftMigrationModelClass.setPortrait_pad(portrait_pad.toString());
                    } else {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey portrait_padExists::- FALSE");
                    }

                    if (landscape_phoneExists) {
                        NSString landscape_phone   = (NSString) devicesArrayobjects.objectForKey("landscape_phone");
                        ftMigrationModelClass.setLandscape_phone(landscape_phone.toString());
                    } else {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey landscape_phoneExists::- FALSE");
                    }

                    if (portrait_phoneExists) {
                        NSString portrait_phone   = (NSString) devicesArrayobjects.objectForKey("portrait_phone");
                        ftMigrationModelClass.setPortrait_phone(portrait_phone.toString());
                    } else {
                        //Log.d("TemplatePicker==>","Migration devicesArrayobjects.containsKey portrait_phoneExists::- FALSE");
                    }

                    migratablesDictInfoList.add(ftMigrationModelClass);

                }

                for (int j=0;j<migratablesDictInfoList.size();j++) {
                    //Log.d("TemplatePicker==>","Migration migratablesDictInfoList Position::-"+j+" getLandscape_pad::-" +migratablesDictInfoList.get(j).getLandscape_pad() +" getPortrait_pad::-"+migratablesDictInfoList.get(j).getPortrait_pad());
                    if (Arrays.asList(FTApp.getInstance().getApplicationContext().getResources().
                            getAssets().list("stockPapers")).contains(migratablesDictInfoList.get(j).getLandscape_pad())
                            && (Arrays.asList(FTApp.getInstance().getApplicationContext().getResources().
                            getAssets().list("stockPapers")).contains(migratablesDictInfoList.get(j).getPortrait_pad()))) {

                        File mFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers_v2/download/");;
                        if (!mFile.exists()) {
                            mFile.mkdirs();
                        }

                        if (mFile.exists()) {
                            if ((Arrays.asList(FTApp.getInstance().getApplicationContext().getResources().
                                    getAssets().list("stockPapers")).contains(migratablesDictInfoList.get(j).getPortrait_pad()))) {
                                //Log.d("TemplatePicker==>","Migration AssetsUtil.isAssetExists::-"+AssetsUtil.isAssetExists("stockPapers/" + migratablesDictInfoList.get(j).getPortrait_pad()));
                                if (AssetsUtil.isAssetExists("stockPapers/" + migratablesDictInfoList.get(j).getPortrait_pad())) {
                                    String destFilePath = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers_v2/download/"+migratablesDictInfoList.get(j).getPortrait_pad();
                                    File destFile = new File(destFilePath);

                                    if (!destFile.exists()) {
                                        destFile.mkdir();
                                    }

                                    migratePortFolder(destFilePath,migratablesDictInfoList.get(j).getPortrait_pad(),migratablesDictInfoList.get(j).getDynamic_id());
                                    migrateLandFolder(destFilePath,migratablesDictInfoList.get(j).getLandscape_pad(),migratablesDictInfoList.get(j).getDynamic_id());
                                }
                            }
                        }
                        //Log.d("TemplatePicker==>","Migration checkFileInAssetsFolder getPortrait_pad::-"+migratablesDictInfoList.get(j).getNew_pack_name());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PropertyListFormatException e) {
            e.printStackTrace();
        }
    }

    private void migrateLandFolder(String destFilePath,String landscape_pad, int dynamic_id) {
        String srcFilePath = "stockPapers/" + landscape_pad;
        FTUrl fturl = new FTUrl(srcFilePath);
        copyAssets(fturl.getPath(),destFilePath,landscape_pad, dynamic_id);
    }

    private void migratePortFolder(String destFilePath, String portrait_pad, int dynamic_id) {
        String srcFilePath = "stockPapers/" + portrait_pad;
        FTUrl fturl = new FTUrl(srcFilePath);
        copyAssets(fturl.getPath(),destFilePath,portrait_pad,dynamic_id);
    }

    private void copyAssets(String filePath, String destPath, String direName, int dynamic_id) {
        AssetManager assetManager = FTApp.getInstance().getApplicationContext().getResources().getAssets();
        String[] files = null;
        try {
            files = assetManager.list(filePath);
        } catch (IOException e) {
            Log.d("TemplatePicker==>", "FTAppConfig Failed to get asset file list.", e);
        }
        Log.d("TemplatePicker==>","FTAppConfig  copyAssets::-"+files.length);
        if (files != null) for (String filename : files) {
            InputStream in   = null;
            OutputStream out = null;
            Log.d("TemplatePicker==>","FTAppConfig  copyAssets filename::- stockPapers/"+direName+"/"+filename);
            try {
                String path = "stockPapers/"+direName+"/"+filename;
                if (path.contains("Land.nsp")) {
                    if (!filename.contains("metadata.plist")) {
                        in = assetManager.open("stockPapers/"+direName+"/"+filename);
                        File outFile = new File(destPath, filename);
                        out = new FileOutputStream(outFile);
                        Log.d("TemplatePicker==>","FTAppConfig  copyAssets mode Landscape IF filename::- "+filename +" path::-"+path);
                        copyFile(in, out);
                        String srcFilePath = destPath+"/"+filename;
                        renameFiles(srcFilePath,direName,destPath);
                    } else {
                        Log.d("TemplatePicker==>","FTAppConfig  copyAssets mode Landscape ELSE filename::- "+filename +" path::-"+path);
                    }
                } else {
                    Log.d("TemplatePicker==>","FTAppConfig copyAssets mode PORT ELSE PORT path::- "+filename +" path::-"+path);
                    in = assetManager.open("stockPapers/"+direName+"/"+filename);
                    File outFile = new File(destPath, filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    String srcFilePath = destPath+"/"+filename;
                    renameFiles(srcFilePath,direName,destPath);
                    if (!direName.toLowerCase().contains("land")) {
                        addDynamicID(3,direName);
                    }
                }

            } catch(IOException e) {
                Log.d("TemplatePicker==>", "FTAppConfig Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                        Log.d("TemplatePicker==>", "FTAppConfig finally in NOT null Failed to copy asset file: " + e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                        Log.d("TemplatePicker==>", "FTAppConfig finally OUT NOT null Failed to copy asset file: " + e);
                    }
                }
            }
        }
    }

    private void addDynamicID(int dynamic_id, String direName) {
        count = count +1;
        //String path = FTConstants.DOWNLOADED_PAPERS_PATH +direName+"/";
        String path = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers_v2/download/"+direName+"/";
        //String path = FTConstants.DOWNLOADED_PAPERS_PATH +"Music Guitar.nsp/";
        //File plist = new File(FTConstants.DOWNLOADED_PAPERS_PATH +"/"+direName+"/metadata"+FTConstants.PLIST_EXTENSION);
        File plist = new File(path + "metadata.plist");
        plist.setWritable(true);
        Log.d("TemplatePicker==>","FTAppConfig url getPath:: addDynamicID path::-"+plist.getPath());
        migratePrevSelectedNSP();
        try {
            NSDictionary root = (NSDictionary) PropertyListParser.parse(plist);
            Log.d("TemplatePicker==>","FTAppConfig url getPath:: NSDictionary Count::-"+root.count());
            root.put("dynamic_id", new NSNumber(3) );
            try {
                File file = new File(plist.getPath());
                FileOutputStream outputStream = null;
                outputStream = new FileOutputStream(file);
                outputStream.write(root.toXMLPropertyList().getBytes());
                outputStream.close();
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d("TemplatePicker==>","FTAppConfig url getPath:: Exception::-"+e);
            }
            //saveDictionary(root, plist,path);
            Log.d("TemplatePicker==>","FTAppConfig url getPath:: addDynamicID root Count::-"+root.count()+" count::-"+count);
        } catch (Exception e) {
            Log.d("TemplatePicker==>","FTAppConfig url getPath:: Exception::-");
            e.printStackTrace();
        }
    }

    private void saveDictionary(NSDictionary root, File plist, String path) {
        try {
            FileOutputStream outputStream = null;
            outputStream = new FileOutputStream(plist);
            outputStream.write(root.toXMLPropertyList().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //String path = FTConstants.DOWNLOADED_PAPERS_PATH +"Music Guitar.nsp/";
        //String path = FTConstants.DOCUMENTS_ROOT_PATH + "/Library/papers_v2/download/"+"Music Guitar.nsp/";
        File metadataPlist = new File(path + "metadata.plist");
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(metadataPlist);
            NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
            if (dictionary.containsKey("dynamic_id")) {
                NSNumber dynamic_id = (NSNumber) dictionary.objectForKey("dynamic_id");
                Log.d("TemplatePicker==>","FTAppConfig url getPath:: addDynamicID dynamic_id::-"+dynamic_id+ " path::-"+path);
            } else {
                Log.d("TemplatePicker==>","FTAppConfig url getPath:: addDynamicID dynamic_id not exists::-");
            }

        } catch (IOException | PropertyListFormatException | ParseException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

    }

    private void savePlistWithChanges(String path, NSDictionary root) {
        try {
            File file = new File(path);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(root.toXMLPropertyList().getBytes());
            outputStream.close();
            Log.d("TemplatePicker==>","savePlistWithChanges TRY::-");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TemplatePicker==>","savePlistWithChanges catch::-");
        }
    }

    private void renameFiles(String srcFilePath, String direName, String destPath) {
        Log.d("TemplatePicker==>","FTAppConfig file path::-"+srcFilePath+" contains::-"+srcFilePath.contains("thumbnail@2x.png")+"destPath::-"+destPath);
        File sourceFile = new File(srcFilePath);
        File destFile = null;

        if (srcFilePath.contains("thumbnail@2x.png")) {
            if (direName.toLowerCase().contains("land")) {
                destFile = new File(destPath+"/"+"thumbnail_land@2x.png");
            } else {
                destFile = new File(destPath+"/"+"thumbnail_port@2x.png");
            }

            if (sourceFile.renameTo(destFile)) {
                Log.d("TemplatePicker==>","File renamed successfully");
            } else {
                Log.d("TemplatePicker==>","Failed to rename file");
            }
        }

        if (srcFilePath.contains("template.pdf")) {

            if (direName.toLowerCase().contains("land")) {
                destFile = new File(destPath+"/"+"template_land.pdf");
            } else {
                destFile = new File(destPath+"/"+"template_port.pdf");
            }

            if (sourceFile.renameTo(destFile)) {
                Log.d("TemplatePicker==>","File renamed successfully");
            } else {
                Log.d("TemplatePicker==>","Failed to rename file");
            }
        }


    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void copyMetadataIfNeeded(String fileName) {
        File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + fileName+".plist");

        if (plistFile.exists()) {
            deleteExistingFile(plistFile.getPath());
            //TODO: Need to remove below commented code for migration
            copyMetadataIfNeeded("ThemesMigrationHelper.plist");
            return;
        } else {
            plistFile.getParentFile().mkdirs();
            AssetManager assetmanager = FTApp.getInstance().getApplicationContext().getAssets();
            try {
                InputStream bundleInputStrem = assetmanager.open("" + fileName);
                this.createFileFromInputStream(bundleInputStrem,fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void createFileFromInputStream(InputStream inputStream, String fileName) {

        try {
            File f = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + fileName);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            checkFileInAssetsFolder();
            //return f;
        } catch (IOException e) {
            e.printStackTrace();
        }

        //return null;
    }

    private static void copyDirectoryImpl(File sourceDir, File destDir)
            throws IOException {
        File[] items = sourceDir.listFiles();
        Log.d("TemplatePicker==>","Migration copyDirectoryImpl items"+items);
        if (items != null && items.length > 0) {
            Log.d("TemplatePicker==>","Migration copyDirectoryImpl IF");
            for (File anItem : items) {
                if (anItem.isDirectory()) {
                    // create the directory in the destination
                    File newDir = new File(destDir, anItem.getName());
                    System.out.println("CREATED DIR: "
                            + newDir.getAbsolutePath());
                    newDir.mkdir();
                    // copy the directory (recursive call)
                    copyDirectory(anItem, newDir);
                } else {
                    // copy the file
                    File destFile = new File(destDir, anItem.getName());
                    copySingleFile(anItem, destFile);
                }
            }
        }
    }

    /**
     * Copy a whole directory to another location.
     * @param sourceDir a File object represents the source directory
     * @param destDir a File object represents the destination directory
     * @throws IOException thrown if IO error occurred.
     */
    public static void copyDirectory(File sourceDir, File destDir)
            throws IOException {
        // creates the destination directory if it does not exist
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // throws exception if the source does not exist
        if (!sourceDir.exists()) {
            throw new IllegalArgumentException("sourceDir does not exist");
        }

        // throws exception if the arguments are not directories
        if (sourceDir.isFile() || destDir.isFile()) {
            throw new IllegalArgumentException(
                    "Either sourceDir or destDir is not a directory");
        }

        copyDirectoryImpl(sourceDir, destDir);
    }

    /**
     * Copy a file from a location to another
     * @param sourceFile a File object represents the source file
     * @param destFile a File object represents the destination file
     * @throws IOException thrown if IO error occurred.
     */
    private static void copySingleFile(File sourceFile, File destFile)
            throws IOException {
        System.out.println("COPY FILE: " + sourceFile.getAbsolutePath()
                + " TO: " + destFile.getAbsolutePath());
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    private void saveImageInSP(Bitmap bitmap) {
        ByteArrayOutputStream baos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte[] arr = baos.toByteArray();
        String encodedImage = Base64.encodeToString(arr, Base64.DEFAULT);
        //return Base64.encodeToString(arr, Base64.DEFAULT);
        FTApp.getPref().save(SystemPref.RECENT_PAPER_BITMAP, encodedImage);
    }

    public void migratePrevSelectedNSP() {
        FTNTheme paperTheme = null;
        String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
        String tabSelected = "portrait";
        File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + "ThemesMigrationHelper"+".plist");
        Log.d("TemplatePicker==>","ManiKanth migratePrevSelectedNSP paperPackName::-"+paperPackName);
        if (paperPackName.endsWith(".nsp")) {
            try {
                FileInputStream inputStream = new FileInputStream(plistFile);
                NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
                NSArray defaultTemplateMigratablesList = (NSArray) dictionary.objectForKey("defaultTemplateMigratables");
                for (int i=0;i<defaultTemplateMigratablesList.count() ;i++) {
                    NSDictionary devicesArrayobjects = (NSDictionary) defaultTemplateMigratablesList.objectAtIndex(i);
                    boolean packNameExists       = devicesArrayobjects.containsKey("pack_name");
                    if (packNameExists) {
                        NSString packName        = (NSString) devicesArrayobjects.objectForKey("pack_name");
                        //Log.d("TemplatePicker==>","ManiKanth migratePrevSelectedNSP Prev version paperPackName::-"+paperPackName+" new version packName::-"+packName.toString());
                        if (packName.toString().equalsIgnoreCase(paperPackName)) {
                            Log.d("TemplatePicker==>","migratePrevSelectedNSP packName::-"+packName+" Position::-"+i);
                            boolean newPackNameExists       = devicesArrayobjects.containsKey("new_pack_name");
                            NSString newPackName = null;
                            NSString colorHex = null;
                            NSString lineType = null;
                            if (newPackNameExists) {
                                newPackName       = (NSString) devicesArrayobjects.objectForKey("new_pack_name");
                            }

                            paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(newPackName.toString()));

                            String metadataUrl = FTNThemeCategory.getUrl(newPackName.toString()).withAppendedPath("metadata.plist").getPath();
                            if (newPackNameExists) {
                                paperTheme.themeName = newPackName.toString();
                            }

                            paperTheme.ftThemeType = FTNThemeCategory.FTThemeType.PAPER;
                            boolean variantsExists       = devicesArrayobjects.containsKey("variants");
                            Log.d("TemplatePicker==>","ManiKanth defaultTemplateMigratables variantsExists::-"+variantsExists);
                            if (variantsExists) {
                                NSDictionary variantsDictionary = (NSDictionary) devicesArrayobjects.objectForKey("variants");
                                Log.d("TemplatePicker==>","ManiKanth defaultTemplateMigratables variantsArray::-"+variantsDictionary.count());

                                boolean templateSizeExists       = variantsDictionary.containsKey("templateSize");
                                if (templateSizeExists) {
                                    NSString templateSize       = (NSString) variantsDictionary.objectForKey("templateSize");
                                    if (templateSize.toString().equalsIgnoreCase("iPad")) {
                                        paperTheme.isTablet = true;
                                    } else {
                                        paperTheme.isTablet = false;
                                    }
                                }

                                boolean colorHexExists       = variantsDictionary.containsKey("colorHex");
                                Log.d("TemplatePicker==>","defaultTemplateMigratables variants colorHexExists::-"+colorHexExists);
                                if (colorHexExists) {
                                    colorHex       = (NSString) variantsDictionary.objectForKey("colorHex");
                                    Log.d("TemplatePicker==>","defaultTemplateMigratables variants colorHex::-"+colorHex.toString());
                                    paperTheme.themeBgClr = colorHex.toString();
                                }

                                boolean isLandscapeExists       = variantsDictionary.containsKey("isLandscape");
                                Log.d("TemplatePicker==>","defaultTemplateMigratables variants isLandscapeExists::-"+isLandscapeExists);
                                if (isLandscapeExists) {
                                    NSNumber isLandscape       = (NSNumber) variantsDictionary.objectForKey("isLandscape");
                                    Log.d("TemplatePicker==>","defaultTemplateMigratables variants isLandscape::-"+isLandscape.boolValue());
                                    if (isLandscape.boolValue()) {
                                        paperTheme.isLandscape = true;
                                    } else {
                                        paperTheme.isLandscape = false;
                                    }
                                }

                                boolean lineTypeExists       = devicesArrayobjects.containsKey("lineType");
                                if (lineTypeExists) {
                                    lineType       = (NSString) devicesArrayobjects.objectForKey("lineType");
                                }
                            }

                            InputStream inputStreamURL = null;
                            Log.d("TemplatePicker==>","metadataDict metadataUrl.startsWith(\"stock\")::-"+metadataUrl);
                            if (metadataUrl.startsWith("stock")) {
                                inputStreamURL = FTApp.getInstance().getCurActCtx().getAssets().open(metadataUrl);
                            } else {
                                inputStreamURL = new FileInputStream(metadataUrl);
                            }

                            NSDictionary metadataDict = (NSDictionary) PropertyListParser.parse(inputStreamURL);

                            FTTemplateUtil ftTemplateUtil   = FTTemplateUtil.getInstance();
                            if (metadataDict.containsKey("dynamic_template_info")) {
                                NSDictionary dynamicTemplateInfoDict = (NSDictionary) metadataDict.get("dynamic_template_info");
                                Log.d("TemplatePicker==>"," metadataDict.containsKey(\"dynamicTemplateInfoDict\") TRUE::-"+dynamicTemplateInfoDict);
                                NSString horizontalLineColorNSString = null;
                                NSString verticalLineColorNSString = null;
                                if (dynamicTemplateInfoDict.containsKey("horizontalLineColor")) {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalLineColor\") TRUE");
                                    horizontalLineColorNSString = (NSString) dynamicTemplateInfoDict.get("horizontalLineColor");
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalLineColor\") FALSE");
                                }

                                if (dynamicTemplateInfoDict.containsKey("verticalLineColor")) {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"verticalLineColor\") TRUE");
                                    verticalLineColorNSString   = (NSString) dynamicTemplateInfoDict.get("verticalLineColor");
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"verticalLineColor\") FALSE");
                                }

                                if (dynamicTemplateInfoDict.containsKey("horizontalLineColor") &&
                                        dynamicTemplateInfoDict.containsKey("verticalLineColor")) {
                                    Log.d("TemplatePicker==>","Basic Template Info FTAppConfig paperTheme.themeBgClr::-"+paperTheme.themeBgClr+" themeCLrName White::-");
                                    ftTemplateUtil.fTTemplateColorsSerializedObject(paperTheme.themeBgClr,"White",
                                            verticalLineColorNSString.toString(),horizontalLineColorNSString.toString());
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"verticalLineColor\") FALSE");
                                }

                                NSNumber horizontalSpacingNSNumber = null;
                                NSNumber verticalSpacingNSNumber = null;
                                if (dynamicTemplateInfoDict.containsKey("horizontalSpacing")) {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalSpacing\") TRUE");
                                    horizontalSpacingNSNumber = (NSNumber) dynamicTemplateInfoDict.get("horizontalSpacing");
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalSpacing\") FALSE");
                                }

                                if (dynamicTemplateInfoDict.containsKey("verticalSpacing")) {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"verticalSpacing\") TRUE");
                                    verticalSpacingNSNumber = (NSNumber) dynamicTemplateInfoDict.get("verticalSpacing");
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"verticalSpacing\") FALSE");
                                }

                                if (dynamicTemplateInfoDict.containsKey("horizontalSpacing") &&
                                        dynamicTemplateInfoDict.containsKey("verticalSpacing")) {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalSpacing and verticalSpacing\") TRUE");
                                    ftTemplateUtil.fTTemplateLineTypeSerializedObject(verticalSpacingNSNumber.intValue(),"Default",horizontalSpacingNSNumber.intValue());
                                } else {
                                    Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalSpacing and verticalSpacing\") FALSE");
                                }

                            } else {
                                Log.d("TemplatePicker==>"," metadataDict.containsKey(\"horizontalSpacing and verticalSpacing\") FALSE");
                            }

                            /*FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();
                            if (paperTheme.isLandscape) {
                                ftSelectedDeviceInfo.setLayoutType("landscape");
                            } else {
                                ftSelectedDeviceInfo.setLayoutType("portrait");
                            }

                            ftSelectedDeviceInfo.setPageWidth(Integer.parseInt(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_WIDTH, "100")));
                            ftSelectedDeviceInfo.setPageHeight(Integer.parseInt(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_HEIGHT, "100")));
                            ftTemplateUtil.setFtSelectedDeviceInfo(ftSelectedDeviceInfo);
                            Log.d("TemplatePicker==>"," FTAppConfig Notebook getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                                    " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight());*/
                            FTNTheme finalPaperTheme = paperTheme;

                            if (paperTheme.dynamicId == 2) {
                                AsyncTask.execute(() -> {
                                    Log.d("TemplatePicker==>"," Sample Notebook FTAppConfig ");
                                    FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                                    finalPaperTheme.template(FTApp.getInstance().getApplicationContext(), (documentInfo, generationError) -> {
                                        final FTUrl fileUri = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getUDID());
                                        final FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(fileUri);
                                    });
                                });
                            }

                            Gson gson = new Gson();
                            String json = gson.toJson(finalPaperTheme);
                            FTApp.getPref().save(SystemPref.RECENT_PAPER_THEME, json);
                            FTTemplatesInfoSingleton.getInstance().setPaperTheme(finalPaperTheme);
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImageInDummy(Bitmap image, String imageName) {
        /*
         * Saving bitmap to internal storage
         * */
        String cachePath    = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/";
        File tempCacheFiles = new File(cachePath);
        if (!tempCacheFiles.exists()) {
            tempCacheFiles.mkdir();
        }

        File pictureFile = new File(cachePath+""+""+imageName+".jpg");
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


}
