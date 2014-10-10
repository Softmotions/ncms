package com.softmotions.ncms.mhttl;

import com.softmotions.ncms.asm.render.AsmRendererContext;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class Image extends ImageMeta {

    private final AsmRendererContext ctx;

    public Image(AsmRendererContext ctx) {
        this.ctx = ctx;
    }

    public String getLink() {
        String link = ctx.getMediaRepository().resolveFileLink(id);
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

    public static Image createImage(AsmRendererContext ctx, ImageMeta meta) {
        Image image = new Image(ctx);

        try {
            BeanUtils.copyProperties(image, meta);
        } catch (Exception e) {
            return null;
        }

        return image;
    }
}
