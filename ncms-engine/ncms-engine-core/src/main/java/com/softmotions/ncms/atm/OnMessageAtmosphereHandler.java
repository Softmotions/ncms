package com.softmotions.ncms.atm;

import java.io.IOException;
import java.util.List;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied and patched {@link org.atmosphere.handler.OnMessage}
 */
@SuppressWarnings("unchecked")
public abstract class OnMessageAtmosphereHandler<T> extends AbstractReflectorAtmosphereHandler {

    private final Logger log = LoggerFactory.getLogger(OnMessageAtmosphereHandler.class);

    @Override
    public final void onRequest(AtmosphereResource resource) throws IOException {
        if ("GET".equalsIgnoreCase(resource.getRequest().getMethod())) {
            onOpen(resource);
        }
    }

    @Override
    public final void onStateChange(AtmosphereResourceEvent event) throws IOException {
        AtmosphereResponse response = ((AtmosphereResourceImpl) event.getResource()).getResponse(false);
        if (log.isTraceEnabled()) {
            log.trace("{} with event {}", event.getResource().uuid(), event);
        }
        if (event.isCancelled() || event.isClosedByApplication() || event.isClosedByClient()) {
            onDisconnect(response, event);
        } else if (event.getMessage() != null && List.class.isAssignableFrom(event.getMessage().getClass())) {
            List<T> messages = List.class.cast(event.getMessage());
            for (T t : messages) {
                onMessage(response, t, event);
            }
        } else if (event.isResuming()) {
            onResume(response, event);
        } else if (event.isResumedOnTimeout()) {
            onTimeout(response, event);
        } else if (event.isSuspended()) {
            onMessage(response, (T) event.getMessage(), event);
        }
        postStateChange(event);
    }

    @Override
    public final void destroy() {
    }

    /**
     * This method will be invoked when an connection has been received and not haven't yet be suspended. Note that
     * the connection will be suspended AFTER the method has been invoked when used with {@link org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor}
     *
     * @param resource an {@link AtmosphereResource}
     * @throws IOException
     */
    public void onOpen(AtmosphereResource resource) throws IOException {
    }

    /**
     * Implement this method to get invoked every time a new {@link org.atmosphere.cpr.Broadcaster#broadcast(Object)}
     * occurs.
     *
     * @param response an {@link AtmosphereResponse}
     * @param message  a message of type T
     */
    public abstract void onMessage(AtmosphereResponse response, T message, AtmosphereResourceEvent event) throws IOException;

    /**
     * This method will be invoked during the process of resuming a connection. By default this method does nothing.
     *
     * @param response an {@link AtmosphereResponse}.
     * @throws IOException
     */
    public void onResume(AtmosphereResponse response, AtmosphereResourceEvent event) throws IOException {
    }

    /**
     * This method will be invoked when a suspended connection times out, e.g no activity has occurred for the
     * specified time used when suspending. By default this method does nothing.
     *
     * @param response an {@link AtmosphereResponse}.
     * @throws IOException
     */
    public void onTimeout(AtmosphereResponse response, AtmosphereResourceEvent event) throws IOException {
    }

    /**
     * This method will be invoked when the underlying WebServer detects a connection has been closed. Please
     * note that not all WebServer supports that features (see Atmosphere's WIKI for help). By default this method does nothing.
     *
     * @param response an {@link AtmosphereResponse}.
     * @throws IOException
     */
    public void onDisconnect(AtmosphereResponse response, AtmosphereResourceEvent event) throws IOException {
    }
}