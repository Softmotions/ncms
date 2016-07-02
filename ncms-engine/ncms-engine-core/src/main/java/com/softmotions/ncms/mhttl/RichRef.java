package com.softmotions.ncms.mhttl;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class RichRef {

    private Image image;

    private String description;

    private String link;

    private String name;

    private String rawLink;

    private String style;

    private String style2;

    private String style3;


    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRawLink() {
        return rawLink;
    }

    public void setRawLink(String rawLink) {
        this.rawLink = rawLink;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle2() {
        return style2;
    }

    public void setStyle2(String style2) {
        this.style2 = style2;
    }

    public RichRef() {
    }

    public RichRef(String name,
                   String link,
                   String rawLink) {
        this.name = name;
        this.link = link;
        this.rawLink = rawLink;
    }

    public RichRef(String name,
                   String link,
                   String description,
                   Image image) {
        this.name = name;
        this.link = link;
        this.description = description;
        this.image = image;
    }

    public RichRef(String name,
                   String link,
                   String rawLink,
                   String description,
                   Image image,
                   String style,
                   String style2,
                   String style3) {
        this.name = name;
        this.link = link;
        this.rawLink = rawLink;
        this.description = description;
        this.image = image;
        this.style = style;
        this.style2 = style2;
        this.style3 = style3;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("RichRef{");
        sb.append("name='").append(name).append('\'');
        sb.append(", style='").append(style).append('\'');
        sb.append(", style2='").append(style2).append('\'');
        sb.append(", style3='").append(style3).append('\'');
        sb.append(", link='").append(link).append('\'');
        sb.append(", rawLink='").append(rawLink).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", image=").append(image);
        sb.append('}');
        return sb.toString();
    }
}
