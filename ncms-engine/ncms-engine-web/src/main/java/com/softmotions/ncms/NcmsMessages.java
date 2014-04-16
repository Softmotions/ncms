package com.softmotions.ncms;

import ninja.i18n.Messages;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class NcmsMessages {

    @Inject
    Messages messages;

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

    public Locale getLocale(HttpServletRequest req) {
        //todo locale selection
        return Locale.getDefault();
    }
}
