package ru.nsu.social;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.PropertyConfiguration;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.PageService;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.weboot.lifecycle.Dispose;
import com.softmotions.weboot.lifecycle.Start;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@Singleton
public class NewsTwitterHandler {
    private static final Logger log = LoggerFactory.getLogger(NewsTwitterHandler.class);

    private static final String NEWS_TWEET_REFDATA_TYPE = "news_tweet";
    private static final String NEWS_PAGE_TYPE = "news.page";

    private static final int TWEET_MAX_LENGTH = 140;

    private final AsmDAO adao;

    private final PageService pageService;

    private final NcmsEventBus ebus;

    private final TwitterFactory twFactory;

    private final ExecutorService tweetSender;

    private final String siteRoot;
    private final String tweetAttr;
    private final String annotationAttr;


    @Inject
    public NewsTwitterHandler(AsmDAO adao, PageService pageService, NcmsEventBus ebus, NcmsEnvironment env) throws Exception {
        this.adao = adao;
        this.pageService = pageService;
        this.ebus = ebus;

        XMLConfiguration xcfg = env.xcfg();
        siteRoot = xcfg.getString("site-root");

        SubnodeConfiguration twCfg = xcfg.configurationAt("social.twitter");

        tweetAttr = twCfg.getString("tweet-attribute", "tweet");
        annotationAttr = twCfg.getString("annotation-attribute", "annotation");

        tweetSender = Executors.newSingleThreadExecutor();

        Properties twProps = new Properties();
        String propsFileName = twCfg.getString("config-properties-file", "");
        if (!StringUtils.isBlank(propsFileName)) {
            File propsFile = new File(env.substitutePath(propsFileName));
            if (propsFile.exists()) {
                try (InputStream is = new FileInputStream(propsFile)) {
                    twProps.load(is);
                }
            }
        }

        if (!twProps.isEmpty()) {
            twFactory = new TwitterFactory(new PropertyConfiguration(twProps));
        } else {
            log.warn("Not configured news twitter sender!");
            twFactory = null;
        }
    }

    @Start
    public void start() {
        if (twFactory != null) {
            ebus.register(this);
        }
    }

    @Dispose
    public void shutdown() {
        tweetSender.shutdown();
    }

    @Subscribe
    public void onPageModify(final AsmModifiedEvent e) {
        if (twFactory == null) {
            return;
        }

        tweetSender.execute(() -> {
            Asm page = adao.selectOne("selectAsmByCriteria", "id", e.getId(), "type", NEWS_PAGE_TYPE);
            if (page == null || !page.isPublished() || !BooleanUtils.toBoolean(page.getEffectiveAttributeAsString(tweetAttr, "false"))) {
                return;
            }
            if (adao.count("countAsmRefByType", "id", page.getId(), "type", NEWS_TWEET_REFDATA_TYPE) > 0) {
                return;
            }

            Asm parent = adao.asmSelectById(page.getNavParentId());
            if (parent == null) { // impossible
                return;
            }

            String name = page.getHname();
            String parentName = parent.getHname() + ":";
            String link = siteRoot + pageService.resolvePageLink(page.getId());
            String annotation = page.getEffectiveAttributeAsString(annotationAttr, "");
            if (StringUtils.isBlank(annotation)) {
                annotation = name;
            }

            int nameLength = name.length();
            int pnameLength = parentName.length();
            int linkLength = link.length();
            int annotLength = annotation.length();

            String text = null;
            if (pnameLength + annotLength + linkLength + 2 <= TWEET_MAX_LENGTH) {
                text = parentName + " " + annotation + "\n" + link;
            } else if (pnameLength + nameLength + linkLength + 2 <= TWEET_MAX_LENGTH) {
                text = parentName + " " + name + "\n" + link;
            } else if (pnameLength + annotLength + 1 <= TWEET_MAX_LENGTH) {
                text = parentName + " " + annotation;
            } else if (pnameLength + nameLength + 1 <= TWEET_MAX_LENGTH) {
                text = parentName + " " + name;
            } else if (annotLength <= TWEET_MAX_LENGTH) {
                text = annotation;
            } else if (nameLength <= TWEET_MAX_LENGTH) {
                text = name;
            }

            if (text != null) {
                try {
                    Twitter twitter = twFactory.getInstance();
                    twitter.updateStatus(text);
                    adao.setAsmRefData(page.getId(), NEWS_TWEET_REFDATA_TYPE, "");
                } catch (TwitterException ex) {
                    log.error("Error update twitter status: {}", ex.getMessage());
                }
            }
        });
    }

}
