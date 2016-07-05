package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import com.google.inject.Singleton;

/**
 * SlideShare embedded presentation tag.
 */
@Singleton
public class SlideSharePresentationTag extends HTMLTag implements INoBodyParsingTag {

    public SlideSharePresentationTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return "code".equalsIgnoreCase(attName) ||
               "width".equalsIgnoreCase(attName) ||
               "height".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter textConverter, Appendable buffer, IWikiModel wikiModel) throws IOException {
        Map<String, String> tagAttributes = getAttributes();

        String code = trimToEmpty(tagAttributes.get("code"));

        if (StringUtils.isEmpty(code)) {
            return;
        }

        String width = defaultIfEmpty(trimToEmpty(tagAttributes.get("width")), "640");
        String height = defaultIfEmpty(trimToEmpty(tagAttributes.get("height")), "320");

        code = Utils.escapeXml(code, true, true, true);
        width = Utils.escapeXml(width, true, true, true);
        height = Utils.escapeXml(height, true, true, true);

        String html = String.format("<iframe src=\"//www.slideshare.net/slideshow/embed_code/%s\" width=\"%s\" height=\"%s\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"no\" style=\"border:1px solid #CCC; border-width:1px; margin-bottom:5px; max-width: 100%%;\" allowfullscreen> </iframe>",
            /* embed code: */ code,
            /* width: */ width,
            /* height: */ height
        );

        buffer.append(html);
    }
}