package com.softmotions.ncms.atm;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.MetaBroadcaster;

import com.google.common.base.MoreObjects;
import com.softmotions.ncms.events.BasicEvent;

/**
 * Fired if got message from UI user.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class UIUserMessageEvent extends BasicEvent {

    private final WSMessage message;

    private final String resourceUUID;

    private final AtmosphereResourceFactory resourceFactory;

    private final MetaBroadcaster metaBroadcaster;

    private final String broadcastAllPattern;

    public UIUserMessageEvent(Object source,
                              WSMessage message,
                              String resourceUUID,
                              String broadcastAllPattern,
                              MetaBroadcaster metaBroadcaster,
                              AtmosphereResourceFactory resourceFactory,
                              HttpServletRequest req) {
        super(source, message.getType(), req);
        this.message = message;
        this.resourceUUID = resourceUUID;
        this.broadcastAllPattern = broadcastAllPattern;
        this.resourceFactory = resourceFactory;
        this.metaBroadcaster = metaBroadcaster;
    }

    public WSMessage getMessage() {
        return message;
    }

    public String getResourceUUID() {
        return resourceUUID;
    }

    public MetaBroadcaster getMetaBroadcaster() {
        return metaBroadcaster;
    }

    public void apply(Consumer<AtmosphereResource> run) {
        AtmosphereResource res = getResource();
        if (res != null) {
            run.accept(res);
        }
    }

    public void broadcast(Object msg) {
        AtmosphereResource res = getResource();
        if (res != null) {
            res.getBroadcaster().broadcast(msg);
        }
    }

    public void broadcastAll(Object msg) {
        metaBroadcaster.broadcastTo(broadcastAllPattern, msg);
    }

    @Nullable
    public AtmosphereResource getResource() {
        return resourceFactory.find(resourceUUID);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("message", message)
                          .toString();
    }
}
