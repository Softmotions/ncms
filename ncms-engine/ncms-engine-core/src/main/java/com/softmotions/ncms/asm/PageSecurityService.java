package com.softmotions.ncms.asm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.mb.MBDAOSupport;
import com.softmotions.weboot.mb.MBSqlSessionListenerSupport;
import com.softmotions.weboot.mb.MBSqlSessionManager;
import com.softmotions.weboot.security.WBSecurityContext;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public class PageSecurityService extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(PageSecurityService.class);

    public static final char WRITE = 'w';
    public static final char NEWS = 'n';
    public static final char DELETE = 'd';
    public static final char OWNER = 'o';

    public static final char[] ALL_RIGHTS = {OWNER, WRITE, NEWS, DELETE};
    public static final String ALL_RIGHTS_STR = "" + OWNER + WRITE + NEWS + DELETE;

    private final WSUserDatabase userdb;
    private final LRUMap<String, Object> aclCache;
    private final MBSqlSessionManager sessionManager;
    private final WBSecurityContext sctx;


    @Inject
    public PageSecurityService(SqlSession sess,
                               WSUserDatabase userdb,
                               NcmsEnvironment env,
                               MBSqlSessionManager sessionManager,
                               WBSecurityContext sctx) {
        super(PageSecurityService.class, sess);
        this.userdb = userdb;
        this.aclCache = new LRUMap<>(env.xcfg().getInt("security.acl-lru-cache-size", 1024));
        this.sessionManager = sessionManager;
        this.sctx = sctx;
    }

    @Nonnull
    private WSUser toWSUser(HttpServletRequest req) {
        return sctx.getWSUser(req);
    }

    @Nonnull
    public WSUser getCurrentWSUserSafe(HttpServletRequest req) {
        return toWSUser(req);
    }

    public boolean isPreviewPageRequest(HttpServletRequest req) {
        return req.getUserPrincipal() != null
               && "1".equals(req.getParameter("preview"));
    }

    /**
     * Returns all acl entities for page
     *
     * @param pid page id
     */
    public Collection<AclEntity> getAcl(long pid) {
        return getAcl(pid, null);
    }

    /**
     * Retruns all existing rights
     */
    public String getAllRights() {
        return ALL_RIGHTS_STR;
    }

    private String getCachedRights(String user, long pid) {
        synchronized (aclCache) {
            return (String) aclCache.get(user + ':' + pid);
        }
    }

    private void cacheRights(String user, long pid, String rights) {
        synchronized (aclCache) {
            aclCache.put(user + ':' + pid, rights);
        }
    }

    private void clearUserRights(String user, long pid) {
        synchronized (aclCache) {
            aclCache.remove(user + ':' + pid);
        }
    }

    private void clearUserRights(String user) {
        synchronized (aclCache) {
            String prefix = user + ':';
            Set<String> keyset = aclCache.keySet();
            String[] keys = aclCache.keySet().toArray(new String[keyset.size()]);
            for (final String k : keys) {
                if (k.startsWith(prefix)) {
                    aclCache.remove(k);
                }
            }
        }
    }

    /**
     * Returns acl entity for page by recursive flag:
     * - <code>true</code> - only recursive acl entities
     * - <code>false</code> - only local acl entities
     * - <code>null</code> - all acl entities
     *
     * @param pid       page id
     * @param recursive recursive flag
     */
    public Collection<AclEntity> getAcl(long pid, @Nullable Boolean recursive) {
        String qname = recursive == null ? "selectAllUserRights" :
                       recursive == true ? "selectRecursiveUserRights" : "selectLocalUserRights";
        List<Map<String, ?>> acl = select(qname, "pid", pid);
        if (recursive == null) {
            Collections.sort(acl, (a1, a2) -> {
                int res = (Integer) a1.get("recursive") - (Integer) a2.get("recursive");
                return res != 0 ? res : ((Comparable<String>) a1.get("user")).compareTo((String) a2.get("user"));
            });
        }

        Collection<AclEntity> res = new ArrayList<>(acl.size());

        for (Map<String, ?> user : acl) {
            WSUser wsUser = userdb.findUser((String) user.get("user"));
            if (wsUser == null) {
                continue;
            }
            res.add(new AclEntity(wsUser.getName(),
                                  wsUser.getFullName(),
                                  (String) user.get("rights"),
                                  recursive == null ? (Integer) user.get("recursive") == 1 : recursive));
        }

        return res;
    }

    /**
     * Add new user acl entity:
     * - for recursive: if user already has rights for this page this rights will added for all childs recursively
     * - non recursive: if user is owner of page rights will be updated to {@link #ALL_RIGHTS}
     *
     * @param pid       page id
     * @param user      user name
     * @param recursive recursive flag
     */
    public void addUserRights(long pid, String user, boolean recursive) {
        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);
        if (aclInfo == null) {
            return;
        }
        Number acl = (Number) aclInfo.get(recursive ? "recursive_acl" : "local_acl");
        String rights = (acl == null) ? "" : (String) selectOne("selectUserRightsByAcl",
                                                                "user", user, "acl", acl);
        if (!recursive && StringUtils.equals(user, (CharSequence) aclInfo.get("owner"))) {
            rights = getAllRights();
        }
        updateUserRights(pid, user, rights, UpdateMode.ADD, recursive);
    }

    /**
     * Set user rights.
     *
     * @param pid       page id
     * @param user      user name
     * @param rights    new rights
     * @param recursive recursive flag
     */
    public void setUserRights(long pid, String user, String rights, boolean recursive) {
        updateUserRights(pid, user, rights, UpdateMode.REPLACE, recursive);
    }

    /**
     * Add specifyed user rights.
     *
     * @param pid       page id
     * @param user      user name
     * @param rights    rights to add
     * @param recursive recursive flag
     */
    public void addUserRights(long pid, String user, String rights, boolean recursive) {
        updateUserRights(pid, user, rights, UpdateMode.ADD, recursive);
    }

    /**
     * Remove specifyed user rights.
     *
     * @param pid       page id
     * @param user      user name
     * @param rights    rights to remove
     * @param recursive recursive flag
     */
    public void removeUserRights(long pid, String user, String rights, boolean recursive) {
        updateUserRights(pid, user, rights, UpdateMode.REMOVE, recursive);
    }

    /**
     * Update user rights
     *
     * @param pid       page id
     * @param user      user name
     * @param rights    rights (to set/to add/to remove - depends on mode)
     * @param mode      update mode
     * @param recursive recursive flag
     */
    @Transactional
    public void updateUserRights(long pid,
                                 @Nonnull String user,
                                 @Nullable String rights,
                                 @Nonnull UpdateMode mode, boolean recursive) {
        if (!recursive) {
            updateLocalAclUser(pid, user, rights, mode);
        } else {
            updateRecursiveAclUser(pid, user, rights, mode);
        }
    }

    /**
     * Delete user from acl for page
     *
     * @param pid       page id
     * @param user      user name
     * @param recursive recursive flag. if <code>true</code> and no parents of page contains user in recursive acl deletion will be ignored
     */
    @Transactional
    public void deleteUserRights(long pid, final String user, boolean recursive) {
        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);
        Number locAcl = (aclInfo != null) ? (Number) aclInfo.get("local_acl") : null;
        Number recAcl = (aclInfo != null) ? (Number) aclInfo.get("recursive_acl") : null;
        if (!recursive && locAcl != null) {
            delete("deleteAclUser", "user", user, "acl", locAcl);
        } else if (recursive && recAcl != null) {
            String navPath = selectOne("selectNavPagePath", "pid", pid);
            Number check = selectOne("checkUserInParentRecursiveAcl", "nav_path", navPath, "user", user);
            if (check != null && check.intValue() > 0) {
                return;
            }
            Number newRecAcl = selectOne("newAclId");
            update("copyAcl", "prev_acl", recAcl, "new_acl", newRecAcl);

            update("updateChildRecursiveAcl",
                   "pid", pid,
                   "nav_path", navPath + pid + "/%",
                   "prev_acl", recAcl,
                   "new_acl", newRecAcl);

            delete("deleteAclUser", "acl", newRecAcl, "user", user);

            List<Number> racls = select("childRecursiveAcls",
                                        "nav_path", navPath + pid + "/%",
                                        "exclude_acl", newRecAcl,
                                        "with_user", user);
            for (Number racl : racls) {
                Number nracl = selectOne("newAclId");
                update("copyAcl", "prev_acl", racl, "new_acl", nracl);
                update("updateChildRecursiveAcl",
                       "nav_path", navPath + pid + "/%",
                       "prev_acl", racl,
                       "new_acl", nracl);

                delete("deleteAclUser", "acl", nracl, "user", user);
            }
        }
        sessionManager.registerNextEventSessionListener(new MBSqlSessionListenerSupport() {
            @Override
            public void commit(boolean success) {
                clearUserRights(user);
            }
        });
    }

    /**
     * Recursively delete user from all acl for page and its childs
     *
     * @param pid  page id
     * @param user user name
     */
    @Transactional
    public void deleteUserRecursive(long pid, String user) {
        deleteUserRights(pid, user, true);
        String navPath = selectOne("selectNavPagePath", "pid", pid);
        delete("deleteLocalAclUserRecursive", "user", user, "pid", pid, "nav_path", navPath + pid + "/%");
    }

    /**
     * Returns user rights for specifyed page
     */
    public String getAccessRights(long pid, HttpServletRequest req) {
        return getAccessRights(pid, toWSUser(req));

    }

    public String getAccessRights(long pid, WSUser wsUser) {
        if (wsUser == null) {
            return "";
        }
        String rights = getCachedRights(wsUser.getName(), pid);
        if (rights != null) {
            return rights;
        }
        return getAccessRightsDB(pid, wsUser);
    }

    @Transactional
    protected String getAccessRightsDB(long pid, WSUser wsUser) {
        String rights = "";
        Map<String, ?> row = selectOne("selectPageAclInfo", "pid", pid);
        String owner = (row != null) ? (String) row.get("owner") : null;
        if (wsUser.getName().equals(owner) ||
            wsUser.isHasAnyRole("admin", "admin.structure")) {
            return getAllRights();
        }
        List<String> arights = select("selectUserRightsForPage",
                                      "pid", pid,
                                      "user", wsUser.getName());
        for (String aright : arights) {
            rights = mergeRights(rights, aright);
        }
        cacheRights(wsUser.getName(), pid, rights);
        return rights;
    }

    /**
     * Checks specifyed access user to page
     */
    public boolean checkAccess(long pid, WSUser wsUser, char access) {
        if (wsUser == null || (!ArrayUtils.contains(ALL_RIGHTS, access))) {
            return false;
        }
        String rights = getAccessRights(pid, wsUser);
        return (rights.indexOf(access) != -1);
    }

    public boolean checkAccess(long pid, HttpServletRequest req, char access) {
        return checkAccess(pid, toWSUser(req), access);
    }

    public boolean checkAccessAll(long pid, HttpServletRequest req, String amask) {
        String umask = getAccessRights(pid, req);
        for (int i = 0, l = amask.length(); i < l; ++i) {
            if (umask.indexOf(amask.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean checkAccessAny(long pid, HttpServletRequest req, String amask) {
        String umask = getAccessRights(pid, req);
        for (int i = 0, l = amask.length(); i < l; ++i) {
            if (umask.indexOf(amask.charAt(i)) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks user access to edit page
     */
    public boolean canEdit2(@Nullable Asm page, HttpServletRequest req) {
        return checkAccessAny2(page, req, "ow");
    }

    public boolean isOwner2(@Nullable Asm page, HttpServletRequest req) {
        return checkAccessAll2(page, req, "o");
    }

    public boolean checkAccessAll2(@Nullable Asm page, HttpServletRequest req, String mask) {
        if (page == null || page.getId() == null) {
            return false;
        } else if ("news.page".equals(page.getType())) {
            return (checkAccessAll(page.getId(), req, mask) ||
                    (page.getNavParentId() != null && canNewsEdit(page.getNavParentId(), req)));
        } else {
            return checkAccessAll(page.getId(), req, mask);
        }
    }

    public boolean checkAccessAny2(@Nullable Asm page, HttpServletRequest req, String mask) {
        if (page == null || page.getId() == null) {
            return false;
        } else if ("news.page".equals(page.getType())) {
            return (checkAccessAny(page.getId(), req, mask) ||
                    (page.getNavParentId() != null && canNewsEdit(page.getNavParentId(), req)));
        } else {
            return checkAccessAny(page.getId(), req, mask);
        }
    }

    public String getAccessRights2(@Nullable Asm page, HttpServletRequest req) {
        if (page == null || page.getId() == null) {
            return "";
        } else if ("news.page".equals(page.getType())) {
            return mergeRights(getAccessRights(page.getId(), req),
                               page.getNavParentId() != null && canNewsEdit(page.getNavParentId(), req) ? String.valueOf(WRITE) : "");
        } else {
            return getAccessRights(page.getId(), req);
        }
    }

    public boolean isOwner(long pid, HttpServletRequest req) {
        return checkAccess(pid, req, OWNER);
    }

    public boolean canEdit(long pid, HttpServletRequest req) {
        return checkAccessAny(pid, req, "ow");
    }

    /**
     * Checks user access to delete page
     *
     * @param pid page id
     * @param req
     */
    public boolean canDelete(long pid, HttpServletRequest req) {
        return checkAccess(pid, req, DELETE);
    }

    /**
     * Checks user access to edit news for page
     */
    public boolean canNewsEdit(long pid, HttpServletRequest req) {
        return checkAccess(pid, req, NEWS);
    }

    /**
     * Merge rights. Returns new rights string that contains all rights from r1 and r2.
     */
    public String mergeRights(@Nullable String r1, @Nullable String r2) {
        String res = r1 != null ? r1 : "";
        for (char r : (r2 != null ? r2 : "").toCharArray()) {
            if (!StringUtils.contains(res, r)) {
                res += r;
            }
        }
        return res;
    }

    private String unsetRights(@Nullable String from,
                               @Nullable String r) {
        from = (from != null) ? from : "";
        return StringUtils.isBlank(r) ? from : from.replaceAll("[" + r + "]", "");
    }

    private void updateLocalAclUser(final long pid,
                                    final String user,
                                    @Nullable final String rights,
                                    UpdateMode mode) {
        String nrights = "";
        Number locAcl = selectOne("getLocalAcl", "pid", pid);

        // update local acl
        if (locAcl == null) {
            locAcl = selectOne("newAclId");
            update("setLocalAcl", "pid", pid, "acl", locAcl);
        } else {
            nrights = selectOne("selectUserRightsByAcl", "user", user, "acl", locAcl);
        }
        update("updateAclUserRights", "acl", locAcl, "user", user, "rights", calcRights(mode, nrights, rights));
        sessionManager.registerNextEventSessionListener(new MBSqlSessionListenerSupport() {
            @Override
            public void commit(boolean success) {
                // clear rights.
                // real user rights must be collected from all (local and recursive) acl.
                // it will be done on next user request to page
                clearUserRights(user, pid);
            }
        });
    }

    private void updateRecursiveAclUser(final long pid,
                                        final String user,
                                        @Nullable final String rights,
                                        UpdateMode mode) {
        String navPath = selectOne("selectNavPagePath", "pid", pid);
        Number recAcl = selectOne("getRecursiveAcl", "pid", pid);

        String cr = "";
        Number newRecAcl = selectOne("newAclId");
        if (recAcl != null) {
            update("copyAcl", "prev_acl", recAcl, "new_acl", newRecAcl);
            cr = selectOne("selectUserRightsByAcl", "user", user, "acl", recAcl);
        }

        update("updateChildRecursiveAcl",
               "pid", pid,
               "nav_path", navPath + pid + "/%",
               "prev_acl", recAcl,
               "new_acl", newRecAcl);
        update("updateAclUserRights", "acl", newRecAcl, "user", user, "rights", calcRights(mode, cr, rights));

        List<Number> racls = select("childRecursiveAcls", "nav_path", navPath + pid + "/%", "exclude_acl", newRecAcl);
        for (Number racl : racls) {
            cr = selectOne("selectUserRightsByAcl", "user", user, "acl", racl);

            Number nracl = selectOne("newAclId");
            update("copyAcl", "prev_acl", racl, "new_acl", nracl);
            update("updateChildRecursiveAcl",
                   "nav_path", navPath + pid + "/%",
                   "prev_acl", racl,
                   "new_acl", nracl);

            update("updateAclUserRights", "acl", nracl, "user", user, "rights", calcRights(mode, cr, rights));
        }
        sessionManager.registerNextEventSessionListener(new MBSqlSessionListenerSupport() {
            @Override
            public void commit(boolean success) {
                clearUserRights(user); //clear all user rights due to recursive acl
            }
        });
    }

    @Nullable
    private String calcRights(UpdateMode mode,
                              @Nullable String cr,
                              @Nullable String nr) {
        switch (mode) {
            case REPLACE:
                return nr;
            case ADD:
                return mergeRights(cr, nr);
            case REMOVE:
                return unsetRights(cr, nr);
        }

        return "";
    }

    public Collection<Long> getAccessibleTemplates(HttpServletRequest req) {
        return getAccessibleTemplates(toWSUser(req));
    }

    @Transactional
    public Collection<Long> getAccessibleTemplates(WSUser user) {
        if (user.isHasAnyRole("admin", "admin.asm")) {
            return select("accessibleAsmsForRoles",
                          "template", true);
        } else {
            Iterator<WSRole> roles = user.getRoles();
            List<String> lRoles = new ArrayList<>(32);
            while (roles.hasNext()) {
                lRoles.add(roles.next().getName());
            }
            return select("accessibleAsmsForRoles",
                          "template", true,
                          "roles", lRoles);
        }
    }

    /**
     * User rights info
     */
    public static class AclEntity {
        private String user;
        private String userFullName;
        private String rights;
        private boolean recursive;

        public AclEntity(String user, String userFullName, String rights, boolean recursive) {
            this.user = user;
            this.userFullName = userFullName;
            this.rights = rights;
            this.recursive = recursive;
        }

        /**
         * @return user name
         */
        public String getUser() {
            return user;
        }

        /**
         * @return user full name
         */
        public String getUserFullName() {
            return userFullName;
        }

        /**
         * @return rights
         */
        public String getRights() {
            return rights;
        }

        /**
         * @return recursive flag
         */
        public boolean isRecursive() {
            return recursive;
        }
    }

    /**
     * Rights update mode
     */
    public enum UpdateMode {
        /**
         * Add rights to exists
         */
        ADD,
        /**
         * Remove rights from exists
         */
        REMOVE,
        /**
         * Replace rights
         */
        REPLACE
    }
}
