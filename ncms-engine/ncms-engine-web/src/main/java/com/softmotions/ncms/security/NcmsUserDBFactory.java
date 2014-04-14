package com.softmotions.ncms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsUserDBFactory implements ObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(NcmsUserDBFactory.class);

    public Object getObjectInstance(Object obj, Name name,
                                    Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if ((obj == null) || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;
        if (!"com.softmotions.web.security.WSUserDatabase".equals(ref.getClassName())) {
            return null;
        }

        EmptyWSUserDatabase db = new EmptyWSUserDatabase(name.toString());
        //todo
        return db;
    }
}
