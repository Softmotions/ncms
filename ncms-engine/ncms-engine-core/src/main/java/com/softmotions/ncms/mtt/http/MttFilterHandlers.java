package com.softmotions.ncms.mtt.http;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class MttFilterHandlers {

    private final Set<MttFilterHandler> filters;

    public Set<MttFilterHandler> getFilters() {
        return filters;
    }

    @Inject
    public MttFilterHandlers(Set<MttFilterHandler> filters) {
        this.filters = filters;
    }
}
