package ru.nsu;

import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.render.AsmRendererContext;

import java.util.Calendar;
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
}
