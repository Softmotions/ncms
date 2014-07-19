package com.softmotions.ncms.events;

import com.softmotions.commons.ebus.EBus;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsEventBus extends EBus {

    void fireOnSuccessCommit(Object event);

    void fireOnRollback(Object event);
}
