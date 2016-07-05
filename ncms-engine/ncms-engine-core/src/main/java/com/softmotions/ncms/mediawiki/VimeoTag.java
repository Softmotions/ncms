package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

/**
 * Vimeo embedded player tag.
 *
 * @author Mozhina Alina (mohinalina@gmail.com)
 */
@Singleton
public class VimeoTag extends HTMLTag implements INoBodyParsingTag {

    public VimeoTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return "code".equalsIgnoreCase(attName) ||
               "width".equalsIgnoreCase(attName) ||
               "height".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        Map<String, String> attrs = this.getAttributes();

        String code = StringUtils.trimToEmpty(attrs.get("code"));

        if (StringUtils.isEmpty(code)) {
            return;
        }

        String width = StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(attrs.get("width")), "640");
        String height = StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(attrs.get("height")), "360");

        code = Utils.escapeXml(code, true, true, true);
        width = Utils.escapeXml(width, true, true, true);
        height = Utils.escapeXml(height, true, true, true);

        String html = String.format("<iframe src=\"//player.vimeo.com/video/%s\" width=\"%s\" height=\"%s\" frameborder=\"0\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>",
                                    /* embed code : */ code,
                                    /* width : */ width,
                                    /* height : */ height
        );

        buf.append(html);
    }
}
