package com.softmotions.ncms.asm;

import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.events.MediaDeleteEvent;
import com.softmotions.ncms.media.events.MediaMoveEvent;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmEventsListener extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmEventsListener.class);

    @Inject
    public AsmEventsListener(NcmsEventBus ebus, SqlSession sess) {
        super(AsmEventsListener.class.getName(), sess);
        ebus.register(this);
    }


    @Subscribe
    @Transactional
    public void mediaMove(MediaMoveEvent e) {
        if (e.isFolder()) {
            String prefix_like = e.getOldPath() + '%';
            update("fixCoreFolderLocation",
                   "prefix_like", prefix_like,
                   "prefix_like_len", prefix_like.length(),
                   "new_prefix", e.getNewPath());
        } else {
            update("fixCoreLocation",
                   "old_location", e.getOldPath(),
                   "new_location", e.getNewPath());
        }
    }

    @Subscribe
    @Transactional
    public void mediaDelete(MediaDeleteEvent e) {
        //todo??
    }
}
