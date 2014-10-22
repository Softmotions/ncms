package ru.nsu.events;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class SMSNotificator implements Notificator {
    private static final Logger log = LoggerFactory.getLogger(SMSNotificator.class);

    private boolean initialized;

    private Pattern PHONE_PATTERN;

    private String messageTemplate;

    private String apiUrl;
    private Properties senderProps;
    private CloseableHttpClient httpClient;
    private ExecutorService smsExecutor;

    public void init(NcmsEnvironment env, Configuration cfg) {
        PHONE_PATTERN = Pattern.compile(cfg.getString("phone-pattern", "^(\\+?7|8)?\\d{10}$"));

        apiUrl = cfg.getString("sms-server-api-url");
        if (StringUtils.isBlank(apiUrl)) {
            log.warn("Not configured sms-sender api. Skipping.");
            return;
        }

        String authPropsFileName = cfg.getString("sender-props");
        if (StringUtils.isBlank(authPropsFileName)) {
            log.warn("Not configured sms-sender auth. Skipping.");
            return;
        }

        File authPropsFile = new File(env.substitutePath(authPropsFileName));
        if (!authPropsFile.exists()) {
            log.warn("Not configured sms-sender auth. Skipping.");
            return;
        }

        try (InputStream is = new FileInputStream(authPropsFile)) {
            senderProps = new Properties();
            senderProps.load(is);
        } catch (Exception e) {
            log.error("Error reading sms-sender auth props: {}", e.getMessage());
            return;
        }

        messageTemplate = cfg.getString("notification.message");

        if (StringUtils.isBlank(messageTemplate)) {
            throw new IllegalArgumentException("Sms sender configuration fail");
        }

        httpClient = HttpClients.createSystem();
        smsExecutor = Executors.newSingleThreadExecutor();

        initialized = true;
    }

    public String prepareContact(final String contact) {
        if (!initialized) {
            return contact;
        }

        String phone = contact.replaceAll("[\\(\\)\\- ]", "");
        phone = phone.replaceFirst("^(\\+7|8)", "7");
        return phone;
    }

    public boolean isAcceptContact(String contact) {
        return initialized && PHONE_PATTERN.matcher(prepareContact(contact)).matches();
    }

    public void sendNotification(final String contact, Asm event) {
        if (!initialized) {
            return;
        }

        final String message = processTemplate(messageTemplate, event);
        smsExecutor.execute(() -> {
            try {
                HttpPost request = createRequest(prepareContact(contact), message);
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() != HttpServletResponse.SC_OK) {
                        // TODO: parse XML for checking errors
                        log.warn("Error sending SMS. Server response status: {}", statusLine.getStatusCode());
                    }
                }
            } catch (Exception e) {
                log.warn("Error sending SMS: {}", e.getMessage());
            }
        });
    }

    public void shutdown() throws Exception {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException ignored) {
            }
            httpClient = null;
        }
        if (smsExecutor != null) {
            smsExecutor.shutdown();
            smsExecutor = null;
        }
        initialized = false;
    }

    private HttpPost createRequest(String phone, String message) throws UnsupportedEncodingException {
        HttpPost req = new HttpPost(apiUrl);

        List<NameValuePair> params = new ArrayList<>();
        senderProps.entrySet().stream().forEach((ap) -> params.add(new BasicNameValuePair((String) ap.getKey(), (String) ap.getValue())));
        params.add(new BasicNameValuePair("to", phone));
        params.add(new BasicNameValuePair("text", message));

        req.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        return req;
    }
}
