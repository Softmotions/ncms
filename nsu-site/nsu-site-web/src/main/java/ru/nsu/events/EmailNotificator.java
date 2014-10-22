package ru.nsu.events;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class EmailNotificator implements Notificator {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificator.class);

    private ExecutorService mailExecutor;
    private Session session;
    private Transport transport;
    private InternetAddress from;
    private String contentType;
    private String subjectTemplate;
    private String messageTemplate;

    private boolean dryRun;

    private final Object senderLock = new Object();

    // TODO: username & password
    public void init(NcmsEnvironment env, Configuration cfg) throws Exception {
        String from = cfg.getString("notification.from");
        contentType = cfg.getString("notification.content-type", "text/plain;charset=UTF-8");
        subjectTemplate = cfg.getString("notification.subject");
        messageTemplate = cfg.getString("notification.message");

        if (StringUtils.isBlank(from) || StringUtils.isBlank(subjectTemplate) || StringUtils.isBlank(messageTemplate)) {
            throw new IllegalArgumentException("Email sender configuration fail");
        }

        this.from = new InternetAddress(from);

        mailExecutor = Executors.newSingleThreadExecutor();
        Properties mailProps = new Properties();
        mailProps.load(new StringReader(cfg.getString("mail-server-config", "")));
        session = Session.getDefaultInstance(mailProps);
        transport = session.getTransport();

        dryRun = cfg.getBoolean("[@dryRun]", false);
    }

    public String prepareContact(String contact) {
        return contact;
    }

    public boolean isAcceptContact(String contact) {
        return EmailValidator.getInstance().isValid(contact);
    }

    public void sendNotification(String contact, Asm event) {
        try {
            final MimeMessage mm = new MimeMessage(session);

            mm.setContent(processTemplate(messageTemplate, event), contentType);
            mm.setSubject(processTemplate(subjectTemplate, event));
            mm.setFrom(from);

            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(contact));

            mailExecutor.submit(() -> {
                synchronized (senderLock) {
                    try {
                        if (!dryRun) {
                            log.info("Send message {} to {}:\n{}", mm.getSubject(), mm.getAllRecipients(), mm.getContent());
                        } else {
                            if (!transport.isConnected()) {
                                transport.connect();
                            }
                            transport.sendMessage(mm, mm.getAllRecipients());
                        }
                    } catch (Throwable tr) {
                        log.warn("", tr);
                    }
                }
            });
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void shutdown() throws Exception {
        if (mailExecutor != null) {
            mailExecutor.shutdown();
            mailExecutor = null;
        }
        if (transport != null) {
            transport.close();
            transport = null;
        }
        if (session != null) {
            session = null;
        }
    }
}
