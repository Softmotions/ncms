package com.softmotions.ncms;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Http client response helper used in test cases.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class HttpTestResponse {

    public final int statusCode;

    public final ByteArrayOutputStream rawData;

    public final ContentType contentType;

    public final String charset;

    public final HttpResponse resp;

    public HttpTestResponse(HttpResponse resp) {
        this.resp = resp;
        this.statusCode = resp.getStatusLine().getStatusCode();
        this.rawData = new ByteArrayOutputStream();
        try {
            resp.getEntity().writeTo(rawData);
            Header h = resp.getFirstHeader("Content-Type");
            if (h != null) {
                contentType = ContentType.parse(h.getValue());
                charset = contentType.getCharset() != null ? contentType.getCharset().name() : null;
            } else {
                contentType = null;
                charset = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        try {
            return (charset != null) ? rawData.toString(charset) : rawData.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
