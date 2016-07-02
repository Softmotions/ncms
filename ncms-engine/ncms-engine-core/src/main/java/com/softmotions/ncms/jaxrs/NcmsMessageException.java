package com.softmotions.ncms.jaxrs;

import java.util.ArrayList;
import java.util.List;

import com.softmotions.commons.cont.Pair;

/**
 * Exception used to generate messages
 * for Qooxdoo based GUI clients.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsMessageException extends RuntimeException {

    private boolean hasError;

    private final List<Pair<String, Boolean>> messages = new ArrayList<>();

    public NcmsMessageException() {
    }

    public NcmsMessageException(String message, boolean err) {
        super(message);
        addMessage(message, err);
    }

    public NcmsMessageException(String message, Throwable cause) {
        super(message, cause);
        addMessage(message, true);
    }

    public NcmsMessageException(Throwable cause) {
        super(cause);
        addMessage(cause.getMessage(), true);
    }

    public NcmsMessageException addMessage(String message, boolean err) {
        messages.add(new Pair<>(message, err));
        if (err) {
            hasError = true;
        }
        return this;
    }

    public List<Pair<String, Boolean>> getMessages() {
        return messages;
    }

    public boolean hasErrorMessages() {
        return hasError;
    }

    public List<String> getErrorMessages() {
        List<String> msgs = new ArrayList<>(messages.size());
        for (Pair<String, Boolean> p : messages) {
            if (p.getTwo().booleanValue()) {
                msgs.add(p.getOne());
            }
        }
        return msgs;
    }

    public List<String> getRegularMessages() {
        List<String> msgs = new ArrayList<>(messages.size());
        for (Pair<String, Boolean> p : messages) {
            if (!p.getTwo().booleanValue()) {
                msgs.add(p.getOne());
            }
        }
        return msgs;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("NcmsMessageException{");
        sb.append("cause=").append(getMessage());
        sb.append("messages=").append(messages);
        sb.append('}');
        return sb.toString();
    }
}
