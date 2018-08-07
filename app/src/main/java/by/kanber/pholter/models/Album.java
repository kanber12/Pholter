package by.kanber.pholter.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import by.kanber.pholter.database.DBHelper;
import by.kanber.pholter.interfaces.PholterMedia;
import by.kanber.pholter.util.Utils;

public class Album implements PholterMedia {
    private int id, index;
    private String name, thumbnail = "";
    private boolean isSelected = false;

    public Album(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static ArrayList<Album> getAlbums(DBHelper helper) {
        int tId, tIndex;
        String tName, tThumb;
        ArrayList<Album> albums = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.query(DBHelper.ALBUM_TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID),
                    indIndex = cursor.getColumnIndex(DBHelper.KEY_ALBUM_INDEX),
                    nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME),
                    thumbIndex = cursor.getColumnIndex(DBHelper.KEY_THUMB);

            do {
                tId = cursor.getInt(idIndex);
                tIndex = cursor.getInt(indIndex);
                tName = cursor.getString(nameIndex);
                tThumb = cursor.getString(thumbIndex);
                Album album = new Album(tIndex, tName);
                album.setId(tId);

                if (Utils.checkFile(tThumb))
                    album.setThumbnail(tThumb);
                else {
                    album.setThumbnail("");
                    insertOrUpdateDB(helper, album);
                }

                albums.add(album);
            } while (cursor.moveToNext());

            cursor.close();
        }

        Utils.sortMedia(albums);

        return albums;
    }

    public static Album getLastAlbum(DBHelper helper) {
        int tId, tIndex;
        String tName, tThumb;
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.ALBUM_TABLE_NAME, null, null, null, null, null, null);

        cursor.moveToLast();

        tId = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID));
        tIndex = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ALBUM_INDEX));
        tName = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME));
        tThumb = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_THUMB));

        cursor.close();

        Album album = new Album(tIndex, tName);
        album.setId(tId);
        album.setThumbnail(tThumb);

        return album;
    }

    public static void insertOrUpdateDB(DBHelper helper, Album album) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase database = helper.getReadableDatabase();

        cv.put(DBHelper.KEY_ALBUM_INDEX, album.getIndex());
        cv.put(DBHelper.KEY_NAME, album.getName());
        cv.put(DBHelper.KEY_THUMB, album.getThumbnail());

        int count = database.update(DBHelper.ALBUM_TABLE_NAME, cv, DBHelper.KEY_ID + "=" + album.getId(), null);

        if (count == 0)
            database.insertWithOnConflict(DBHelper.ALBUM_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void deleteFromDB(DBHelper helper, Album album) {
        SQLiteDatabase database = helper.getReadableDatabase();
        database.delete(DBHelper.ALBUM_TABLE_NAME, DBHelper.KEY_ID + "=" + album.getId(), null);
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", index=" + index +
                ", name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
