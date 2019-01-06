package com.softmotions.ncms.asm;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.softmotions.ncms.asm.events.PagesCacheInvalidateEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.media.events.MediaDeleteEvent;
import com.softmotions.ncms.media.events.MediaMoveEvent;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class AsmEventsListener extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmEventsListener.class);

    private final NcmsEventBus ebus;

    @Inject
    public AsmEventsListener(NcmsEventBus ebus, SqlSession sess) {
        super(AsmEventsListener.class, sess);
        ebus.register(this);
        this.ebus = ebus;
    }


    @Subscribe
    @Transactional
    public void mediaMove(MediaMoveEvent e) {
        int cnt;
        if (e.isFolder()) {
            String prefix_like = e.getOldPath() + '%';
            cnt = update("fixCoreFolderLocation",
                         "prefix_like", prefix_like,
                         "prefix_like_len", prefix_like.length(),
                         "new_prefix", e.getNewPath());
        } else {
            cnt = update("fixCoreLocation",
                         "old_location", e.getOldPath(),
                         "new_location", e.getNewPath());
        }
        if (cnt > 0) {
           ebus.fireOnSuccessCommit(new PagesCacheInvalidateEvent());
        }
    }

    @Subscribe
    @Transactional
    public void onMediaDelete(MediaDeleteEvent e) {
        //todo??
    }
}
