package com.softmotions.ncms.asm.am;

import com.softmotions.commons.ctype.CTypeUtils;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.commons.num.NumberUtils;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.events.EnsureResizedImageJobEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.Medialine;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmMedialineAM extends MBDAOSupport implements AsmAttributeManager {

    private static final Logger log = LoggerFactory.getLogger(AsmMedialineAM.class);

    public static final int DEFAULT_IMG_WIDTH = 800;

    public static final int DEFAULT_IMG_THUMB_WIDTH = 64;

    private static final String[] TYPE = new String[]{"medialine"};

    private final ObjectMapper mapper;

    private final NcmsEventBus ebus;

    @Inject
    public AsmMedialineAM(ObjectMapper mapper,
                          NcmsEventBus ebus,
                          SqlSession sqlSession) {
        super(AsmMedialineAM.class.getName(), sqlSession);
        this.mapper = mapper;
        this.ebus = ebus;
    }

    public String[] getSupportedAttributeTypes() {
        return TYPE;
    }

    @Transactional
    public AsmAttribute prepareGUIAttribute(Asm page, Asm template, AsmAttribute tmplAttr, AsmAttribute attr) throws Exception {
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
        attr.setEffectiveValue(value.toString());
        return attr;
    }

    @Transactional
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname, Map<String, String> options) throws AsmRenderingException {
        Collection<Medialine> res = new ArrayList<>();
        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        AsmOptions opts = new AsmOptions(attr.getOptions());
        int width = opts.getInt("width", DEFAULT_IMG_WIDTH);
        int thumbWidth = opts.getInt("thumb_width", DEFAULT_IMG_THUMB_WIDTH);

        String value = attr.getEffectiveValue();
        if (StringUtils.isBlank(value)) {
            return res;
        }
        JsonToken t;
        try (JsonParser parser = mapper.getFactory().createParser(value)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                return Collections.EMPTY_LIST;
            }
            while ((t = parser.nextToken()) != null && t != JsonToken.END_ARRAY) {
                if (t != JsonToken.VALUE_NUMBER_INT) {
                    continue;
                }
                Image img = new Image(ctx);
                img.setId(parser.getLongValue());
                img.setOptionsWidth(width);
                img.setSkipSmall(true);


                Image thumbnail = new Image(ctx);
                img.setId(parser.getLongValue());
                img.setOptionsWidth(thumbWidth);
                img.setSkipSmall(true);

                res.add(new Medialine(img, thumbnail));
            }
        } catch (IOException e) {
            throw new AsmRenderingException(e);
        }
        return res;
    }

    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions asmOpts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, asmOpts,
                                        "width", "thumb_width");
        attr.setOptions(asmOpts.toString());
        return attr;
    }

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
            ctx.registerMediaFileDependency(attr, fileId);
            ebus.fireOnSuccessCommit(new EnsureResizedImageJobEvent(fileId, width, null, MediaRepository.RESIZE_SKIP_SMALL));
            ebus.fireOnSuccessCommit(new EnsureResizedImageJobEvent(fileId, thumbWidth, null, MediaRepository.RESIZE_SKIP_SMALL));
            sval.add(fileId);
        }
        attr.setEffectiveValue(sval.toString());
        return attr;
    }

    public void attributePersisted(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {

    }
}
