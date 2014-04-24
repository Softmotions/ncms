package com.softmotions.ncms.adm;

import com.softmotions.commons.weboot.mb.MBDAOSupport;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;

import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Редактирование выбранного экземпляра ассембли.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/asms")
@Produces("application/json")
public class AsmEditorRS extends MBDAOSupport {

    final AsmDAO adao;

    @Inject
    public AsmEditorRS(SqlSession sess, AsmDAO adao) {
        super(AsmEditorRS.class.getName(), sess);
        this.adao = adao;
    }

    @PUT
    @Path("save/{id}")
    @Transactional
    public void save(@PathParam("id") Long id) {

    }


    @GET
    @Path("get/{id}")
    @Transactional
    public Asm get(@PathParam("id") Long id) {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        return asm;
    }
}
