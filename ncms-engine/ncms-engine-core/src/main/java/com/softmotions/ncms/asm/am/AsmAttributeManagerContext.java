package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.PageSecurityService;
import com.softmotions.web.security.WSUser;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import net.jcip.annotations.NotThreadSafe;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Assembly attribute manager context.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@NotThreadSafe
@RequestScoped
public class AsmAttributeManagerContext extends MBDAOSupport {

    private static final Pattern GUID_REGEXP = Pattern.compile("^[0-9a-f]{32}$");

    private final HttpServletRequest request;

    private final PageSecurityService pageSecurity;

    private Long asmId;

    private Map<AsmAttribute, Set<Long>> fileDeps;

    private Map<AsmAttribute, Set<String>> pageDeps;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setAsmId(Long asmId) {
        this.asmId = asmId;
    }

    public Long getAsmId() {
        return asmId;
    }

    @Inject
    public AsmAttributeManagerContext(HttpServletRequest request,
                                      PageSecurityService pageSecurity,
                                      SqlSession sess) {
        super(AsmAttributeManagerContext.class.getName(), sess);
        this.pageSecurity = pageSecurity;
        this.request = request;
    }

    public void registerMediaFileDependency(AsmAttribute attr, Long fileId) {
        if (fileDeps == null) {
            fileDeps = new HashMap<>();
        }
        Set<Long> fset = fileDeps.get(attr);
        if (fset == null) {
            fset = new HashSet<>();
            fileDeps.put(attr, fset);
        }
        fset.add(fileId);
    }

    public void registerPageDependency(AsmAttribute attr, String guid) {
        if (pageDeps == null) {
            pageDeps = new HashMap<>();
        }
        Set<String> pset = pageDeps.get(attr);
        if (pset == null) {
            pset = new HashSet<>();
            pageDeps.put(attr, pset);
        }
        pset.add(guid);
    }

    @Transactional
    public void flush() {
        WSUser user = pageSecurity.getCurrentWSUserSafe(request);
        update("updateMUser",
               "mdate", new Date(),
               "muser", (user != null) ? user.getName() : null,
               "id", asmId);
        flushFileDeps();
        flushPageDeps();
    }

    private void flushPageDeps() {
        if (pageDeps == null) {
            return;
        }
        Collection<Long> attrs = new ArrayList<>(pageDeps.size());
        List<Object[]> rows = new ArrayList<>(pageDeps.size() * 3);
        for (final Map.Entry<AsmAttribute, Set<String>> e : pageDeps.entrySet()) {
            attrs.add(e.getKey().getId());
            for (final String name : e.getValue()) {
                if (GUID_REGEXP.matcher(name).matches()) {
                    rows.add(new Object[]{e.getKey().getId(), name});
                }
            }
        }
        delete("deletePageDeps", "attrs", attrs);
        for (int i = 0, step = 128, to = Math.min(rows.size(), i + step);
             i < rows.size();
             i = to, to = Math.min(rows.size(), i + step)) {
            update("mergePageDependencies", rows.subList(i, to));
        }
    }

    private void flushFileDeps() {
        if (fileDeps == null) {
            return;
        }
        Collection<Long> attrs = new ArrayList<>(fileDeps.size());
        List<Long[]> rows = new ArrayList<>(fileDeps.size() * 3);
        for (final Map.Entry<AsmAttribute, Set<Long>> e : fileDeps.entrySet()) {
            attrs.add(e.getKey().getId());
            for (final Long fid : e.getValue()) {
                rows.add(new Long[]{e.getKey().getId(), fid});
            }
        }
        delete("deleteFileDeps", "attrs", attrs);
        for (int i = 0, step = 128, to = Math.min(rows.size(), i + step);
             i < rows.size();
             i = to, to = Math.min(rows.size(), i + step)) {
            update("mergeFileDependencies", rows.subList(i, to));
        }
    }
}