package ru.nsu.legacy;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageRS;
import com.softmotions.ncms.asm.PageSecurityService;
import com.softmotions.ncms.asm.am.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.am.AsmWikiAM;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.weboot.lifecycle.Start;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.jongo.bson.DBObjectUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
public class NSULegacyRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(NSULegacyRS.class);

    private final Jongo jongo;

    private final GridFS gridFS;

    private final AsmDAO adao;

    private final MediaRepository mediaRepository;

    private final ObjectMapper mapper;

    private final NcmsEnvironment env;

    private final PageRS pageRS;

    private final PageSecurityService pageSecurityService;

    private final NcmsEventBus ebus;

    private final Injector injector;

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
                       ObjectMapper mapper,
                       NcmsEnvironment env,
                       PageRS pageRS,
                       SqlSession sess,
                       PageSecurityService pageSecurityService,
                       NcmsEventBus ebus,
                       Injector injector) {

        super(NSULegacyRS.class, sess);

        this.jongo = jongo;
        this.adao = adao;
        this.gridFS = gridFS;
        this.mediaRepository = mediaRepository;
        this.mapper = mapper;
        this.env = env;
        this.pageRS = pageRS;
        this.pageSecurityService = pageSecurityService;
        this.ebus = ebus;
        this.injector = injector;
    }


    @Start(order = Integer.MAX_VALUE, parallel = true)
    public void importAllNews() {
        for (HierarchicalConfiguration cfg : env.xcfg().configurationsAt("nsu.import-news.branch")) {
            String root = cfg.getString("[@root]");
            Long attach = cfg.getLong("[@attach]", null);
            String template = cfg.getString("[@template]");
            if (StringUtils.isBlank(root) ||
                attach == null ||
                StringUtils.isBlank(template)) {
                log.error("Invalid import news branch configuration");
                continue;
            }
            String lguid = fetchNSUGuid(root);
            if (lguid == null) {
                log.error("Invalid import news branch configuration, root not found: " + root);
                continue;
            }
            ImportNewsCtx ctx = new ImportNewsCtx(attach, root, lguid, true, true);
            ctx.cfg = cfg;
            ctx.template = adao.asmSelectByName(template);
            if (template == null) {
                log.error("Unable to find assembly template: null");
                continue;
            }
            try {
                importNewsBranch(ctx);
            } catch (Exception e) {
                log.error("Failed to import news branch from: " + root + " into " + attach, e);
            }
        }
        log.info("NEWS IMPORT DONE");
    }


    public void importNewsBranch(ImportNewsCtx ctx) throws Exception {
        //icon: attrs.image.value : String
        //annotation: annotation
        //categoty: category
        //wiki: extra.content
        //files: media
        MongoCollection navtree = jongo.getCollection("navtree");
        DBRef refpage = new DBRef("navtree", new ObjectId(ctx.lguid));
        MongoCursor<DBObject> cur = navtree
                .find("{type : 2, refpage:#}", refpage)
                .sort("{cdate : 1}")
                .map(res -> {
                    return res;
                });

        String lguid = ctx.lguid;
        long attach = ctx.id;
        int c = 0;
        while (cur.hasNext()) {
            log.info("NEWS: " + (++c));
            DBObject pdbo = cur.next();
            try {
                importNewsNode(ctx, pdbo);
            } catch (LegacyImportAbortException | IIOException e) {
                log.error("Import aborted for: " + pdbo.get("_id") + " " + pdbo.get("alias") +
                          " MSG: " + e.getMessage() + "\n" + pdbo);
                //throw e;
            } finally {
                ctx.files = new HashMap<>();
                ctx.lastInWikiResources = new ArrayList<>();
            }
            ctx.id = attach;
            ctx.lguid = lguid;
        }
    }

    @Transactional
    public void importNewsNode(ImportNewsCtx ctx, DBObject p) throws Exception, LegacyImportAbortException {
        String lwiki = DBObjectUtils.get(p, "extra.content");
        if (StringUtils.isBlank(lwiki)) {
            throw new LegacyImportAbortException("No content wiki attached to page: " + p.get("_id"));
        }
        String name = (String) p.get("name");

        log.info("Importing: " + p.get("_id") + " '" + name + '\'');
        if (name == null) {
            throw new LegacyImportAbortException("Aborting");
        }
        long parentId = ctx.id;
        ctx.lguid = String.valueOf(p.get("_id"));

        String guid = ctx.lguid + "00000000";
        if (guid.length() != 32) {
            log.error("Invalid page guid: " + guid.length());
        }
        //log.info("Actual guid: " + guid);

        Number cdate = (Number) p.get("cdate");
        Number mdate = (Number) p.get("mdate");
        String alias = (String) p.get("alias");
        if (alias != null && alias.length() > 0 && alias.charAt(0) == '/') {
            alias = alias.substring(1);
        }
        Boolean published = (Boolean) p.get("published");
        if (published == null) {
            published = Boolean.TRUE;
        }

        int uc = update("mergeNewPage",
                        "guid", guid,
                        "name", name,
                        "description", name,
                        "type", "news.page",
                        "user", "system",
                        "nav_parent_id", parentId,
                        "nav_cached_path", pageRS.getPageIDsPath(parentId),
                        "recursive_acl", null,
                        "cdate", (cdate != null) ? new Date(cdate.longValue()) : new Date(),
                        "nav_alias2", alias,
                        "published", published.booleanValue() ? 1 : 0);

        if (uc < 1) {
            throw new LegacyImportAbortException("Aborting");
        }
        Asm asm = adao.asmSelectByName(guid);
        if (asm == null) {
            throw new LegacyImportAbortException("No asm with name: " + guid);
        }
        ctx.asm = asm;
        ctx.id = asm.getId();

        //Setup news template
        adao.asmRemoveAllParents(asm.getId());
        adao.asmSetParent(asm.getId(), ctx.template.getId());


        AsmAttribute attr = new AsmAttribute("annotation", "string", DBObjectUtils.get(p, "annotation"));
        attr.setAsmId(asm.getId());
        attr.setLabel("Аннотация");
        attr.setRequired(true);
        adao.asmUpsertAttribute(attr);

        importFiles(ctx);

        String image = DBObjectUtils.get(p, "attrs.image.value");
        if (image != null) {
            image = image.substring(ctx.lguid.length());
            String ipath = mediaRepository.getPageLocalFolderPath(asm.getId()) + '/' + image;
            if (mediaRepository.ensureResizedImage(ipath, 81, 68, MediaRepository.RESIZE_COVER_AREA) != null) {
                MediaResource mres = mediaRepository.findMediaResource(ipath, null);
                AsmAttributeManagerContext actx = new AsmAttributeManagerContext(null, null, pageSecurityService, sess);
                attr = adao.asmAttributeByName(asm.getId(), "icon");
                if (attr == null) {
                    attr = new AsmAttribute();
                }
                attr.setName("icon");
                attr.setLabel("Иконка");
                attr.setType("image");
                attr.setOptions("restrict=false,width=81,height=68,skipSmall=false,cover=true,resize=false");
                attr.setEffectiveValue(String.format(
                        "{\"id\":%d,\"options\":{\"restrict\":\"false\",\"width\":81,\"height\":null,\"skipSmall\":\"false\",\"cover\":\"true\",\"resize\":\"false\"},\"path\":\"%s\"}"
                        , mres.getId(), StringEscapeUtils.escapeJson(ipath)));
                attr.setAsmId(asm.getId());

                adao.asmUpsertAttribute(attr);
                if (attr.getId() == null) {
                    Number gid = selectOne("prevAttrID");
                    if (gid != null) {
                        attr.setId(gid.longValue());
                    }
                }
                actx.registerMediaFileDependency(attr, mres.getId());
                actx.flushFileDeps();
            }
        }

        String category = (String) p.get("category");
        if (category != null) {
            ArrayNode sc0 = mapper.createArrayNode();
            ArrayNode sc1 = mapper.createArrayNode();
            sc0.add(sc1);
            sc1.add(true).add(category).add(category);
            attr = adao.asmAttributeByName(asm.getId(), "icon");
            if (attr == null) {
                attr = new AsmAttribute();
            }
            attr.setAsmId(asm.getId());
            attr.setName("subcategory");
            attr.setType("select");
            attr.setLabel("Подкатегория новости");
            attr.setEffectiveValue(sc0.toString());
            adao.asmUpsertAttribute(attr);
        }


        //Process wiki
        //log.info("lwiki=" + lwiki);
        lwiki = importWiki(ctx);
        if (StringUtils.isBlank(lwiki)) {
            throw new LegacyImportAbortException("No wiki data");
        }

        attr = adao.asmAttributeByName(asm.getId(), "content");
        if (attr == null) {
            attr = new AsmAttribute();
        }
        attr.setAsmId(asm.getId());
        attr.setName("content");
        attr.setType("wiki");
        attr.setLabel("Содержимое новости");
        attr.setOptions("markup=mediawiki");

        AsmWikiAM wikiAm = injector.getInstance(AsmWikiAM.class);
        AsmAttributeManagerContext actx = new AsmAttributeManagerContext(null, null, pageSecurityService, sess);
        ObjectNode wikiAmSpec = mapper.createObjectNode();
        wikiAmSpec.put("markup", "mediawiki");
        wikiAmSpec.put("value", lwiki);
        attr = wikiAm.applyAttributeValue(actx, attr, wikiAmSpec);
        adao.asmUpsertAttribute(attr);
        if (attr.getId() == null) {
            Number gid = selectOne("prevAttrID");
            if (gid != null) {
                attr.setId(gid.longValue());
            }
        }
        actx.flushFileDeps();
        actx.flushPageDeps();

        //Finishing
        adao.asmSetSysprop(asm.getId(), "nsu.legacy.guid", ctx.lguid);
        ebus.fireOnSuccessCommit(new AsmModifiedEvent(this, asm.getId()));
    }

    @PUT
    @Path("/import/{id}")
    @Produces("application/json;charset=UTF-8")
    @Transactional
    public ObjectNode importMedia(@PathParam("id") Long id,
                                  ObjectNode importSpec) throws Exception {
        //log.info("Importing nsu legacy media: " + importSpec + " id=" + id);
        ObjectNode ret = mapper.createObjectNode();
        JsonNode n = importSpec.get("url");
        if (n == null || !n.isTextual()) {
            throw new BadRequestException();
        }
        String url = n.asText();
        String lguid = fetchNSUGuid(url);
        if (lguid == null) {
            throw new BadRequestException("Invalid URL passed");
        }
        n = importSpec.get("wiki");
        boolean importWiki = (n != null && n.booleanValue());
        n = importSpec.get("links");
        boolean fixLinks = (n != null && n.booleanValue());
        ImportCtx ctx = new ImportCtx(id, url, lguid, fixLinks, importWiki);

        //log.info("Processing legacy page with guid: " + lguid);
        String legacyAlias = fetchLegacyAlias(lguid);
        //log.info("Found legacy alias: " + legacyAlias);
        adao.asmUpdateAlias2(id, legacyAlias);

        importFiles(ctx);

        if (ctx.importWiki) {
            String wiki = importWiki(ctx);
            if (!StringUtils.isBlank(wiki)) {
                ret.put("wiki", wiki);
            }
        }
        adao.asmSetSysprop(id, "nsu.legacy.guid", lguid);
        log.info("Import completed");
        return ret;
    }


    private void importFiles(ImportCtx ctx) throws Exception {
        String pFolder = mediaRepository.getPageLocalFolderPath(ctx.id);
        List<String> fnames =
                jongo.getCollection("navtree")
                        .findOne("{_id : #}", new ObjectId(ctx.lguid))
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
            fn = fn.substring(ctx.lguid.length());
            String target = pFolder + '/' + fn;
            //log.info("Processing file: " + fn + " into: " + target);
            try (InputStream is = f.getInputStream()) {
                Long fid = mediaRepository.importFile(is, target, false);
                ctx.files.put(fn, fid);
            } catch (Exception e) {
                log.error("File was not imported", e);
            }
        }
    }


    private String importWiki(ImportCtx ctx) throws IOException {
        DBCollection navtree = jongo.getCollection("navtree").getDBCollection();
        DBObject page = navtree.findOne(jongo.createQuery("{_id: #}", new ObjectId(ctx.lguid)).toDBObject());
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
        Pattern p = Pattern.compile("\\[\\[(image|media):([0-9a-f]{24})([^\\|\\]]+)((\\|[^\\|\\]]+)*)\\]\\]",
                                    Pattern.CASE_INSENSITIVE);

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
                    //log.info("Processing file: " + fn + " into: " + target);
                    try (InputStream is = f.getInputStream()) {
                        fid = mediaRepository.importFile(is, target, false);
                        ctx.files.put(fn, fid);
                    } catch (Exception e) {
                        log.error("File was not imported", e);
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

        wiki = sb.toString();
        sb = new StringBuffer(wiki.length());

        //Process page links
        Pattern pp = Pattern.compile("\\[\\[page:\\s*([0-9a-f]{24})(\\s*\\|(.*))?\\]\\]",
                                     Pattern.CASE_INSENSITIVE);
        m = pp.matcher(wiki);
        while (m.find()) {
            String full = m.group();
            String guid = m.group(1);
            String rest = m.group(2);
            if (guid == null) {
                m.appendReplacement(sb, full);
                continue;
            }
            List<Map> rows = select("asmByLegacyGUID", guid);
            if (!rows.isEmpty()) {
                Map row = rows.iterator().next();
                log.info("Wiki pageref fixed by legacy guid: " + guid);
                m.appendReplacement(sb, "[[page:" + row.get("name") + ((rest != null) ? rest : "") + "]]");
                continue;
            }
            String alias = jongo.getCollection("navtree").findOne("{_id: #}", new ObjectId(guid)).projection("{alias:1}")
                    .map(res -> res.get("alias").toString());
            if (alias != null) {
                rows = select("asmByNavAlias2", alias);
                if (!rows.isEmpty()) {
                    Map row = rows.iterator().next();
                    log.info("Wiki pageref fixed by nav_alias2: " + alias);
                    m.appendReplacement(sb, "[[page:" + row.get("name") + ((rest != null) ? rest : "") + "]]");
                    continue;
                }
            }
            m.appendReplacement(sb, full);
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

    private String fetchNSUGuid(String url) {
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
            //log.info("Find guid by alias: " + alias);
            MongoCollection navtree = jongo.getCollection("navtree");
            guid = navtree.findOne("{alias: #}", alias).projection("{_id : 1}").map(res -> (res.get("_id").toString()));
        }
        return guid;
    }

    private static class ImportCtx {
        Long id;
        String url;
        String lguid;
        boolean fixLinks;
        boolean importWiki;
        HierarchicalConfiguration cfg;
        Asm template;
        Asm asm;
        Map<String, Long> files = new HashMap<>();
        List<MediaResource> lastInWikiResources = new ArrayList<>();


        ImportCtx(Long id, String url, String lguid, boolean fixLinks, boolean importWiki) {
            this.id = id;
            this.url = url;
            this.lguid = lguid;
            this.fixLinks = fixLinks;
            this.importWiki = importWiki;

        }
    }


    private static class ImportNewsCtx extends ImportCtx {

        String navAlias2;

        private ImportNewsCtx(Long id, String url, String guid, boolean fixLinks, boolean importWiki) {
            super(id, url, guid, fixLinks, importWiki);
        }
    }


    public static class LegacyImportAbortException extends Exception {
        public LegacyImportAbortException(String message) {
            super(message);
        }
    }
}
