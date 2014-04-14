package com.softmotions.ncms.security;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class XMLWSUserDatabaseJNDIFactory implements ObjectFactory {

    private static final Logger log = LoggerFactory.getLogger(XMLWSUserDatabaseJNDIFactory.class);

    public Object getObjectInstance(Object obj, Name name,
                                    Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        if ((obj == null) || !(obj instanceof Reference)) {
            return null;
        }
        Reference ref = (Reference) obj;
        if (!"com.softmotions.web.security.WSUserDatabase".equals(ref.getClassName())) {
            return null;
        }
        boolean autoSave = false;
        String config = null;
        RefAddr ra = ref.get("config");
        if (ra != null) {
            config = ra.getContent().toString();
        }
        ra = ref.get("autoSave");
        if (ra != null) {
            autoSave = BooleanUtils.toBoolean(ra.getContent().toString());
        }
        if (config == null) {
            throw new RuntimeException("Missing required 'config' parameter");
        }
        log.info("Using database configuration: " + config);
        log.info("autoSave: " + autoSave);
        return new XMLWSUserDatabase(name.toString(), config, autoSave);
    }
}
