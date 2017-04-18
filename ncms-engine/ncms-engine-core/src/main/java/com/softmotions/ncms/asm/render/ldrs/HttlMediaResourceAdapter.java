package com.softmotions.ncms.asm.render.ldrs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import httl.Resource;
import httl.util.IOUtils;

import com.softmotions.ncms.asm.render.AsmResourceNotFoundException;
import com.softmotions.ncms.media.MediaResource;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class HttlMediaResourceAdapter implements MediaResource {

    private final Resource res;

    public HttlMediaResourceAdapter(Resource res) {
        this.res = res;
    }

    @Nullable
    @Override
    public Long getId() {
        return null;
    }

    @Override
    public String getName() {
        return res.getName();
    }

    @Override
    public String getEncoding() {
        return res.getEncoding();
    }

    @Override
    public Locale getLocale() {
        return res.getLocale();
    }

    @Override
    public long getLastModified() {
        return res.getLastModified();
    }

    @Override
    public long getLength() {
        return res.getLength();
    }

    @Override
    public Path getFileSystemPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Nonnull
    public String getOwner() {
        return "system";
    }

    @Override
    public String getSource() throws IOException {
        try {
            return IOUtils.readToString(openReader());
        } catch (FileNotFoundException e) {
            throw new AsmResourceNotFoundException(getName(), e);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Reader openReader() throws IOException {
        return res.openReader();
    }

    @Override
    public InputStream openStream() throws IOException {
        return res.openStream();
    }

    @Override
    public long writeTo(Writer out) throws IOException {
        try (final Reader r = openReader()) {
            return org.apache.commons.io.IOUtils.copyLarge(r, out);
        }
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        try (final InputStream is = openStream()) {
            return org.apache.commons.io.IOUtils.copyLarge(is, out);
        }
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getImageWidth() {
        return -1;
    }

    @Override
    public int getImageHeight() {
        return -1;
    }
}
