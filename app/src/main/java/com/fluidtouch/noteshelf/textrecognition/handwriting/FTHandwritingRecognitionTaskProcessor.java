package com.fluidtouch.noteshelf.textrecognition.handwriting;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SizeF;
import android.widget.Toast;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.annotation.FTStrokeV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTDeviceUtils;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels.BoundingBox;
import com.fluidtouch.noteshelf.textrecognition.handwriting.myscriptmodels.Letter;
import com.fluidtouch.noteshelf.textrecognition.handwriting.utils.FontMetricsProvider;
import com.fluidtouch.noteshelf.textrecognition.handwriting.utils.FontUtils;
import com.fluidtouch.noteshelf.textrecognition.helpers.FTRecognitionUtils;
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols;
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols.FTBackgroundTaskProcessor;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTSegment;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.google.gson.Gson;
import com.myscript.iink.Configuration;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.MimeType;
import com.myscript.iink.PointerEvent;
import com.myscript.iink.PointerEventType;
import com.myscript.iink.PointerType;
import com.myscript.iink.Renderer;
import com.myscript.iink.graphics.Transform;
import com.samsung.android.sdk.pen.plugin.interfaces.SpenRecognizerResultContainerInterface;
import com.samsung.android.sdk.pen.plugin.interfaces.SpenRecognizerResultInterface;
import com.samsung.android.sdk.pen.recogengine.SpenRecognizer;
import com.samsung.android.sdk.pen.recogengine.SpenResourceProvider;
import com.samsung.android.sdk.pen.recogengine.interfaces.SpenRecognizerResultDocumentInterface;
import com.samsung.android.sdk.pen.recogengine.preload.SpenRecognizerResultText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FTScriptEvent {
    public int type = 0; //0 -> down 1 -> moved 2 -> up
    public PointF point = new PointF();
}

public class FTHandwritingRecognitionTaskProcessor implements FTBackgroundTaskProcessor {
    private Editor editor;
    private String languageCode;
    private boolean canAcceptNewTask = true;

    private Context context;
    private Engine engine;
    private Map<String, Typeface> typefaceMap = new HashMap<>();
    private SpenRecognizer mSpenRecognizer;
    private SpenResourceProvider mResTextProvider;
    private SpenResourceProvider mResDLAProvider;
    private SpenRecognizerResultContainerInterface mResultContainer = null;

    public FTHandwritingRecognitionTaskProcessor(Context context, String languageCode) {
        this.context = context;
        this.languageCode = languageCode;
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            if (mSpenRecognizer == null) {
                mSpenRecognizer = FTApp.getEngine(context);
            }
        } else {
            if (engine == null) {
                engine = FTApp.getEngine();
            }
        }
        createEditorWRTLanguage();
    }

    private void createEditorWRTLanguage() {
//        if (FTApp.isForSamsungStore()) {
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            // get text DB files from "/sdcard/HWRDB/"
            mResTextProvider = mSpenRecognizer.getResourceProvider();
            mResTextProvider.setRootDirectory(FTRecognitionUtils.recognitionResourcesFolderURL(context).getPath());
            mResDLAProvider = new SpenResourceProvider(context, SpenResourceProvider.EngineType.DOCUMENT, SpenResourceProvider.ResourceType.ASSETS);
            setLanguage(languageCode);
        } else {
            String configurationPath = FTRecognitionUtils.configurationPathForLanguage(context, this.languageCode);
            if (configurationPath == null) {
                FTLanguageResourceManager.getInstance().setCurrentLanguageCode("en_US");
            } else {
                Configuration conf = this.engine.getConfiguration();
                conf.setStringArray("configuration-manager.search-path", new String[]{configurationPath});
                String tempDir = context.getFilesDir().getPath() + File.separator + "tmp";
                conf.setString("content-package.temp-folder", tempDir);
                conf.setString("lang", this.languageCode);
            }
            initializeRecognitionProcessor();
        }
    }

    private void initializeRecognitionProcessor() {
        Configuration conf = engine.getConfiguration();
        float verticalMarginMM = 0;
        float horizontalMarginMM = 0;

        conf.setBoolean("export.jiix.text.chars", true);
        conf.setBoolean("text.guides.enable", false);

        conf.setNumber("text.margin.top", verticalMarginMM);
        conf.setNumber("text.margin.left", horizontalMarginMM);
        conf.setNumber("text.margin.right", horizontalMarginMM);
        conf.setNumber("math.margin.top", verticalMarginMM);
        conf.setNumber("math.margin.bottom", verticalMarginMM);
        conf.setNumber("math.margin.left", horizontalMarginMM);
        conf.setNumber("math.margin.right", horizontalMarginMM);
        loadFonts();

        if (editor == null) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            Renderer renderer = engine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, null);
            editor = engine.createEditor(renderer);
            editor.setFontMetricsProvider(new FontMetricsProvider(displayMetrics, typefaceMap));
            editor.setViewSize(768, 960);
            editor.setPart(null);
            FTHandwritingRecognitionPackage recognitionPackage = new FTHandwritingRecognitionPackage(context, this.editor, this.engine);
            recognitionPackage.assignPartToEditor();
        }
    }

    private void loadFonts() {
        AssetManager assets = context.getApplicationContext().getAssets();
        try {
            String assetsDir = "fonts";
            String[] files = assets.list(assetsDir);
            for (String filename : files) {
                String fontPath = assetsDir + File.separatorChar + filename;
                String fontFamily = FontUtils.getFontFamily(assets, fontPath);
                final Typeface typeface = Typeface.createFromAsset(assets, fontPath);
                if (fontFamily != null && typeface != null) {
                    typefaceMap.put(fontFamily, typeface);
                }
            }
        } catch (IOException e) {
            Log.e(FTLog.HW_RECOGNITION, "Failed to list fonts from assets", e);
        }
    }

    private void updateRecognitionLanguage(String languageCode) {
        this.languageCode = languageCode;
        this.editor = null;
        this.createEditorWRTLanguage();
    }

    @Override
    public boolean canAcceptNewTask() {
        return this.canAcceptNewTask;
    }

    @Override
    public void startTask(FTBackgroundTaskProtocols.FTBackgroundTask task, FTBackgroundTaskProtocols.OnCompletion onCompletion) {
        FTHandwritingRecognitionTask currentTask = (FTHandwritingRecognitionTask) task;
        this.canAcceptNewTask = false;
        if (this.mSpenRecognizer == null) {
            this.canAcceptNewTask = true;
            onCompletion.didFinish();
        }

        if (!currentTask.languageCode.equals(this.languageCode)) {
            updateRecognitionLanguage(currentTask.languageCode);
        }

        FTHandwritingRecognitionResult result = getRecognitionText(currentTask.pageAnnotations, currentTask.viewSize);
        this.canAcceptNewTask = true;

        onCompletion.didFinish();

        Error error = null;
        if (!currentTask.pageAnnotations.isEmpty() && result.characterRects.isEmpty())
            error = new Error("Recognition failed for page");
        currentTask.onCompletion(result, error);
    }

    private boolean isHighlighterPen(FTPenType type) {
        return type == FTPenType.highlighter || type == FTPenType.flatHighlighter;
    }

    private FTHandwritingRecognitionResult getRecognitionText(List<FTAnnotation> annotations, SizeF viewSize) {
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            if (mSpenRecognizer == null) {
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            }
            mSpenRecognizer.clearStrokes();
            ArrayList<SpenRecognizerResultInterface> mResults = new ArrayList<>();
            mResultContainer = null;

            // Add strokes
            List<FTAnnotation> strokeAnnotations = new ArrayList<>();
            for (int i = 0; i < annotations.size(); i++) {
                boolean shouldProcessAnnotation = false;
                FTAnnotation annotation = annotations.get(i);
                if (annotation != null && annotation.annotationType() == FTAnnotationType.stroke && !isHighlighterPen(((FTStroke) annotation).penType)) {
                    shouldProcessAnnotation = true;
                }

                if (shouldProcessAnnotation) {
                    FTStrokeV1 stroke = (FTStrokeV1) annotation;
                    strokeAnnotations.add(stroke);
                    mSpenRecognizer.addStroke(stroke.getXPoints(), stroke.getYPoints());
                }
            }
            if (strokeAnnotations.size() == 0) {
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            }
            // Recognizer
            mResultContainer = mSpenRecognizer.recognize();
            if (mResultContainer == null || mResultContainer.getResultCount() <= 0) {
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            }

            int resultCount = mResultContainer.getResultCount();
            String[] resultString = new String[resultCount];

            for (int i = 0; i < resultCount; i++) {
                mResults.add(mResultContainer.getResult(i));
            }
            String label = "";
            ArrayList<NSValue> characterRects = new ArrayList<>();
            for (int i = 0; i < resultCount; i++) {
                SpenRecognizerResultInterface.ResultType type = mResults.get(i).getResultType();

                if (type == SpenRecognizerResultInterface.ResultType.TEXT) {
                    SpenRecognizerResultText retText = (SpenRecognizerResultText) mResults.get(i);
                    String firstCand = retText.getResultString(0);
                    label = label + firstCand;
                    resultString[i] = "[" + i + "] Text : " + firstCand;

                    for (int charIdx = 0; charIdx < firstCand.length(); charIdx++) {
                        char ch = firstCand.charAt(charIdx);
                        if (ch != ' ') {
                            RectF charRect = new RectF();
                            for (Integer strokeIdx : retText.getStrokeIndex(charIdx)) {
                                FTAnnotation rectStrokeData = strokeAnnotations.get(strokeIdx);
                                charRect.union(rectStrokeData.getBoundingRect());
                            }
                            NSValue transformedRect = new NSValue(charRect);
                            characterRects.add(transformedRect);
                        }
                    }
                } else if (type == SpenRecognizerResultInterface.ResultType.DOCUMENT) {
                    resultString[i] = "[" + i + "] Document";
                    SpenRecognizerResultDocumentInterface documentResult = (SpenRecognizerResultDocumentInterface) mResults.get(i);
                    int group_count = documentResult.getGroupCount();
                    for (int group = 0; group < group_count; group++) {
                        resultString[i] += "\n    " + documentResult.getGroupType(group).toString();
                        resultString[i] += "(" + documentResult.getGroupStroke(group).size() + "): " + Arrays.toString(documentResult.getGroupStroke(group).toArray());
                        if (documentResult.getGroupType(group) == SpenRecognizerResultDocumentInterface.GroupType.TEXT) {
                            int subGroupCount = documentResult.getSubGroupCount(i);
                            for (int subGroup = 0; subGroup < subGroupCount; subGroup++) {
                                resultString[i] += "\n        #" + subGroup;
                                resultString[i] += "(" + documentResult.getSubGroupStrokeCount(group, subGroup)
                                        + ", skewed: " + documentResult.isSubGroupSkewed(group, subGroup) + "): "
                                        + Arrays.toString(documentResult.getSubGroupStroke(group, subGroup).toArray());
                            }
                        }
                    }
                } else {
                    resultString[i] = "[" + i + "] Unknown";
                }
            }
            strokeAnnotations.clear();
            FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
            recognitionData.recognisedString = label;
            recognitionData.characterRects = characterRects;
            recognitionData.languageCode = this.languageCode;
            recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
            return recognitionData;
        } else {
            ArrayList<FTScriptEvent> events = new ArrayList<>();

            int totalPoints = 0;

            FTLog.debug(FTLog.HW_RECOGNITION, "Annotations found for page = " + annotations.size());
            for (int a = 0; a < annotations.size(); a++) {
                FTAnnotation annotation = annotations.get(a);
                boolean shouldProcessAnnotation = false;
                if (annotation != null && annotation.annotationType() == FTAnnotationType.stroke && !isHighlighterPen(((FTStroke) annotation).penType)) {
                    shouldProcessAnnotation = true;
                }

                if (shouldProcessAnnotation) {
                    FTStroke strokeAnnotation = (FTStroke) annotation;

                    boolean isStartingPoint = true;
                    PointF endingPoint = new PointF();
                    //************************
                    int segmentCount = strokeAnnotation.segmentCount;
                    if (segmentCount > 0) {
                        for (int iCount = 0; iCount < segmentCount; iCount++) {

                            FTSegment segment = strokeAnnotation.getSegmentAtIndex(iCount);
                            PointF capturePoint = new PointF(segment.startPoint.x, segment.startPoint.y);

                            if (isStartingPoint) {
                                isStartingPoint = false;
                                FTScriptEvent scriptPoint = new FTScriptEvent();
                                scriptPoint.point = capturePoint;
                                scriptPoint.type = 0;
                                events.add(scriptPoint);
                            } else {
                                FTScriptEvent scriptPoint = new FTScriptEvent();
                                scriptPoint.point = capturePoint;
                                scriptPoint.type = 1;
                                events.add(scriptPoint);
                            }
                            totalPoints = totalPoints + 1;
                            endingPoint = capturePoint;
                        }
                        if (!isStartingPoint) {
                            FTScriptEvent scriptPoint = new FTScriptEvent();
                            scriptPoint.point = endingPoint;
                            scriptPoint.type = 2;
                            events.add(scriptPoint);
                        }
                        //************************
                    }
                }
            }
            if (events.size() == 0) {
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            }

            try {
                editor.clear();
                this.editor.setViewSize((int) viewSize.getWidth(), (int) viewSize.getHeight());
                finishBulkEvents(events.size(), events);
                editor.waitForIdle();
                String jsonString = this.editor.export_(editor.getRootBlock(), MimeType.JIIX);
                Gson gson = new Gson();
                FTHandwritingRecognitionResultJSON jsonDict = gson.fromJson(jsonString, FTHandwritingRecognitionResultJSON.class);

                List<Letter> arrayChars = jsonDict.getChars();
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = jsonDict.getLabel();
                Transform transform = editor.getRenderer().getViewTransform();

                ArrayList<NSValue> characterRects = new ArrayList<>();
                if (arrayChars != null) {
                    for (Letter dictChar : arrayChars) {
                        BoundingBox boundingBox = dictChar.getBoundingBox();
                        if (boundingBox != null) {
                            RectF charRect = new RectF();
                            charRect.left = boundingBox.getX().floatValue();
                            charRect.top = boundingBox.getY().floatValue();
                            charRect.right = charRect.left + boundingBox.getWidth().floatValue();
                            charRect.bottom = charRect.top + boundingBox.getHeight().floatValue();

                            Matrix transformMatrix = new Matrix();

                            float[] transformValues = new float[9];
                            transformValues[Matrix.MPERSP_0] = 0;
                            transformValues[Matrix.MPERSP_1] = 0;
                            transformValues[Matrix.MPERSP_2] = 1;
                            transformValues[Matrix.MSCALE_X] = (float) transform.xx;
                            transformValues[Matrix.MSKEW_X] = (float) transform.yx;
                            transformValues[Matrix.MTRANS_X] = (float) transform.tx;
                            transformValues[Matrix.MSKEW_Y] = (float) transform.xy;
                            transformValues[Matrix.MSCALE_Y] = (float) transform.yy;
                            transformValues[Matrix.MTRANS_Y] = (float) transform.ty;
                            transformMatrix.setValues(transformValues);
                            transformMatrix.mapRect(charRect);

                            NSValue transformedRect = new NSValue(charRect);
                            characterRects.add(transformedRect);
                        } else {
                            characterRects.add(new NSValue());
                        }
                    }
                }

                recognitionData.characterRects = characterRects;
                recognitionData.languageCode = this.languageCode;
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;

            } catch (IOException e) {
                e.printStackTrace();
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            } catch (Exception e) {
                e.printStackTrace();
                FTLog.logCrashException(e);
                FTHandwritingRecognitionResult recognitionData = new FTHandwritingRecognitionResult();
                recognitionData.recognisedString = "";
                recognitionData.languageCode = this.languageCode;
                recognitionData.characterRects = new ArrayList<>();
                recognitionData.lastUpdated = FTDeviceUtils.getTimeStamp();
                return recognitionData;
            }
        }
    }

    private void finishBulkEvents(int totalCount, ArrayList<FTScriptEvent> events) {
        int index = 0;
        PointerEvent[] pointerEvents = new PointerEvent[totalCount];
        for (FTScriptEvent eachEvent : events) {
            switch (eachEvent.type) {
                case 0:
                    pointerEvents[index] = new PointerEvent(PointerEventType.DOWN, eachEvent.point.x, eachEvent.point.y, -1, 0, PointerType.PEN, 0);
                    break;
                case 1:
                    pointerEvents[index] = new PointerEvent(PointerEventType.MOVE, eachEvent.point.x, eachEvent.point.y, -1, 0, PointerType.PEN, 0);
                    break;
                case 2:
                    pointerEvents[index] = new PointerEvent(PointerEventType.UP, eachEvent.point.x, eachEvent.point.y, -1, 0, PointerType.PEN, 0);
                    break;
                default:
                    break;
            }
            index++;
        }
        editor.pointerEvents(pointerEvents, false);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (editor != null) {
            editor.close();
        }
    }

    private void setLanguage(String language) {
        try {
            // DO NOT USE this because this api "setLanguage(String)" is only for Samsung devices.
            //mSpenRecognizer.setLanguage(language);

            // Instead, USE this api "setLanguageData(String, byte[], byte[])"
            if (mResTextProvider == null || mResDLAProvider == null) {
                Toast.makeText(context, "resource provider is null!", Toast.LENGTH_LONG).show();
                return;
            }
            byte[][] langData = mResTextProvider.getLanguageData(context, language);
            Log.d(FTLog.HW_RECOGNITION, "langData length = " + langData.length);
            if (langData.length == 1) {
                mSpenRecognizer.setLanguageData(language, langData[0], null);
            } else if (langData.length == 2) {
                mSpenRecognizer.setLanguageData(language, langData[0], langData[1]);
            } else {
                return;
            }
            byte[][] dlaData = mResDLAProvider.getDocumentData(context);
            Log.d(FTLog.HW_RECOGNITION, "dlaData length = " + langData.length);
            if (dlaData.length == 2) {
                mSpenRecognizer.setAnalyzerData(dlaData[0], dlaData[1]);
            } else {
                return;
            }
        } catch (RuntimeException e) {
            String err = "Cannot setLanguage : " + language;
            Log.e(FTLog.HW_RECOGNITION, err);
            e.printStackTrace();
        }
    }
}