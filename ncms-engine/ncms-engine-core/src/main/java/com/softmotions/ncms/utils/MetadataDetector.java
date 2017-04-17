package com.softmotions.ncms.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.xml.sax.helpers.DefaultHandler;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.KVOptions;

/**
 * Tika metadata detector.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MetadataDetector {

    private MetadataDetector() {
    }

    /**
     * Fetch metadata from stream with already detected metadata
     *
     * @param type Mime type of data
     * @param is   Data imput stream
     * @return
     */
    public static Metadata detect(final MediaType type, InputStream is) {
        AutoDetectParser parser = new AutoDetectParser((input, metadata) -> type);
        Metadata metadata = new Metadata();
        try {
            parser.parse(is, new DefaultHandler(), metadata);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return metadata;
    }


    public static Metadata detect(final MediaType type, File f) {
        try (InputStream is = Files.newInputStream(f.toPath())) {
            return detect(type, is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Convert metadata
     *
     * @param m
     * @return
     */
    public static KVOptions metadata2Options(Metadata m, String... keys) {
        if (keys == null || keys.length == 0) {
            keys = m.names();
        }
        KVOptions opts = new KVOptions();
        for (String k : keys) {
            String[] values = m.getValues(k);
            if (values.length == 0) {
                continue;
            }
            if (values.length == 1) {
                opts.put(k, values[0]);
            } else {
                opts.put(k, ArrayUtils.stringJoin(values, "|"));
            }
        }
        return opts;
    }


}
