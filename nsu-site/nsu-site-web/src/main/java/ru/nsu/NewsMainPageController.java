package ru.nsu;

import com.softmotions.commons.cont.Pair;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.render.AsmController;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.media.MediaResource;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.RichRef;
import com.softmotions.ncms.mhttl.Tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main page for news {@link com.softmotions.ncms.asm.render.AsmController}
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Singleton
public class NewsMainPageController implements AsmController {

    private static final Logger log = LoggerFactory.getLogger(NewsMainPageController.class);

    private static final int MAX_TOTAL_NEWS_LIMIT = 16;

    private static final int CACHED_LINE_LIFETIME = 1000 * 60 * 60;  // 1 hour

    private static final int MAX_PHOTOLINE_ITEMS = 30;

    private static final int MAX_PHOTOLINE_ITEMS_PER_ASM = 1;

    private final AsmDAO adao;

    private final SubnodeConfiguration npCfg;

    private final PageService pageService;

    private final MediaRepository mediaRepository;

    private final ObjectMapper mapper;

    private final NcmsMessages messages;

    private volatile PhotolineSlot cachedPhotoline;


    @Inject
    public NewsMainPageController(AsmDAO adao,
                                  MediaRepository mediaRepository,
                                  ObjectMapper mapper,
                                  NcmsMessages messages,
                                  PageService pageService,
                                  NcmsEnvironment env) {
        this.adao = adao;
        this.mediaRepository = mediaRepository;
        this.mapper = mapper;
        this.messages = messages;
        this.pageService = pageService;
        this.npCfg = env.xcfg().configurationAt("content.newsmain");
    }

    @Transactional
    public boolean execute(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();
        HttpServletResponse resp = ctx.getServletResponse();

        String action = StringUtils.trimToEmpty(req.getParameter("mnc.action"));
        String partialResource;
        switch (action) {
            case "fetchMoreNews":
                addNews(ctx);
                partialResource = "/site/cores/inc/news_main_news.httl";
                break;

            default:
                partialResource = null;
        }

        if (partialResource != null) {
            resp.setContentType("text/html");
            ctx.getRenderer().renderTemplate(partialResource, ctx, resp.getWriter());
            return true;
        }

        addMainEvents(ctx);
        addNews(ctx);
        addPhotoLine(ctx, req);

        return false;
    }

    private void addPhotoLine(AsmRendererContext ctx, HttpServletRequest req) {
        if (cachedPhotoline != null &&
            (System.currentTimeMillis() - cachedPhotoline.buildTime) < CACHED_LINE_LIFETIME) {
            ctx.put("photoline", cachedPhotoline.photoline);
            return;
        }

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withTemplates("index_news",
                           "index_interview",
                           "index_reportage",
                           "index_announce");
        crit.withNotNullAttributes("medialine");
        crit.limit(MAX_PHOTOLINE_ITEMS);
        crit.onAsm().orderBy("ordinal").desc();

        Locale locale = messages.getLocale(req);
        Collection<Asm> asms = crit.selectAsAsms();

        Tree photoline = new Tree();
        cachedPhotoline = new PhotolineSlot(photoline);
        List<Tree> refs = photoline.getChildren();


        for (final Asm asm : asms) {
            int c = 0;
            if (refs.size() >= MAX_PHOTOLINE_ITEMS) {
                break;
            }
            AsmAttribute attr = asm.getAttribute("medialine");
            if (attr == null) {
                continue;
            }
            String json = attr.getEffectiveValue();
            if (StringUtils.isBlank(json) || json.charAt(0) != '[') {
                continue;
            }
            try {
                ArrayNode idsArr = (ArrayNode) mapper.readTree(json);
                for (int i = 0, l = idsArr.size(); i < l && i < MAX_PHOTOLINE_ITEMS_PER_ASM && refs.size() < MAX_PHOTOLINE_ITEMS; ++i) {
                    JsonNode n = idsArr.get(i);
                    if (n == null || !n.isNumber()) {
                        continue;
                    }
                    long id = n.asLong();
                    MediaResource mres = mediaRepository.findMediaResource(id, locale);
                    if (mres == null) {
                        continue;
                    }
                    Pair<Integer, Integer> szret;
                    try {
                        szret = mediaRepository.ensureResizedImage(id, 322, 185, //todo design-hardcoded
                                                                   MediaRepository.RESIZE_COVER_AREA);
                    } catch (IOException ignored) {
                        continue;
                    }
                    if (szret == null) {
                        continue;
                    }
                    Tree item = new Tree();
                    RichRef ref = new RichRef();
                    Image img = new Image(ctx);
                    img.setId(id);
                    img.setCover(true);
                    img.setOptionsWidth(szret.getOne());
                    img.setOptionsHeight(szret.getTwo());
                    ref.setImage(img);
                    ref.setName(mres.getDescription());
                    ref.setLink(pageService.resolvePageLink(asm.getName()) + "#medialine");
                    item.setRichRef(ref);
                    refs.add(item);
                    if (c >= 3) {
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("", e);
            }
            Collections.shuffle(refs);
        }
        ctx.put("photoline", photoline);
    }


    private void addMainEvents(AsmRendererContext ctx) throws Exception {
        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withTypeLike("news.page");
        crit.withTemplates("index_news",
                           "index_interview",
                           "index_reportage",
                           "index_announce",
                           "index_orders");
        crit.withAttributeLike("mainevent", "true");
        crit.limit(3);
        crit.onAsm().orderBy("ordinal").desc();
        Collection<Asm> events = crit.selectAsAsms();
        ctx.put("events", events);
    }

    private void addNews(AsmRendererContext ctx) throws Exception {
        HttpServletRequest req = ctx.getServletRequest();

        String activeType = StringUtils.trimToEmpty(req.getParameter("mnc.news.type"));

        String defaultType = null;
        Collection<Pair<String, String>> ncList = new ArrayList<>();
        Map<String, Configuration> ncConfigs = new HashMap<>();
        for (HierarchicalConfiguration nCfg : npCfg.configurationsAt("news.type")) {
            String type = nCfg.getString("[@type]", "");
            String title = nCfg.getString("[@title]", "");

            ncList.add(new Pair<>(type, title));
            ncConfigs.put(type, nCfg);
            if (defaultType == null) {
                defaultType = type;
            }
        }
        if (activeType == null || !ncConfigs.containsKey(activeType)) {
            activeType = defaultType;
        }

        int skip = 0;
        String skipStr = req.getParameter("mnc.news.skip");
        try {
            skip = !StringUtils.isBlank(skipStr) ? Integer.parseInt(skipStr) : skip;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_skip", skip);

        int limit = MAX_TOTAL_NEWS_LIMIT;
        String limitStr = req.getParameter("mnc.news.limit");
        try {
            limit = !StringUtils.isBlank(limitStr) ? Integer.parseInt(limitStr) : limit;
        } catch (NumberFormatException ignored) {
        }
        ctx.put("news_limit", limit);

        AsmDAO.PageCriteria crit = adao.newPageCriteria();
        crit.withPublished(true);
        crit.withTypeLike("news.page");

        Configuration nCfg = activeType != null ? ncConfigs.get(activeType) : null;
        String[] templates = nCfg != null ? nCfg.getStringArray("[@templates]") : null;
        if (templates == null) {
            templates = new String[]{"index_news", "index_interview", "index_reportage", "faculty_news", "dept_news", "index_orders"};
        }
        crit.withTemplates(templates);

        crit.skip(skip);
        crit.limit(limit);
        crit.onAsm().orderBy("ordinal").desc();
        Collection<Asm> news = crit.selectAsAsms();
        ctx.put("news", news);
        ctx.put("news_active_type", activeType);
        ctx.put("news_categories", ncList);
    }


    static class PhotolineSlot {
        final Tree photoline;
        final long buildTime;

        PhotolineSlot(Tree photoline) {
            this.buildTime = System.currentTimeMillis();
            this.photoline = photoline;
        }
    }
}
