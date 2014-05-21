package com.softmotions.ncms.media;

import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.io.InputStreamWrapper;
import com.softmotions.commons.io.ReaderWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
class MediaResourceImpl implements MediaResource {

    private final MediaRS rs;

    private final long id;

    private final String path;

    private final String contentType;

    private final long lastModified;

    private final String encoding;

    private final long length;


    MediaResourceImpl(MediaRS rs,
                      long id,
                      String path,
                      String contentType,
                      long lastModified,
                      long length) {
        this.rs = rs;
        this.id = id;
        this.path = path;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.length = length;
        if (contentType != null) {
            MediaType mt = MediaType.parse(contentType);
            this.encoding = mt.getParameters().get("charset");
        } else {
            this.encoding = null;
        }

    }

    public long getId() {
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
            reader = new ReaderSession(lock, new FileReader(new File(rs.getBasedir(), spath)));
        } catch (Throwable t) {
            lock.close();
            throw new IOException(t);
        }
        return reader;
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

    private static class ReaderSession extends ReaderWrapper {

        @SuppressWarnings("FieldNameHidesFieldInSuperclass")
        private final Closeable lock;

        private ReaderSession(Closeable lock, Reader r) {
            super(r);
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
