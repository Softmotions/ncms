package ru.nsu.legacy;

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

@Path("legacy")
@Singleton
public class NSULegacyRS {

    private static final Logger log = LoggerFactory.getLogger(NSULegacyRS.class);

    private final Jongo jongo;

    @Inject
    public NSULegacyRS(Jongo jongo) {
        this.jongo = jongo;
    }

    @PUT
    @Path("/import/{guid}")
    public void importMedia(@PathParam("guid") String guid,
                            String resource) {
        log.info("Importing nsu legacy media: " + resource + " guid=" + guid);
    }
}
