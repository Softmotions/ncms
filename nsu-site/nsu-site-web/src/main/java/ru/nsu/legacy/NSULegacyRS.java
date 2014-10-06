package ru.nsu.legacy;

import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.jaxrs.BadRequestException;
import com.softmotions.ncms.media.MediaRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/legacy")
@Singleton
public class NSULegacyRS {

    private static final Logger log = LoggerFactory.getLogger(NSULegacyRS.class);

    private final Jongo jongo;

    private final GridFS gridFS;

    private final AsmDAO adao;

    private final MediaRepository mediaRepository;


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
                       MediaRepository mediaRepository) {
        this.jongo = jongo;
        this.adao = adao;
        this.gridFS = gridFS;
        this.mediaRepository = mediaRepository;
    }

    @PUT
    @Path("/import/{id}")
    @Transactional
    public void importMedia(@PathParam("id") Long id,
                            ObjectNode importSpec) throws Exception {
        log.info("Importing nsu legacy media: " + importSpec + " id=" + id);
        JsonNode n = importSpec.get("url");
        if (n == null || !n.isTextual()) {
            throw new BadRequestException();
        }
        String guid = fetchNSUGuid(n.asText());
        if (guid == null) {
            throw new BadRequestException("Invalid url passed");
        }
        log.info("Processing legacy page with guid: " + guid);
        String legacyAlias = fetchLegacyAlias(guid);
        log.info("Found legacy alias: " + legacyAlias);
        adao.asmUpdateAlias2(id, legacyAlias);

        Pattern fnRE = Pattern.compile(guid + "(.*)");
        String pFolder = mediaRepository.getPageLocalFolderPath(id);
        for (GridFSDBFile f : gridFS.find(jongo.createQuery("{filename : #}", fnRE).toDBObject())) {
            String fn = f.getFilename();
            if (fn.endsWith(".thumb") || fn.startsWith("pdfview")) {
                continue;
            }
            fn = fn.substring(guid.length());
            String target = pFolder + '/' + fn;
            log.info("Processing file: " + fn + " into: " + target);
            try (InputStream is = f.getInputStream()) {
                mediaRepository.importFile(is, target, false);
            }
        }
        log.info("Import completed");
    }


    private String fetchLegacyAlias(String guid) throws Exception {
        return jongo.getCollection("navtree").findOne("{_id: #}", new ObjectId(guid)).projection("{alias:1}")
                .map(res -> res.get("alias").toString());

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
}
