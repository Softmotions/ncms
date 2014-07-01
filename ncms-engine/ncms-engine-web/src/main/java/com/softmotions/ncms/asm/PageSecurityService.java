package com.softmotions.ncms.asm;

import com.softmotions.web.security.WSUser;
import com.softmotions.web.security.WSUserDatabase;
import com.softmotions.weboot.mb.MBDAOSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

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
public class PageSecurityService extends MBDAOSupport {

    private static final char WRITE = 'w';
    private static final char NEWS = 'n';
    private static final char DELETE = 'd';
    private static final char STRUCTURAL = 's';

    private static final char[] ALL_RIGHTS = {WRITE, NEWS, DELETE};
    private static final String ALL_RIGHTS_STR = StringUtils.join(ALL_RIGHTS, "");

    final ObjectMapper mapper;

    final WSUserDatabase userdb;

    @Inject
    public PageSecurityService(SqlSession sess,
                               ObjectMapper mapper,
                               WSUserDatabase userdb) {
        super(PageSecurityService.class.getName(), sess);
        this.mapper = mapper;
        this.userdb = userdb;
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
     * - for recursive:
     * - if user already has rights for this page this rights will added for all childs recursively
     * - non recursive
     * - if user is owner of page rights will be updated to {@link ALL_RIGHTS}
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
            rights = ALL_RIGHTS_STR;
        }

        updateUserRights(pid, user, rights, recursive);
    }

    /**
     * Update user rights for page.
     *
     * @param pid       page id
     * @param user      user name
     * @param rights    new rights
     * @param recursive recursive flag
     */
    public void updateUserRights(Long pid, String user, String rights, boolean recursive) {
        String navPath = selectOne("selectNavPagePath", "pid", pid);

        Map<String, ?> aclInfo = selectOne("selectPageAclInfo", "pid", pid);
        Number recAcl = aclInfo != null ? (Number) aclInfo.get("recursive_acl") : null;
        Number locAcl = aclInfo != null ? (Number) aclInfo.get("local_acl") : null;

        rights = rights == null ? "" : rights;

        if (!recursive) {
            // update local acl
            if (locAcl == null) {
                locAcl = selectOne("newAclId");
                update("setLocalAcl", "pid", pid, "acl", locAcl);
            }
            update("updateAclUserRights", "acl", locAcl, "user", user, "rights", rights);
        } else {
            updateRecursiveAclUser(pid, navPath, user, rights, recAcl);
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
     * Returns user rights for specifyed page
     *
     * @param pid  page id
     * @param user user name
     */
    public String getUserRights(Long pid, String user) {
        if (user == null) {
            return "";
        }
        WSUser wsUser = userdb.findUser(user);
        if (wsUser == null) {
            return "";
        }

        Map<String, ?> row = selectOne("selectPageAclInfo", "pid", pid);
        String owner = row != null ? (String) row.get("owner") : null;
        if (user.equals(owner) || wsUser.isHasAnyRole("admin.structure")) {
            return ALL_RIGHTS_STR + STRUCTURAL;
        }

        String rights = "";
        List<String> arights = select("selectAclUserRightsForPage", "pid", pid, "user", user);
        for (String aright : arights) {
            rights = mergeRights(rights, aright);
        }

        return rights;
    }

    public boolean checkAccess(Long pid, String user, Character access) {
        // TODO: check specified access
        return false;
    }

    private String mergeRights(String r1, String r2) {
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

    private void updateRecursiveAclUser(Long pid, String navPath, String user, String rights, Number recAcl) {
        String urights = null;
        Number newRecAcl = selectOne("newAclId");
        if (recAcl != null) {
            urights = unsetRights((String) selectOne("selectUserRights", "user", user, "acl", recAcl), rights);
            urights = StringUtils.isBlank(urights) ? null : urights;

            update("copyAcl", "prev_acl", recAcl, "new_acl", newRecAcl);
        }

        update("updateChildRecursiveAcl",
               "pid", pid,
               "nav_path", navPath + pid + "/%",
               "prev_acl", recAcl,
               "new_acl", newRecAcl);
        update("updateAclUserRights", "acl", newRecAcl, "user", user, "rights", rights);

        List<Number> racls = select("childRecursiveAcls", "nav_path", navPath + pid + "/%", "exclude_acl", newRecAcl);
        for (Number racl : racls) {
            String rrights = selectOne("selectUserRights", "user", user, "acl", racl);
            if (rrights == null && urights != null) {
                continue;
            }

            Number nracl = selectOne("newAclId");
            update("copyAcl", "prev_acl", racl, "new_acl", nracl);
            update("updateChildRecursiveAcl",
                   "nav_path", navPath + pid + "/%",
                   "prev_acl", racl,
                   "new_acl", nracl);

            String nrights = urights == null ? mergeRights(rrights, rights) : unsetRights(rrights, urights);
            update("updateAclUserRights", "acl", nracl, "user", user, "rights", nrights);
        }
    }

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

        public String getUser() {
            return user;
        }

        public String getUserFullName() {
            return userFullName;
        }

        public String getRights() {
            return rights;
        }

        public boolean isRecursive() {
            return recursive;
        }
    }
}
