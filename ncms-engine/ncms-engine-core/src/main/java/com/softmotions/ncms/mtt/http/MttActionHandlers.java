package com.softmotions.ncms.mtt.http;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MttActionHandlers {

    private Set<MttActionHandler> actions;

    public Set<MttActionHandler> getActions() {
        return actions;
    }

    @Inject
    public MttActionHandlers(Set<MttActionHandler> actions) {
        this.actions = actions;
    }
}
