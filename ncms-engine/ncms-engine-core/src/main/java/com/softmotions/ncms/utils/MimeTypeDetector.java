package com.softmotions.ncms.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.txt.UniversalEncodingDetector;

/**
 * Simple tika-based mime type detector.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class MimeTypeDetector {

    private MimeTypeDetector() {
    }

    private static final TikaConfig TIKA_DEFAULT_CFG = TikaConfig.getDefaultConfig();

    public static MediaType detect(BufferedInputStream bis,
                                   String resourceName,
                                   String ctype,
                                   String enc) throws IOException {
        AutoDetectParser parser = new AutoDetectParser(TIKA_DEFAULT_CFG);
        Detector detector = parser.getDetector();
        Metadata md = new Metadata();
        if (resourceName != null) {
            md.add(Metadata.RESOURCE_NAME_KEY, resourceName);
        }
        if (ctype != null) {
            md.add(Metadata.CONTENT_TYPE, ctype);
        }
        if (enc != null) {
            md.add(Metadata.CONTENT_ENCODING, enc);
        }
        return detector.detect(bis, md);
    }

    public static Charset detectCharset(BufferedInputStream bis,
                                        String resourceName,
                                        String ctype,
                                        String enc) throws IOException {

        UniversalEncodingDetector detector = new UniversalEncodingDetector();
        Metadata md = new Metadata();
        if (resourceName != null) {
            md.add(Metadata.RESOURCE_NAME_KEY, resourceName);
        }
        if (ctype != null) {
            md.add(Metadata.CONTENT_TYPE, ctype);
        }
        if (enc != null) {
            md.add(Metadata.CONTENT_ENCODING, enc);
        }
        return detector.detect(bis, md);
    }
}
