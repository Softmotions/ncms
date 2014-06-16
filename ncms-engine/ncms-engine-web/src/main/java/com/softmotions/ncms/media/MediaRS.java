package com.softmotions.ncms.media;

import com.softmotions.commons.cont.ArrayUtils;
import com.softmotions.commons.cont.TinyParamMap;
import com.softmotions.commons.io.DirUtils;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.fts.FTSUtils;
import com.softmotions.ncms.io.MimeTypeDetector;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.web.HttpServletRequestAdapter;
import com.softmotions.web.ResponseUtils;
import com.softmotions.weboot.mb.MBCriteriaQuery;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Provider;

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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import java.security.Principal;
import java.sql.Blob;
import java.text.Collator;
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
@Path("/media")
@Produces("application/json")
public class MediaRS extends MBDAOSupport implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaRS.class);

    private static final int MB = 1048576;

    private static final File[] EMPTY_FILES_ARRAY = new File[0];

    final NcmsConfiguration cfg;

    final File basedir;

    final RWLocksLRUCache locksCache;

    final ObjectMapper mapper;

    final NcmsMessages message;

    final Provider<ServletContext> sctx;

    final NcmsEventBus ebus;

    @Inject
    public MediaRS(NcmsConfiguration cfg,
                   SqlSession sess,
                   ObjectMapper mapper,
                   NcmsMessages message,
                   NcmsEventBus ebus,
                   Provider<ServletContext> sctx) throws IOException {

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
        this.sctx = sctx;
    }

    /**
     * Save uploaded file.
     * <p/>
     * Example:
     * curl --upload-file ./myfile.txt http://localhost:8080/ncms/rs/media/file/foo/bar/test.txt
     */
    @PUT
    @Consumes("*/*")
    @Path("/file/{folder:.*}/{name}")
    @Transactional(executorType = ExecutorType.SIMPLE)
    public void put(@PathParam("folder") String folder,
                    @PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws Exception {
        _put(folder, name, req, in);
    }

    @PUT
    @Consumes("*/*")
    @Path("/file/{name}")
    @Transactional(executorType = ExecutorType.SIMPLE)
    public void put(@PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws Exception {
        _put("", name, req, in);
    }


    @GET
    @Path("/file/{folder:.*}/{name}")
    @Transactional
    public Response get(@PathParam("folder") String folder,
                        @PathParam("name") String name,
                        @Context HttpServletRequest req) throws Exception {
        return _get(folder, name, req, true);
    }

    @GET
    @Path("/file/{name}")
    @Transactional
    public Response get(@PathParam("name") String name,
                        @Context HttpServletRequest req) throws Exception {
        return _get("", name, req, true);
    }

    @HEAD
    @Path("/file/{folder:.*}/{name}")
    @Transactional
    public Response head(@PathParam("folder") String folder,
                         @PathParam("name") String name,
                         @Context HttpServletRequest req) throws Exception {
        return _get(folder, name, req, false);
    }

    @HEAD
    @Path("/file/{name}")
    @Transactional
    public Response head(@PathParam("name") String name,
                         @Context HttpServletRequest req) throws Exception {
        return _get("", name, req, false);
    }

    @GET
    @Path("/thumbnail2/{id}")
    @Transactional
    public Response thumbnail(@PathParam("id") Long id,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(id, null, null, req);
    }


    @GET
    @Path("/thumbnail/{folder:.*}/{name}")
    @Transactional
    public Response thumbnail(@PathParam("folder") String folder,
                              @PathParam("name") String name,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(null, folder, name, req);
    }

    @GET
    @Path("/thumbnail/{name}")
    @Transactional
    public Response thumbnail(@PathParam("name") String name,
                              @Context HttpServletRequest req) throws Exception {
        return _thumbnail(null, "", name, req);
    }


    @GET
    @Path("/files/{folder:.*}")
    @Transactional
    public JsonNode listFiles(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws IOException {
        return _list(folder, FileFileFilter.FILE, req);
    }

    @GET
    @Path("/files")
    @Transactional
    public JsonNode listFiles(@Context HttpServletRequest req) throws IOException {
        return _list("", FileFileFilter.FILE, req);
    }

    @GET
    @Path("/folders/{folder:.*}")
    @Transactional
    public JsonNode listFolders(@PathParam("folder") String folder,
                                @Context HttpServletRequest req) throws IOException {
        return _list(folder, DirectoryFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/folders")
    @Transactional
    public JsonNode listFolders(@Context HttpServletRequest req) throws IOException {
        return _list("", DirectoryFileFilter.INSTANCE, req);
    }


    @GET
    @Path("/all/{folder:.*}")
    @Transactional
    public JsonNode listAll(@PathParam("folder") String folder,
                            @Context HttpServletRequest req) throws IOException {
        return _list(folder, TrueFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/all")
    @Transactional
    public JsonNode listAll(@Context HttpServletRequest req) throws IOException {
        return _list("", TrueFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/select")
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
    @Path("/select/count")
    @Produces("text/plain")
    @Transactional
    public Integer selectCount(@Context HttpServletRequest req) {
        MBCriteriaQuery cq = createSelectQ(req, true);
        return selectOne(cq.getStatement(), cq);
    }

    @PUT
    @Path("/folder/{folder:.*}")
    @Transactional
    public JsonNode newFolder(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws Exception {

        try (ResourceLock l = new ResourceLock(folder, true)) {
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
                        new MediaCreateEvent(this, true, id, dirname + name));
            } else {
                throw new NcmsMessageException(message.get("ncms.mmgr.folder.exists", req, folder), true);
            }
            return mapper.createObjectNode()
                    .put("label", name)
                    .put("status", 1)
                    .put("owner", req.getRemoteUser());
        }
    }

    @PUT
    @Path("/move/{path:.*}")
    @Transactional(executorType = ExecutorType.BATCH)
    public void move(@PathParam("path") String path,
                     @Context HttpServletRequest req,
                     String npath) throws Exception {

        path = StringUtils.strip(path, "/");
        npath = StringUtils.strip(npath, "/");
        if (npath.contains("..") || path.contains("..") || StringUtils.isBlank(npath)) {
            throw new BadRequestException();
        }
        if (npath.equals(path)) {
            return;
        }
        Long id;

        try (ResourceLock l1 = new ResourceLock(path, true)) {
            try (ResourceLock l2 = new ResourceLock(npath, true)) {
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

                    String nname = getResourceName(npath);
                    String nfolder = getResourceParentFolder(npath);
                    update("fixResourceLocation",
                           "nfolder", nfolder,
                           "nname", nname,
                           "folder", getResourceParentFolder(path),
                           "name", getResourceName(path));

                    FileUtils.moveFile(f1, f2);

                    id = selectOne("selectEntityIdByPath",
                                   "name", nname,
                                   "folder", nfolder);
                    if (id != null) {
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
    @Path("/delete/{path:.*}")
    @Transactional(executorType = ExecutorType.BATCH)
    public void deleteResource(@PathParam("path") String path,
                               @Context HttpServletRequest req) throws Exception {
        path = StringUtils.strip(path, "/");
        if (path.contains("..")) {
            throw new BadRequestException();
        }
        if (log.isDebugEnabled()) {
            log.debug("deleteResource: " + path);
        }
        boolean isdir;
        try (ResourceLock l = new ResourceLock(path, true)) {
            File f = new File(basedir, path);
            if (!f.exists()) {
                throw new NotFoundException(path);
            }

            checkEditAccess(path, req);

            isdir = f.isDirectory();
            if (isdir) {
                deleteDirectoryInternal(path, true);
                delete("deleteFolder",
                       "prefix_like", '/' + path + "/%");
                delete("deleteFile",
                       "folder", getResourceParentFolder(path),
                       "name", getResourceName(path));
            } else {
                boolean exists = f.exists();
                if (f.delete() || !exists) {
                    delete("deleteFile",
                           "folder", getResourceParentFolder(path),
                           "name", getResourceName(path));
                } else {
                    throw new NcmsMessageException(message.get("ncms.mmgr.file.cannot.delete", req, path), true);
                }
            }
        }

        ebus.fireOnSuccessCommit(new MediaDeleteEvent(this, isdir, path));
    }

    @DELETE
    @Path("/delete-batch")
    public void deleteBatch(@Context HttpServletRequest req,
                            ArrayNode files) throws Exception {
        for (int i = 0, l = files.size(); i < l; ++i) {
            String path = files.get(i).asText();
            deleteResource(path, req);
        }
    }

    /**
     * Update some meta fields of files (by path).
     */
    @POST
    @Path("/meta/path/{path:.*}")
    @Consumes("application/x-www-form-urlencoded")
    @Transactional(executorType = ExecutorType.BATCH)
    public void updateMeta(@PathParam("path") String path,
                           @Context HttpServletRequest req,
                           MultivaluedMap<String, String> form) throws Exception {
        Long id = selectOne("selectEntityIdByPath", "folder", getResourceParentFolder(path), "name", getResourceName(path));
        if (id == null) {
            String msg = message.get("ncms.mmgr.meta.notExists", req, path);
            throw new NcmsMessageException(msg, true);
        }

        updateMeta(id, req, form);
    }
    /**
     * Update some meta fields of files.
     */
    @POST
    @Path("/meta/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Transactional(executorType = ExecutorType.BATCH)
    public void updateMeta(@PathParam("id") Long id,
                           @Context HttpServletRequest req,
                           MultivaluedMap<String, String> form) throws Exception {

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
            for (String tag : tagSet) {
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
            checkEditAccess(id, req);
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


    @Transactional(executorType = ExecutorType.SIMPLE)
    public void importDirectory(File dir) throws IOException {
        if (dir == null || !dir.isDirectory()) {
            throw new IOException(dir + " is not a directory");
        }
        importDirectoryInternal(dir, dir, new LocalPUTRequest());
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

    private void importDirectoryInternal(File bdir, File dir, HttpServletRequest req) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        String prefix = bdir.getCanonicalPath();
        for (final File f : files) {
            if (f.isDirectory()) {
                importDirectoryInternal(bdir, f, req);
            } else if (f.isFile()) {
                String fpath = f.getCanonicalPath();
                fpath = fpath.substring(prefix.length());
                File tgt = new File(basedir, fpath);
                if (tgt.exists() && !FileUtils.isFileNewer(f, tgt)) {
                    continue;
                }
                String name = getResourceName(fpath);
                String folder = getResourceParentFolder(fpath);
                log.info("Importing " + fpath);
                try (FileInputStream fis = new FileInputStream(f)) {
                    try {
                        Long id = _put(folder, name, req, fis);
                        if (id != null) {
                            update("updateSysFlag",
                                   "id", id,
                                   "sys", 1);
                        }
                    } catch (Exception e) {
                        throw new IOException(e);
                    }
                }
            }
        }
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

        String val = null;
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
        for (String k : keywords) {
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
            for (File sf : flist) {
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
        String val = null;

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
     *       {"label" : file name, "status" : 1 if it is folder 0 otherwise, "owner" : owner name },
     *       ...
     *     ]
     * </pre>
     */
    private JsonNode _list(String folder,
                           FileFilter filter,
                           HttpServletRequest req) throws IOException {
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
            final Map<String, ObjectNode> fnodes = new HashMap<>(files.length);
            for (int i = 0, l = files.length; i < l; ++i) {
                fnodes.put(files[i].getName(),
                           res.addObject()
                                   .put("label", files[i].getName())
                                   .put("status", files[i].isDirectory() ? 1 : 0)
                );
            }

            // load owners for selected files/folders (for checking user rights on client side)
            if (!fnodes.isEmpty()) {
                select("selectOwnersByFolderNames",
                       new ResultHandler() {
                           public void handleResult(ResultContext context) {
                               Map<String, ?> row = (Map<String, ?>) context.getResultObject();
                               String name = (String) row.get("name");
                               if (fnodes.containsKey(name)) {
                                   fnodes.get(name).put("owner", (String) row.get("owner"));
                               }
                           }
                       },
                       "folder", normalizeFolder(folder),
                       "names", fnodes.keySet());
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
            if (folder.contains("..")) {
                throw new BadRequestException(folder);
            }
            folder = normalizeFolder(folder);
            rec = selectOne("selectIcon", "folder", folder, "name", name);
            if (rec == null) {
                throw new NotFoundException();
            }
            id = ((Number) rec.get("id")).longValue();
        }

        String path = folder + name;
        XMLConfiguration xcfg = cfg.impl();
        String thumbFormat = xcfg.getString("media.thumbnails-default-format", "jpeg");
        thumbFormat = thumbFormat.toLowerCase();
        int thumWidth = xcfg.getInt("media.thumbnails-width", 255);

        String ctype = (String) rec.get("content_type");
        String iconCtype = (String) rec.get("icon_content_type");
        if (ctype == null || !ctype.startsWith("image/")) {
            throw new BadRequestException(path);
        }
        //Trying to avoid cross-format conversions for better thumbnails quality
        if (ctype.startsWith("image/jpeg")) {
            thumbFormat = "jpeg";
        }
        if (ctype.startsWith("image/png")) {
            thumbFormat = "png";
        }
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
        try (ResourceLock l = new ResourceLock(path, false)) {
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
        BufferedImage thumbnail = Scalr.resize(image, thumWidth);
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


    private Response _get(String folder,
                          String name,
                          HttpServletRequest req,
                          boolean transfer) throws Exception {
        if (folder.contains("..")) {
            throw new BadRequestException(folder);
        }
        if (!folder.endsWith("/")) {
            folder += '/';
        }
        Response r;
        Response.ResponseBuilder rb = Response.ok();
        String path = folder + name;
        final ResourceLock l = new ResourceLock(path, false);
        try {
            final File f = new File(new File(basedir, folder), name);
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
            MediaType mtype = MediaType.parse(ctype);
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
                                try (FileInputStream fis = new FileInputStream(f)) {
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

    private Long _put(String folder,
                      String name,
                      HttpServletRequest req,
                      InputStream in) throws Exception {


        if (folder.contains("..")) {
            throw new BadRequestException(folder);
        }
        Number id;
        folder = normalizeFolder(folder);

        //Used in order to detect ctype with TIKA (mark/reset are supported by BufferedInputStream)
        BufferedInputStream bis = new BufferedInputStream(in);

        String rctype = req.getContentType();
        if (rctype == null) {
            rctype = req.getServletContext().getMimeType(name);
        }
        //We do not trust to the content-type provided by request
        MediaType mtype = MimeTypeDetector.detect(bis, name, rctype, req.getCharacterEncoding());
        if (mtype.getBaseType().toString().startsWith("text/") &&
            mtype.getParameters().get("charset") == null) {
            Charset charset = MimeTypeDetector.detectCharset(bis, name, rctype, req.getCharacterEncoding());
            if (charset != null) {
                mtype = new MediaType(mtype, charset);
            }
        }

        XMLConfiguration xcfg = cfg.impl();
        int memTh = xcfg.getInt("media.max-upload-inmemory-size", MB); //1Mb by default
        int uplTh = xcfg.getInt("media.max-upload-size", MB * 10); //10Mb by default
        FileUploadStream us = new FileUploadStream(memTh, uplTh, "ncms-", ".upload", cfg.getTmpdir());

        try (ResourceLock l = new ResourceLock(folder + name, true)) {
            long actualLength = IOUtils.copyLarge(bis, us);
            us.close();
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
            try (FileOutputStream fos = new FileOutputStream(target)) {
                us.writeTo(fos);
                fos.flush();
            }
            id = selectOne("selectEntityIdByPath",
                           "folder", folder,
                           "name", name);
            if (id == null) {

                Map<String, Object> args = new HashMap<>();
                args.put("folder", folder);
                args.put("name", name);
                args.put("status", 0);
                args.put("content_type", mtype.toString());
                args.put("put_content_type", req.getContentType());
                args.put("content_length", actualLength);
                args.put("owner", req.getRemoteUser());
                insert("insertEntity", args);
                id = (Number) args.get("id"); //autogenerated

            } else {

                update("updateEntity",
                       "id", id,
                       "content_type", mtype.toString(),
                       "content_length", actualLength,
                       "owner", req.getRemoteUser());
            }

            if (id != null) {
                ebus.fireOnSuccessCommit(new MediaCreateEvent(this, false, id, folder + name));
                updateFTSKeywords(id.longValue(), req);
            }

        } finally {
            if (us.getFile() != null) {
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
        if (!req.isUserInRole("admin")) {
            Map<String, ?> fmeta = selectOne("selectResourceAttrsById",
                                             "id", id);
            // fmeta == null - meta not found! Access denied.
            if (fmeta == null || !req.getRemoteUser().equals(fmeta.get("owner"))) {
                String msg = message.get("ncms.mmgr.access.denied", req, "");
                throw new NcmsMessageException(msg, true);
            }
        }
    }

    private void checkEditAccess(String path, HttpServletRequest req) {
        if (!req.isUserInRole("admin")) {
            Map<String, ?> fmeta = selectOne("selectResourceAttrsByPath",
                                             "folder", getResourceParentFolder(path),
                                             "name", getResourceName(path));
            // fmeta == null - meta not found! Access denied.
            if (fmeta == null || !req.getRemoteUser().equals(fmeta.get("owner"))) {
                String msg = message.get("ncms.mmgr.access.denied", req, path);
                throw new NcmsMessageException(msg, true);
            }
        }
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


    private final class LocalPUTRequest extends HttpServletRequestAdapter {

        public String getMethod() {
            return "PUT";
        }

        public String getRemoteUser() {
            return "system";
        }

        public Principal getUserPrincipal() {
            return new Principal() {
                public String getName() {
                    return getRemoteUser();
                }
            };
        }

        public ServletContext getServletContext() {
            return sctx.get();
        }

        public String getCharacterEncoding() {
            return "UTF-8";
        }
    }
}
