package com.softmotions.ncms.mediawiki;

import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

import java.io.IOException;
import java.util.Map;

/**
 * Youtube embedded player tag.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class YoutubeTag extends HTMLTag implements INoBodyParsingTag {

    public YoutubeTag() {
        super("div");
    }

    public boolean isAllowedAttribute(String attName) {
        return "width".equalsIgnoreCase(attName) ||
               "height".equalsIgnoreCase(attName) ||
               "videoId".equalsIgnoreCase(attName);
    }

    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        Map<String, String> attrs = this.getAttributes();
        String videoId = Utils.escapeXml(attrs.get("videoid"), true, true, true);
        if (videoId == null || "".equals(videoId.trim())) {
            return;
        }
        if (attrs.get("width") == null) {
            attrs.put("width", "640");
        }
        if (attrs.get("height") == null) {
            attrs.put("height", "360");
        }
        String width = Utils.escapeXml(attrs.get("width"), true, true, true);
        String height = Utils.escapeXml(attrs.get("height"), true, true, true);

        StringBuilder ibuff = new StringBuilder();
        String vRef = String.format("http://www.youtube.com/v/%s?feature=player_embedded", videoId);
        ibuff.append(String.format("<object style=\"%s\">\n", String.format("width:%s;height:%s;", width, height)));
        ibuff.append(String.format("<param name=\"movie\" value=\"%s\"/>\n", vRef));
        ibuff.append("<param name=\"allowFullScreen\" value=\"true\"/>\n");
        ibuff.append(String.format("<embed src=\"%s\" width=\"%s\" height=\"%s\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\"/>",
                                   vRef, width, height));
        ibuff.append("</object>");

        buf.append(ibuff);
    }
}
