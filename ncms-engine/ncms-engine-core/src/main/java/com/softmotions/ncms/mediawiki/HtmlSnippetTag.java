package com.softmotions.ncms.mediawiki;

import java.io.IOException;

import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

import com.google.inject.Singleton;

/**
 * Allows inserting og arbitrary html content.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class HtmlSnippetTag extends HTMLTag implements INoBodyParsingTag {

    public HtmlSnippetTag() {
        super("div");
    }

    @Override
    public boolean isAllowedAttribute(String attName) {
        return true;
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {
        String body = this.getBodyString();
        buf.append(body.trim());
    }
}
