package com.softmotions.ncms.media;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.KVOptions;
import com.softmotions.commons.cont.Pair;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.commons.io.scanner.DirectoryScanner;
import com.softmotions.commons.io.scanner.DirectoryScannerFactory;
import com.softmotions.commons.io.scanner.DirectoryScannerVisitor;
import com.softmotions.commons.io.watcher.FSWatcher;
import com.softmotions.commons.io.watcher.FSWatcherCollectEventHandler;
import com.softmotions.commons.io.watcher.FSWatcherCreateEvent;
import com.softmotions.commons.io.watcher.FSWatcherDeleteEvent;
import com.softmotions.commons.io.watcher.FSWatcherEventHandler;
import com.softmotions.commons.io.watcher.FSWatcherEventSupport;
import com.softmotions.commons.io.watcher.FSWatcherModifyEvent;
import com.softmotions.commons.io.watcher.FSWatcherRegisterEvent;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.events.PageDroppedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.fts.FTSUtils;
import com.softmotions.ncms.io.MetadataDetector;
import com.softmotions.ncms.io.MimeTypeDetector;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.ncms.media.events.MediaDeleteEvent;
import com.softmotions.ncms.media.events.MediaMoveEvent;
import com.softmotions.ncms.media.events.MediaUpdateEvent;
import com.softmotions.web.HttpServletRequestAdapter;
import com.softmotions.web.ResponseUtils;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.imgscalr.Scalr;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.sql.Blob;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Media files manager rest service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@javax.ws.rs.Path("/media")
@Produces("application/json")
public class MediaRS extends MBDAOSupport implements MediaRepository, FSWatcherEventHandler {

    private static final Logger log = LoggerFactory.getLogger(MediaRS.class);

    public static final String SIZE_CACHE_FOLDER = ".size_cache";

    private static final int MB = 1048576;

    private static final File[] EMPTY_FILES_ARRAY = new File[0];

    private static final int PUT_NO_KEYS = 1;

    private static final int PUT_SYSTEM = 1 << 1;

    final NcmsConfiguration cfg;

    final File basedir;

    final RWLocksLRUCache locksCache;

    final ObjectMapper mapper;

    final NcmsMessages message;

    final NcmsEventBus ebus;

    final WSUserDatabase userdb;


    @Inject
    public MediaRS(NcmsConfiguration cfg,
                   SqlSession sess,
                   ObjectMapper mapper,
                   NcmsMessages message,
                   NcmsEventBus ebus,
                   WSUserDatabase userdb) throws IOException {
        super(MediaRS.class.getName(), sess);
        this.cfg = cfg;
        XMLConfiguration xcfg = cfg.impl();
        String dir = xcfg.getString("media[@basedir]");
        if (dir == null) {
            throw new RuntimeException("Missing required configuration property: media[@basedir]");
        }
        dir = cfg.substitutePath(dir);
        basedir = new File(dir);
        DirUtils.ensureDir(basedir, true);
        locksCache = new RWLocksLRUCache(xcfg.getInt("media.locks-lrucache-size", 0x7f));
        this.mapper = mapper;
        this.message = message;
        this.ebus = ebus;
        this.userdb = userdb;
        this.ebus.register(this);
    }


    /**
     * Get media-entity path by specified ID
     *
     * @param id Media entry ID
     */
    @GET
    @javax.ws.rs.Path("/path/{id}")
    @Transactional
    public String path(@PathParam("id") Long id) {
        Map<String, ?> row = selectOne("selectEntityPathById", "id", id);
        if (row == null) {
            throw new NotFoundException();
        }
        return String.valueOf(row.get("folder")) + row.get("name");
    }


    /**
     * Save uploaded file.
     * <p/>
     * Example:
     * curl --upload-file ./myfile.txt http://localhost:8080/ncms/rs/media/file/foo/bar/test.txt
     */
    @PUT
    @Consumes("*/*")
    @javax.ws.rs.Path("/file/{folder:.*}/{name}")
    @Transactional(executorType = ExecutorType.SIMPLE)
    public void put(@PathParam("folder") String folder,
                    @PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws Exception {
        _put(folder, name, req, in, 0);
    }

    @PUT
    @Consumes("*/*")
    @javax.ws.rs.Path("/file/{name}")
    @Transactional(executorType = ExecutorType.SIMPLE)
    public void put(@PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws Exception {
        _put("", name, req, in, 0);
    }


    @GET
    @javax.ws.rs.Path("/file/{folder:.*}/{name}")
    @Transactional
    public Response get(@PathParam("folder") String folder,
                        @PathParam("name") String name,
                        @Context HttpServletRequest req,
                        @QueryParam("w") Integer width,
                        @QueryParam("h") Integer height) throws Exception {
        return _get(folder, name, req, width, height, true);
    }

    @GET
    @javax.ws.rs.Path("/file/{name}")
    @Transactional
    public Response get(@PathParam("name") String name,
                        @Context HttpServletRequest req,
                        @QueryParam("w") Integer width,
                        @QueryParam("h") Integer height) throws Exception {
        return _get("", name, req, width, height, true);
    }


    @GET
    @javax.ws.rs.Path("/fileid/{id}")
    @Transactional
    public Response get(@PathParam("id") Long id,
                        @Context HttpServletRequest req,
                        @QueryParam("w") Integer width,
                        @QueryParam("h") Integer height) throws Exception {
        Map<String, ?> row = selectOne("selectEntityPathById", "id", id);
        if (row == null) {
            throw new NotFoundException();
        }
        return _get((String) row.get("folder"), (String) row.get("name"),
                    req, width, height, true);
    }


    @HEAD
    @javax.ws.rs.Path("/fileid/{id}")
    @Transactional
    public Response head(@PathParam("id") Long id,
                         @Context HttpServletRequest req,
                         @QueryParam("w") Integer width,
                         @QueryParam("h") Integer height) throws Exception {
        Map<String, ?> row = selectOne("selectEntityPathById", "id", id);
        if (row == null) {
            throw new NotFoundException();
        }
        return _get((String) row.get("folder"), (String) row.get("name"),
                    req, width, height, false);
    }

    @HEAD
    @javax.ws.rs.Path("/file/{folder:.*}/{name}")
    @Transactional
    public Response head(@PathParam("folder") String folder,
                         @PathParam("name") String name,
                         @Context HttpServletRequest req,
                         @QueryParam("w") Integer width,
                         @QueryParam("h") Integer height) throws Exception {
        return _get(folder, name, req, width, height, false);
    }

    @HEAD
    @javax.ws.rs.Path("/file/{name}")
    @Transactional
    public Response head(@PathParam("name") String name,
                         @Context HttpServletRequest req,
                         @QueryParam("w") Integer width,
                         @QueryParam("h") Integer height) throws Exception {
        return _get("", name, req, width, height, false);
    }

    @GET
    @javax.ws.rs.Path("/thumbnail2/{id}")
    @Transactional
    public Response thumbnail(@PathParam("id") Long id,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(id, null, null, req);
    }


    @GET
    @javax.ws.rs.Path("/thumbnail/{folder:.*}/{name}")
    @Transactional
    public Response thumbnail(@PathParam("folder") String folder,
                              @PathParam("name") String name,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(null, folder, name, req);
    }

    @GET
    @javax.ws.rs.Path("/thumbnail/{name}")
    @Transactional
    public Response thumbnail(@PathParam("name") String name,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(null, "", name, req);
    }


    @GET
    @javax.ws.rs.Path("/files/{folder:.*}")
    @Transactional
    public JsonNode listFiles(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws IOException {
        return _list(folder, FileFileFilter.FILE, req);
    }

    @GET
    @javax.ws.rs.Path("/files")
    @Transactional
    public JsonNode listFiles(@Context HttpServletRequest req) throws IOException {
        return _list("", FileFileFilter.FILE, req);
    }

    @GET
    @javax.ws.rs.Path("/folders/{folder:.*}")
    @Transactional
    public JsonNode listFolders(@PathParam("folder") String folder,
                                @Context HttpServletRequest req) throws IOException {
        return _list(folder, DirectoryFileFilter.INSTANCE, req);
    }

    @GET
    @javax.ws.rs.Path("/folders")
    @Transactional
    public JsonNode listFolders(@Context HttpServletRequest req) throws IOException {
        return _list("", DirectoryFileFilter.INSTANCE, req);
    }


    @GET
    @javax.ws.rs.Path("/all/{folder:.*}")
    @Transactional
    public JsonNode listAll(@PathParam("folder") String folder,
                            @Context HttpServletRequest req) throws IOException {
        return _list(folder, TrueFileFilter.INSTANCE, req);
    }

    @GET
    @javax.ws.rs.Path("/all")
    @Transactional
    public JsonNode listAll(@Context HttpServletRequest req) throws IOException {
        return _list("", TrueFileFilter.INSTANCE, req);
    }

    @GET
    @javax.ws.rs.Path("/select")
    @Transactional
    public Response select(@Context final HttpServletRequest req) {
        return Response.ok(new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                final JsonGenerator gen = new JsonFactory().createGenerator(output);
                try {
                    MBCriteriaQuery cq = createSelectQ(req, false);
                    gen.writeStartArray();
                    //noinspection InnerClassTooDeeplyNested
                    select(cq.getStatement(), new ResultHandler() {
                        public void handleResult(ResultContext context) {
                            Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                            try {
                                gen.writeStartObject();
                                gen.writeNumberField("id", ((Number) row.get("id")).longValue());
                                gen.writeStringField("name", (String) row.get("name"));
                                gen.writeStringField("folder", (String) row.get("folder"));
                                gen.writeStringField("content_type", (String) row.get("content_type"));
                                gen.writeStringField("owner", (String) row.get("owner"));
                                String username = (String) row.get("owner");
                                WSUser user = (username != null) ? userdb.findUser(username) : null;
                                if (user != null) {
                                    gen.writeStringField("owner_fullName", user.getFullName());
                                }
                                if (row.get("content_length") != null) {
                                    gen.writeNumberField("content_length", ((Number) row.get("content_length")).longValue());
                                }
                                gen.writeStringField("description", (String) row.get("description"));
                                gen.writeStringField("tags", ArrayUtils.stringJoin(row.get("tags"), ", "));
                                gen.writeEndObject();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, cq);
                } finally {
                    gen.writeEndArray();
                }
                gen.flush();
            }
        }).type(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE)
                .encoding("UTF-8")
                .build();
    }

    @GET
    @javax.ws.rs.Path("/select/count")
    @Produces("text/plain")
    @Transactional
    public Integer selectCount(@Context HttpServletRequest req) {
        MBCriteriaQuery cq = createSelectQ(req, true);
        return selectOne(cq.getStatement(), cq);
    }

    @PUT
    @javax.ws.rs.Path("/folder/{folder:.*}")
    @Transactional
    public JsonNode newFolder(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws Exception {

        try (final ResourceLock l = new ResourceLock(folder, true)) {
            File f = new File(basedir, folder);
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    throw new IOException("Cannot create dir: " + folder);
                }
            }
            String name = f.getName();
            String dirname = getResourceParentFolder(folder);
            Number id = selectOne("selectEntityIdByPath",
                                  "folder", dirname,
                                  "name", name);
            if (id == null) {
                Map params = new HashMap();
                params.put("folder", dirname);
                params.put("name", name);
                params.put("owner", req.getRemoteUser());
                params.put("status", 1);
                insert("insertEntity", params);

                id = (Number) params.get("id");
                ebus.fireOnSuccessCommit(
                        new MediaUpdateEvent(this, true, id, dirname + name));
            } else {
                throw new NcmsMessageException(message.get("ncms.mmgr.folder.exists", req, folder), true);
            }
            return mapper.createObjectNode()
                    .put("label", name)
                    .put("status", 1)
                    .put("system", isInSystemFolder(dirname + name));
        }
    }

    @PUT
    @javax.ws.rs.Path("/copy-batch/{target:.*}")
    public void copy(@Context HttpServletRequest req,
                     @PathParam("target") String target,
                     ArrayNode files) throws Exception {
        _copy(req, target, files);
    }

    @PUT
    @javax.ws.rs.Path("/copy-batch")
    public void copy(@Context HttpServletRequest req, ArrayNode files) throws Exception {
        _copy(req, "", files);
    }

    private void _copy(HttpServletRequest req, String tfolder, ArrayNode files) throws Exception {
        checkFolder(tfolder);
        tfolder = normalizeFolder(tfolder);
        checkEditAccess(tfolder, req);

        for (int i = 0, l = files.size(); i < l; ++i) {
            String spath = normalizePath(files.get(i).asText());
            checkFolder(spath);
            try (final ResourceLock l1 = new ResourceLock(spath, false)) {
                String sfolder = getResourceParentFolder(spath);
                String sname = getResourceName(spath);
                String tpath = tfolder + sname;
                if (spath.equals(tpath)) {
                    continue;
                }
                File sfile = new File(basedir, spath);
                if (!sfile.exists()) {
                    continue;
                }

                try (final ResourceLock l2 = new ResourceLock(tpath, true)) {
                    File tfile = new File(basedir, tpath);
                    Map<String, Object> row = selectOne("selectResourceAttrsByPath",
                                                        "folder", sfolder,
                                                        "name", sname);
                    if (row == null) {
                        log.error("File to be copied: " + spath + " is missing in DB");
                        continue;
                    }

                    FileUtils.copyFile(sfile, tfile);

                    row.put("folder", tfolder);
                    row.put("owner", req.getRemoteUser());
                    row.remove("id");

                    delete("deleteFile",
                           "folder", tfolder,
                           "name", sname);

                    insert("insertEntity", row);

                    if (row.get("id") != null) {
                        updateFTSKeywords(((Number) row.get("id")).longValue(), req);
                    }

                } catch (IOException e) {
                    log.error("Failed to copy " + spath + " => " + tpath, e);
                    throw e;
                }
            }
        }
    }

    @PUT
    @javax.ws.rs.Path("/move/{path:.*}")
    @Transactional(executorType = ExecutorType.BATCH)
    public void move(@PathParam("path") String path,
                     @Context HttpServletRequest req,
                     String npath) throws Exception {

        path = StringUtils.strip(path, "/");
        npath = StringUtils.strip(npath, "/");
        if (StringUtils.isBlank(npath)) {
            throw new BadRequestException();
        }
        if (npath.equals(path)) {
            return;
        }

        checkFolder(path);
        checkFolder(npath);

        Long id;

        try (final ResourceLock l1 = new ResourceLock(path, true)) {
            try (final ResourceLock l2 = new ResourceLock(npath, true)) {
                File f1 = new File(basedir, path);
                if (!f1.exists()) {
                    throw new NotFoundException(path);
                }

                checkEditAccess(path, req);

                File f2 = new File(basedir, npath);
                if (f2.exists()) {
                    throw new NcmsMessageException(message.get("ncms.mmgr.file.exists", req, npath), true);
                }
                File pf = f2.getParentFile();
                if (pf != null && !pf.exists() && !pf.mkdirs()) {
                    throw new IOException("Cannot create the target directory");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Moving " + f1 + " => " + f2);
                }
                if (f1.isDirectory()) {

                    String p1 = f1.getCanonicalPath();
                    String p2 = f2.getCanonicalPath();
                    if (p2.startsWith(p1 + '/')) {
                        String msg = message.get("ncms.mmgr.folder.cantMoveIntoSubfolder", req, path, npath);
                        throw new NcmsMessageException(msg, true);
                    }
                    String like = '/' + path + "/%";
                    update("fixFolderName",
                           "new_prefix", '/' + npath + '/',
                           "prefix_like_len", like.length(),
                           "prefix_like", like);

                    String nname = getResourceName(npath);
                    String nfolder = getResourceParentFolder(npath);
                    update("fixResourceLocation",
                           "nfolder", nfolder,
                           "nname", nname,
                           "folder", getResourceParentFolder(path),
                           "name", getResourceName(path));

                    FileUtils.moveDirectory(f1, f2);

                    ebus.fireOnSuccessCommit(new MediaMoveEvent(this, null, true, path, npath));

                } else if (f1.isFile()) {

                    String folder = getResourceParentFolder(path);
                    String nname = getResourceName(npath);
                    String nfolder = getResourceParentFolder(npath);
                    update("fixResourceLocation",
                           "nfolder", nfolder,
                           "nname", nname,
                           "folder", folder,
                           "name", getResourceName(path));

                    FileUtils.moveFile(f1, f2);

                    id = selectOne("selectEntityIdByPath",
                                   "name", nname,
                                   "folder", nfolder);

                    if (id != null) {
                        //Handle resize image dir
                        File rdir = getResizedImageDir(folder, id);
                        if (rdir.exists() && !folder.equals(nfolder)) {
                            File nrdir = getResizedImageDir(nfolder, id);
                            File pnrdir = nrdir.getParentFile();
                            if (pnrdir != null && (pnrdir.exists() || pnrdir.mkdirs())) {
                                try {
                                    FileUtils.deleteDirectory(nrdir);
                                    FileUtils.moveDirectory(rdir, pnrdir);
                                } catch (IOException e) {
                                    log.error("Failed to move directory: " +
                                              rdir + " to " + nrdir, e);
                                }
                            }
                        }

                        ebus.fireOnSuccessCommit(new MediaMoveEvent(this, id, false, path, npath));
                        updateFTSKeywords(id, req);
                    }

                } else {
                    throw new IOException("Unsupported file type");
                }
            }
        }
    }

    @DELETE
    @javax.ws.rs.Path("/delete/{path:.*}")
    @Transactional(executorType = ExecutorType.BATCH)
    public void deleteResource(@PathParam("path") String path,
                               @Context HttpServletRequest req) throws Exception {
        path = StringUtils.strip(path, "/");
        checkFolder(path);
        if (log.isDebugEnabled()) {
            log.debug("deleteResource: " + path);
        }

        boolean isdir;
        String name = getResourceName(path);
        String folder = getResourceParentFolder(path);

        try (final ResourceLock l = new ResourceLock(path, true)) {

            File f = new File(basedir, path);
            checkEditAccess(path, req);

            isdir = f.isDirectory();
            if (isdir) {

                deleteDirectoryInternal(path, true);
                delete("deleteFolder",
                       "prefix_like", '/' + path + "/%");
                delete("deleteFile",
                       "folder", folder,
                       "name", name);

            } else {

                Long id = selectOne("selectEntityIdByPath",
                                    "folder", folder,
                                    "name", name);
                boolean exists = f.exists();
                if (f.delete() || !exists) {
                    delete("deleteFile",
                           "folder", folder,
                           "name", name);
                } else {
                    throw new NotFoundException(message.get("ncms.mmgr.file.cannot.delete", req, path));
                }
                if (id != null) {
                    File sdir = getResizedImageDir(folder, id);
                    if (sdir.exists()) {
                        try {
                            FileUtils.deleteDirectory(sdir);
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                }
            }
        }

        ebus.fireOnSuccessCommit(
                new MediaDeleteEvent(this, isdir, path)
        );
    }

    @DELETE
    @javax.ws.rs.Path("/delete-batch")
    public void deleteBatch(@Context HttpServletRequest req,
                            ArrayNode files) throws Exception {
        for (int i = 0, l = files.size(); i < l; ++i) {
            String path = files.get(i).asText();
            deleteResource(path, req);
        }
    }


    @GET
    @javax.ws.rs.Path("/meta/{id}")
    @Transactional
    public ObjectNode getMeta(@PathParam("id") Long id,
                              @Context HttpServletRequest req) throws Exception {
        Map<String, ?> row = selectOne("selectResourceAttrsById", "id", id);
        if (row == null) {
            throw new NotFoundException();
        }
        ObjectNode res = mapper.createObjectNode();
        res.put("id", id);
        res.put("folder", (String) row.get("folder"));
        res.put("name", (String) row.get("name"));
        res.put("meta", (String) row.get("meta"));
        return res;
    }

    /**
     * Update some meta fields of files.
     */
    @POST
    @javax.ws.rs.Path("/meta/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Transactional(executorType = ExecutorType.BATCH)
    public void updateMeta(@PathParam("id") Long id,
                           @Context HttpServletRequest req,
                           MultivaluedMap<String, String> form) throws Exception {

        checkEditAccess(id, req);

        Map<String, Object> qm = new TinyParamMap();
        qm.put("id", id);

        if (form.containsKey("tags")) {
            final Collator coll = Collator.getInstance(message.getLocale(req));
            String tagStr = form.getFirst("tags");
            Set<String> tagSet = new HashSet<>();
            if (tagStr != null) {
                String[] tagArr = tagStr.split(",");
                for (String tag : tagArr) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        tagSet.add(tag);
                    }
                }
            }
            int i = 0;
            Object[] qtags = new Object[tagSet.size()];
            for (final String tag : tagSet) {
                qtags[i++] = tag;
            }
            Arrays.sort(qtags, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    return coll.compare(String.valueOf(o1), String.valueOf(o2));
                }
            });
            qm.put("tags", qtags);
        }
        if (form.containsKey("description")) {
            String desc = form.getFirst("description");
            qm.put("description", StringUtils.isBlank(desc) ? "" : desc);
        }
        if (form.containsKey("owner")) {
            String owner = form.getFirst("owner");
            if (StringUtils.isBlank(owner)) {
                throw new BadRequestException();
            }
            qm.put("owner", owner);
        }
        // if qm.size() < 2: only .put(id) called - update query is empty!
        if (qm.size() > 1) {
            update("updateMeta", qm);
            updateFTSKeywords(id, req);
        }
    }

    @Transactional
    public MediaResource findMediaResource(String path, Locale locale) {
        Map<String, Object> res;
        if (path.startsWith("entity:")) {
            Long id;
            try {
                id = Long.parseLong(path.substring("entity:".length()));
            } catch (NumberFormatException e) {
                log.error("", e);
                return null;
            }
            res = selectOne("selectResourceAttrsById",
                            "id", id);
        } else {
            res = selectOne("selectResourceAttrsByPath",
                            "folder", getResourceParentFolder(path),
                            "name", getResourceName(path));
        }
        if (res == null) {
            return null;
        }
        String folder = (String) res.get("folder");
        String name = (String) res.get("name");
        Date mdate = (Date) res.get("mdate");
        Number length = (Number) res.get("content_length");

        return new MediaResourceImpl(this,
                                     ((Number) res.get("id")).longValue(),
                                     (folder + name),
                                     (String) res.get("content_type"),
                                     (mdate != null ? mdate.getTime() : 0),
                                     (length != null ? length.longValue() : -1L),
                                     locale);
    }

    public File getBasedir() {
        return basedir;
    }

    /**
     * Update media item search tokens
     */
    private void updateFTSKeywords(Long id, HttpServletRequest req) throws Exception {
        Map<String, Object> row = selectOne("selectMeta", "id", id);
        if (row == null) {
            return;
        }
        Locale locale = message.getLocale(req);
        Set<String> keywords = new HashSet<>();
        String name = (String) row.get("name");
        String ctype = (String) row.get("content_type");
        String val;

        if (row.get("description") != null) {
            val = ((String) row.get("description")).toLowerCase();
            Collections.addAll(keywords, FTSUtils.stemWordsLangAware(val, locale, 3));
        }
        if (row.get("tags") != null) {
            StringBuilder tags = new StringBuilder();
            Iterator it = new ArrayIterator(row.get("tags"));
            while (it.hasNext()) {
                String tag = (String) it.next();
                tags.append(' ').append(tag.toLowerCase());
            }
            Collections.addAll(keywords, FTSUtils.stemWordsLangAware(tags.toString(), locale, 3));
        }
        val = FilenameUtils.getName(name);
        if (!StringUtils.isBlank(val) && val.length() > 2) {
            val = val.toLowerCase();
            Collections.addAll(keywords, FTSUtils.stemWordsLangAware(val, Locale.ENGLISH, 3));
        }
        val = FilenameUtils.getExtension(name);
        if (!StringUtils.isBlank(val) && val.length() > 2) {
            keywords.add(val.toLowerCase());
        }
        MediaType mtype = MediaType.parse(ctype);
        if (mtype != null) {
            mtype = mtype.getBaseType();
            keywords.add(mtype.getType());
            keywords.add(mtype.getSubtype());
        }
        delete("dropKeywords", "id", id);
        for (final String k : keywords) {
            if (k.length() > 24) { //VARCHAR(24)
                continue;
            }
            insert("insertKeyword",
                   "id", id,
                   "keyword", k);
        }
    }

    private boolean deleteDirectoryInternal(String path, boolean nolock) throws Exception {
        boolean res = true;
        ReentrantReadWriteLock rwlock = null;
        try {
            rwlock = nolock ? null : acquirePathRWLock(path, true);
            File f = new File(basedir, path);
            if (!f.exists() || !f.isDirectory()) {
                return true;
            }
            File[] flist = f.listFiles();
            if (flist == null) {
                return false;
            }
            for (final File sf : flist) {
                if (sf.isDirectory()) {
                    deleteDirectoryInternal(path + '/' + sf.getName(), false);
                } else {
                    boolean exists = sf.exists();
                    if (!sf.delete() && exists) {
                        log.error("Cannot to delete file: " + sf.getAbsolutePath());
                    }
                }
            }
            if (!f.delete()) {
                log.error("Cannot delete directory: " + f.getAbsolutePath());
                res = false;
            }
        } finally {
            if (rwlock != null) {
                rwlock.writeLock().unlock();
            }
        }
        return res;
    }

    private MBCriteriaQuery createSelectQ(HttpServletRequest req, boolean count) {
        Locale locale = message.getLocale(req);
        MBCriteriaQuery cq = createCriteria();
        String val;
        if (req.isUserInRole("admin")) {
            cq.withParam("system", true);
        }
        if (!count) {
            val = req.getParameter("firstRow");
            if (val != null) {
                Integer frow = Integer.valueOf(val);
                cq.offset(frow);
                val = req.getParameter("lastRow");
                if (val != null) {
                    Integer lrow = Integer.valueOf(val);
                    cq.limit(Math.abs(frow - lrow) + 1);
                }
            }
        }
        val = req.getParameter("status");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("status", Integer.parseInt(val));
        }
        val = req.getParameter("folder");
        if (!StringUtils.isBlank(val)) {
            if (!val.endsWith("/")) {
                val += '/';
            }
            if (BooleanUtils.toBoolean(req.getParameter("subfolders"))) {
                val += '%';
            }
            cq.withParam("folder", val);
        }

        val = req.getParameter("nfolder");
        if (!StringUtils.isBlank(val)) {
            if (!val.endsWith("/")) {
                val += '/';
            }
            val += '%';
            cq.withParam("nfolder", val);
        } else if (!BooleanUtils.toBoolean(req.getParameter("inpages"))) {
            String folder = (String) cq.get("folder");
            if (folder == null || !folder.startsWith("/pages/")) {
                cq.withParam("nfolder", "/pages/%");
            }
        }

        val = req.getParameter("stext");
        if (!StringUtils.isBlank(val)) {
            val = val.toLowerCase();
            if (BooleanUtils.toBoolean(req.getParameter("fts"))) {
                String[] stemWords = FTSUtils.stemWordsLangAware(val, locale, 3);
                if (stemWords.length == 0) { //no keywords fetched fallback to plain query
                    val = val.toLowerCase(locale).trim() + '%';
                    cq.withParam("name", val);
                    cq.withStatement(count ? "count" : "select");
                } else {
                    for (int i = 0; i < stemWords.length; ++i) {
                        stemWords[i] += '%';
                    }
                    List<String> keywords = Arrays.asList(stemWords);
                    while (keywords.size() > 10) { //limit to 10 keywords
                        keywords.remove(keywords.size() - 1);
                    }
                    cq.withParam("keywords", keywords);
                    cq.withParam("keywordsSize", keywords.size());
                    cq.withStatement(count ? "countByKeywords" : "selectByKeywords");
                }
            } else {
                val = val.toLowerCase(locale).trim() + '%';
                cq.withParam("name", val);
                cq.withStatement(count ? "count" : "select");
            }
        } else {
            cq.withStatement(count ? "count" : "select");
        }

        if (!count) {
            val = req.getParameter("sortAsc");
            if (!StringUtils.isBlank(val)) {
                cq.orderBy("e." + val).asc();
            }
            val = req.getParameter("sortDesc");
            if (!StringUtils.isBlank(val)) {
                cq.orderBy("e." + val).desc();
            }
        }
        return cq.finish();
    }

    /**
     * GET list of files in the specified directory(folder).
     * <p/>
     * Produces the following JSON:
     * <p/>
     * <pre>
     *     [
     *       {
     *          "label" : file name,
     *          "status" : 1 if it is folder 0 otherwise,
     *          "system" : 1 if it is in system folder 0 otherwise
     *       },
     *       ...
     *     ]
     * </pre>
     */
    private JsonNode _list(String folder,
                           FileFilter filter,
                           HttpServletRequest req) throws IOException {
        checkFolder(folder);
        ArrayNode res = mapper.createArrayNode();
        ReentrantReadWriteLock rwlock = acquirePathRWLock(folder, false);
        try {
            File f = new File(basedir, folder);
            if (!f.exists()) {
                throw new NotFoundException(folder);
            }
            if (!f.isDirectory()) {
                return res;
            }
            final Collator collator = Collator.getInstance(message.getLocale(req));
            File[] files = f.listFiles(filter);
            if (files == null) files = EMPTY_FILES_ARRAY;
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    int res = Integer.compare(f2.isDirectory() ? 1 : 0, f1.isDirectory() ? 1 : 0);
                    if (res == 0) {
                        return collator.compare(f1.getName(), f2.getName());
                    }
                    return res;
                }
            });
            folder = normalizeFolder(folder);
            boolean parentInSystem = isInSystemFolder(folder);
            for (int i = 0, l = files.length; i < l; ++i) {
                File file = files[i];
                boolean inSystem = parentInSystem || isInSystemFolder(folder + file.getName());
                if (file.isDirectory() && inSystem && !req.isUserInRole("admin")) {
                    continue;
                }
                res.addObject()
                        .put("label", file.getName())
                        .put("status", file.isDirectory() ? 1 : 0)
                        .put("system", inSystem ? 1 : 0);
            }
        } finally {
            rwlock.readLock().unlock();
        }
        return res;
    }

    private Response _thumbnail(Long id,
                                String folder,
                                String name,
                                HttpServletRequest req) throws Exception {

        Map<String, ?> rec;
        if (id != null) {
            rec = selectOne("selectIcon2", "id", id);
            if (rec == null) {
                throw new NotFoundException();
            }
            folder = (String) rec.get("folder");
            name = (String) rec.get("name");

        } else {

            checkFolder(folder);
            folder = normalizeFolder(folder);
            rec = selectOne("selectIcon", "folder", folder, "name", name);
            if (rec == null) {
                throw new NotFoundException();
            }
            id = ((Number) rec.get("id")).longValue();
        }

        String path = folder + name;
        XMLConfiguration xcfg = cfg.impl();
        int thumbWidth = xcfg.getInt("media.thumbnails-width", 255);
        String ctype = (String) rec.get("content_type");
        String iconCtype = (String) rec.get("icon_content_type");
        if (ctype == null || !ctype.startsWith("image/")) {
            throw new BadRequestException(path);
        }
        String thumbFormat = getImageFileResizeFormat(ctype);
        final Blob icon = (Blob) rec.get("icon");

        if (icon != null) {
            final byte[] icondata = icon.getBytes(0, (int) icon.length());
            return Response.ok(new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    output.write(icondata);
                }
            }).type(iconCtype)
                    .header(HttpHeaders.CONTENT_LENGTH, icondata.length)
                    .build();
        }

        BufferedImage image;
        try (final ResourceLock l = new ResourceLock(path, false)) {
            File f = new File(basedir, path.substring(1));
            if (!f.exists()) {
                throw new NotFoundException(path);
            }
            image = ImageIO.read(f);
        }
        if (image == null) {
            log.warn("Unable to generated thumbnail. Content type: " + ctype +
                     " cannot read source image: " + path);
            return Response.serverError().build();
        }

        //Now resize the image
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage thumbnail = (image.getWidth() > thumbWidth || image.getHeight() > thumbWidth) ?
                                  Scalr.resize(image, thumbWidth) : image;
        if (!ImageIO.write(thumbnail, thumbFormat, bos)) {
            throw new RuntimeException("Cannot find image writer for thumbFormat=" + thumbFormat);
        }

        iconCtype = "image/" + thumbFormat;
        final byte[] icondata = bos.toByteArray();
        update("updateIcon",
               "id", id,
               "icon", icondata,
               "icon_content_type", iconCtype);

        return Response.ok(new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                output.write(icondata);
            }
        }).type(iconCtype)
                .header(HttpHeaders.CONTENT_LENGTH, icondata.length)
                .build();
    }

    private String getImageFileResizeFormat(String ctype) {
        if (ctype.startsWith("image/jpeg")) {
            return "jpeg";
        } else if (ctype.startsWith("image/png")) {
            return "png";
        } else {
            return cfg.impl().getString("media.resize-default-format", "jpeg");
        }
    }

    private Response _get(String folder,
                          String name,
                          HttpServletRequest req,
                          Integer width,
                          Integer height,
                          boolean transfer) throws Exception {
        checkFolder(folder);
        if (!folder.endsWith("/")) {
            folder += '/';
        }
        Response r;
        Response.ResponseBuilder rb = Response.ok();
        String path = folder + name;

        final ResourceLock l = new ResourceLock(path, false);
        try {

            File f = new File(new File(basedir, folder), name);
            if (!f.exists() || !f.isFile()) {
                //noinspection ThrowCaughtLocally
                throw new NotFoundException(path);
            }
            if (folder.isEmpty() || folder.charAt(0) != '/') {
                folder = '/' + folder;
            }

            Map<String, ?> res = selectOne("selectByPath",
                                           "folder", folder,
                                           "name", name);
            if (res == null) {
                //noinspection ThrowCaughtLocally
                throw new NotFoundException(path);
            }
            String ctype = (String) res.get("content_type");
            if (ctype == null) {
                ctype = "application/octet-stream";
            }

            Number clength = (Number) res.get("content_length");
            Number id = (Number) res.get("id");
            MediaType mtype = MediaType.parse(ctype);
            final File respFile;

            if (id == null) {
                throw new NotFoundException(path);
            }
            if ((width != null || height != null) && CTypeUtils.isImageContentType(ctype)) {
                MediaType rsMtype = MediaType.parse("image/" + getImageFileResizeFormat(ctype));
                File rsFile = getResizedImageFile(rsMtype, folder, id.longValue(), width, height);
                if (rsFile.exists() && rsFile.length() > 0) { //we have resized version
                    mtype = rsMtype;
                    clength = rsFile.length();
                    respFile = rsFile;
                } else {
                    respFile = f;
                }
            } else {
                respFile = f;
            }
            if (mtype != null) {
                rb.type(mtype.toString());
                rb.encoding(mtype.getParameters().get("charset"));
            }
            if (clength != null) {
                rb.header(HttpHeaders.CONTENT_LENGTH, clength);
            }
            rb.header(HttpHeaders.CONTENT_DISPOSITION, ResponseUtils
                    .encodeContentDisposition(name, BooleanUtils.toBoolean(req.getParameter("inline"))));

            if (transfer) {
                l.releaseParent(); //unlock parent folder read-lock
                rb.entity(
                        new StreamingOutput() {
                            public void write(OutputStream output) throws IOException, WebApplicationException {
                                try (final FileInputStream fis = new FileInputStream(respFile)) {
                                    IOUtils.copyLarge(fis, output);
                                } finally {
                                    l.close();
                                }
                            }
                        }
                );
            } else {
                rb.status(Response.Status.NO_CONTENT);
            }

            r = rb.build();

        } catch (Throwable e) {
            l.close();
            throw e;
        }
        if (!transfer) {
            l.close();
        }

        return r;
    }


    @Transactional
    public void updateResizedImages(long id) throws IOException {
        Map<String, ?> row = selectOne("selectEntityPathById", "id", id);
        if (row == null) {
            return;
        }
        updateResizedImages(String.valueOf(row.get("folder")) + row.get("name"));
    }

    @Transactional
    public void updateResizedImages(String path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path");
        }
        //collect widths
        List<Pair<Integer, Integer>> hints = new ArrayList<>();
        try (final ResourceLock l = new ResourceLock(path, false)) {
            String folder = getResourceParentFolder(path);
            String name = getResourceName(path);
            Number id = selectOne("selectEntityIdByPath",
                                  "folder", folder,
                                  "name", name);
            if (id == null) {
                return;
            }
            File dir = getResizedImageDir(folder, id.longValue());
            if (!dir.exists()) {
                return;
            }
            for (String h : dir.list(FileFileFilter.FILE)) {
                h = FilenameUtils.removeExtension(h);
                //noinspection EmptyCatchBlock
                try {
                    int ind = h.indexOf('x');
                    if (ind == 0) {
                        hints.add(new Pair<Integer, Integer>(
                                null, Integer.parseInt(h.substring(1))));
                    } else if (ind > 0) {
                        if (ind < h.length() - 1) {
                            hints.add(new Pair<>(
                                    Integer.parseInt(h.substring(0, ind)),
                                    Integer.parseInt(h.substring(ind + 1))
                            ));
                        }
                    } else if (!h.isEmpty()) {
                        hints.add(new Pair<Integer, Integer>(Integer.parseInt(h), null));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        for (Pair<Integer, Integer> h : hints) {
            ensureResizedImage(path, h.getOne(), h.getTwo(), false);
        }
    }

    @Transactional
    public void ensureResizedImage(long id, Integer width, Integer height,
                                   boolean skipSmall) throws IOException {
        Map<String, ?> row = selectOne("selectEntityPathById", "id", id);
        if (row == null) {
            return;
        }
        ensureResizedImage(String.valueOf(row.get("folder")) + row.get("name"),
                           width, height, skipSmall);
    }

    @Transactional
    public void ensureResizedImage(String path,
                                   Integer width, Integer height,
                                   boolean skipSmall) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path");
        }
        if ((width == null && height == null) ||
            (width != null && (width <= 0 || width > 6000)) ||
            (height != null && (height <= 0 || height > 6000))) {
            throw new IllegalArgumentException("width|height");
        }
        try (final ResourceLock l = new ResourceLock(path, false)) {
            String folder = getResourceParentFolder(path);
            String name = getResourceName(path);
            Map<String, ?> info = selectOne("selectResourceAttrsByPath",
                                            "folder", folder,
                                            "name", name);
            if (info == null || info.get("id") == null) {
                return;
            }

            KVOptions meta = new KVOptions();
            meta.loadOptions((String) info.get("meta"));
            if (meta.containsKey("width") && meta.containsKey("height")) {
                if ((width == null || (width.intValue() == meta.getInt("width", Integer.MAX_VALUE))) &&
                    (height == null || (height.intValue() == meta.getInt("height", Integer.MAX_VALUE)))) {
                    return;
                }
                if (skipSmall) {
                    if ((width == null || (width.intValue() > meta.getInt("width", Integer.MAX_VALUE))) &&
                        (height == null || (height.intValue() > meta.getInt("height", Integer.MAX_VALUE)))) {
                        return;
                    }
                }
            }

            long id = ((Number) info.get("id")).longValue();
            String ctype = (String) info.get("content_type");
            if (!CTypeUtils.isImageContentType(ctype)) {
                log.warn("ensureResizedImage: Not applicable file content type: " + ctype);
                return;
            }
            MediaType mtype = MediaType.parse("image/" + getImageFileResizeFormat(ctype));
            File source = new File(basedir, path.startsWith("/") ? path.substring(1) : path);
            if (!source.exists()) {
                return;
            }
            File tfile = getResizedImageFile(mtype, folder, id, width, height);
            if (tfile.exists() && !FileUtils.isFileNewer(source, tfile)) {
                return; //up-to-date resized file version exists
            }
            BufferedImage image = ImageIO.read(source);
            if (image == null) {
                log.warn("Cannot read file as image: " + source);
                return;
            }
            if (width != null && height != null) {
                image = Scalr.resize(image, Scalr.Mode.FIT_EXACT, width, height);
            } else if (width != null) {
                image = Scalr.resize(image, Scalr.Mode.FIT_TO_WIDTH, width);
            } else {
                image = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, height);
            }
            //Unlock read lock before acuiring exclusive write lock
            l.close();
            try (final ResourceLock wl = new ResourceLock(path, true)) {
                if (source.exists()) {
                    tfile.getParentFile().mkdirs();
                    try (final FileOutputStream fos = new FileOutputStream(tfile)) {
                        if (!ImageIO.write(image, mtype.getSubtype(), fos)) {
                            throw new RuntimeException("Cannot find image writer for: '" +
                                                       mtype.getSubtype() + "'");
                        }
                    }
                }
            }
        }
    }

    private File getResizedImageDir(String folder, long entryId) {
        if (!folder.endsWith("/")) {
            folder += "/";
        }
        return new File(basedir,
                        folder +
                        SIZE_CACHE_FOLDER +
                        '/' + entryId);
    }

    private File getResizedImageFile(MediaType mtype,
                                     String folder, long entryId,
                                     Integer width,
                                     Integer height) {
        if (!folder.endsWith("/")) {
            folder += "/";
        }
        String hint;
        if (width != null && height != null) {
            hint = width.toString() + "x" + height;
        } else if (width != null) {
            hint = width.toString();
        } else if (height != null) {
            hint = "x" + height;
        } else {
            throw new IllegalArgumentException("Either width or height must be specified");
        }
        return new File(basedir,
                        folder +
                        SIZE_CACHE_FOLDER +
                        '/' + entryId +
                        '/' + hint + '.' + mtype.getSubtype());
    }

    private void checkFolder(String folder) {
        if (folder == null) {
            throw new IllegalArgumentException();
        }
        if (folder.contains("..") || folder.contains(SIZE_CACHE_FOLDER)) {
            throw new BadRequestException(folder);
        }
    }

    private Long _put(String folder,
                      String name,
                      HttpServletRequest req,
                      InputStream in,
                      int flags) throws Exception {

        checkFolder(folder);
        Number id;
        folder = normalizeFolder(folder);

        //Used in order to detect ctype with TIKA (mark/reset are supported by BufferedInputStream)
        BufferedInputStream bis = new BufferedInputStream(in);
        FileUploadStream us = null;
        boolean localPut = (req instanceof LocalRequest);
        String rctype = (req.getContentType() == null) ?
                        req.getServletContext().getMimeType(name) :
                        req.getContentType();

        //We do not trust to the content-type provided by request
        MediaType mtype = MimeTypeDetector.detect(bis, name, rctype, req.getCharacterEncoding());
        if (mtype.getBaseType().toString().startsWith("text/") &&
            mtype.getParameters().get("charset") == null) {
            Charset charset = MimeTypeDetector.detectCharset(bis, name, rctype, req.getCharacterEncoding());
            if (charset != null) {
                mtype = new MediaType(mtype, charset);
            }
        }

        if (!localPut) {
            XMLConfiguration xcfg = cfg.impl();
            int memTh = xcfg.getInt("media.max-upload-inmemory-size", MB); //1Mb by default
            int uplTh = xcfg.getInt("media.max-upload-size", MB * 10); //10Mb by default
            us = new FileUploadStream(memTh, uplTh, "ncms-", ".upload", cfg.getTmpdir());
        }

        try (final ResourceLock l = new ResourceLock(folder + name, true)) {
            id = selectOne("selectEntityIdByPath",
                           "folder", folder,
                           "name", name);

            checkEditAccess(folder + name, req);

            long actualLength;
            if (localPut) {
                actualLength = ((LocalRequest) req).file.length();
            } else {
                actualLength = IOUtils.copyLarge(bis, us);
                us.close();
            }
            if (req.getContentLength() != -1 && req.getContentLength() != actualLength) {
                throw new BadRequestException(
                        String.format("Wrong Content-Length request header specified. " +
                                      "The file %s/%s will be rejected",
                                      folder, name
                        )
                );
            }
            File dir = new File(basedir, folder.substring(1));
            File target = new File(dir, name);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (log.isDebugEnabled()) {
                log.debug("Writing {}/{} into: {} as {} size: {}",
                          folder, name, target.getAbsolutePath(), mtype, actualLength);
            }
            try (final FileOutputStream fos = new FileOutputStream(target)) {
                if (us != null) {
                    us.writeTo(fos);
                } else {
                    IOUtils.copyLarge(bis, fos);
                }
                fos.flush();
            }

            KVOptions meta;
            if (CTypeUtils.isImageContentType(mtype.toString())) {
                Metadata metadata = MetadataDetector.detect(mtype, target);
                if (metadata.get("width") == null) {
                    if (metadata.get(Metadata.IMAGE_WIDTH) != null) {
                        metadata.add("width", metadata.get(Metadata.IMAGE_WIDTH));
                    }
                }
                if (metadata.get("height") == null) {
                    if (metadata.get(Metadata.IMAGE_LENGTH) != null) {
                        metadata.add("height", metadata.get(Metadata.IMAGE_LENGTH));
                    }
                }
                meta = MetadataDetector.metadata2Options(metadata,
                                                         "width", "height");
            } else {
                meta = new KVOptions();
            }

            if (id == null) {

                Map<String, Object> args = new HashMap<>();
                args.put("folder", folder);
                args.put("name", name);
                args.put("status", 0);
                args.put("content_type", mtype.toString());
                args.put("put_content_type", req.getContentType());
                args.put("content_length", actualLength);
                args.put("owner", req.getRemoteUser());
                args.put("meta", meta.toString());
                args.put("system", ((flags & PUT_SYSTEM) != 0) ? 1 : 0);
                insert("insertEntity", args);
                id = (Number) args.get("id"); //autogenerated

            } else {

                update("updateEntity",
                       "id", id,
                       "content_type", mtype.toString(),
                       "content_length", actualLength,
                       "owner", req.getRemoteUser(),
                       "meta", meta.toString(),
                       "system", ((flags & PUT_SYSTEM) != 0) ? 1 : 0);
            }

            if (id != null) {
                ebus.fireOnSuccessCommit(new MediaUpdateEvent(this, false, id, folder + name));
                if ((PUT_NO_KEYS & flags) == 0) {
                    updateFTSKeywords(id.longValue(), req);
                }
            }

        } finally {
            if (us != null && us.getFile() != null) {
                us.getFile().delete();
            }
            bis.close();
        }

        return id != null ? id.longValue() : null;
    }

    public Closeable acquireReadResourceLock(String path) {
        return new ResourceLock(path, false);
    }

    public Closeable acquireWriteResourceLock(String path) {
        return new ResourceLock(path, true);
    }

    final class ResourceLock implements Closeable {

        ReentrantReadWriteLock parent;

        ReentrantReadWriteLock child;

        final boolean parentWriteLock;

        final boolean childWriteLock;

        private ResourceLock(String path, boolean childWriteLock) {
            this(path, false, childWriteLock);
        }

        private ResourceLock(String path, boolean parentWriteLock, boolean childWriteLock) {
            if (path.isEmpty() || path.charAt(0) != '/') {
                path = '/' + path;
            }
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            this.parent = null;
            this.child = null;
            this.parentWriteLock = parentWriteLock;
            this.childWriteLock = childWriteLock;
            String folder = getResourceParentFolder(path);
            try {
                if (!folder.equals(path)) {
                    parent = acquirePathRWLock(folder, parentWriteLock);
                    child = acquirePathRWLock(path, childWriteLock);
                } else {
                    parent = null;
                    child = acquirePathRWLock(path, childWriteLock);
                }
            } catch (Throwable e) {
                //noinspection finally
                try {
                    close();
                } catch (IOException e1) {
                    log.error("", e1);
                } finally {
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException(e);
                }
            }
        }

        public void releaseParent() {
            if (parent != null) {
                if (parentWriteLock) {
                    parent.writeLock().unlock();
                } else {
                    parent.readLock().unlock();
                }
                parent = null;
            }
        }

        public void releaseChild() {
            if (child != null) {
                if (childWriteLock) {
                    child.writeLock().unlock();
                } else {
                    child.readLock().unlock();
                }
                child = null;
            }
        }

        public void close() throws IOException {
            try {
                releaseChild();
            } finally {
                releaseParent();
            }
        }
    }

    private ReentrantReadWriteLock acquirePathRWLock(String path, boolean acquireWrite) {
        if (path.isEmpty() || path.charAt(0) != '/') {
            path = '/' + path;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (log.isDebugEnabled()) {
            log.debug("Locking: " + path + " W: " + acquireWrite);
        }
        ReentrantReadWriteLock rwlock;
        while (true) {
            synchronized (locksCache) {
                rwlock = (ReentrantReadWriteLock) locksCache.get(path);
                if (rwlock == null) {
                    rwlock = new ReentrantReadWriteLock();
                    locksCache.put(path, rwlock);
                }
            }

            //Optimistic locking used here

            if (acquireWrite) {
                rwlock.writeLock().lock();
            } else {
                rwlock.readLock().lock();
            }

            synchronized (locksCache) {
                //noinspection ObjectEquality
                if (rwlock == locksCache.get(path)) {
                    //Locked rwlock is not changed we are safe to use it until it remains locked
                    break;
                } else {
                    //Locked rwlock is changed (removed from locksCache and replaced) so release it and try again
                    if (acquireWrite) {
                        rwlock.writeLock().unlock();
                    } else {
                        rwlock.readLock().unlock();
                    }
                }
            }
        }
        return rwlock;
    }

    private void checkEditAccess(Long id, HttpServletRequest req) {
        if (req.isUserInRole("admin")) {
            return;
        }
        Map<String, ?> fmeta = selectOne("selectResourceAttrsById", "id", id);
        _checkEditAccess(fmeta, StringUtils.strip((String) fmeta.get("folder") + fmeta.get("name"), "/"), req);
    }

    private void checkEditAccess(String path, HttpServletRequest req) {
        if (req.isUserInRole("admin")) {
            return;
        }
        Map<String, ?> fmeta = selectOne("selectResourceAttrsByPath",
                                         "folder", getResourceParentFolder(path),
                                         "name", getResourceName(path));
        _checkEditAccess(fmeta, path, req);
    }

    private void _checkEditAccess(Map<String, ?> fmeta, String path, HttpServletRequest req) {

        if ((req instanceof LocalRequest) || req.isUserInRole("admin")) {
            return;
        }

        //todo check page access

        boolean isFile = (fmeta != null && 0 == ((Number) fmeta.get("status")).intValue());
        if (isFile) {
            if (!req.getRemoteUser().equals(fmeta.get("owner"))) {
                String msg = message.get("ncms.mmgr.access.denied", req, path);
                throw new SecurityException(msg);
            }
        } else {
            File f = new File(basedir, path);
            if (!f.isDirectory()) {
                return;
            }
            // check not owned files in the directory
            Number count = selectOne("countNotOwned",
                                     "folder", normalizeFolder(path),
                                     "owner", req.getRemoteUser());
            if (count != null && count.intValue() > 0) {
                throw new SecurityException(message.get("ncms.mmgr.access.denied.notOwned", req));
            }
        }
    }

    private boolean isInSystemFolder(String path) {
        path = normalizeFolder(path);
        List<Object> sdirs = cfg.impl().getList("media.system-directories.directory");
        for (final Object sdirObj : sdirs) {
            String sdir = String.valueOf(sdirObj);
            if (path.startsWith(normalizeFolder(sdir))) {
                return true;
            }
        }

        return false;
    }

    public static String getResourceParentFolder(String path) {
        String dirname = FilenameUtils.getPath(path);
        if (StringUtils.isBlank(dirname)) {
            dirname = "/";
        } else {
            dirname = normalizeFolder(dirname);
        }
        return dirname;
    }

    public static String getResourceName(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return null;
        }
        return FilenameUtils.getName(path);
    }

    public static String normalizeFolder(String folder) {
        if (folder.isEmpty() || folder.charAt(0) != '/') {
            folder = '/' + folder;
        }
        if (!folder.endsWith("/")) {
            folder += '/';
        }
        return folder;
    }

    public static String normalizePath(String path) {
        if (path.isEmpty() || path.charAt(0) != '/') {
            path = '/' + path;
        }
        return path;
    }

    @Subscribe
    public void pageDropped(PageDroppedEvent ev) {
        String path = getPageLocalFolderPath(ev.getId());
        try (final ResourceLock l = new ResourceLock(path, true)) {
            File pdir = new File(basedir, path);
            if (pdir.isDirectory()) {
                log.info("Removing page dir: " + path);
                FileUtils.deleteDirectory(pdir);
            }
        } catch (IOException e) {
            log.error("Failed to drop page files dir: " + path, e);
        }
    }

    @Subscribe
    public void mediaUpdate(MediaUpdateEvent ev) {
        if (!ev.isFolder()) {
            try {
                updateResizedImages(ev.getPath());
            } catch (IOException e) {
                log.error("Failed to update resized images dir", e);
            }
        }
    }

    private String getPageLocalFolderPath(long pageId) {
        StringBuilder sb = new StringBuilder(16);
        sb.append("/pages");
        String sPageId = Long.toString(pageId);
        for (int i = 0, l = sPageId.length(); i < l; ++i) {
            if (i % 3 == 0) {
                sb.append('/');
            }
            sb.append(sPageId.charAt(i));
        }
        return sb.toString();
    }

    private static final class RWLocksLRUCache extends LRUMap {

        private RWLocksLRUCache(int maxSize) {
            super(maxSize, 0.75f, true);
        }

        //we must be already in synchronized block!
        protected boolean removeLRU(LinkEntry entry) {
            ReentrantReadWriteLock lock = (ReentrantReadWriteLock) entry.getValue();
            if (lock.writeLock().tryLock()) { //check if rwlock is completely free
                lock.writeLock().unlock();
                return true;
            }
            return false; //Do not remove: someone holds the lock
        }
    }

    private static final class FileUploadStream extends DeferredFileOutputStream {

        final int hardThreshould;

        /**
         * @param memThreashould Threshold after that new data will be flushed on disk.
         * @param hardThreshould Threshold after that IOException will fired
         */
        FileUploadStream(int memThreashould, int hardThreshould, String prefix, String suffix, File dir) {
            super(memThreashould, prefix, suffix, dir);
            this.hardThreshould = hardThreshould;
        }

        void check(int length) throws IOException {
            if (getByteCount() + length >= hardThreshould) {
                throw new IOException("Reached the maximum upload file size: " + hardThreshould);
            }
        }

        public void write(int b) throws IOException {
            check(1);
            super.write(b);
        }

        public void write(byte[] b) throws IOException {
            check(b.length);
            super.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            check(len);
            super.write(b, off, len);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //                       Import resources staff                          //
    ///////////////////////////////////////////////////////////////////////////

    private final List<DirectoryScanner> watchScanners = Collections.synchronizedList(new ArrayList<DirectoryScanner>());

    private final FSWatcherCollectEventHandler watchHandler = new FSWatcherCollectEventHandler(
            FSWatcherCollectEventHandler.MOVE_CREATED_INTO_MODIFIED
    );

    public void init(FSWatcher watcher) {
    }

    public void handlePollTimeout(FSWatcher watcher) {
        FSWatcherCollectEventHandler snapshot;
        synchronized (watchHandler) {
            if (watchHandler.getModified().isEmpty() &&
                watchHandler.getDeleted().isEmpty()) {
                watchHandler.clear();
                return;
            }
            snapshot = (FSWatcherCollectEventHandler) watchHandler.clone();
            watchHandler.clear();
        }
        FSWatcherUserData data = watcher.getUserData();
        Set<Path> modified = new HashSet<>(snapshot.getModified());
        Set<Path> deleted = new HashSet<>(snapshot.getDeleted());
        for (final Path path : snapshot.getModified()) {
            if (modified.contains(path)) {
                if (Files.exists(path)) {
                    deleted.remove(path);
                } else {
                    modified.remove(path);
                }
            }
        }

        for (final Path path : deleted) {
            Path target = data.target.resolve(data.ds.getBasedir().relativize(path));
            try {
                deleteResource(target.toString(), new LocalRequest(target.toFile()));
            } catch (NotFoundException ignored) {
            } catch (Exception e) {
                log.error("File deletion failed. Path: " + path + " target: " + target + " error: " + e.getMessage());
            }
        }

        for (final Path path : modified) {
            Path target = data.target.resolve(data.ds.getBasedir().relativize(path));
            try {
                importFile(path.toString(),
                           target.toString(),
                           data.overwrite,
                           data.system);
            } catch (IOException e) {
                log.error("File import failed. Path: " + path + " target: " + target + " error: " + e.getMessage());
            }

        }
    }

    public void handleRegisterEvent(FSWatcherRegisterEvent ev) throws Exception {
        watcherImportFile(ev);
    }

    public void handleCreateEvent(FSWatcherCreateEvent ev) {
        synchronized (watchHandler) {
            watchHandler.handleCreateEvent(ev);
        }
    }

    public void handleModifyEvent(FSWatcherModifyEvent ev) {
        synchronized (watchHandler) {
            watchHandler.handleModifyEvent(ev);
        }
    }

    public void handleDeleteEvent(FSWatcherDeleteEvent ev) throws Exception {
        synchronized (watchHandler) {
            watchHandler.handleDeleteEvent(ev);
        }
    }

    private void watcherImportFile(FSWatcherEventSupport ev) throws Exception {
        FSWatcherUserData data = ev.getWatcher().getUserData();
        Path target = data.target.resolve(data.ds.getBasedir().relativize(ev.getFullPath()));
        importFile(ev.getFullPath().toString(),
                   target.toString(),
                   data.overwrite,
                   data.system);
    }

    @Transactional(executorType = ExecutorType.SIMPLE)
    public void importDirectory(String source,
                                String target,
                                String[] includes,
                                String[] excludes,
                                final boolean overwrite,
                                final boolean watch,
                                final boolean system) throws IOException {

        if (target.startsWith("/")) {
            target = target.substring(1);
        }
        log.info("Importing " + source + " into " + target + " watch=" + watch);
        final DirectoryScannerFactory sf = new DirectoryScannerFactory(Paths.get(source));
        for (final String inc : includes) {
            sf.include(inc);
        }
        for (final String exc : excludes) {
            sf.exclude(exc);
        }
        if (watch) {
            DirectoryScanner ds = sf.createScanner();
            watchScanners.add(ds);
            ds.activateFileSystemWatcher(this, 200L,
                                         new FSWatcherUserData(ds,
                                                               Paths.get(target),
                                                               overwrite,
                                                               system));
        } else {
            final String importTarget = target;
            try (DirectoryScanner ds = sf.createScanner()) {
                ds.scan(new DirectoryScannerVisitor() {
                    public void visit(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        importFile(sf.getBasedir().resolve(path).toString(),
                                   Paths.get(importTarget).resolve(path).toString(),
                                   overwrite, system);
                    }

                    public void error(Path path, IOException e) throws IOException {
                        log.error("Failed to scan path: " + path, e);
                    }
                });
            }
        }
    }


    @Transactional(executorType = ExecutorType.SIMPLE)
    public void importFile(String source, String target, boolean overwrite, boolean system) throws IOException {
        File srcFile = new File(source);
        if (!srcFile.isFile()) {
            throw new IOException(srcFile.getAbsolutePath() + " is not a file");
        }
        File tgt = new File(basedir, target);
        if (tgt.isDirectory()) {
            throw new IOException("Cannot overwrite existing directory: " + target);
        }
        String name = getResourceName(target);
        String folder = getResourceParentFolder(target);
        if (!overwrite &&
            tgt.exists() &&
            !FileUtils.isFileNewer(srcFile, tgt)) {
            //check db
            Number id = selectOne("selectEntityIdByPath",
                                  "name", name,
                                  "folder", folder);
            if (id != null) {
                return;
            }
        }
        log.info("Importing " + target);
        try (final FileInputStream fis = new FileInputStream(srcFile)) {
            try {
                int flags = PUT_NO_KEYS;
                if (system) {
                    flags |= PUT_SYSTEM;
                }
                _put(folder, name, new LocalRequest(srcFile), fis, flags);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private static final class FSWatcherUserData {
        private final DirectoryScanner ds;
        private final Path target;
        private final boolean overwrite;
        private final boolean system;

        private FSWatcherUserData(DirectoryScanner ds,
                                  Path target,
                                  boolean overwrite,
                                  boolean system) {
            this.ds = ds;
            this.target = target;
            this.overwrite = overwrite;
            this.system = system;
        }
    }

    private final class LocalRequest extends HttpServletRequestAdapter {

        private final File file;

        private LocalRequest(File file) {
            this.file = file;
        }

        public String getMethod() {
            return "PUT";
        }

        public String getRemoteUser() {
            return "system";
        }

        public boolean isUserInRole(String role) {
            return true;
        }

        public Principal getUserPrincipal() {
            return new Principal() {
                public String getName() {
                    return getRemoteUser();
                }
            };
        }

        public ServletContext getServletContext() {
            return cfg.getServletContext();
        }

        public String getCharacterEncoding() {
            return "UTF-8";
        }
    }


    public void close() throws IOException {
        DirectoryScanner[] wsArr = watchScanners.toArray(new DirectoryScanner[watchScanners.size()]);
        for (final DirectoryScanner ds : wsArr) {
            try {
                ds.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        watchHandler.clear();
        watchScanners.clear();
    }
}