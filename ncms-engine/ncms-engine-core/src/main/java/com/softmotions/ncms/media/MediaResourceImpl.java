package com.softmotions.ncms.media;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.io.InputStreamWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class MediaResourceImpl implements MediaResource, Serializable {

    private final MediaRS rs;

    private final long id;

    private final String path;

    private final String contentType;

    private final long lastModified;

    private final String encoding;

    private final long length;

    private final Locale locale;

    private final KVOptions meta;

    private final String description;


    MediaResourceImpl(MediaRS rs,
                      long id,
                      String path,
                      String contentType,
                      long lastModified,
                      long length,
                      Locale locale,
                      String description,
                      KVOptions meta) {
        this.rs = rs;
        this.id = id;
        this.path = path;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.length = length;
        this.locale = locale;
        this.meta = meta;
        this.description = description;
        if (contentType != null) {
            MediaType mt = MediaType.parse(contentType);
            this.encoding = mt.getParameters().get("charset");
        } else {
            this.encoding = null;
        }

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getEncoding() {
        return encoding;
    }

    public long getLength() {
        return length;
    }

    public String getSource() throws IOException {
        StringWriter sw = new StringWriter();
        try (Reader r = openReader()) {
            IOUtils.copyLarge(r, sw);
        }
        return sw.toString();
    }

    public Reader openReader() throws IOException {
        if (!CTypeUtils.isTextualContentType(getContentType())) {
            throw new IOException("Resource: " + getName() +
                                  " of type: " + getContentType() +
                                  " does not contains text data");
        }

        String spath = path;
        if (spath.charAt(0) == '/') {
            spath = spath.substring(1);
        }
        Reader reader;
        Closeable lock = rs.acquireReadResourceLock(path);
        try {
            InputStream is = new InputStreamSession(lock, new FileInputStream(new File(rs.getBasedir(), spath)));
            reader = (getEncoding() != null) ? new InputStreamReader(is, getEncoding()) : new InputStreamReader(is);
        } catch (Throwable t) {
            lock.close();
            throw new IOException(t);
        }
        return reader;
    }

    public Locale getLocale() {
        return locale;
    }

    public InputStream openStream() throws IOException {
        String spath = path;
        if (spath.charAt(0) == '/') {
            spath = spath.substring(1);
        }
        InputStream is;
        Closeable lock = rs.acquireReadResourceLock(path);
        try {
            is = new InputStreamSession(lock, new FileInputStream(new File(rs.getBasedir(), spath)));
        } catch (Throwable t) {
            lock.close();
            throw new IOException(t);
        }
        return is;
    }


    public long writeTo(Writer out) throws IOException {
        try (final Reader r = openReader()) {
            return IOUtils.copyLarge(r, out);
        }
    }

    public long writeTo(OutputStream out) throws IOException {
        try (final InputStream is = openStream()) {
            return IOUtils.copyLarge(is, out);
        }
    }

    public int getImageWidth() {
        return (meta != null) ? meta.getInt("width", -1) : -1;
    }

    public int getImageHeight() {
        return (meta != null) ? meta.getInt("height", -1) : -1;
    }

    public String getDescription() {
        return description;
    }

    private static class InputStreamSession extends InputStreamWrapper {

        private final Closeable lock;

        private InputStreamSession(Closeable lock, InputStream is) {
            super(is);
            this.lock = lock;
        }

        public void close() throws IOException {
            try {
                super.close();
            } finally {
                lock.close();
            }
        }
    }
}
