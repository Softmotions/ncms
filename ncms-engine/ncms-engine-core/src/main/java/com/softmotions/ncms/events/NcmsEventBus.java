package com.softmotions.ncms.events;

import java.util.concurrent.locks.Lock;

import com.softmotions.commons.ebus.EBus;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsEventBus extends EBus {

    void fireOnSuccessCommit(Object event);

    void fireOnRollback(Object event);

    void unlockOnTxFinish(Lock lock);

    void doOnSuccessCommit(Runnable action);

    void doOnRollback(Runnable action);

    void doOnTxFinish(Runnable action);
}
