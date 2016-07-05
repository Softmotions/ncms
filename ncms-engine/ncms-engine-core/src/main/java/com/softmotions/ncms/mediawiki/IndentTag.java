package com.softmotions.ncms.mediawiki;

import java.io.IOException;

import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;

public class IndentTag extends HTMLTag implements INoBodyParsingTag {

    public IndentTag() {
        super("ind");
    }

    @Override
    public void renderHTML(ITextConverter textConverter, Appendable buf, IWikiModel wikiModel) throws IOException {
        buf.append("<span style=\"margin-left: 1.5em\"></span>");
    }
}
