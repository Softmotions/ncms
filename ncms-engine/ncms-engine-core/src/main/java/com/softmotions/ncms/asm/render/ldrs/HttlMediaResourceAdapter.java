package com.softmotions.ncms.asm.render.ldrs;

import httl.Resource;
import httl.util.IOUtils;
import com.softmotions.ncms.asm.render.AsmResourceNotFoundException;
import com.softmotions.ncms.media.MediaResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public final class HttlMediaResourceAdapter implements MediaResource {

    private final Resource res;

    public HttlMediaResourceAdapter(Resource res) {
        this.res = res;
    }

    public String getName() {
        return res.getName();
    }

    public String getEncoding() {
        return res.getEncoding();
    }

    public Locale getLocale() {
        return res.getLocale();
    }

    public long getLastModified() {
        return res.getLastModified();
    }

    public long getLength() {
        return res.getLength();
    }

    public String getContentType() {
        return "text/plain";
    }

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

    public Reader openReader() throws IOException {
        return res.openReader();
    }

    public InputStream openStream() throws IOException {
        return res.openStream();
    }

    public long writeTo(Writer out) throws IOException {
        try (final Reader r = openReader()) {
            return org.apache.commons.io.IOUtils.copyLarge(r, out);
        }
    }

    public long writeTo(OutputStream out) throws IOException {
        try (final InputStream is = openStream()) {
            return org.apache.commons.io.IOUtils.copyLarge(is, out);
        }
    }
}
