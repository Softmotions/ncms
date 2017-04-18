package com.softmotions.ncms.media;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;

import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.io.InputStreamWrapper;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class MediaResourceImpl implements MediaResource, Serializable {

    private final MediaRS rs;

    private final long id;

    private final String path;

    private final String owner;

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
                      String owner,
                      String contentType,
                      long lastModified,
                      long length,
                      Locale locale,
                      String description,
                      KVOptions meta) {
        this.rs = rs;
        this.id = id;
        this.path = path;
        this.owner = owner;
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

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return path;
    }

    @Override
    @Nonnull
    public String getOwner() {
        return owner;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public String getSource() throws IOException {
        StringWriter sw = new StringWriter();
        try (Reader r = openReader()) {
            IOUtils.copyLarge(r, sw);
        }
        return sw.toString();
    }

    @Override
    public Path getFileSystemPath() {
        String spath = path;
        if (spath.charAt(0) == '/') {
            spath = spath.substring(1);
        }
        return rs.getBaseDir().toPath().resolve(spath);
    }

    @Override
    public Reader openReader() throws IOException {
        if (getLength() == 0) {
            return new StringReader("");
        }
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
            InputStream is = new InputStreamSession(lock, Files.newInputStream(rs.getBaseDir().toPath().resolve(spath)));
            reader = (getEncoding() != null) ? new InputStreamReader(is, getEncoding()) : new InputStreamReader(is);
        } catch (Throwable t) {
            lock.close();
            throw new IOException(t);
        }
        return reader;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public InputStream openStream() throws IOException {
        String spath = path;
        if (spath.charAt(0) == '/') {
            spath = spath.substring(1);
        }
        InputStream is;
        Closeable lock = rs.acquireReadResourceLock(path);
        try {
            is = new InputStreamSession(lock, Files.newInputStream(rs.getBaseDir().toPath().resolve(spath)));
        } catch (Throwable t) {
            lock.close();
            throw new IOException(t);
        }
        return is;
    }


    @Override
    public long writeTo(Writer out) throws IOException {
        try (final Reader r = openReader()) {
            return IOUtils.copyLarge(r, out);
        }
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        try (final InputStream is = openStream()) {
            return IOUtils.copyLarge(is, out);
        }
    }

    @Override
    public int getImageWidth() {
        return (meta != null) ? meta.getInt("width", -1) : -1;
    }

    @Override
    public int getImageHeight() {
        return (meta != null) ? meta.getInt("height", -1) : -1;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaResourceImpl that = (MediaResourceImpl) o;
        return id == that.id;
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    private static class InputStreamSession extends InputStreamWrapper {

        private final Closeable lock;

        private InputStreamSession(Closeable lock, InputStream is) {
            super(is);
            this.lock = lock;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                lock.close();
            }
        }
    }
}
