package com.softmotions.ncms.mediawiki;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.html.Redirect;

import info.bliki.wiki.filter.Encoder;

import com.google.inject.Inject;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.weboot.i18n.I18n;

/**
 * Media-wiki services.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Path("mw")
public class MediaWikiRS {

    // 100px-/121/P4033297.JPG
    // /123/bg-interview.png
    private static final Pattern RES_REGEXP
            = Pattern.compile("((\\d+)px\\-)?/(\\d+)/.*");

    // Image:100px-/121/P4033297.JPG
    // File:/121/P4033297.JPG
    private static final Pattern LINK_FILE_REGEXP
            = Pattern.compile("(Image|File|Media):((\\d+)px\\-)?/?(\\d+)/?.*");

    private static final Pattern EXT_LINK_REGEXP
            = Pattern.compile("((Http|Https|Ftp|Smb|Sftp|Scp)://)(.*)");

    private static final Pattern CHECK_ENCODED_REGEXP
            = Pattern.compile(".*[^a-zA-Z0-9-_.!~*'()/#%]+.*");

    private final MediaRepository repository;

    private final I18n messages;

    private final NcmsEnvironment env;

    @Inject
    public MediaWikiRS(MediaRepository repository,
                       I18n messages,
                       NcmsEnvironment env) {
        this.repository = repository;
        this.messages = messages;
        this.env = env;
    }

    @GET
    @Path("res/{spec:.*}")
    public Response res(@PathParam("spec") String spec,
                        @Context HttpServletRequest req) throws Exception {
        Matcher matcher = RES_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            //todo fallback for old site format
            throw new BadRequestException("");
        }
        Integer w = null;
        Long id = Long.parseLong(matcher.group(3));
        if (matcher.group(2) != null) {
            w = Integer.parseInt(matcher.group(2));
        } else {
            int maxWidth = env.xcfg().getInt("mediawiki.max-inline-image-width-px", 0);
            if (maxWidth > 0) {
                MediaResource mres = repository.findMediaResource(id, messages.getLocale(req));
                if (mres == null) {
                    throw new NotFoundException("");
                }
                if (CTypeUtils.isImageContentType(mres.getContentType())) {
                    if (mres.getImageWidth() > maxWidth) { //restrict maximal image width
                        repository.ensureResizedImage(id, maxWidth, null, MediaRepository.RESIZE_SKIP_SMALL);
                        w = maxWidth;
                    }
                }
            }
        }
        return repository.get(id, req, w, null, true);
    }

    @GET
    @Path("link/{spec:(Image|File|Media):.*}")
    public Response link(@PathParam("spec") String spec,
                         @Context HttpServletRequest req) throws Exception {
        Matcher m = LINK_FILE_REGEXP.matcher(spec);
        if (!m.matches()) {
            //todo fallback for old site format
            throw new BadRequestException("spec: " + spec);
        }
        Integer w = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : null;
        Long id = Long.parseLong(m.group(4));
        return repository.get(id, req, w, null, true);
    }

    @GET
    @Path("link/{spec:Page:.*}")
    public Redirect pageLink(@PathParam("spec") String spec,
                             @Context HttpServletRequest req) throws Exception {
        String title = spec.substring("Page:".length());
        String link = env.getAppRoot() + "/" + title;
        return new Redirect(link);
    }

    /**
     * Perform external link processing.
     * <p/>
     * <p>According to
     * {@link <a href="http://download.oracle.com/otn-pub/jcp/jaxrs-2_0-fr-eval-spec/jsr339-jaxrs-2.0-final-spec.pdf">
     * JAX-RS specification section 3.7.3</a>}:
     * "4. If the resulting string ends with ‘/’ then remove the final character".
     * To differentiate links the trailing slash is checked manually.</p>
     */
    @GET
    @Path("link/{spec:(Http|Https|Ftp|Smb|Sftp|Scp)://.*}")
    @Produces("text/html;charset=UTF-8")
    public Redirect externalLink(@PathParam("spec") String spec,
                                 @Context HttpServletRequest req,
                                 @Context HttpServletResponse resp) throws Exception {

        if (req.getRequestURI().endsWith("/") && !spec.endsWith("/")) {
            spec += "/";
        }

        Matcher matcher = EXT_LINK_REGEXP.matcher(spec);
        if (!matcher.matches()) {
            throw new BadRequestException();
        }

        URL url;
        try {
            url = new URL(spec);
        } catch (MalformedURLException ignored) {
            throw new BadRequestException();
        }

        Matcher cematcher = CHECK_ENCODED_REGEXP.matcher(url.getPath());
        String path = cematcher.matches() ? Encoder.encodeUrl(url.getPath()) : url.getPath();

        return new Redirect(new URL(url.getProtocol(), IDN.toASCII(url.getHost()), url.getPort(),
                                    path + (!StringUtils.isBlank(url.getQuery()) ? "?" + url.getQuery() : "")).toURI());
    }
}
