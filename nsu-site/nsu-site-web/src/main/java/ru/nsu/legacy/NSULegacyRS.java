package ru.nsu.legacy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.jongo.Jongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

@Path("adm/legacy")
@Singleton
public class NSULegacyRS {

    private static final Logger log = LoggerFactory.getLogger(NSULegacyRS.class);

    private final Jongo jongo;

    @Inject
    public NSULegacyRS(Jongo jongo) {
        this.jongo = jongo;
    }

    @PUT
    @Path("/import/{id}")
    public void importMedia(@PathParam("id") Long id,
                            ObjectNode importSpec) {
        log.info("Importing nsu legacy media: " + importSpec + " id=" + id);
    }
}
