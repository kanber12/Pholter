package by.kanber.pholter;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class Image implements PholterMedia {
    private int id, index, albumId;
    private String uri, description = "";
    private boolean isSelected = false;

    public Image(int index, String uri, int albumId) {
        this.index = index;
        this.uri = uri;
        this.albumId = albumId;
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

    public int getAlbumId() {
        return albumId;
    }

    public String getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static ArrayList<Image> getImages(DBHelper helper, int albumId) {
        int tId, tIndex, tAlbumId;
        String tUri, tDescription;
        ArrayList<Image> images = new ArrayList<>();
        SQLiteDatabase database = helper.getReadableDatabase();

        Cursor cursor = database.query(DBHelper.IMAGE_TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID),
                    indIndex = cursor.getColumnIndex(DBHelper.KEY_IMAGE_INDEX),
                    albumIdIndex = cursor.getColumnIndex(DBHelper.KEY_ALBUM_ID),
                    uriIndex = cursor.getColumnIndex(DBHelper.KEY_URI),
                    descriptionIndex = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION);

            do {
                tId = cursor.getInt(idIndex);
                tIndex = cursor.getInt(indIndex);
                tAlbumId = cursor.getInt(albumIdIndex);
                tUri = cursor.getString(uriIndex);
                tDescription = cursor.getString(descriptionIndex);
                Image image = new Image(tIndex, tUri, tAlbumId);
                image.setId(tId);
                image.setDescription(tDescription);

                if (Utils.checkFile(tUri)) {
                    if (tAlbumId == albumId)
                        images.add(image);
                } else
                    deleteFromDB(helper, image);
            } while (cursor.moveToNext());

            Utils.sortMedia(images);
            cursor.close();
        }

        return images;
    }

    public static Image getLastImage(DBHelper helper) {
        int tId, tIndex, tAlbumId;
        String tUri, tDescription;
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.IMAGE_TABLE_NAME, null, null, null, null, null, null);

        cursor.moveToLast();

        tId = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID));
        tIndex = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IMAGE_INDEX));
        tAlbumId = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ALBUM_ID));
        tUri = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_URI));
        tDescription = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION));

        cursor.close();

        Image image = new Image(tIndex, tUri, tAlbumId);
        image.setId(tId);
        image.setDescription(tDescription);

        return image;
    }

    public static void insertOrUpdateDB(DBHelper helper, Image image) {
        ContentValues cv = new ContentValues();
        SQLiteDatabase database = helper.getReadableDatabase();

        cv.put(DBHelper.KEY_IMAGE_INDEX, image.getIndex());
        cv.put(DBHelper.KEY_URI, image.getUri());
        cv.put(DBHelper.KEY_DESCRIPTION, image.getDescription());
        cv.put(DBHelper.KEY_ALBUM_ID, image.getAlbumId());

        int count = database.update(DBHelper.IMAGE_TABLE_NAME, cv, DBHelper.KEY_ID + "=" + image.getId(), null);

        if (count == 0)
            database.insertWithOnConflict(DBHelper.IMAGE_TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void deleteFromDB(DBHelper helper, Image image) {
        SQLiteDatabase database = helper.getReadableDatabase();
        database.delete(DBHelper.IMAGE_TABLE_NAME, DBHelper.KEY_ID + "=" + image.getId(), null);
    }
}
