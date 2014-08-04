package com.softmotions.ncms;

import ninja.i18n.Lang;
import ninja.i18n.Messages;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class NcmsMessages {

    private static final Logger log = LoggerFactory.getLogger(NcmsMessages.class);

    final Messages messages;

    final Lang lang;


    @Inject
    public NcmsMessages(Messages messages, Lang lang) {
        this.messages = messages;
        this.lang = lang;
    }

    public ResourceBundle getResourceBundle(HttpServletRequest req) {
        final String langName = getLocale(req).getLanguage();
        @SuppressWarnings("deprecation")
        final Set all = messages.getAll(Optional.of(langName)).keySet();

        return new ResourceBundle() {

            protected Object handleGetObject(String key) {
                Optional<String> res = messages.get(key, Optional.of(langName));
                return res.orNull();
            }

            public Enumeration<String> getKeys() {
                //noinspection unchecked
                return new IteratorEnumeration(all.iterator());
            }
        };


    }

    public String get(String key, HttpServletRequest req, String... params) {
        //todo lang selection
        Optional<String> lang = Optional.absent();
        Optional<String> msg = messages.get(key, lang, params);
        if (msg.isPresent()) {
            return msg.get();
        } else {
            return null;
        }
    }

    public Lang getLang() {
        return lang;
    }

    public Locale getLocale(HttpServletRequest req) {
        //todo locale selection
        return Locale.getDefault();
    }

    public Messages getNinjaMessages() {
        return messages;
    }
}
