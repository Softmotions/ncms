package com.softmotions.ncms.user;

import com.softmotions.weboot.mb.MBDAOSupport;

import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("/user/env")
@Produces("application/json")
public class UserEnvRS extends MBDAOSupport {

    @Inject
    public UserEnvRS(SqlSession sess) {
        super(UserEnvRS.class.getName(), sess);
    }
}
