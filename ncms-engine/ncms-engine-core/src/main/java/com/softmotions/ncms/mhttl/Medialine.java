package com.softmotions.ncms.mhttl;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Medialine {

    private final Image image;

    private final Image thumbnail;

    private final String description;

    public Image getImage() {
        return image;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public Medialine(Image image, Image thumbnail, String description) {
        this.image = image;
        this.thumbnail = thumbnail;
        this.description = description;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Medialine{");
        sb.append("image=").append(image);
        sb.append(", thumbnail=").append(thumbnail);
        sb.append(", description=").append(description);
        sb.append('}');
        return sb.toString();
    }
}
