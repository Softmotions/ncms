package com.softmotions.ncms.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * JS compiler service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
@Path("adm/rs/x/js")
public class JsRS extends MBDAOSupport {

    private static final Pattern HASH_REGEXP = Pattern.compile("^[0-9a-f]{32}(\\.js)?$");

    private final NcmsEnvironment env;

    private final File jsCache;

    @Inject
    public JsRS(SqlSession sess,
                NcmsEnvironment env,
                MediaReader mediaReader) throws IOException {
        super(JsRS.class.getName(), sess);
        this.env = env;
        this.jsCache = new File(mediaReader.getBaseDir(), ".jscache");
        DirUtils.ensureDir(jsCache, true);
    }

    @Path("/script/{hash}")
    public Response script(@PathParam("hash") String hash) {
        Matcher m = HASH_REGEXP.matcher(hash);
        if (!m.matches()) {
            throw new BadRequestException();
        }
        if (m.group(1) == null) {
            hash += ".js";
        }
        File jsFile = new File(jsCache, hash);
        if (!jsFile.exists()) {
            throw new NotFoundException();
        }
        return Response.ok((StreamingOutput) o -> {
            try (InputStream is = new FileInputStream(jsFile)) {
                IOUtils.copyLarge(is, o);
                o.flush();
            }
        }).type("application/javascript;charset=UTF-8").build();
    }


}
