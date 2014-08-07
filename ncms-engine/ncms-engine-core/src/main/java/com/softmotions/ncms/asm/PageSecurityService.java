package com.softmotions.ncms.asm;

import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public final class PageSecurityService extends MBDAOSupport {

    public static final char WRITE = 'w';
    public static final char NEWS = 'n';
    public static final char DELETE = 'd';
    // public static final char STRUCTURAL = 's';

    public static final char[] ALL_RIGHTS = {WRITE, NEWS, DELETE};
    public static final String ALL_RIGHTS_STR = "" + WRITE + NEWS + DELETE;

    private final WSUserDatabase userdb;

    @Inject
    public PageSecurityService(SqlSession sess,
                               WSUserDatabase userdb) {
        super(PageSecurityService.class.getName(), sess);
        this.userdb = userdb;
    }

    private WSUser toWSUser(HttpServletRequest req) {
        Principal p = req.getUserPrincipal();
        if (p instanceof WSUser) {
            return (WSUser) p;
        }
        return null;
    }

    /**
     * Returns all acl entities for page
     *
     * @param pid page id
     */
    public Collection<AclEntity> getAcl(Long pid) {
        return getAcl(pid, null);
    }

    /**
     * Retruns all existing rights
     */
    public String getAllRights() {
        return ALL_RIGHTS_STR;
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
    public Collection<AclEntity> getAcl(Long pid, Boolean recursive) {
        String qname = recursive == null ? "selectAllUserRights" :
                       recursive == true ? "selectRecursiveUserRights" : "selectLocalUserRights";
        List<Map<String, ?>> acl = select(qname, "pid", pid);
        if (recursive == null) {
            Collections.sort(acl, new Comparator<Map<String, ?>>() {
                public int compare(Map<String, ?> a1, Map<String, ?> a2) {
                    int res = (Integer) a1.get("recursive") - (Integer) a2.get("recursive");
                    return res != 0 ? res : ((String) a1.get("user")).compareTo((String) a2.get("user"));
                }
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
     * - non recursive: if user is owner of page rights will be updated to {@link ALL_RIGHTS}
     *
     * @param pid       page id
     * @param user      user name
     * @param recursive recursive flag
     */
    public void addUserRights(Long pid, String user, boolean recursive) {
        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);
        Number acl = (Number) aclInfo.get(recursive ? "recursive_acl" : "local_acl");
        String rights = acl == null ? "" : (String) selectOne("selectUserRightsByAcl", "user", user, "acl", acl);
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
    public void setUserRights(Long pid, String user, String rights, boolean recursive) {
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
    public void addUserRights(Long pid, String user, String rights, boolean recursive) {
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
    public void removeUserRights(Long pid, String user, String rights, boolean recursive) {
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
    public void updateUserRights(Long pid, String user, String rights, UpdateMode mode, boolean recursive) {
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
    public void deleteUserRights(Long pid, String user, boolean recursive) {
        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);

        Number locAcl = aclInfo != null ? (Number) aclInfo.get("local_acl") : null;
        Number recAcl = aclInfo != null ? (Number) aclInfo.get("recursive_acl") : null;
        if (!recursive && locAcl != null) {
            delete("deleteAclUser", "user", user, "acl", locAcl);
        } else if (recursive && recAcl != null) {
            String navPath = selectOne("selectNavPagePath", "pid", pid);

            int check = selectOne("checkUserInParentRecursiveAcl", "nav_path", navPath, "user", user);
            if (check > 0) {
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
    }

    /**
     * Recursively delete user from all acl for page and its childs
     *
     * @param pid  page id
     * @param user user name
     */
    public void deleteUserRecursive(Long pid, String user) {
        deleteUserRights(pid, user, true);

        String navPath = selectOne("selectNavPagePath", "pid", pid);
        delete("deleteLocalAclUserRecursive", "user", user, "pid", pid, "nav_path", navPath + pid + "/%");
    }

    /**
     * Returns user rights for specifyed page
     *
     * @param pid  page id
     * @param user user name
     */
    public String getUserRights(Long pid, HttpServletRequest req) {
        WSUser wsUser = toWSUser(req);
        if (wsUser == null) {
            return "";
        }

        Map<String, ?> row = selectOne("selectPageAclInfo", "pid", pid);
        String owner = row != null ? (String) row.get("owner") : null;
        if (wsUser.getName().equals(owner) || wsUser.isHasAnyRole("admin.structure")) {
            return getAllRights();
        }

        String rights = "";
        List<String> arights = select("selectUserRightsForPage", "pid", pid, "user", wsUser.getName());
        for (String aright : arights) {
            rights = mergeRights(rights, aright);
        }
        return rights;
    }

    /**
     * Checks specifyed access user to page
     *
     * @param pid    page id
     * @param user   user name
     * @param access checked access
     */
    public boolean checkAccess(Long pid, WSUser wsUser, Character access) {
        if (wsUser == null || access == null || (!ArrayUtils.contains(ALL_RIGHTS, access))) {
            return false;
        }
        Map<String, ?> row = selectOne("selectPageAclInfo", "pid", pid);
        String owner = row != null ? (String) row.get("owner") : null;
        if (wsUser.getName().equals(owner) || wsUser.isHasAnyRole("admin.structure")) {
            return true;
        }
        return ((Number) selectOne("checkUserAccess",
                                   "pid", pid,
                                   "user", wsUser.getName(),
                                   "right", access)).intValue() > 0;
    }


    public boolean checkAccess(Long pid, HttpServletRequest req, Character access) {
        return checkAccess(pid, toWSUser(req), access);
    }

    public boolean checkAccessAll(Long pid, HttpServletRequest req, String amask) {
        String umask = getUserRights(pid, req);
        for (int i = 0, l = amask.length(); i < l; ++i) {
            if (umask.indexOf(amask.charAt(i)) == -1) {
                return false;
            }
        }
        return true;
    }

    public boolean checkAccessAny(Long pid, HttpServletRequest req, String amask) {
        String umask = getUserRights(pid, req);
        for (int i = 0, l = amask.length(); i < l; ++i) {
            if (umask.indexOf(amask.charAt(i)) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks user access to edit page
     *
     * @param pid page id
     * @param req
     */
    public boolean canEdit(Long pid, HttpServletRequest req) {
        return checkAccess(pid, req, WRITE);
    }

    /**
     * Checks user access to delete page
     *
     * @param pid page id
     * @param req
     */
    public boolean canDelete(Long pid, HttpServletRequest req) {
        return checkAccess(pid, req, DELETE);
    }

    /**
     * Checks user access to edit news for page
     *
     * @param pid  page id
     * @param user user name
     */
    public boolean canNewsEdit(Long pid, HttpServletRequest req) {
        return checkAccess(pid, req, NEWS);
    }

    /**
     * Merge rights. Returns new rights string that contains all rights from r1 and r2.
     */
    public String mergeRights(String r1, String r2) {
        String res = r1 != null ? r1 : "";
        for (char r : (r2 != null ? r2 : "").toCharArray()) {
            if (!StringUtils.contains(res, r)) {
                res += r;
            }
        }

        return res;
    }

    private String unsetRights(String from, String r) {
        from = from != null ? from : "";
        return StringUtils.isBlank(r) ? from : from.replaceAll("[" + r + "]", "");
    }

    private void updateLocalAclUser(Long pid, String user, String rights, UpdateMode mode) {
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
    }

    private void updateRecursiveAclUser(Long pid, String user, String rights, UpdateMode mode) {
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
    }

    private String calcRights(UpdateMode mode, String cr, String nr) {
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
