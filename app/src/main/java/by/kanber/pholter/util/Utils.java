package by.kanber.pholter.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import by.kanber.pholter.models.GalleryFolder;
import by.kanber.pholter.MainActivity;
import by.kanber.pholter.interfaces.PholterMedia;
import by.kanber.pholter.models.GalleryImage;

public class Utils {
    public static boolean isEmpty(String text) {
        return text.equals("") || text.trim().equals("") || text.split("\n").length == 0;
    }

    public static void changeStatusBarColor(MainActivity activity, int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(color));
    }

    public static int[] getImageSize(Uri uri){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        return new int[] {imageWidth, imageHeight};
    }

    public static int getDisplayWidth(Context context) {
        Display display = ((MainActivity) context).getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        return point.x;
    }

    public static boolean checkFile(String uri) {
        File file = new File(uri);

        return file.exists();
    }

    public static void sortMedia(ArrayList<? extends PholterMedia> images) {
        Comparator<PholterMedia> comparator = new Comparator<PholterMedia>() {
            @Override
            public int compare(PholterMedia media1, PholterMedia media2) {
                return Integer.compare(media1.getIndex(), media2.getIndex());
            }
        };

        Collections.sort(images, comparator);
    }

    public static ArrayList<GalleryFolder> getGallery(Context context) {
        ArrayList<GalleryFolder> folders = new ArrayList<>();
        SortedSet<String> foldersSet = new TreeSet<>();
        ArrayList<GalleryImage> images = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int indexData = cursor.getColumnIndex(MediaStore.MediaColumns.DATA),
                        indexFolderName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                do {
                    String imageUri = cursor.getString(indexData),
                            folderName = cursor.getString(indexFolderName);

                    foldersSet.add(folderName);
                    images.add(new GalleryImage(folderName, imageUri));
                } while (cursor.moveToNext());
            }

            for (String s : foldersSet)
                folders.add(new GalleryFolder(s));

            for (GalleryFolder folder : folders)
                for (GalleryImage image : images)
                    if (image.getFolderName().equals(folder.getName()))
                        folder.addImage(image);

            cursor.close();
        }

        return folders;
    }
}
