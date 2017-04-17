package com.softmotions.ncms.events;

import java.util.concurrent.locks.Lock;

import com.softmotions.commons.ebus.EBus;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public interface NcmsEventBus extends EBus {

    <T> T fireOnSuccessCommit(T event);

    <T> T fireOnRollback(T event);

    void unlockOnTxFinish(Lock lock);

    void doOnSuccessCommit(Runnable action);

    void doOnRollback(Runnable action);

    void doOnTxFinish(Runnable action);
}
