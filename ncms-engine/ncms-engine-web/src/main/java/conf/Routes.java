package conf;

import ninja.RouteBuilder;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.adm.WorkspaceController;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@Singleton
public class Routes implements ApplicationRoutes {

    private static final Logger log = LoggerFactory.getLogger(Routes.class);

    final NcmsConfiguration cfg;

    final String prefix;

    @Inject
    public Routes(NcmsConfiguration cfg) {
        this.cfg = cfg;
        this.prefix = cfg.getNcmsPrefix() + "/exec";
    }

    public void init(Router r) {
        log.info("Init ADMIN routes, prefix: " + this.prefix);

        route(r.GET(), "/workspace").with(WorkspaceController.class, "workspace");
    }

    protected RouteBuilder route(RouteBuilder r, String pattern) {
        if (pattern.charAt(0) != '/') {
            pattern = '/' + pattern;
        }
        return r.route(prefix + pattern);
    }


}
