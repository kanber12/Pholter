package by.kanber.pholter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pholterDB";
    public static final String ALBUM_TABLE_NAME = "album";
    public static final String IMAGE_TABLE_NAME = "image";

    public static final String KEY_ID = "_id";
    public static final String KEY_ALBUM_INDEX = "album_index";
    public static final String KEY_NAME = "album_name";
    public static final String KEY_THUMB = "album_thumbnail";
    public static final String KEY_URI = "image_uri";
    public static final String KEY_DESCRIPTION = "image_description";
    public static final String KEY_IMAGE_INDEX = "image_index";
    public static final String KEY_ALBUM_ID = "album_id";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + ALBUM_TABLE_NAME + "(" + KEY_ID + " integer primary key autoincrement, " + KEY_ALBUM_INDEX + " integer, "
                + KEY_NAME + " text, " + KEY_THUMB + " text" + ")");

        sqLiteDatabase.execSQL("create table " + IMAGE_TABLE_NAME + "(" + KEY_ID + " integer primary key autoincrement, " + KEY_IMAGE_INDEX + " integer, "
                + KEY_URI + " text, " + KEY_DESCRIPTION + " text, " + KEY_ALBUM_ID + " integer" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + ALBUM_TABLE_NAME);
        sqLiteDatabase.execSQL("drop table if exists " + IMAGE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}