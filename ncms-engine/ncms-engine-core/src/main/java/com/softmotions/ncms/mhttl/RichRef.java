package com.softmotions.ncms.mhttl;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;

import com.softmotions.ncms.asm.PageService;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class RichRef implements Serializable {

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

    public String getStyle3() {
        return style3;
    }

    public void setStyle3(String style3) {
        this.style3 = style3;
    }

    @Nullable
    public String toHtmlLink() {
        return toHtmlLink(null);
    }

    @Nullable
    public String toHtmlLink(@Nullable Map<String, ?> amap) {
        if (link == null) {
            return null;
        }
        if (name == null) {
            name = link;
        }
        StringBuilder attrs = null;
        if (amap != null && !amap.isEmpty()) {
            attrs = new StringBuilder();
            for (Map.Entry<String, ?> e : amap.entrySet()) {
                attrs.append(' ').append(e.getKey()).append('=').append(e.getValue());
            }
        }
        return String.format("<a href=\"%s\"%s>%s</a>",
                             link,
                             attrs != null ? attrs : "",
                             StringEscapeUtils.escapeHtml4(name));
    }

    public RichRef() {
    }

    public RichRef(String rawLink, PageService pageService) {
        this.name = null;
        this.rawLink = rawLink;
        link = rawLink;
        int ind = link.indexOf('|');
        if (ind != -1) {
            if (ind < link.length() - 1) {
                name = link.substring(ind + 1).trim();
            }
            link = link.substring(0, ind).trim();
        }
        if (name == null) {
            name = link;
        }
        link = pageService.resolveResourceLink(link);
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
       return link;
    }
}
