package com.softmotions.ncms.mhttl;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class RichRef {

    private final Image image;

    private final String description;

    private final String link;

    private final String name;

    private final String style;

    private final String style2;

    public Image getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }

    public String getStyle2() {
        return style2;
    }

    public RichRef(String name, String link, String description,
                   Image image, String style, String style2) {
        this.name = name;
        this.link = link;
        this.description = description;
        this.image = image;
        this.style = style;
        this.style2 = style2;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("RichRef{");
        sb.append("name='").append(name).append('\'');
        sb.append(", style='").append(style).append('\'');
        sb.append(", style2='").append(style2).append('\'');
        sb.append(", link='").append(link).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", image=").append(image);
        sb.append('}');
        return sb.toString();
    }
}
