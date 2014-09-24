package ru.nsu.pressa;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRendererContextFactory;
import com.softmotions.ncms.media.MediaReader;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.mhttl.RichRef;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Path("pressa")
@Produces("application/json")
@Singleton
public class NSUPressaRS {

    private static final Logger log = LoggerFactory.getLogger(NSUPressaRS.class);

    private final ObjectMapper mapper;

    private final NcmsEnvironment env;

    private final AsmDAO adao;

    private final MediaReader mediaReader;

    private final NcmsMessages messages;

    private final AsmRendererContextFactory rendererContextFactory;


    @Inject
    public NSUPressaRS(ObjectMapper mapper,
                       NcmsEnvironment env,
                       AsmDAO adao,
                       MediaReader mediaReader,
                       NcmsMessages messages,
                       AsmRendererContextFactory rendererContextFactory) {
        this.mapper = mapper;
        this.env = env;
        this.adao = adao;
        this.mediaReader = mediaReader;
        this.messages = messages;
        this.rendererContextFactory = rendererContextFactory;
    }

    @GET
    @Path("/issues/{journal}")
    @Produces("application/json;charset=UTF-8")
    @Transactional
    public ArrayNode issues(@PathParam("journal") String journal,
                            @Context HttpServletRequest req,
                            @Context HttpServletResponse resp) {

        List<HierarchicalConfiguration> clist = env.xcfg().configurationsAt("content.journals." + journal);
        if (clist.size() != 1) {
            throw new NotFoundException(journal);
        }
        HierarchicalConfiguration scfg = clist.get(0);
        String aname = scfg.getString("issues-tree-attribute", "issues");
        AsmDAO.PageCriteria pc =
                adao.newPageCriteria()
                        .withAlias(journal)
                        .withAttributes(aname)
                        .withLargeAttrValues();
        Asm p = pc.selectOneAsm();
        AsmAttribute attr = p.getAttribute(aname);
        if (attr == null) {
            throw new NotFoundException(journal + "#" + aname);
        }
        AsmRendererContext rctx = rendererContextFactory.createStandalone(req, resp, p);
        Object val = rctx.renderAttribute(aname, Collections.EMPTY_MAP);
        if (!(val instanceof Tree)) {
            throw new RuntimeException("The '" + aname + "' attribute value is not a Tree object");
        }
        return buildResponse((Tree) val, req);
    }

    private ArrayNode buildResponse(Tree tree, HttpServletRequest req) {
        ArrayNode anodes = mapper.createArrayNode();
        if (!tree.isHasChildren()) {
            return anodes;
        }
        List<ObjectNode> snodes = new ArrayList<>();
        for (final Tree fld : tree) {
            if (!"folder".equals(fld.getType())) {
                continue;
            }
            int year;
            try {
                year = Integer.parseInt(fld.getName());
            } catch (NumberFormatException ignored) {
                continue;
            }
            for (final Tree inode : fld) {
                if (!"file".equals(inode.getType()) ||
                    inode.getRichRef() == null ||
                    inode.getRichRef().getImage() == null ||
                    inode.getRichRef().getLink() == null) {
                    continue;
                }
                RichRef rr = inode.getRichRef();
                Long fid = env.getFileIdByResourceSpec(rr.getRawLink());
                if (fid == null) {
                    continue;
                }
                MediaResource mr = mediaReader.findMediaResource(fid, messages.getLocale(req));
                if (mr == null) {
                    continue;
                }
                ObjectNode item = mapper.createObjectNode();
                item.put("year", year);
                item.put("name", rr.getName());
                item.put("description", rr.getDescription());
                item.put("mdate", mr.getLastModified());
                item.put("icon", env.getAbsoluteLink(req, rr.getImage().getLink()));
                item.put("pdf", env.getAbsoluteLink(req, rr.getLink()));
                snodes.add(item);
            }
        }
        Collections.sort(snodes, new Comparator<ObjectNode>() {
            public int compare(ObjectNode o1, ObjectNode o2) {
                return Long.compare(o1.get("mdate").asLong(), o2.get("mdate").asLong());
            }
        });
        for (ObjectNode n : snodes) {
            anodes.add(n);
        }
        return anodes;
    }
}
