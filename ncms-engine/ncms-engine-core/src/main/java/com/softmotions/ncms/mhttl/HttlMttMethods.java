package com.softmotions.ncms.mhttl;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.Flat3Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.softmotions.commons.string.EscapeHelper;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mtt.http.MttHttpFilter;
import com.softmotions.ncms.mtt.tp.MttActivatedTp;
import com.softmotions.ncms.mtt.tp.MttTpService;
import com.softmotions.web.HttpUtils;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@SuppressWarnings("unchecked")
public class HttlMttMethods {

    private static final Logger log = LoggerFactory.getLogger(HttlMttMethods.class);

    private HttlMttMethods() {
    }

    ///////////////////////////////////////////////////////////
    //                    A/B Testing                        //
    ///////////////////////////////////////////////////////////

    public static boolean abtA() {
        return abt("a", false);
    }

    public static boolean abtB() {
        return abt("b", false);
    }

    public static boolean abtC() {
        return abt("c", false);
    }

    public static boolean abtD() {
        return abt("d", false);
    }

    public static boolean abt(String name) {
        return abt(name, false);
    }

    /**
     * A/B testing check.
     *
     * @param name A/B mode to test
     * @param def  Default value if A/B test is not passed
     */
    public static boolean abt(String name, boolean def) {
        if (name == null) {
            return def;
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        NcmsEnvironment env = ctx.getEnvironment();
        HttpServletRequest req = ctx.getServletRequest();

        String pi = req.getRequestURI();
        boolean isAdmRequest = pi.startsWith(env.getNcmsAdminRoot());
        if (isAdmRequest) { // All A/B modes are ON in admin preview mode
            return true;
        }
        name = name.toLowerCase();
        //noinspection unchecked
        Collection<Long> rids = (Collection<Long>) req.getAttribute(MttHttpFilter.MTT_RIDS_KEY);

        if (rids == null || rids.isEmpty()) {
            Enumeration<String> pnames = req.getParameterNames();
            while (pnames.hasMoreElements()) {
                String pn = pnames.nextElement();
                if (pn.startsWith("_abm_")) {
                    String pv = req.getParameter(pn);
                    if (pv != null) {
                        StringTokenizer st = new StringTokenizer(",");
                        while (st.hasMoreTokens()) {
                            String m = st.nextToken().trim();
                            if (m.equals(name)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Found _abm_ request parameter: {}={}", pn, pv);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("No mtt actions found, no _abm_ request parameters found");
            }
            return def;
        }
        Long mrid = null;
        Cookie mcookie = null;
        Set<String> marks = null;
        for (Long rid : rids) {
            //noinspection unchecked
            Set<String> m = (Set<String>) req.getAttribute("_abm_" + rid);
            if (m != null) {
                mrid = rid;
                marks = m;
            }
        }
        if (marks == null) {
            for (Long rid : rids) {
                Cookie cookie = HttpUtils.findCookie(req, "_abm_" + rid);
                if (cookie != null) {
                    mrid = rid;
                    mcookie = cookie;
                }
            }
            if (mcookie != null) {
                String[] split = StringUtils.split(EscapeHelper.decodeURIComponent(mcookie.getValue()), ",");
                marks = new HashSet<>(split.length);
                Collections.addAll(marks, split);
                req.setAttribute("_abm_" + mrid, marks);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("AB Marks: {} matches: {}={}", marks, name,
                      (marks != null && marks.contains(name))
            );
        }
        if (marks == null) {
            return def;
        }
        return marks.contains(name) || def;
    }

    ///////////////////////////////////////////////////////////
    //                    Pixel tracking                     //
    ///////////////////////////////////////////////////////////

    private static final AtomicReference<MttTpService> TP_SERVICE_REF = new AtomicReference<>();

    public static String trackingPixels() {
        return trackingPixels("*");
    }

    public static String trackingPixels(String tpNameGlob) {
        return trackingPixels(tpNameGlob, null);
    }

    public static String trackingPixels(String tpNameGlob,
                                        @Nullable Map<String, Object> params) {
        if (params == null) {
            params = new Flat3Map<>();
        }
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        MttTpService tps = TP_SERVICE_REF.updateAndGet(
                mttTpService ->
                        mttTpService != null ? mttTpService : ctx.getInjector().getInstance(MttTpService.class)

        );
        // Pair<Tp URL, Tp Script>
        Iterable<MttActivatedTp> activatedTps =
                tps.activateTrackingPixels(ctx.getServletRequest(),
                                           ctx.getServletResponse(),
                                           tpNameGlob, params, true);

        StringBuilder sb = new StringBuilder(255);
        String sep = System.getProperty("line.separator");
        for (final MttActivatedTp atp : activatedTps) {
            if (!StringUtils.isBlank(atp.getUrl())) {
                sb.append(sep)
                  .append("<img style=\"display:none;\" width=\"0\" height=\"0\" src=\"")
                  .append(atp.getUrl())
                  .append("\"/>");
            }
            if (!StringUtils.isBlank(atp.getScript())) {
                sb.append(sep)
                  .append("<script type=\"text/javascript\">")
                  .append(atp.getScript())
                  .append("</script>");
            }
        }
        return sb.toString();
    }
}




