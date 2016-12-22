package com.softmotions.ncms.mediawiki.events;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MediaWikiHTMLRenderEvent {

    private final String markup;

    private final String result;


    public String getMarkup() {
        return markup;
    }

    public String getResult() {
        return result;
    }

    public MediaWikiHTMLRenderEvent(String markup, String result) {
        this.markup = markup;
        this.result = result;
    }
}
