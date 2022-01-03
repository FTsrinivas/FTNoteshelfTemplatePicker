package com.fluidtouch.noteshelf.document

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1
import com.fluidtouch.noteshelf.annotation.FTStrokeV1
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1
import com.fluidtouch.noteshelf.commons.utils.ObservingService
import com.fluidtouch.noteshelf.document.undomanager.UndoManager
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage
import com.fluidtouch.renderingengine.annotation.*
import com.fluidtouch.renderingengine.utils.FTGeometryUtils

class FTAnnotationManager(callback: FTAnnotationManagerCallback) {
    lateinit var currentPage: FTNoteshelfPage
    val containerCallback: FTAnnotationManagerCallback = callback

    fun addAnnotations(annotations: List<FTAnnotation>, refreshview: Boolean) {
        addAnnotations(annotations, ArrayList(), refreshview)
    }

    fun addAnnotations(annotations: List<FTAnnotation>, annotationIndexes: List<Int>, refreshview: Boolean) {
        val items = getAudioFreeAnnotations(annotations)

        for (i in items.indices) {
            if (items[i] is FTStroke) {
                (items.get(i) as FTStroke).isErased = false
            }
        }
        currentPage.addAnnotations(annotations, annotationIndexes)
        if (!items.isEmpty()) {
            if (annotationIndexes.size > 0)
                addToUndoManager(items, annotationIndexes, "removeAnnotations")
            else
                addToUndoManager(items, "removeAnnotations")
        }
//        (containerCallback.getContext() as FTDocumentActivity).enableUndoButton()
        ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true)
        if (refreshview) {
            var refreshRect = getRefreshRect(annotations)
            containerCallback.refreshOffscreen(refreshRect)
            refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
            containerCallback.reloadInRect(refreshRect)
        }
        currentPage.setPageDirty(true)
    }

    fun removeAnnotations(annotations: List<FTAnnotation>, refreshView: Boolean) {
        removeAnnotations(annotations, ArrayList<Int>(), refreshView);
    }

    fun removeAnnotations(annotations: List<FTAnnotation>, annotationIndexes: List<Int>, refreshView: Boolean) {
        val items = getAudioFreeAnnotations(annotations)
        if (!items.isEmpty()) {
            if (annotationIndexes.size > 0)
                addToUndoManager(items, annotationIndexes, "addAnnotations")
            else
                addToUndoManager(items, "addAnnotations")
        }
        currentPage.removeAnnotations(annotations)
        if (refreshView) {
            var refreshRect = getRefreshRect(annotations)
            containerCallback.refreshOffscreen(refreshRect)
            refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
            containerCallback.reloadInRect(refreshRect)
        }
//        (containerCallback.getContext() as FTDocumentActivity).enableUndoButton()
        ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true)
        currentPage.setPageDirty(true)
    }

    fun updateAnnotations(oldAnnotations: java.util.ArrayList<FTAnnotation>, helperAnnotations: java.util.ArrayList<FTAnnotation>, refreshView: Boolean) {
        val undoAnnotations: ArrayList<FTAnnotation> = ArrayList();
        val items = getAudioFreeAnnotations(oldAnnotations)
        val items1 = getAudioFreeAnnotations(helperAnnotations)
        for (index in items.indices) {
            undoAnnotations.add(getUndoAnnotation(items[index], items1.get(index)))
        }
        containerCallback.currentUndoManager().addUndo(FTAnnotationManager::class.java, "updateAnnotations", 3, arrayOf(items, undoAnnotations, true), this)
        currentPage.isPageDirty = true
        if (refreshView) {
            var refreshRect = RectF(getRefreshRect(oldAnnotations))
            refreshRect.union(getRefreshRect(undoAnnotations))
            containerCallback.refreshOffscreen(refreshRect)
            refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
            containerCallback.reloadInRect(refreshRect)
        }
//        (containerCallback.getContext() as FTDocumentActivity).enableUndoButton()
        ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true)
    }

    private fun getUndoAnnotation(oldAnnotation: FTAnnotation, helperAnnotation: FTAnnotation): FTAnnotation {
        val undoAnnotation: FTAnnotation;
        if (oldAnnotation.annotationType() == FTAnnotationType.text) {
            undoAnnotation = setUpTextAnnotation(oldAnnotation as FTTextAnnotationV1, helperAnnotation as FTTextAnnotationV1)
        } else if (oldAnnotation.annotationType() == FTAnnotationType.image) {
            undoAnnotation = setUpImageAnnotation(oldAnnotation as FTImageAnnotation, helperAnnotation as FTImageAnnotation)
        } else if (oldAnnotation.annotationType() == FTAnnotationType.stroke) {
            undoAnnotation = setUpStrokeAnnotation(oldAnnotation as FTStroke, helperAnnotation as FTStroke)
        } else {
            undoAnnotation = FTAnnotation(oldAnnotation.context)
        }
        return undoAnnotation
    }

    fun updateAnnotation(oldAnnotation: FTAnnotation, helperAnnotation: FTAnnotation, refreshView: Boolean) {
        val undoAnnotation: FTAnnotation;
        if (oldAnnotation.annotationType() == FTAnnotationType.text) {
            undoAnnotation = setUpTextAnnotation(oldAnnotation as FTTextAnnotationV1, helperAnnotation as FTTextAnnotationV1)
        } else if (oldAnnotation.annotationType() == FTAnnotationType.image) {
            undoAnnotation = setUpImageAnnotation(oldAnnotation as FTImageAnnotation, helperAnnotation as FTImageAnnotation)
        } else if (oldAnnotation.annotationType() == FTAnnotationType.audio) {
            oldAnnotation.boundingRect = helperAnnotation.boundingRect
            (oldAnnotation as FTAudioAnnotationV1).audioRecording = (helperAnnotation as FTAudioAnnotationV1).audioRecording
            updateAnnotation(oldAnnotation, true)
            return
        } else if (oldAnnotation.annotationType() == FTAnnotationType.stroke) {
            undoAnnotation = setUpStrokeAnnotation(oldAnnotation as FTStroke, helperAnnotation as FTStroke)
        } else {
            undoAnnotation = FTAnnotation(oldAnnotation.context)
        }
        containerCallback.currentUndoManager().addUndo(FTAnnotationManager::class.java, "updateAnnotation", 3, arrayOf(oldAnnotation, undoAnnotation, true), this)
        currentPage.setPageDirty(true)
        if (refreshView) {
            var refreshRect: RectF
            if (oldAnnotation.annotationType() == FTAnnotationType.image) {
                var newAnn = undoAnnotation as FTImageAnnotation
                var oldAnn = oldAnnotation as FTImageAnnotation
                refreshRect = RectF(oldAnn.renderingRect)
                refreshRect.union(newAnn.renderingRect)
            } else {
                refreshRect = RectF(oldAnnotation.boundingRect)
                refreshRect.union(undoAnnotation.boundingRect)
            }
            containerCallback.refreshOffscreen(refreshRect)
            refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
            containerCallback.reloadInRect(refreshRect)
        }
//        (containerCallback.getContext() as FTDocumentActivity).enableUndoButton()
        ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true)
    }

    fun replaceAnnotations(replaceAnnotations: List<FTAnnotation>, addedAnnotations: List<FTAnnotation>) {
        currentPage.removeAnnotations(replaceAnnotations)
        currentPage.addAnnotations(addedAnnotations)
//        (containerCallback.getContext() as FTDocumentActivity).enableUndoButton()
        ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true)
        var refreshRect = getRefreshRect(addedAnnotations)
        containerCallback.refreshOffscreen(refreshRect)
        refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
        containerCallback.reloadInRect(refreshRect)
        currentPage.setPageDirty(true)
        containerCallback.currentUndoManager().addUndo(FTAnnotationManager::class.java, "replaceAnnotations", 2, arrayOf<Any>(addedAnnotations, replaceAnnotations), this)
    }

    private fun setUpTextAnnotation(oldAnnotation: FTTextAnnotation, helperAnnotation: FTTextAnnotation): FTAnnotation {
        val undoAnnotation = FTTextAnnotationV1(oldAnnotation.context)

        undoAnnotation.boundingRect = oldAnnotation.boundingRect
        undoAnnotation.setInputTextWithInfo((oldAnnotation as FTTextAnnotationV1).textInputInfo)
        undoAnnotation.textInputInfo.plainText = oldAnnotation.textInputInfo.plainText

        oldAnnotation.boundingRect = helperAnnotation.boundingRect
        oldAnnotation.setInputTextWithInfo((helperAnnotation as FTTextAnnotationV1).textInputInfo)
        oldAnnotation.textInputInfo.plainText = helperAnnotation.textInputInfo.plainText
        oldAnnotation.hidden = false
        return undoAnnotation
    }

    private fun setUpImageAnnotation(oldAnnotation: FTImageAnnotation, helperAnnotation: FTImageAnnotation): FTAnnotation {
        val undoAnnotation = FTImageAnnotationV1(oldAnnotation.context, currentPage)

        undoAnnotation.boundingRect = oldAnnotation.getBoundingRect()
        undoAnnotation.setBitmap(oldAnnotation.image)
        undoAnnotation.imgAngel = oldAnnotation.imgAngel
        undoAnnotation.imgTxMatrix = oldAnnotation.imgTxMatrix

        oldAnnotation.boundingRect = helperAnnotation.boundingRect
        oldAnnotation.setBitmap(helperAnnotation.image)
        oldAnnotation.imgAngel = helperAnnotation.imgAngel
        oldAnnotation.imgTxMatrix = helperAnnotation.imgTxMatrix
        oldAnnotation.hidden = false
        return undoAnnotation
    }

    private fun setUpStrokeAnnotation(oldAnnotation: FTStroke, helperAnnotation: FTStroke): FTAnnotation {
        val undoAnnotation = FTStrokeV1(oldAnnotation.context)

        undoAnnotation.boundingRect = oldAnnotation.getBoundingRect()
        undoAnnotation.strokeColor = oldAnnotation.strokeColor
        undoAnnotation.strokeWidth = oldAnnotation.strokeWidth
        for (j in 0 until oldAnnotation.segmentCount) {
            val segment = oldAnnotation.getSegmentAtIndex(j)
            val newSegment = FTSegment(PointF(segment.startPoint.x, segment.startPoint.y), PointF(segment.endPoint.x, segment.endPoint.y),
                    segment.thickness, RectF(segment.boundingRect), segment.opacity)
            newSegment.setSegmentAsErased(segment.isSegmentErased())
            undoAnnotation.addSegment(newSegment)
//            undoAnnotation.addSegment(PointF(segment.startPoint.x, segment.startPoint.y), PointF(segment.endPoint.x, segment.endPoint.y),
//                    segment.thickness, segment.opacity, RectF(segment.boundingRect))
        }

        oldAnnotation.boundingRect = helperAnnotation.boundingRect
        oldAnnotation.strokeColor = helperAnnotation.strokeColor
        oldAnnotation.strokeWidth = helperAnnotation.strokeWidth
        for (j in 0 until helperAnnotation.segmentCount) {
            val segment = helperAnnotation.getSegmentAtIndex(j)
            val newSegment = FTSegment(PointF(segment.startPoint.x, segment.startPoint.y), PointF(segment.endPoint.x, segment.endPoint.y),
                    segment.thickness, RectF(segment.boundingRect), segment.opacity)
            newSegment.setSegmentAsErased(segment.isSegmentErased())
            oldAnnotation.setSegmentAtIndex(j, newSegment)
//            oldAnnotation.setSegmentAtIndex(j, FTSegment(PointF(segment.startPoint.x, segment.startPoint.y), PointF(segment.endPoint.x, segment.endPoint.y),
//                    segment.thickness, RectF(segment.boundingRect), segment.opacity))
        }
        oldAnnotation.hidden = false
        return undoAnnotation
    }

    fun updateAnnotation(oldAnnotation: FTAnnotation, refreshview: Boolean) {
        oldAnnotation.hidden = false
        currentPage.setPageDirty(true)
        if (refreshview) {
            var refreshRect = oldAnnotation.boundingRect
            containerCallback.refreshOffscreen(refreshRect)
            refreshRect = FTGeometryUtils.scaleRect(refreshRect, containerCallback.scale)
            containerCallback.reloadInRect(refreshRect)
        }
    }

    fun clearPageAnnotations(annotations: List<FTAnnotation>) {
        val items = getAudioFreeAnnotations(annotations)
        this.removeAnnotations(items, true)
    }

    private fun getAudioFreeAnnotations(list: List<FTAnnotation>): List<FTAnnotation> {
        val annotations = ArrayList(list)
        var i = 0
        while (i < annotations.size) {
            if (annotations.get(i).annotationType() == FTAnnotationType.audio) {
                annotations.removeAt(i)
                i--
            }
            i++
        }

        return annotations;
    }

    private fun getRefreshRect(annotations: List<FTAnnotation>): RectF {
        val refreshRect = RectF()
        for (index in annotations.indices) {
            val annotation = annotations.get(index)
            val boundingRect = if (annotation.annotationType() == FTAnnotationType.image) (annotation as FTImageAnnotation).renderingRect else annotation.boundingRect
            if (index == 0) {
                refreshRect.set(boundingRect.left, boundingRect.top, boundingRect.right, boundingRect.bottom)
            } else {
                refreshRect.union(boundingRect.left, boundingRect.top, boundingRect.right, boundingRect.bottom)
            }
        }
        return refreshRect
    }

    private fun addToUndoManager(annotations: List<FTAnnotation>, methodName: String) {
        containerCallback.currentUndoManager().addUndo(FTAnnotationManager::class.java, methodName, 2, arrayOf<Any>(annotations, true), this)
    }

    private fun addToUndoManager(annotations: List<FTAnnotation>, annotationIndexes: List<Int>, methodName: String) {
        containerCallback.currentUndoManager().addUndo(FTAnnotationManager::class.java, methodName, 3, arrayOf<Any>(annotations, annotationIndexes, true), this)
    }

    interface FTAnnotationManagerCallback {
        val scale: Float

        fun refreshOffscreen(rect: RectF);
        fun reloadInRect(refreshRect: RectF?)
        fun getContext(): Context
        fun currentUndoManager(): UndoManager
    }
    //endregion
}