package com.fluidtouch.noteshelf.document

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument

object FTNDTempHolder {

    private var ndDocuments = ArrayList<FTNoteshelfDocument>()

    fun putNDDocument(document: FTNoteshelfDocument) {
        ndDocuments.add(document)
    }

    fun getNDDocument(index: Int): FTNoteshelfDocument? {
        if (ndDocuments.size > index) {
            var document = ndDocuments.get(index)
            ndDocuments.removeAt(index)
            return document
        }
        return null
    }
}