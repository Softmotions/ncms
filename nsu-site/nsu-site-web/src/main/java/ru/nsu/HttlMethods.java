package ru.nsu;

import ru.nsu.pagepdf.PagePdfRS;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.mhttl.Image;
import com.softmotions.ncms.mhttl.ImageMeta;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.solr.common.SolrDocument;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class HttlMethods {

    private HttlMethods() {
    }

    public static Date eventDate(Asm n) {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        Date date = n.getEdate();
        if (date != null) {
            return date;
        }
        if (n.isHasAttribute("event_date")) {
            date = (Date) ctx.renderAttribute(n, "event_date", Collections.EMPTY_MAP);
        }
        if (date == null) {
            date = n.getCdate();
        }
        return date;
    }

    public static boolean isTodayEvent(Asm n) {
        Date edate = eventDate(n);
        Calendar cal = Calendar.getInstance();
        cal.setTime(edate);
        Calendar ncal = Calendar.getInstance();
        return (
                cal.get(Calendar.DAY_OF_YEAR) == ncal.get(Calendar.DAY_OF_YEAR) &&
                cal.get(Calendar.YEAR) == ncal.get(Calendar.YEAR)
        );
    }

    public static boolean isPastEvent(Asm n) {
        return (eventDate(n).getTime() < System.currentTimeMillis());
    }

    public static boolean isFutureEvent(Asm n) {
        return (eventDate(n).getTime() > System.currentTimeMillis());
    }

    public static boolean pagePdfExists() {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        PagePdfRS datars = ctx.getInjector().getInstance(PagePdfRS.class);
        return datars.isPagePdfExists(ctx.getAsm().getId());
    }

    public static String pagePdfLink() throws UnsupportedEncodingException {
        AsmRendererContext ctx = AsmRendererContext.getSafe();
        NcmsEnvironment env = ctx.getEnvironment();
        String hname = ctx.getAsm().getHname();
        return env.getNcmsRoot()
               + "/rs/pagepdf/"
               + ctx.getAsm().getId() + '/'
               + URLEncoder.encode(hname + ".pdf", "UTF-8");
    }

    public static Collection solrCollection(SolrDocument sd, String name) {
        Object sv = sd != null ? sd.getFieldValue(name) : null;
        if (sv == null) {
            return null;
        }

        if (sv instanceof Collection) {
            return (Collection) sv;
        } else {
            return Collections.singletonList(sv);
        }
    }

    public static Date solrDate(SolrDocument sd, String name) {
        Object sv = sd != null ? sd.getFirstValue(name) : null;
        return sv != null && sv instanceof Long ? new Date((Long) sv) : null;
    }

    public static Image solrImage(SolrDocument sd, String name) {
        Object sv = sd != null ? sd.getFirstValue(name) : null;
        return sv != null && sv instanceof byte[] ? Image.createImage(AsmRendererContext.getSafe(), SerializationUtils.deserialize((byte[]) sv)) : null;
    }

    public static String solrString(SolrDocument sd, String name) {
        Object value = sd != null ? sd.getFirstValue(name) : null;
        return value != null ? String.valueOf(value) : null;
    }
}
