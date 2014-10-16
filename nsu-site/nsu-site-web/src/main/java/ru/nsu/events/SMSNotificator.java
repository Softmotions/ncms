package ru.nsu.events;

import com.softmotions.ncms.asm.Asm;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SMSNotificator implements Notificator {
    private static final Logger log = LoggerFactory.getLogger(SMSNotificator.class);

    private Pattern PHONE_PATTERN;

    public void init(Configuration cfg) {
        PHONE_PATTERN = Pattern.compile(cfg.getString("phone-pattern", "^(\\+7|8)?\\d{10}$"));
    }

    public String prepareContact(String contact) {
        return contact.replaceAll("[\\(\\)\\- ]", "");
    }

    public boolean isAcceptContact(String contact) {
        return PHONE_PATTERN.matcher(prepareContact(contact)).matches();
    }

    public void sendNotification(String contact, Asm event) {
        // TODO:
        log.info("TODO: send sms to '{}', event: '{}'", contact, event.getHname());
    }

    public void shutdown() throws Exception {
    }
}
