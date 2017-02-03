package com.softmotions.ncms.asm;

import javax.annotation.Nullable;

/**
 * Index page
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface IndexPage extends CachedPage {

    String ROBOTS_TXT = "robots.txt";

    String FAVICON_ICO = "favicon.ico";

    /**
     * robots.txt content
     *
     * @return
     */
    @Nullable
    String getRobotsConfig();

    /**
     * favicon.ico as base64 string
     */
    @Nullable
    String getFaviconBase64();
}
