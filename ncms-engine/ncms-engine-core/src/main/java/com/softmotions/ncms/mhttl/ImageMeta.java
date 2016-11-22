package com.softmotions.ncms.mhttl;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */

public class ImageMeta implements Serializable {

    public static final long serialVersionUID = 2876298736L;

    protected Long id;
    protected boolean resize;
    protected boolean cover;
    protected Integer optionsWidth;
    protected Integer optionsHeight;
    private boolean restrict;
    private boolean skipSmall;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isRestrict() {
        return restrict;
    }

    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }

    public boolean isResize() {
        return resize;
    }

    public void setResize(boolean resize) {
        this.resize = resize;
    }

    public boolean isSkipSmall() {
        return skipSmall;
    }

    public void setSkipSmall(boolean skipSmall) {
        this.skipSmall = skipSmall;
    }

    public Integer getOptionsWidth() {
        return optionsWidth;
    }

    public void setOptionsWidth(Integer optionsWidth) {
        this.optionsWidth = optionsWidth;
        if (optionsWidth != null && optionsWidth < 1) {
            this.optionsWidth = null;
        }
    }

    public Integer getOptionsHeight() {
        return optionsHeight;
    }

    public void setOptionsHeight(Integer optionsHeight) {
        this.optionsHeight = optionsHeight;
        if (optionsHeight != null && optionsHeight < 1) {
            this.optionsHeight = null;
        }
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("resize", resize)
                          .add("cover", cover)
                          .add("optionsWidth", optionsWidth)
                          .add("optionsHeight", optionsHeight)
                          .add("restrict", restrict)
                          .add("skipSmall", skipSmall)
                          .toString();
    }
}
