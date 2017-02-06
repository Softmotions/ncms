package com.softmotions.ncms.asm;

import javax.annotation.Nullable;

/**
 * Index page
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface IndexPage extends CachedPage {

    String PAGE_404 = "page_404";

    String PAGE_500 = "page_500";

    String ROBOTS_TXT = "robots.txt";

    String FAVICON_ICO = "favicon.ico";

    /**
     * guid of custom 404 page
     */
    @Nullable
    String get404page();

    /**
     * guid of custom 500 page
     */
    @Nullable
    String get500page();

    /**
     * robots.txt content
     */
    @Nullable
    String getRobotsConfig();

    /**
     * favicon.ico as base64 string
     */
    @Nullable
    String getFaviconBase64();
}
