package com.softmotions.ncms.mediawiki;

import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Media-wiki services.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("mw")
public class MediaWikiRS {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiRS.class);

    // 100px-/121/P4033297.JPG
    // /123/bg-interview.png
    private static final Pattern RES_REGEXP
            = Pattern.compile("((\\d+)px\\-)?/(\\d+)/.*");

    // Image:100px-/121/P4033297.JPG
    // File:/121/P4033297.JPG
    private static final Pattern LINK_REGEXP
            = Pattern.compile("(Image|File):((\\d+)px\\-)?/(\\d+)/.*",
                              CASE_INSENSITIVE);

    private final MediaRepository repository;

    private final NcmsMessages messages;

    private final NcmsConfiguration cfg;

    @Inject
    public MediaWikiRS(MediaRepository repository,
                       NcmsMessages messages,
                       NcmsConfiguration cfg) {
        this.repository = repository;
        this.messages = messages;
        this.cfg = cfg;
    }

    @GET
    @Path("res/{spec:.*}")
    public Response res(@PathParam("spec") String spec,
                        @Context HttpServletRequest req) throws Exception {
        Matcher matcher = RES_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            //todo fallback for old site format
            throw new BadRequestException();
        }
        Integer width = null;
        String widthStr = matcher.group(2);
        Long id = Long.parseLong(matcher.group(3));
        if (widthStr != null) {
            width = Integer.parseInt(widthStr);
        } else {
            int maxWidth = cfg.impl().getInt("mediawiki.max-inline-image-width-px", 0);
            if (maxWidth > 0) {
                MediaResource mres = repository.findMediaResource(id, messages.getLocale(req));
                if (mres == null) {
                    throw new NotFoundException();
                }
                if (CTypeUtils.isImageContentType(mres.getContentType())) {
                    if (mres.getImageWidth() > maxWidth) { //restrict maximal image width
                        repository.ensureResizedImage(id, maxWidth, null, true);
                        width = maxWidth;
                    }
                }
            }
        }
        return repository.get(id, req, width, null, true);
    }

    @GET
    @Path("link/{spec:.*}")
    public Response link(@PathParam("spec") String spec,
                         @Context HttpServletRequest req) throws Exception {
        Matcher matcher = LINK_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            //todo fallback for old site format
            throw new BadRequestException();
        }
        Long id = Long.parseLong(matcher.group(4));
        return repository.get(id, req, null, null, true);
    }

}
