package com.softmotions.ncms.asm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.NotThreadSafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.softmotions.web.security.WSUser;
import com.softmotions.weboot.i18n.I18n;
import com.softmotions.weboot.mb.MBDAOSupport;

/**
 * Assembly attribute manager context.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@NotThreadSafe
@RequestScoped
public class AsmAttributeManagerContext extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmAttributeManagerContext.class);

    private static final Pattern GUID_REGEXP = Pattern.compile("^[0-9a-f]{32}$");

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final PageSecurityService pageSecurity;

    private final Locale locale;

    private final ObjectMapper mapper;

    private final I18n i18n;

    private final List<AsmAttribute> attributes = new ArrayList<>();

    private Long asmId;

    private Map<AsmAttribute, Set<Long>> fileDeps;

    private Map<AsmAttribute, Set<String>> pageDeps;

    private Map<String, Object> userData;

    private List<Consumer<AsmAttributeManagerContext>> flushListeners;


    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Locale getLocale() {
        return locale;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public I18n getI18n() {
        return i18n;
    }

    public void setAsmId(Long asmId) {
        this.asmId = asmId;
    }

    public Long getAsmId() {
        return asmId;
    }

    @Inject
    public AsmAttributeManagerContext(HttpServletRequest request,
                                      HttpServletResponse response,
                                      PageSecurityService pageSecurity,
                                      I18n i18n,
                                      ObjectMapper mapper,
                                      SqlSession sess) {
        super(AsmAttributeManagerContext.class, sess);
        this.pageSecurity = pageSecurity;
        this.request = request;
        this.response = response;
        this.mapper = mapper;
        this.i18n = i18n;
        this.locale = i18n.getLocale(request);
    }

    public void registerFileDependency(AsmAttribute attr, Long fileId) {
        if (fileDeps == null) {
            fileDeps = new HashMap<>();
        }
        Set<Long> fset = fileDeps.computeIfAbsent(attr, k -> new HashSet<>());
        fset.add(fileId);
    }

    public void registerPageDependency(AsmAttribute attr, String guid) {
        if (pageDeps == null) {
            pageDeps = new HashMap<>();
        }
        Set<String> pset = pageDeps.computeIfAbsent(attr, k -> new HashSet<>());
        pset.add(guid);
    }

    public void setUserData(String key, Object value) {
        if (userData == null) {
            userData = new HashMap<>();
        }
        userData.put(key, value);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getUserData(String key) {
        return (userData == null) ? null : (T) userData.get(key);
    }

    public List<AsmAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void notifyOnFlush(Consumer<AsmAttributeManagerContext> lsnr) {
        if (flushListeners == null) {
            flushListeners = new ArrayList<>();
        }
        flushListeners.add(lsnr);
    }

    @Transactional
    void flush() {
        if (flushListeners != null) {
            for (Consumer<AsmAttributeManagerContext> l : flushListeners) {
                l.accept(this);
            }
            flushListeners.clear();
        }
        WSUser user = pageSecurity.getCurrentWSUserSafe(request);
        update("updateMUser",
               "mdate", new Date(),
               "muser", user.getName(),
               "id", asmId);
        flushFileDeps();
        flushPageDeps();
        attributes.clear();
    }

    private void flushPageDeps() {
        delete("deletePageDeps", getAsmId());
        if (pageDeps == null) {
            return;
        }
        List<Object[]> rows = new ArrayList<>(pageDeps.size() * 3);
        for (final Map.Entry<AsmAttribute, Set<String>> e : pageDeps.entrySet()) {
            for (final String name : e.getValue()) {
                if (GUID_REGEXP.matcher(name).matches()) {
                    rows.add(new Object[]{getAsmId(), e.getKey().getId(), name});
                }
            }
        }
        for (int i = 0, step = 128, to = Math.min(rows.size(), i + step);
             i < rows.size();
             i = to, to = Math.min(rows.size(), i + step)) {
            update("mergePageDependencies", rows.subList(i, to));
        }
    }

    private void flushFileDeps() {
        delete("deleteFileDeps", getAsmId());
        if (fileDeps == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            for (Map.Entry<AsmAttribute, Set<Long>> fde : fileDeps.entrySet()) {
                log.debug("Attr name: {} Attr ID: {} File deps: {}",
                          fde.getKey().getName(), fde.getKey().getId(), fde.getValue());
            }
        }
        List<Long[]> rows = new ArrayList<>(fileDeps.size() * 3);
        for (final Map.Entry<AsmAttribute, Set<Long>> e : fileDeps.entrySet()) {
            for (final Long fid : e.getValue()) {
                rows.add(new Long[]{getAsmId(), e.getKey().getId(), fid});
            }
        }
        for (int i = 0, step = 128, to = Math.min(rows.size(), i + step);
             i < rows.size();
             i = to, to = Math.min(rows.size(), i + step)) {
            update("mergeFileDependencies", rows.subList(i, to));
        }
    }

    void registerAttribute(AsmAttribute attr) {
        attributes.add(attr);
    }
}