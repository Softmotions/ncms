package ru.nsu.events;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.jaxrs.NcmsMessageException;
import com.softmotions.weboot.lifecycle.Dispose;
import com.softmotions.weboot.lifecycle.Start;
import com.softmotions.weboot.scheduler.Scheduled;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Tyutyunkov VE (tyutyunkov@gmail.com)
 */
@Singleton
public class EventsRemember {
    public static final String TYPE = "remember_event";

    private static final Logger log = LoggerFactory.getLogger(EventsRemember.class);

    private final Injector injector;

    private final AsmDAO adao;

    private final NcmsMessages messages;

    private final SubnodeConfiguration cfg;

    private final Scheduler scheduler;

    private Collection<Notificator> notificators;

    @Inject
    public EventsRemember(Injector injector, AsmDAO adao, NcmsMessages messages, Scheduler scheduler, NcmsEnvironment env) {
        this.injector = injector;
        this.adao = adao;
        this.messages = messages;
        this.scheduler = scheduler;

        this.cfg = env.xcfg().configurationAt("events-remember");
    }

    @Start(order = 150)
    public void startup() {
        ClassLoader cl = ObjectUtils.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader()
        );

        List<HierarchicalConfiguration> ncfgs = cfg.configurationsAt("notificator");
        notificators = new ArrayList<>(ncfgs.size());
        for (HierarchicalConfiguration ncfg : ncfgs) {
            String ncname = ncfg.getString("[@class]");
            if (StringUtils.isBlank(ncname)) {
                log.warn("Empty notificator class in configuration.");
                continue;
            }

            Class<? extends Notificator> nclass;
            try {
                nclass = (Class<? extends Notificator>) cl.loadClass(ncname);
            } catch (ClassNotFoundException ignored) {
                log.warn("Not found notificator class: '{}'", ncname);
                continue;
            }

            Notificator instance = injector.getInstance(nclass);
            try {
                instance.init(ncfg);
                notificators.add(instance);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if (notificators.isEmpty()) {
            log.error("Not registered notificators for EventsRemember");
        } else {
            String sendPattern = cfg.getString("sender-pattern");
            if (StringUtils.isBlank(sendPattern)) {
                log.error("Not specified notification job pattern");
            } else if (!SchedulingPattern.validate(sendPattern)) {
                log.error("Invalid pattern for notification job: '{}'", sendPattern);
            } else {
                log.info("Register sender");
                scheduler.schedule(sendPattern, this::sendNotify);
            }
        }
    }

    @Dispose
    public void shutdown() {
        for (Notificator notificator : notificators) {
            try {
                notificator.shutdown();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    public void saveRememeber(Long eventId, final String contact) throws NcmsMessageException {
        if (StringUtils.isBlank(contact)) {
            throw new NcmsMessageException(messages.get("ncms.events.remember.contacts.empty"), true);
        }
        Asm event = adao.asmSelectById(eventId);
        if (event == null) {
            throw new NcmsMessageException(messages.get("ncms.events.remember.event.not.found"), true);
        }

        Optional<Notificator> an = notificators.stream().filter((n) -> n.isAcceptContact(contact)).findFirst();
        if (!an.isPresent()) {
            throw new NcmsMessageException(messages.get("ncms.events.remember.contacts.not.valid"), true);
        }

        Notificator n = an.get();

        adao.setAsmRefData(event.getId(), TYPE, n.prepareContact(contact));
    }

    public void sendNotify() {
        log.info("Send notify");

        Calendar scal = Calendar.getInstance();
        Calendar fcal = Calendar.getInstance();
        fcal.add(Calendar.DAY_OF_YEAR, cfg.getInt("notification-days-before", 1));

        final Map<Long, Asm> events = new HashMap<>();

        adao.select("selectAsmEventRef", context -> {
            Map<String, ?> obj = (Map<String, ?>) context.getResultObject();
            Long asmId = (Long) obj.get("asm_id");
            final String contact = (String) obj.get("svalue");

            final Asm event;
            if (!events.containsKey(asmId)) {
                event = adao.asmSelectById(asmId);
                events.put(asmId, event);
            } else {
                event = events.get(asmId);
            }
            if (event == null) {
                return;
            }

            notificators.stream().filter((n) -> n.isAcceptContact(contact)).forEach(t -> t.sendNotification(contact, event));
        },
                    "type", TYPE,
                    "edateLTYear", fcal.get(Calendar.YEAR),
                    "edateLTDay", fcal.get(Calendar.DAY_OF_YEAR),
                    "edateGTYear", scal.get(Calendar.YEAR),
                    "edateGTDay", scal.get(Calendar.DAY_OF_YEAR)
        );

        if (!events.isEmpty()) {
            adao.delete("deleteAsmRefByType",
                        "type", TYPE,
                        "ids", events.keySet());
        }
    }

}
