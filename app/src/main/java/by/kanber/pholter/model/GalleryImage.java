package by.kanber.pholter.model;

public class GalleryImage {
    private boolean isSelected = false;
    private String folderName, uri;

    public GalleryImage(String folderName, String uri) {
        this.folderName = folderName;
        this.uri = uri;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getUri() {
        return uri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
