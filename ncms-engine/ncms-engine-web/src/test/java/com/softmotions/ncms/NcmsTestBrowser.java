package com.softmotions.ncms;

import ninja.utils.NinjaTestBrowser;

import org.apache.http.client.methods.HttpGet;

import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsTestBrowser extends NinjaTestBrowser {


    public HttpTestResponse makeGET(String url) {
        return makeGET(url, null);
    }

    public HttpTestResponse makeGET(String url, Map<String, String> headers) {
        HttpTestResponse response;
        try {
            HttpGet getRequest = new HttpGet(url);
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    getRequest.addHeader(header.getKey(), header.getValue());
                }
            }
            response = new HttpTestResponse(getHttpClient().execute(getRequest));
            getRequest.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}
