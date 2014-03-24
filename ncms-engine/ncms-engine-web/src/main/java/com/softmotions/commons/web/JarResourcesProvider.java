package com.softmotions.commons.web;

import java.net.URL;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface JarResourcesProvider {

    interface ContentDescriptor {

        URL getUrl();

        String getMimeType();

        String getRequestedPath();
    }

    public ContentDescriptor getContentDescriptor(String path);

}
