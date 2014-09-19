package com.softmotions.ncms.asm.am;

import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.PageSecurityService;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Assembly attribute manager context.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@RequestScoped
public class AsmAttributeManagerContext extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmAttributeManagerContext.class);

    private final HttpServletRequest request;

    private final PageSecurityService pageSecurity;

    private Long asmId;

    private Map<AsmAttribute, Set<Long>> fileDeps;

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

    @Transactional
    public void flush(SecurityContext sctx) {

        update("updateMUser",
               "mdate", new Date(),
               "muser", pageSecurity.getCurrentWSUserSafe(sctx).getName(),
               "id", asmId);

        if (fileDeps == null || fileDeps.isEmpty()) {
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
        delete("deleteDeps", "attrs", attrs);
        for (int i = 0, step = 128, to = Math.min(rows.size(), i + step);
             i < rows.size();
             i = to, to = Math.min(rows.size(), i + step)) {
            update("mergeFileDependencies", rows.subList(i, to));
        }
    }
}