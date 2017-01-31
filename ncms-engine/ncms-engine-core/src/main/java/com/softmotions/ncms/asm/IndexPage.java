package com.softmotions.ncms.asm;

import javax.annotation.Nullable;

/**
 * Index page.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface IndexPage extends CachedPage {

    @Nullable
    String getRobotsConfig();
}
