package com.softmotions.ncms.events;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsEventBus {

    void fire(Object event);

    void fireOnSuccessCommit(Object event);

    void fireOnRollback(Object event);

    void register(Object object);

    void unregister(Object object);
}
