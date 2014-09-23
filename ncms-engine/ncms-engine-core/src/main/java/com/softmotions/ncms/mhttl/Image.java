package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.asm.render.AsmRendererContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class Image {

    private Long id;
    private boolean restrict;
    private boolean resize;
    private boolean skipSmall;
    private boolean cover;
    private Integer optionsWidth;
    private Integer optionsHeight;
    private final AsmRendererContext ctx;

    public Image(AsmRendererContext ctx) {
        this.ctx = ctx;
    }

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
        if (optionsWidth != null && optionsWidth.intValue() < 1) {
            this.optionsWidth = null;
        }
    }

    public Integer getOptionsHeight() {
        return optionsHeight;
    }

    public void setOptionsHeight(Integer optionsHeight) {
        this.optionsHeight = optionsHeight;
        if (optionsHeight != null && optionsHeight.intValue() < 1) {
            this.optionsHeight = null;
        }
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    public String getLink() {
        String link = ctx.getEnv().getFileLink(id);
        if (resize || cover) {
            if (optionsWidth != null) {
                link += "?w=" + optionsWidth;
            }
            if (optionsHeight != null) {
                link += (((optionsWidth != null) ? "&h=" : "?h=") + optionsHeight);
            }
        }
        return link;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Image{");
        sb.append("id=").append(id);
        sb.append(", optionsWidth=").append(optionsWidth);
        sb.append(", optionsHeight=").append(optionsHeight);
        sb.append(", restrict=").append(restrict);
        sb.append(", cover=").append(cover);
        sb.append(", resize=").append(resize);
        sb.append(", skipSmall=").append(skipSmall);
        sb.append('}');
        return sb.toString();
    }
}
