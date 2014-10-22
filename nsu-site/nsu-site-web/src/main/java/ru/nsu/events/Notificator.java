package ru.nsu.events;


import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public interface Notificator {
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    void init(NcmsEnvironment env, Configuration cfg) throws Exception;

    String prepareContact(String contact);

    boolean isAcceptContact(String contact);

    void sendNotification(String contact, Asm event);

    void shutdown() throws Exception;

    default String processTemplate(String template, Asm event) {
        String name = StringUtils.trimToEmpty(event.getHname());
        String date = DATE_FORMAT.format(event.getEdate());

        return StringUtils.trim(template.replaceAll("\\{name\\}", name).replaceAll("\\{date\\}", date));
    }
}
