package com.softmotions.ncms.adm;

import com.softmotions.commons.weboot.mb.MBDAOSupport;
import com.softmotions.ncms.NcmsMessages;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.jaxrs.BadRequestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Редактирование выбранного экземпляра ассембли.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Path("adm/asms")
@Produces("application/json")
public class AsmEditorRS extends MBDAOSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmEditorRS.class);

    final AsmDAO adao;

    final ObjectMapper mapper;

    final NcmsMessages messages;


    @Inject
    public AsmEditorRS(SqlSession sess,
                       AsmDAO adao, ObjectMapper mapper,
                       NcmsMessages messages) {
        super(AsmEditorRS.class.getName(), sess);
        this.adao = adao;
        this.mapper = mapper;
        this.messages = messages;

    }

    /**
     * Create new empty asse,mbly instance
     */
    @PUT
    @Path("/new")
    @Produces("text/plain")
    public Long newasm(@Context HttpServletRequest req) {
        String namePrefix = messages.get("ncms.asm.new.name.prefix", req);
        Asm asm = adao.asmInsertEmptyNew(namePrefix);
        return asm.getId();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        adao.asmRemove(id);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public void save(@PathParam("id") Long id) {

    }

    @GET
    @Path("/{id}")
    @Transactional
    public Asm get(@PathParam("id") Long id) {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        return asm;
    }

    @DELETE
    @Path("/{id}/parents")
    @Transactional
    public String[] removeParent(@PathParam("id") Long id, JsonNode jsdata) throws IOException {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException();
        }
        ArrayNode an = (ArrayNode) jsdata;
        for (int i = 0, l = an.size(); i < l; ++i) {
            JsonNode pnode = an.get(i);
            if (!pnode.isObject() || !pnode.has("id")) {
                continue;
            }
            adao.asmRemoveParent(id, pnode.get("id").asLong());
        }
        asm = adao.asmSelectById(id); //refresh
        return asm.getParentRefs();
    }


    @PUT
    @Path("/{id}/parents")
    @Transactional
    public String[] saveParent(@PathParam("id") Long id, JsonNode jsdata) throws IOException {
        Asm asm = adao.asmSelectById(id);
        if (asm == null) {
            throw new NotFoundException();
        }
        if (!jsdata.isArray()) {
            throw new BadRequestException();
        }

        ArrayNode an = (ArrayNode) jsdata;
        Set<String> currParents = asm.getCumulativeParentNames();
        Set<Asm> newParents = new HashSet<>();

        for (int i = 0, l = an.size(); i < l; ++i) {
            JsonNode pnode = an.get(i);
            if (!pnode.isObject() || !pnode.has("id") || !pnode.has("name")) {
                continue;
            }
            String pname = pnode.get("name").asText();
            if (currParents.contains(pname) || pname.equals(asm.getName())) {  //self or already parent
                continue;
            }
            Asm pasm = adao.asmSelectById(pnode.get("id").asLong());
            if (pasm == null || !pasm.getName().equals(pname)) {
                continue;
            }
            Set<String> pparents = pasm.getCumulativeParentNames();
            if (pparents.contains(asm.getName())) { //cyclic dependency
                continue;
            }
            newParents.add(pasm);
        }
        for (final Asm np : newParents) { //insert parent
            adao.asmSetParent(asm, np);
        }
        asm = adao.asmSelectById(id); //refresh
        return asm.getParentRefs();
    }

}
