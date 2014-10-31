package com.softmotions.ncms.update;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public interface HotFix {

    /**
     * If <code>null</code> - hotfix will be applying every time at system startup.
     * Max length: 64
     *
     * @return
     */
    String getId();

    default int getOrder() {
        return 0;
    }

    void apply() throws Exception;
}
