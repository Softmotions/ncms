package ru.nsu.legacy;

import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Path("adm/legacy")
@Singleton
public class NSULegacyRS {

    private static final Logger log = LoggerFactory.getLogger(NSULegacyRS.class);

    private final Jongo jongo;

    private final GridFS gridFS;

    private final AsmDAO adao;

    private final MediaRepository mediaRepository;

    private final ObjectMapper mapper;

    /**
     * MongoDB model Item
     * <p/>
     * navItem : {
     * parent : dbref,
     * refpage : dbref,      //Referenced page for news pages
     * name : String,        //Page name
     * mdate : Date,         //Page modification time
     * cdate : Date,         //Page creation time
     * popupdate : Date      //Popup date for news pages
     * published : Boolean
     * type : int [0 - category, 1 - page, 2 - news page]
     * asm  : String, name of assembly on which page based
     * tags : [tagName]
     * attrs : {},
     * extra : {},
     * media : [fnames],
     * hierarchy : [nodeIds], //parent nodes ids (with out current node id)
     * cachedPath : String,
     * alias : String,        //Page alias
     * aliasFix : String      //User defined page alias, instead transliterated page name will be used
     * creator : String       //Page creator user ID
     * owner : String         //Page owner user ID
     * category : String      //News category, only for news pages
     * annotation : String    //News annotation, only for news pages
     * visit_count : Integer  //News visit count
     * access : {             //Access rights
     * <mode name> : [users]
     * }
     * }
     */

    // http://www.nsu.ru/exp/pe68bf74daa6d3b2c13000000
    // http://www.nsu.ru/exp/university/top-5-100
    // http://www.nsu.ru/exp/university/top-5-100/upravlenie_programmoi

    //4
    private static final Pattern L_GUID = Pattern.compile("(http|https)?://(www.)?nsu.ru/exp/(p|pp)([0-9a-f]{24})");

    //3
    private static final Pattern L_ALIAS = Pattern.compile("(http|https)?://(www.)?nsu.ru/exp/(.*)");

    @Inject
    public NSULegacyRS(Jongo jongo,
                       AsmDAO adao,
                       GridFS gridFS,
                       MediaRepository mediaRepository,
                       ObjectMapper mapper) {
        this.jongo = jongo;
        this.adao = adao;
        this.gridFS = gridFS;
        this.mediaRepository = mediaRepository;
        this.mapper = mapper;
    }

    @PUT
    @Path("/import/{id}")
    @Produces("application/json;charset=UTF-8")
    @Transactional
    public ObjectNode importMedia(@PathParam("id") Long id,
                                  ObjectNode importSpec) throws Exception {
        log.info("Importing nsu legacy media: " + importSpec + " id=" + id);
        ObjectNode ret = mapper.createObjectNode();
        JsonNode n = importSpec.get("url");
        if (n == null || !n.isTextual()) {
            throw new BadRequestException();
        }
        String url = n.asText();
        String guid = fetchNSUGuid(url);
        if (guid == null) {
            throw new BadRequestException("Invalid URL passed");
        }
        n = importSpec.get("wiki");
        boolean importWiki = (n != null && n.booleanValue());
        n = importSpec.get("links");
        boolean fixLinks = (n != null && n.booleanValue());
        ImportCtx ctx = new ImportCtx(id, url, guid, fixLinks, importWiki);

        log.info("Processing legacy page with guid: " + guid);
        String legacyAlias = fetchLegacyAlias(guid);
        log.info("Found legacy alias: " + legacyAlias);
        adao.asmUpdateAlias2(id, legacyAlias);

        String pFolder = mediaRepository.getPageLocalFolderPath(id);
        List<String> fnames =
                jongo.getCollection("navtree")
                        .findOne("{_id : #}", new ObjectId(guid))
                        .projection("{media : 1}")
                        .map(result -> (List<String>) result.get("media"));

        List<GridFSDBFile> files =
                (fnames != null && !fnames.isEmpty()) ? gridFS.find(jongo.createQuery("{filename : {$in : #}}", fnames).toDBObject()) :
                Collections.emptyList();
        for (GridFSDBFile f : files) {
            String fn = f.getFilename();
            if (fn.endsWith(".thumb") || fn.startsWith("pdfview")) {
                continue;
            }
            fn = fn.substring(guid.length());
            String target = pFolder + '/' + fn;
            log.info("Processing file: " + fn + " into: " + target);
            try (InputStream is = f.getInputStream()) {
                Long fid = mediaRepository.importFile(is, target, false);
                ctx.files.put(fn, fid);
            }
        }
        if (ctx.importWiki) {
            String wiki = importWiki(ctx);
            if (!StringUtils.isBlank(wiki)) {
                ret.put("wiki", wiki);
            }
        }
        adao.asmSetSysprop(id, "nsu.legacy.guid", guid);
        log.info("Import completed");
        return ret;
    }

    private String importWiki(ImportCtx ctx) throws IOException {
        DBCollection navtree = jongo.getCollection("navtree").getDBCollection();
        DBObject page = navtree.findOne(jongo.createQuery("{_id: #}", new ObjectId(ctx.guid)).toDBObject());
        DBObject extra = (DBObject) page.get("extra");
        if (extra == null) {
            return null;
        }
        String content = (String) extra.get("content");
        if (StringUtils.isBlank(content)) {
            return null;
        }
        return processWikiContent(content, ctx);
    }

    private String processWikiContent(String wiki, ImportCtx ctx) throws IOException {
        wiki = wiki.replaceAll("class='tableShort'", "class='short'");
        wiki = wiki.replaceAll("class='tableWide'", "class='wide'");
        if (!ctx.fixLinks) {
            return wiki;
        }
        String pFolder = mediaRepository.getPageLocalFolderPath(ctx.id);
        StringBuffer sb = new StringBuffer(wiki.length());
        Pattern p = Pattern.compile("\\[\\[(image|media):([0-9a-f]{24})([^\\|]+)((\\|[^\\|\\]]+)*)\\]\\]",
                                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Matcher m = p.matcher(wiki);
        while (m.find()) {
            String all = m.group(0);
            String type = m.group(1).toLowerCase();
            String guid = m.group(2);
            String fname = m.group(3);
            String spec = m.group(4);
            spec = spec.replaceAll("\\|link=$", "");
            if ("image".equals(type) && !spec.contains("none")) {
                spec = "|none" + spec;
            }
            Long fid = ctx.files.get(fname);
            if (fid == null) {
                GridFSDBFile f = gridFS.findOne(jongo.createQuery("{filename : #}", guid + fname).toDBObject());
                if (f != null) {
                    String fn = f.getFilename();
                    if (fn.endsWith(".thumb") || fn.startsWith("pdfview")) {
                        continue;
                    }
                    fn = fn.substring(guid.length());
                    String target = pFolder + '/' + fn;
                    log.info("Processing file: " + fn + " into: " + target);
                    try (InputStream is = f.getInputStream()) {
                        fid = mediaRepository.importFile(is, target, false);
                        ctx.files.put(fn, fid);
                    }
                }
                if (fid == null) {
                    log.warn("The file: " + fname + " is not imported");
                    m.appendReplacement(sb, all);
                    continue;
                }
            }
            StringBuilder nl = new StringBuilder(all.length());
            nl.append("[[");
            nl.append(StringUtils.capitalize(type));
            nl.append(":/");
            nl.append(fid);
            nl.append('/');
            nl.append(fname);
            nl.append(spec);
            nl.append("]]");
            m.appendReplacement(sb, nl.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String fetchLegacyAlias(String guid) throws Exception {
        String alias = jongo.getCollection("navtree").findOne("{_id: #}", new ObjectId(guid)).projection("{alias:1}")
                .map(res -> res.get("alias").toString());
        while (alias != null && alias.length() > 0 && alias.charAt(0) == '/') {
            alias = alias.substring(1);
        }
        return alias;
    }

    private String fetchNSUGuid(String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String guid = null;
        Matcher m = L_GUID.matcher(url);
        if (m.matches()) {
            return m.group(4);
        }
        m = L_ALIAS.matcher(url);
        if (m.matches()) {
            String alias = m.group(3);
            if (alias == null) {
                return null;
            }
            alias = '/' + alias;
            log.info("Find guid by alias: " + alias);
            MongoCollection navtree = jongo.getCollection("navtree");
            guid = navtree.findOne("{alias: #}", alias).projection("{_id : 1}").map(res -> {
                return (res.get("_id").toString());
            });
        }
        return guid;
    }

    private static class ImportCtx {
        private final Long id;
        private final String url;
        private final String guid;
        private final boolean fixLinks;
        private final boolean importWiki;
        private final Map<String, Long> files = new HashMap<>();

        private ImportCtx(Long id, String url, String guid, boolean fixLinks, boolean importWiki) {
            this.id = id;
            this.url = url;
            this.guid = guid;
            this.fixLinks = fixLinks;
            this.importWiki = importWiki;
        }
    }
}
