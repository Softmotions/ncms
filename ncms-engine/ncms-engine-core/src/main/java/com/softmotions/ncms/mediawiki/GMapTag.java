package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

/**
 * Google map tag.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class GMapTag extends HTMLTag implements INoBodyParsingTag {

    private static final Logger log = LoggerFactory.getLogger(GMapTag.class);

    public static final Pattern GMAP_FRAME_PATTERN =
            Pattern.compile("<iframe.* src=\"http(s)?://(www\\.)?(maps\\.)?google\\.com/maps/.*>.*</iframe>");

    public GMapTag() {
        super("div");
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        String body = this.getBodyString();
        /*
       <iframe width="425" height="550" frameborder="0" scrolling="no" marginheight="0" marginwidth="0"
              src="http://maps.google.com/maps/ms?msa=0&amp;msid=201791464133870448049.0004a6bfb7edf6bf3d0db&amp;ie=UTF8&amp;ll=54.845311,83.092904&amp;spn=0.013591,0.027423&amp;z=15&amp;output=embed"></iframe><br /><small>View <a href="http://maps.google.com/maps/ms?msa=0&amp;msid=201791464133870448049.0004a6bfb7edf6bf3d0db&amp;ie=UTF8&amp;ll=54.845311,83.092904&amp;spn=0.013591,0.027423&amp;z=15&amp;source=embed" style="color:#0000FF;text-align:left">NSU </a> in a larger map</small>
        */
        if (body == null || !GMAP_FRAME_PATTERN.matcher(body.trim()).matches()) {
            log.warn("Invalid google map iframe tag body: {}", body);
            return;
        }
        buf.append(body.trim());
    }
}
