package com.softmotions.ncms.mediawiki;

import com.google.inject.Singleton;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

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

    public void renderHTML(ITextConverter textConverter, Appendable buffer, IWikiModel wikiModel) throws IOException {
        Map<String, String> tagAttributes = getAttributes();

        String embedCodeAttribute = StringUtils.trimToEmpty(tagAttributes.get("code"));

        if (!StringUtils.isNumeric(embedCodeAttribute)) {
            return;
        }

        int embedCode = Integer.parseInt(embedCodeAttribute);

        String widthAttribute = StringUtils.trimToEmpty(tagAttributes.getOrDefault("width", "640"));

        if (!StringUtils.isNumeric(widthAttribute)) {
            return;
        }

        int width = Integer.parseInt(widthAttribute);

        String heightAttribute = StringUtils.trimToEmpty(tagAttributes.getOrDefault("height", "320"));

        if (!StringUtils.isNumeric(heightAttribute)) {
            return;
        }

        int height = Integer.parseInt(heightAttribute);

        String html = String.format("<iframe src=\"//www.slideshare.net/slideshow/embed_code/%d\" width=\"%d\" height=\"%d\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"no\" style=\"border:1px solid #CCC; border-width:1px; margin-bottom:5px; max-width: 100%%;\" allowfullscreen> </iframe>",
            /* embed code: */ embedCode,
            /* width: */ width,
            /* height: */ height
        );

        buffer.append(html);
    }
 }