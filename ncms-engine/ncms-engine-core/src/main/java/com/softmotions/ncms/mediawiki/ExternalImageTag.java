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
 * Wiki tag for pasting external images.
 *
 * @author Mozhina Alina (mohinalina@gmail.com)
 */
@Singleton
public class ExternalImageTag extends HTMLTag implements INoBodyParsingTag {
    public ExternalImageTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return "src".equalsIgnoreCase(attName) ||
               "href".equalsIgnoreCase(attName) ||
               "width".equalsIgnoreCase(attName) ||
               "height".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter textConverter, Appendable buffer, IWikiModel wikiModel) throws IOException {
        Map<String, String> attrs = getAttributes();

        String source = StringUtils.trimToEmpty(attrs.get("src"));

        if (StringUtils.isEmpty(source)) {
            return;
        }

        String link = StringUtils.trimToEmpty(attrs.get("href"));
        String width = StringUtils.trimToEmpty(attrs.get("width"));
        String height = StringUtils.trimToEmpty(attrs.get("height"));

        source = Utils.escapeXml(source, true, true, true);
        link = Utils.escapeXml(link, true, true, true);
        width = Utils.escapeXml(width, true, true, true);
        height = Utils.escapeXml(height, true, true, true);

        StringBuilder ibuff = new StringBuilder();
        ibuff.append(String.format("<img src=\"%s\"", source));
        if (!StringUtils.isEmpty(width)) {
            ibuff.append(String.format("width=\"%s\"", width));
        }

        if (!StringUtils.isEmpty(height)) {
            ibuff.append(String.format("height=\"%s\"", height));
        }
        ibuff.append(">\n");

        if (!StringUtils.isEmpty(link)) {
            buffer.append(String.format("<a href=\"%s\">\n", link));
            buffer.append(ibuff);
            buffer.append(String.format("</a>"));
        } else {
            buffer.append(ibuff);
        }
    }
}
