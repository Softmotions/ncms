package com.softmotions.ncms.security;

import com.softmotions.web.security.WSGroup;
import com.softmotions.web.security.WSRole;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
public class XMLUserDatabaseTest {

    private static final Logger log = LoggerFactory.getLogger(XMLUserDatabaseTest.class);

    @Test
    public void testXMLUserdatabase() throws Exception {
        XMLWSUserDatabase db =
                new XMLWSUserDatabase("db1",
                                      "com/softmotions/ncms/security/test-users-db.xml",
                                      false);

        List<WSGroup> groups = IteratorUtils.toList(db.getGroups());
        List<WSRole> roles = IteratorUtils.toList(db.getRoles());
        log.info("Roles=" + roles);

    }

}
