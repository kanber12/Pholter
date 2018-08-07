package by.kanber.pholter.models;

import java.util.ArrayList;

import by.kanber.pholter.models.GalleryImage;

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
