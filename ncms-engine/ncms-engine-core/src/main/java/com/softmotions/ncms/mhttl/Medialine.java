package com.softmotions.ncms.mhttl;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class Medialine {

    private final Image image;

    private final Image thumbnail;

    public Image getImage() {
        return image;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public Medialine(Image image, Image thumbnail) {
        this.image = image;
        this.thumbnail = thumbnail;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Medialine{");
        sb.append("image=").append(image);
        sb.append(", thumbnail=").append(thumbnail);
        sb.append('}');
        return sb.toString();
    }
}
