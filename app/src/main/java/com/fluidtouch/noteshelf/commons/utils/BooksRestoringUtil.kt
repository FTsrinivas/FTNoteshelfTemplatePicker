package com.fluidtouch.noteshelf.commons.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.NSNumber
import com.dd.plist.PropertyListParser
import java.io.File
import java.io.FileOutputStream

/**
 * Created by sreenu on 8/6/21.
 */
class BooksRestoringUtil {
    fun copyPlistData(context: Context) {
        var root = NSDictionary()
        try {
            root = PropertyListParser.parse(
                    FTFileManagerUtil.getFileInputStream(File(ContextCompat.getDataDir(context).toString() + "/Noteshelf.nsdata/User Documents/My Notes.shelf/Teachers Grammar of English OCR.ns_a/Document.plist"))) as NSDictionary
            var dictionaries: NSArray? = null
            val file = File(ContextCompat.getDataDir(context).toString() + "/Noteshelf.nsdata/User Documents/My Notes.shelf/Teachers Grammar of English OCR.ns_a/Annotations")
            if (file.isDirectory) {
                val list = file.listFiles()
                var count = 0
                dictionaries = NSArray(142)
                for (resultFile in list) {
                    if (!resultFile.name.contains("journal")) {
                        dictionaries.setValue(count, dictionaryRepresentation(resultFile))
                        count++
                    }
                }
            }
            root["pages"] = dictionaries
            try {
                val finalFile = File(ContextCompat.getDataDir(context).toString() + "/Noteshelf.nsdata/User Documents/My Notes.shelf/Teachers Grammar of English OCR.ns_a/Document.plist")
                var outputStream: FileOutputStream? = null
                outputStream = FileOutputStream(finalFile)
                outputStream.write(root.toXMLPropertyList().toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dictionaryRepresentation(file: File): NSDictionary {
        val dict = NSDictionary();
        dict.put("uuid", file.name);
        dict.put("associatedPDFFileName", "A4D89B41-0E05-4EA0-BBAC-1437A33B965B.ns_pdf");
        dict.put("associatedPageIndex", NSNumber(1));
        dict.put("associatedPDFKitPageIndex", NSNumber(1));
        dict.put("bookmarkColor", "8ACCEA");
        dict.put("bookmarkTitle", "");
        dict.put("deviceModel", "Samsung SM-T976B");
        dict.put("isBookmarked", false);
        dict.put("creationDate", NSNumber(file.lastModified()));
        dict.put("lastUpdated", NSNumber(file.lastModified()));
        dict.put("lineHeight", NSNumber(34));
        dict.put("tags", "");
        dict.put("pdfKitPageRect", "{{0, 0}, {1651, 2511}}");
        return dict;
    }
}