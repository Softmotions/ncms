package com.softmotions.ncms.asm.am;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import static com.softmotions.ncms.asm.am.AsmFileAttributeManagerSupport.translateClonedFile;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.commons.num.NumberUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.events.EnsureResizedImageJobEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.Medialine;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Singleton
public class AsmMedialineAM extends MBDAOSupport implements AsmAttributeManager {

    public static final int DEFAULT_IMG_WIDTH = 800;

    public static final int DEFAULT_IMG_THUMB_WIDTH = 96;

    private static final String[] TYPE = new String[]{"medialine"};

    private final ObjectMapper mapper;

    private final NcmsEventBus ebus;


    @Inject
    public AsmMedialineAM(ObjectMapper mapper,
                          NcmsEventBus ebus,
                          SqlSession sqlSession) {
        super(AsmMedialineAM.class, sqlSession);
        this.mapper = mapper;
        this.ebus = ebus;
    }

    @Override
    public boolean isUniqueAttribute() {
        return false;
    }

    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPE;
    }

    @Override
    @Transactional
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {

        String eval = attr.getEffectiveValue();
        if (StringUtils.isBlank(eval)) {
            eval = "[]";
        }
        ArrayNode items = (ArrayNode) mapper.readTree(eval);
        ArrayNode value = mapper.createArrayNode();
        List<Long> ids = new ArrayList<>();
        for (int i = 0, l = items.size(); i < l; ++i) {
            JsonNode n = items.get(i);
            if (n == null || !n.isNumber()) {
                continue;
            }
            ids.add(n.asLong());
        }
        if (ids.isEmpty()) {
            attr.setEffectiveValue("[]");
            return attr;
        }
        for (int i = 0, step = 128, to = Math.min(ids.size(), i + step);
             i < ids.size();
             i = to, to = Math.min(ids.size(), i + step)) {

            List<Map<String, Object>> rows = select("selectBasicMediaInfo", ids.subList(i, to));
            for (Map<String, Object> row : rows) {
                if (!CTypeUtils.isImageContentType((String) row.get("content_type"))) {
                    continue;
                }
                ArrayNode vnode = mapper.createArrayNode();
                vnode.add(NumberUtils.number2Int((Number) row.get("id"), 0));
                vnode.add((String) row.get("name"));
                vnode.add((String) row.get("description"));
                value.add(vnode);
            }
        }
        attr.setEffectiveValue(mapper.writeValueAsString(value));
        return attr;
    }

    @Override
    public Object[] fetchFTSData(AsmAttribute attr) {
        return null;
    }

    @Override
    @Transactional
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null) {
            return Collections.EMPTY_LIST;
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        AsmOptions opts = new AsmOptions(attr.getOptions());
        int width = opts.getInt("width", DEFAULT_IMG_WIDTH);
        int thumbWidth = opts.getInt("thumb_width", DEFAULT_IMG_THUMB_WIDTH);
        String value = attr.getEffectiveValue();
        if (StringUtils.isBlank(value)) {
            return Collections.EMPTY_LIST;
        }
        List<Long> ids = new ArrayList<>();
        JsonToken t;
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return Collections.EMPTY_LIST;
            }
            while ((t = parser.nextToken()) != null && t != JsonToken.END_ARRAY) {
                if (t != JsonToken.VALUE_NUMBER_INT) {
                    continue;
                }
                ids.add(parser.getLongValue());
            }
        } catch (IOException e) {
            throw new AsmRenderingException(e);
        }
        if (ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        Collection<Medialine> res = new ArrayList<>(ids.size());
        for (int i = 0, step = 128, to = Math.min(ids.size(), i + step);
             i < ids.size();
             i = to, to = Math.min(ids.size(), i + step)) {
            List<Map<String, Object>> rows = select("selectBasicMediaInfo", ids.subList(i, to));
            for (Map<String, Object> row : rows) {
                if (!CTypeUtils.isImageContentType((String) row.get("content_type"))) {
                    continue;
                }
                Image img = new Image(ctx);
                img.setId(NumberUtils.number2Long((Number) row.get("id"), 0L));
                img.setOptionsWidth(width);
                img.setSkipSmall(true);
                img.setResize(true);

                Image thumbnail = new Image(ctx);
                thumbnail.setId(img.getId());
                thumbnail.setOptionsWidth(thumbWidth);
                thumbnail.setSkipSmall(true);
                thumbnail.setResize(true);

                res.add(new Medialine(img, thumbnail, (String) row.get("description")));
            }
        }
        return res;
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "thumb_width");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        if (!val.isArray()) {
            return attr;
        }
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        AsmOptions opts = new AsmOptions(attr.getOptions());
        int width = opts.getInt("width", DEFAULT_IMG_WIDTH);
        int thumbWidth = opts.getInt("thumb_width", DEFAULT_IMG_THUMB_WIDTH);
        ArrayNode sval = mapper.createArrayNode();
        ArrayNode aval = (ArrayNode) val;

        for (int i = 0, l = aval.size(); i < l; ++i) {
            JsonNode node = aval.get(i);
            if (node == null || !node.isArray()) {
                continue;
            }
            ArrayNode a = (ArrayNode) node;
            if (a.size() < 1 || !a.get(0).isNumber()) {
                continue;
            }
            long fileId = a.get(0).asLong();
            if (fileId < 1) {
                continue;
            }
            ctx.registerFileDependency(attr, fileId);
            ebus.fireOnSuccessCommit(new EnsureResizedImageJobEvent(fileId, width, null, MediaRepository.RESIZE_SKIP_SMALL));
            ebus.fireOnSuccessCommit(new EnsureResizedImageJobEvent(fileId, thumbWidth, null, MediaRepository.RESIZE_SKIP_SMALL));
            sval.add(fileId);
        }
        if (sval.size() > 0) {
            attr.setEffectiveValue(mapper.writeValueAsString(sval));
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    @Override
    public AsmAttribute handleAssemblyCloned(AsmAttributeManagerContext ctx,
                                             AsmAttribute attr,
                                             Map<Long, Long> fmap) throws Exception {
        if (StringUtils.isBlank(attr.getEffectiveValue())) {
            return attr;
        }
        ArrayNode tval = mapper.createArrayNode();
        ArrayNode aval = (ArrayNode) mapper.readTree(attr.getEffectiveValue());
        for (int i = 0, l = aval.size(); i < l; ++i) {
            long fid = aval.get(0).asLong(0);
            if (fid == 0) {
                continue;
            }
            Long tid = translateClonedFile(fid, fmap);
            if (tid != null) {
                tval.add(tid);
                ctx.registerFileDependency(attr, tid);
            } else {
                tval.add(fid);
                ctx.registerFileDependency(attr, fid);
            }
        }
        if (tval.size() > 0) {
            attr.setEffectiveValue(mapper.writeValueAsString(tval));
        } else {
            attr.setEffectiveValue(null);
        }
        return attr;
    }

    @Override
    public void attributePersisted(AsmAttributeManagerContext ctx,
                                   AsmAttribute attr, JsonNode val, JsonNode opts) throws Exception {
    }
}
