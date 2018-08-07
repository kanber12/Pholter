package by.kanber.pholter.model;

import java.util.ArrayList;

public class GalleryFolder {
    private String name;
    private ArrayList<GalleryImage> images;

    public GalleryFolder(String name) {
        this.name = name;
        images = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<GalleryImage> getImages() {
        return images;
    }

    public void addImage(GalleryImage image) {
        images.add(image);
    }
}
