package com.fluidtouch.noteshelf.documentframework.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.RectF;

import com.dd.plist.NSData;
import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTStrokeV1;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ColorUtil;
import com.fluidtouch.noteshelf.document.enums.NSTextAlignment;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "annotation";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ANNOTATIONTYPE = "annotationType";
    private static final String COLUMN_STROKEWIDTH = "strokeWidth";
    private static final String COLUMN_STROKECOLOR = "strokeColor";
    private static final String COLUMN_PENTYPE = "penType";
    private static final String COLUMN_BOUDINGRECT = "boundingRect_x";
    private static final String COLUMN_SCREENSCLAE = "screenScale";
    private static final String COLUMN_TXMATRIX = "txMatrix";
    private static final String COLUMN_IMGMATRIX = "imgTxMatrix";
    private static final String COLUMN_IMGANGLE = "imgAngle";
    private static final String COLUMN_ATTRTEXT = "attrText";
    private static final String COLUMN_NONATTRTEXT = "nonAttrText";
    private static final String COLUMN_SEGMENTCOUNT = "segmentCount";
    private static final String COLUMN_SEGMENTDATA = "segments";
    private static final String COLUMN_CREATEDTIME = "createdTime";
    private static final String COLUMN_MODIFIEDTIME = "modifiedTime";
    private static final String COLUMN_EMOJI = "emojiName";
    private static final String COLUMN_READONLY = "isReadonly";
    private static final String COLUMN_VERSION = "version";
    private static final String COLUMN_TRANSFORM = "transformScale";
    private static final String COLUMN_FONTCOLOR = "fontColor";
    private static final String COLUMN_FONTSIZE = "fontSize";
    private static final String COLUMN_FONTFAMILY = "fontFamily";
    private static final String COLUMN_FONTSTYLE = "fontStyle";
    private static final String COLUMN_ISUNDERLINED = "isUnderlined";
    private static final String COLUMN_TEXTALIGNMENT = "textAlignment";
    private static final String COLUMN_JSONSTRING = "textJsonString";
    private static final String COLUMN_TEXT_PADDING = "textPadding";
    private static final String COLUMN_IMG_ANT_LOCK_STATUS = "imgAntLockStatus";

    String annotationCreateQuery = "CREATE TABLE IF NOT EXISTS annotation (id TEXT DEFAULT null,annotationType INTEGER DEFAULT 0,strokeWidth NUMERIC DEFAULT 0," +
            "strokeColor INTEGER DEFAULT 0,penType INTEGER DEFAULT 0,boundingRect_x NUMERIC DEFAULT 0,boundingRect_y NUMERIC DEFAULT 0,boundingRect_w NUMERIC DEFAULT 0,boundingRect_h NUMERIC DEFAULT 0,screenScale " +
            "NUMERIC DEFAULT 1,txMatrix TEXT DEFAULT null,imgTxMatrix TEXT DEFAULT null,imgAngle REAL DEFAULT 0,attrText BLOB DEFAULT null,nonAttrText TEXT DEFAULT null,segmentCount INTEGER DEFAULT 0,segments BLOB DEFAULT null,createdTime " +
            "REAL DEFAULT 0,modifiedTime REAL DEFAULT 0,emojiName TEXT DEFAULT null,isReadonly NUMERIC DEFAULT 0,version INTEGER DEFAULT 2,transformScale NUMERIC DEFAULT 1, textJsonString TEXT DEFAULT null," +
            "fontColor INTEGER DEFAULT 0, fontSize INTEGER DEFAULT 0, fontFamily TEXT DEFAULT null, fontStyle INTEGER DEFAULT 0, isUnderlined BOOLEAN DEFAULT 0, textAlignment INTEGER DEFAULT 0, textPadding INTEGER DEFAULT 10,imgAntLockStatus INTEGER DEFAULT 0)";

    public DatabaseHelper(Context context, String filePath) {
        super(context, filePath, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.annotationCreateQuery);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!columnExistsInTable(db, TABLE_NAME, COLUMN_IMG_ANT_LOCK_STATUS)) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAntLockStatus INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            if (newVersion == 2 && oldVersion == 1) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN fontColor INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN fontSize INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN fontFamily TEXT DEFAULT null");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN fontStyle INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN isUnderlined BOOLEAN DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textAlignment INTEGER DEFAULT 0");
            }

            if (newVersion == 3) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAngle REAL DEFAULT 0");
            }

            if (newVersion == 4) {
                if (oldVersion == 3) {
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textPadding INTEGER DEFAULT 10");
                } else if (oldVersion == 2) {
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAngle REAL DEFAULT 0");
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textPadding INTEGER DEFAULT 10");
                }
            }

//            if (newVersion == 5) {
//                if (oldVersion == 4) {
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAntLockStatus INTEGER DEFAULT 0");
//                } else if (oldVersion == 3) {
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textPadding INTEGER DEFAULT 10");
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAntLockStatus INTEGER DEFAULT 0");
//                } else if (oldVersion == 2) {
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAngle REAL DEFAULT 0");
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN textPadding INTEGER DEFAULT 10");
//                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN imgAntLockStatus INTEGER DEFAULT 0");
//                }
//            }
        }
    }

    public boolean saveAnnotations(ArrayList<FTAnnotation> anotations) {
        boolean success = true;
        int count = anotations.size();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            deleteData(db);
            db.execSQL("PRAGMA auto_vacuum = 1;");
            Cursor c1 = db.rawQuery("PRAGMA journal_mode=DELETE", null);
            c1.close();
            db.beginTransaction();
            for (int i = 0; i < count; i++) {
                FTAnnotation anotation = anotations.get(i);
                ContentValues contentValues = new ContentValues();
                switch (anotation.annotationType()) {
                    case stroke: {
                        FTStroke stroke = (FTStroke) anotation;
                        contentValues.put("id", stroke.uuid);
                        contentValues.put("annotationType", stroke.annotationType().toInt());
                        contentValues.put("strokeWidth", stroke.strokeWidth);
                        contentValues.put("strokeColor", ColorUtil.iOSColor(stroke.strokeColor));
                        contentValues.put("penType", stroke.penType.toInt());
                        contentValues.put("boundingRect_x", stroke.getBoundingRect().left);
                        contentValues.put("boundingRect_y", stroke.getBoundingRect().top);
                        contentValues.put("boundingRect_w", stroke.getBoundingRect().width());
                        contentValues.put("boundingRect_h", stroke.getBoundingRect().height());
                        contentValues.put("screenScale", stroke.screenScale);
                        contentValues.put("transformScale", stroke.transformScale);
                        contentValues.put("segmentCount", stroke.segmentCount);
                        contentValues.put("segments", stroke.segmentData().bytes());

                        contentValues.put("createdTime", stroke.createdTimeInterval);
                        contentValues.put("modifiedTime", stroke.modifiedTimeInterval);
                        contentValues.put("isReadonly", stroke.readOnly);
                        contentValues.put("version", stroke.defaultAnnotationVersion());
                    }
                    break;
                    case image: {
                        FTImageAnnotation image = (FTImageAnnotation) anotation;
                        contentValues.put(COLUMN_ID, image.uuid);
                        contentValues.put(COLUMN_ANNOTATIONTYPE, image.annotationType().toInt());
                        contentValues.put("boundingRect_x", image.getBoundingRect().left);
                        contentValues.put("boundingRect_y", image.getBoundingRect().top);
                        contentValues.put("boundingRect_w", image.getBoundingRect().width());
                        contentValues.put("boundingRect_h", image.getBoundingRect().height());
                        contentValues.put(COLUMN_SCREENSCLAE, image.screenScale);
                        contentValues.put(COLUMN_CREATEDTIME, image.createdTimeInterval);
                        contentValues.put(COLUMN_MODIFIEDTIME, image.modifiedTimeInterval);
                        contentValues.put(COLUMN_READONLY, image.readOnly);
                        contentValues.put(COLUMN_IMGMATRIX, image.getImgTxMatrix());//"[1, 0, 0, 1, 0, 0]"
                        contentValues.put(COLUMN_IMGANGLE, image.getImgAngel());//"[1, 0, 0, 1, 0, 0]"
                        contentValues.put(COLUMN_TXMATRIX, "[1, 0, 0, 1, 0, 0]");
                        contentValues.put(COLUMN_VERSION, image.version);
                        contentValues.put(COLUMN_IMG_ANT_LOCK_STATUS, ((FTImageAnnotationV1) image).getImageLockStatus());
                    }
                    break;
                    case text: {
                        FTTextAnnotationV1 text = (FTTextAnnotationV1) anotation;
                        contentValues.put("id", text.uuid);
                        contentValues.put("annotationType", text.annotationType().toInt());
                        contentValues.put("boundingRect_x", text.getBoundingRect().left);
                        contentValues.put("boundingRect_y", text.getBoundingRect().top);
                        contentValues.put("boundingRect_w", text.getBoundingRect().width());
                        contentValues.put("boundingRect_h", text.getBoundingRect().height());
                        contentValues.put("screenScale", text.screenScale);
                        contentValues.put("createdTime", text.createdTimeInterval);
                        contentValues.put("modifiedTime", text.modifiedTimeInterval);
                        contentValues.put("isReadonly", text.readOnly);
                        contentValues.put("version", text.version);
                        contentValues.put(COLUMN_NONATTRTEXT, text.getNonAttributedString());

                        FTStyledText styledText = text.getTextInputInfo();
                        contentValues.put(COLUMN_FONTCOLOR, ColorUtil.iOSColor(styledText.getColor()));
                        contentValues.put(COLUMN_FONTSIZE, styledText.getSize());
                        contentValues.put(COLUMN_FONTSTYLE, styledText.getStyle());
                        contentValues.put(COLUMN_FONTFAMILY, styledText.getFontFamily());
                        contentValues.put(COLUMN_ISUNDERLINED, styledText.isUnderline());
                        contentValues.put(COLUMN_TEXTALIGNMENT, styledText.getAlignment().toInt());
                        contentValues.put(COLUMN_TEXT_PADDING, styledText.getPadding());
                    }
                    break;
                    case audio: {
                        FTAudioAnnotation audio = (FTAudioAnnotation) anotation;
                        contentValues.put("id", audio.uuid);
                        contentValues.put("annotationType", audio.annotationType().toInt());
                        contentValues.put("boundingRect_x", audio.getBoundingRect().left);
                        contentValues.put("boundingRect_y", audio.getBoundingRect().top);
                        contentValues.put("boundingRect_w", audio.getBoundingRect().width());
                        contentValues.put("boundingRect_h", audio.getBoundingRect().height());
                        contentValues.put("screenScale", audio.screenScale);
                        contentValues.put("createdTime", audio.createdTimeInterval);
                        contentValues.put("modifiedTime", audio.modifiedTimeInterval);
                        contentValues.put("isReadonly", audio.readOnly);
                        contentValues.put("version", audio.version);
                    }
                    break;
                    default:
                        break;
                }
                long result = db.insert(TABLE_NAME, null, contentValues);
                if (result == -1) {
                    success = false;
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            if (db != null)
                db.close();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            FTLog.logCrashException(new Exception("connection pool has been closed"));
        }
        return success;
    }

    public int deleteData(SQLiteDatabase db) {
        return db.delete(TABLE_NAME, null, null);
    }

    public Cursor displayData(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " where username=? and password=?", new String[]{username, password});
        return res;
    }

    public synchronized ArrayList<FTAnnotation> getAllAnnotationsForPage(Context context, FTNoteshelfPage page) {
        ArrayList<FTAnnotation> annotations = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
            if (res.getCount() > 0) {

                while (res.moveToNext()) {
                    FTAnnotationType annotationType = getAnnotationType(res);
                    switch (annotationType) {
                        case stroke: {
                            FTStrokeV1 stroke = new FTStrokeV1(context);
                            stroke.version = res.getInt(res.getColumnIndex(COLUMN_VERSION));
                            stroke.uuid = res.getString(res.getColumnIndex(COLUMN_ID));
                            stroke.strokeWidth = res.getFloat(res.getColumnIndex(COLUMN_STROKEWIDTH));
                            stroke.strokeColor = ColorUtil.androidColor(res.getInt(res.getColumnIndex(COLUMN_STROKECOLOR)));
                            stroke.penType = getPenType(res);
                            stroke.setBoundingRect(this.getCanvasBoundingRect(res));
                            stroke.screenScale = res.getFloat(res.getColumnIndex(COLUMN_SCREENSCLAE));
                            stroke.transformScale = res.getFloat(res.getColumnIndex(COLUMN_TRANSFORM));
                            stroke.segmentCount = res.getInt(res.getColumnIndex(COLUMN_SEGMENTCOUNT));
                            stroke.setSegmentData(new NSData(res.getBlob(res.getColumnIndex(COLUMN_SEGMENTDATA))));
                            stroke.createdTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_CREATEDTIME));
                            stroke.modifiedTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_MODIFIEDTIME));
                            annotations.add(stroke);
                        }
                        break;
                        case image: {
                            FTImageAnnotationV1 image = new FTImageAnnotationV1(context, page);
                            image.uuid = res.getString(res.getColumnIndex(COLUMN_ID));
                            image.setBoundingRect(this.getCanvasBoundingRect(res));
                            image.screenScale = res.getFloat(res.getColumnIndex(COLUMN_SCREENSCLAE));
                            image.transformScale = res.getFloat(res.getColumnIndex(COLUMN_TRANSFORM));
                            image.createdTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_CREATEDTIME));
                            image.modifiedTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_MODIFIEDTIME));
                            image.version = res.getInt(res.getColumnIndex(COLUMN_VERSION));
                            image.setImgTxMatrix(res.getString(res.getColumnIndex(COLUMN_IMGMATRIX)));
                            image.setImgAngel(res.getFloat(res.getColumnIndex(COLUMN_IMGANGLE)));
                            image.setImageLockStatus(res.getInt(res.getColumnIndex(COLUMN_IMG_ANT_LOCK_STATUS)));
                            annotations.add(image);
                        }
                        break;
                        case text: {
                            FTTextAnnotationV1 text = new FTTextAnnotationV1(context);
                            text.uuid = res.getString(res.getColumnIndex(COLUMN_ID));
                            text.setBoundingRect(this.getCanvasBoundingRect(res));
                            text.screenScale = res.getFloat(res.getColumnIndex(COLUMN_SCREENSCLAE));
                            text.transformScale = res.getFloat(res.getColumnIndex(COLUMN_TRANSFORM));
                            text.createdTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_CREATEDTIME));
                            text.modifiedTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_MODIFIEDTIME));
                            text.version = res.getInt(res.getColumnIndex(COLUMN_VERSION));

                            FTStyledText styledText = new FTStyledText();
                            String textString = res.getString(res.getColumnIndex(COLUMN_NONATTRTEXT));
                            if (textString != null) {
                                styledText.setPlainText(textString);
                            }
                            if (res.getColumnIndex(COLUMN_FONTCOLOR) != -1) {
                                int fontColor = res.getInt(res.getColumnIndex(COLUMN_FONTCOLOR));
                                styledText.setColor(ColorUtil.androidColor(fontColor));

                                int fontSize = res.getInt(res.getColumnIndex(COLUMN_FONTSIZE));
                                if (fontSize > 0) {
                                    styledText.setSize(fontSize);
                                }

                                int fontStyle = res.getInt(res.getColumnIndex(COLUMN_FONTSTYLE));
                                styledText.setStyle(fontStyle);

                                String fontFamily = res.getString(res.getColumnIndex(COLUMN_FONTFAMILY));
                                if (fontFamily != null) {
                                    styledText.setFontFamily(fontFamily);
                                }

                                int alignment = res.getInt(res.getColumnIndex(COLUMN_TEXTALIGNMENT));
                                styledText.setAlignment(NSTextAlignment.initWithRawValue(alignment));

                                int underlined = res.getInt(res.getColumnIndex(COLUMN_ISUNDERLINED));
                                styledText.setUnderline(underlined != 0);

                                int padding = res.getInt(res.getColumnIndex(COLUMN_TEXT_PADDING));
                                styledText.setPadding(padding);
                            }
                            text.setInputTextWithInfo(styledText);
                            annotations.add(text);
                        }
                        break;
                        case audio: {
                            FTAudioAnnotationV1 audio = new FTAudioAnnotationV1(context, page);
                            audio.uuid = res.getString(res.getColumnIndex(COLUMN_ID));
                            audio.setBoundingRect(this.getCanvasBoundingRect(res));
                            audio.screenScale = res.getFloat(res.getColumnIndex(COLUMN_SCREENSCLAE));
                            audio.transformScale = res.getFloat(res.getColumnIndex(COLUMN_TRANSFORM));
                            audio.createdTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_CREATEDTIME));
                            audio.modifiedTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_MODIFIEDTIME));
                            audio.version = res.getInt(res.getColumnIndex(COLUMN_VERSION));
                            annotations.add(audio);
                        }
                        break;
                    }
                }
            }
            if (res != null)
                res.close();
            if (db != null)
                db.close();
        } catch (Exception e) {
            e.printStackTrace();
            FTLog.logCrashException(e);
        }
        return annotations;
    }

    public ArrayList<FTTextAnnotation> textAnnotationsContainingKeyword(Context context, String keyWord) {
        ArrayList<FTTextAnnotation> annotations = new ArrayList<>();
        String keywordSelQuery = "SELECT * from annotation WHERE " + COLUMN_NONATTRTEXT + " like ('%" + keyWord + "%')";
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery(keywordSelQuery, null);

            if (res.getCount() > 0) {

                while (res.moveToNext()) {
                    FTAnnotationType annotationType = getAnnotationType(res);
                    switch (annotationType) {
                        case text: {
                            FTTextAnnotationV1 text = new FTTextAnnotationV1(context);
                            text.uuid = res.getString(res.getColumnIndex(COLUMN_ID));
                            text.setBoundingRect(this.getCanvasBoundingRect(res));
                            text.screenScale = res.getFloat(res.getColumnIndex(COLUMN_SCREENSCLAE));
                            text.transformScale = res.getFloat(res.getColumnIndex(COLUMN_TRANSFORM));
                            text.createdTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_CREATEDTIME));
                            text.modifiedTimeInterval = res.getDouble(res.getColumnIndex(COLUMN_MODIFIEDTIME));
                            text.version = res.getInt(res.getColumnIndex(COLUMN_VERSION));

                            FTStyledText styledText = new FTStyledText();
                            String textString = res.getString(res.getColumnIndex(COLUMN_NONATTRTEXT));
                            if (textString != null) {
                                styledText.setPlainText(textString);
                            }
                            if (res.getColumnIndex(COLUMN_FONTCOLOR) != -1) {
                                int fontColor = res.getInt(res.getColumnIndex(COLUMN_FONTCOLOR));
                                styledText.setColor(ColorUtil.androidColor(fontColor));

                                int fontSize = res.getInt(res.getColumnIndex(COLUMN_FONTSIZE));
                                if (fontSize > 0) {
                                    styledText.setSize(fontSize);
                                }

                                int fontStyle = res.getInt(res.getColumnIndex(COLUMN_FONTSTYLE));
                                styledText.setStyle(fontStyle);

                                String fontFamily = res.getString(res.getColumnIndex(COLUMN_FONTFAMILY));
                                if (fontFamily != null) {
                                    styledText.setFontFamily(fontFamily);
                                }

                                int alignment = res.getInt(res.getColumnIndex(COLUMN_TEXTALIGNMENT));
                                styledText.setAlignment(NSTextAlignment.initWithRawValue(alignment));

                                int underlined = res.getInt(res.getColumnIndex(COLUMN_ISUNDERLINED));
                                styledText.setUnderline(underlined != 0);
                            }
                            text.setInputTextWithInfo(styledText);
                            annotations.add(text);
                        }
                    }
                }
            }
            if (res != null)
                res.close();
            if (db != null)
                db.close();
        } catch (Exception e) {
            e.printStackTrace();
            FTLog.logCrashException(new Exception("connection pool has been closed"));
        }
        return annotations;
    }

    private FTAnnotationType getAnnotationType(Cursor res) {
        int annotationRawValue = res.getInt(res.getColumnIndex(COLUMN_ANNOTATIONTYPE));
        FTAnnotationType annotationType = FTAnnotationType.initWithRawValue(annotationRawValue);
        return annotationType;
    }

    private FTPenType getPenType(Cursor res) {
        int annotationRawValue = res.getInt(res.getColumnIndex(COLUMN_PENTYPE));
        return FTPenType.initWithRawValue(annotationRawValue);
    }

    private RectF getCanvasBoundingRect(Cursor res) {
        RectF boundingRect = new RectF();
        float x = res.getFloat(res.getColumnIndex(COLUMN_BOUDINGRECT));
        float y = res.getFloat(res.getColumnIndex(COLUMN_BOUDINGRECT) + 1);
        float w = res.getFloat(res.getColumnIndex(COLUMN_BOUDINGRECT) + 2);
        float h = res.getFloat(res.getColumnIndex(COLUMN_BOUDINGRECT) + 3);
        float rightX = x + w;
        float bottomY = y + h;
        boundingRect.set(x, y, rightX, bottomY);
        return boundingRect;
    }

    private boolean columnExistsInTable(SQLiteDatabase db, String table, String columnToCheck) {
        Cursor cursor = null;
        try {
            //query a row. don't acquire db lock
            cursor = db.rawQuery("SELECT * FROM " + table + " LIMIT 0", null);

            // getColumnIndex()  will return the index of the column
            //in the table if it exists, otherwise it will return -1
            if (cursor.getColumnIndex(columnToCheck) != -1) {
                //great, the column exists
                return true;
            } else {
                //sorry, the column does not exist
                return false;
            }

        } catch (SQLiteException Exp) {
            //Something went wrong with SQLite.
            //If the table exists and your query was good,
            //the problem is likely that the column doesn't exist in the table.
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }


//    int toAndroidColor(int c){
//        int r = (c>>16)&0xFF;
//        int g = (c>>8)&0xFF;
//        int b = (c)&0xFF;
//        int colorCode = (255 << 24) | (((c>>24)&0xFF) << 16) | (((c>>16)&0xFF) << 8) | ((c>>8)&0xFF);
//        return colorCode;
//    }
//    int toiOSColor(int c){
//
//        int r = (c>>24)&0xFF;
//        int g = (c>>16)&0xFF;
//        int b = (c>>8)&0xFF;
//        int colorCode = (((c>>24)&0xFF) << 16) | (((c>>16)&0xFF) << 8) | ((c>>8)&0xFF);
//        return colorCode;
//    }
}