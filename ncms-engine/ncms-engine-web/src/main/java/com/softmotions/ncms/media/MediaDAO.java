package com.softmotions.ncms.media;

import com.softmotions.commons.weboot.mb.MBCriteriaQuery;
import com.softmotions.commons.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;

/**
 * DAO for media files model.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class MediaDAO extends MBDAOSupport {

    @Inject
    public MediaDAO(SqlSession sess) {
        super(MediaDAO.class.getName(), sess);
    }

    @SuppressWarnings("unchecked")
    static class CriteriaBase<T extends CriteriaBase> extends MBCriteriaQuery<T> {

        CriteriaBase(MBDAOSupport dao, String namespace) {
            super(dao, namespace);
        }

        public T onEntity() {
            prefixedBy("ENT_");
            return (T) this;
        }

        public T onTag() {
            prefixedBy("TSG_");
            return (T) this;
        }

        public T onPrimaryACL() {
            prefixedBy("ACLP_");
            return (T) this;
        }

        public T onSecondaryACL() {
            prefixedBy("ACLS_");
            return (T) this;
        }
    }

    public static class Criteria extends CriteriaBase<Criteria> {
        public Criteria(MediaDAO dao, String namespace) {
            super(dao, namespace);
        }
    }

    public static class EntityCriteria extends CriteriaBase<EntityCriteria> {

        public EntityCriteria(MediaDAO dao, String namespace) {
            super(dao, namespace);
        }

        public EntityCriteria onEntity() {
            prefixedBy(null);
            return this;
        }
    }

}
