package com.softmotions.ncms.events;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public interface NcmsEventBus {

    void post(Object event);

    void postOnSuccessTx(Object event);

    void register(Object object);

    void unregister(Object object);
}
