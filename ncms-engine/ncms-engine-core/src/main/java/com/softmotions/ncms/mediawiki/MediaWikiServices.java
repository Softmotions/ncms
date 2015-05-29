package com.softmotions.ncms.mediawiki;

import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.MediaRepository;
import com.softmotions.ncms.mediawiki.events.MediaWikiHTMLRenderEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;

/**
 * Auxiliary
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MediaWikiServices {

    private static final Logger log = LoggerFactory.getLogger(MediaWikiServices.class);

    private static final Pattern WIKI_IMAGE_REGEXP =
            Pattern.compile("\\[\\[(Image|File):/(\\d+)/[^\\[]*(\\|(\\d+)(px|x\\d+px)\\|)[^\\[]*\\]\\]",
                            MULTILINE | CASE_INSENSITIVE);

    private final MediaRepository repository;


    @Inject
    public MediaWikiServices(NcmsEventBus ebus,
                             MediaRepository repository) {
        ebus.register(this);
        this.repository = repository;
    }

    @Subscribe
    public void htmlRendered(MediaWikiHTMLRenderEvent ev) {
        String markup = ev.getMarkup();
        // [[Image:/121/P4033297.JPG|frame|none|100px|Даша]]
        Matcher matcher = WIKI_IMAGE_REGEXP.matcher(markup);
        while (matcher.find()) {
            String idStr = matcher.group(2);
            String widthStr = matcher.group(4);
            if (idStr == null || widthStr == null) {
                continue;
            }
            Long id = Long.parseLong(idStr);
            Integer width = Integer.parseInt(widthStr);
            try {
                repository.ensureResizedImage(id, width, null, MediaRepository.RESIZE_SKIP_SMALL);
            } catch (Exception e) {
                log.error("Failed to resize image", e);
            }
        }
    }
}
