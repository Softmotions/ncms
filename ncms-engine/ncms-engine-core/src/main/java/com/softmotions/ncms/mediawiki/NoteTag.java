package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.Map;

import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

/**
 * Simple note tag.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class NoteTag extends HTMLTag implements INoBodyParsingTag {

    public NoteTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return "style".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        Map<String, String> attrs = getAttributes();
        String body = this.getBodyString();
        if ("warning".equals(attrs.get("style"))) {
            buf.append("<div class=\"note-warn\">");
        } else {
            buf.append("<div class=\"note\">");
        }
        buf.append(Encoder.encodeHtml(body));
        buf.append("</div>");

    }
}
