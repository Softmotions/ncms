package com.softmotions.ncms.media;

import com.softmotions.commons.io.DirUtils;
import com.softmotions.commons.weboot.mb.MBCriteriaQuery;
import com.softmotions.commons.weboot.mb.MBDAOSupport;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.io.MimeTypeDetector;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.jaxrs.NcmsMessageException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.tika.mime.MediaType;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Media files manager rest service.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("/media")
@Produces("application/json")
public class MediaRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(MediaRS.class);

    private static final int MB = 1048576;

    private static final File[] EMPTY_FILES_ARRAY = new File[0];

    private final NcmsConfiguration cfg;

    private final File basedir;

    private final RWLocksLRUCache locksCache;

    private final ObjectMapper mapper;

    private final NcmsMessages message;

    @Inject
    public MediaRS(NcmsConfiguration cfg,
                   SqlSession sess,
                   ObjectMapper mapper,
                   NcmsMessages message) throws IOException {

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
    }

    /**
     * Save uploaded file.
     * <p/>
     * Example:
     * curl --upload-file ./myfile.txt http://localhost:8080/ncms/rs/media/file/foo/bar/test.txt
     */
    @PUT
    @Consumes("application/octet-stream")
    @Path("/file/{folder:.*}/{name}")
    @Transactional
    public void put(@PathParam("folder") String folder,
                    @PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws IOException {
        _put("/" + folder, name, req, in);
    }

    @PUT
    @Consumes("application/octet-stream")
    @Path("/file/{name}")
    @Transactional
    public void put(@PathParam("name") String name,
                    @Context HttpServletRequest req,
                    InputStream in) throws IOException {
        _put("/", name, req, in);
    }

    @GET
    @Path("/files/{folder:.*}")
    @Transactional
    public JsonNode listFiles(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws IOException {
        return _list("/" + folder, FileFileFilter.FILE, req);
    }

    @GET
    @Path("/files")
    @Transactional
    public JsonNode listFiles(@Context HttpServletRequest req) throws IOException {
        return _list("/", FileFileFilter.FILE, req);
    }

    @GET
    @Path("/folders/{folder:.*}")
    @Transactional
    public JsonNode listFolders(@PathParam("folder") String folder,
                                @Context HttpServletRequest req) throws IOException {
        return _list("/" + folder, DirectoryFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/folders")
    @Transactional
    public JsonNode listFolders(@Context HttpServletRequest req) throws IOException {
        return _list("/", DirectoryFileFilter.INSTANCE, req);
    }


    @GET
    @Path("/all/{folder:.*}")
    @Transactional
    public JsonNode listAll(@PathParam("folder") String folder,
                            @Context HttpServletRequest req) throws IOException {
        return _list("/" + folder, TrueFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/all")
    @Transactional
    public JsonNode listAll(@Context HttpServletRequest req) throws IOException {
        return _list("/", TrueFileFilter.INSTANCE, req);
    }

    @GET
    @Path("/select")
    @Transactional
    public JsonNode select(@Context HttpServletRequest req) {
        ArrayNode res = mapper.createArrayNode();
        List<Map<String, ?>> rows = select("select", createSelectQ(req));
        for (Map<String, ?> row : rows) {
            ObjectNode on = res.addObject();
            on.put("id", ((Number) row.get("id")).longValue());
            on.put("name", (String) row.get("name"));
            on.put("folder", (String) row.get("folder"));
            on.put("content_type", (String) row.get("content_type"));
            if (row.get("content_length") != null) {
                on.put("content_length", ((Number) row.get("content_length")).longValue());
            }
        }
        return res;
    }

    @GET
    @Path("/select/count")
    @Produces("text/plain")
    @Transactional
    public Integer selectCount(@Context HttpServletRequest req) {
        return selectOne("count", createSelectQ(req));
    }


    @PUT
    @Path("/folder/{folder:.*}")
    public JsonNode newFolder(@PathParam("folder") String folder,
                              @Context HttpServletRequest req) throws Exception {
        File f = new File(basedir, folder);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new IOException("Cannot create dir: " + folder);
            }
        }

        String name = f.getName();
        String dirname = FilenameUtils.getPath(folder);
        if (StringUtils.isBlank(dirname)) {
            dirname = "/";
        }

        Number id = selectOne("selectEntityIdByPath",
                              "folder", dirname,
                              "name", name);
        if (id == null) {
            insert("insertEntity",
                   "folder", dirname,
                   "name", name,
                   "status", 1,
                   "mdate", new Timestamp(System.currentTimeMillis()));
        } else {
            throw new NcmsMessageException(message.get("ncms.mmgr.folder.exists", req, folder), true);
        }

        return mapper.createObjectNode()
                .put("label", name)
                .put("status", 1);
    }


    private MBCriteriaQuery createSelectQ(HttpServletRequest req) {
        MBCriteriaQuery cq = createCriteria();
        String val = req.getParameter("firstRow");
        if (val != null) {
            Integer frow = Integer.valueOf(val);
            cq.offset(frow);
            val = req.getParameter("lastRow");
            if (val != null) {
                Integer lrow = Integer.valueOf(val);
                cq.limit(Math.abs(frow - lrow) + 1);
            }
        }
        val = req.getParameter("status");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("status", Integer.parseInt(val));
        }
        val = req.getParameter("folder");
        if (!StringUtils.isBlank(val)) {
            cq.withParam("folder", val);
        }
        val = req.getParameter("stext");
        if (!StringUtils.isBlank(val)) {
            val = val.toLowerCase(message.getLocale(req)).trim() + '%';
            cq.withParam("name", val);
        }
        val = req.getParameter("sortAsc");
        if (!StringUtils.isBlank(val)) {
            cq.orderBy("e." + val).asc();
        }
        val = req.getParameter("sortDesc");
        if (!StringUtils.isBlank(val)) {
            cq.orderBy("e." + val).desc();
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
     *       {"label" : file name, "status" : 1 if it is folder 0 otherwise },
     *       ...
     *     ]
     * </pre>
     */
    private JsonNode _list(String folder,
                           FileFilter filter,
                           HttpServletRequest req) throws IOException {

        ArrayNode res = mapper.createArrayNode();
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
        for (int i = 0, l = files.length; i < l; ++i) {
            res.addObject()
                    .put("label", files[i].getName())
                    .put("status", files[i].isDirectory() ? 1 : 0);
        }
        return res;
    }

    private void _put(String folder,
                      String name,
                      HttpServletRequest req,
                      InputStream in) throws IOException {
        if (folder.contains("..")) {
            throw new BadRequestException(folder);
        }

        //Used in order to proper ctype detection by TIKA (mark/reset are supported by BufferedInputStream)
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
        ReentrantReadWriteLock rwlock = null;
        try {
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
            rwlock = acquirePathRWLock(folder, true); //lock the folder
            File dir = new File(basedir, folder);
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
            Number id = selectOne("selectEntityIdByPath",
                                  "folder", folder,
                                  "name", name);
            if (id == null) {
                insert("insertEntity",
                       "folder", folder,
                       "name", name,
                       "status", 0,
                       "content_type", mtype.toString(),
                       "put_content_type", req.getContentType(),
                       "content_length", actualLength,
                       "mdate", new Timestamp(System.currentTimeMillis()));
            } else {
                update("updateEntity",
                       "id", id,
                       "content_type", mtype.toString(),
                       "content_length", actualLength,
                       "mdate", new Timestamp(System.currentTimeMillis()));
            }
        } finally {
            if (rwlock != null) {
                rwlock.writeLock().unlock();
            }
            if (us.getFile() != null) {
                us.getFile().delete();
            }
            bis.close();
        }
    }


    private ReentrantReadWriteLock acquirePathRWLock(String path, boolean acquireWrite) {
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
                    //Locked rwlock is not changed we are save to use it until it remains locked
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


    private static class RWLocksLRUCache extends LRUMap {

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


    private static class FileUploadStream extends DeferredFileOutputStream {

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
}
