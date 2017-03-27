package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

/**
 * Youtube embedded player tag.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class YoutubeTag extends HTMLTag implements INoBodyParsingTag {

    private static final Pattern URL_PATTERN =
            Pattern.compile("(http|https)://(www.)?youtube.com/watch\\?v=(\\w{3,15})[\\w#!:\\.\\+=&%@!\\-/]*");

    private static final Pattern CODE_PATTERN =
            Pattern.compile("^(\\w|\\-){3,15}$");


    public YoutubeTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return "width".equalsIgnoreCase(attName) ||
               "height".equalsIgnoreCase(attName) ||
               "videoId".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        Map<String, String> attrs = this.getAttributes();
        String videoId = attrs.get("videoid");
        if (StringUtils.isBlank(videoId)) {
            return;
        }
        videoId = videoId.trim();
        Matcher matcher = URL_PATTERN.matcher(videoId);
        if (matcher.matches()) {
            videoId = matcher.group(3);
        } else {
            matcher = CODE_PATTERN.matcher(videoId);
            if (!matcher.matches()) {
                return;
            }
        }
        attrs.putIfAbsent("width", "640");
        attrs.putIfAbsent("height", "360");
        videoId = Utils.escapeXml(videoId, true, true, true);
        String width = Utils.escapeXml(attrs.get("width"), true, true, true);
        String height = Utils.escapeXml(attrs.get("height"), true, true, true);

        StringBuilder ibuff = new StringBuilder();
        String vRef = String.format("//www.youtube.com/v/%s?feature=player_embedded", videoId);
        ibuff.append(String.format("<object style=\"%s\">\n", String.format("width:%s;height:%s;", width, height)));
        ibuff.append(String.format("<param name=\"movie\" value=\"%s\"/>\n", vRef));
        ibuff.append("<param name=\"allowFullScreen\" value=\"true\"/>\n");
        ibuff.append(String.format("<embed src=\"%s\" width=\"%s\" height=\"%s\" type=\"application/x-shockwave-flash\" allowfullscreen=\"true\"/>",
                                   vRef, width, height));
        ibuff.append("</object>");

        buf.append(ibuff);
    }
}
