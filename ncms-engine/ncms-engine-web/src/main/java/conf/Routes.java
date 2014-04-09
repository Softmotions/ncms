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
        this.prefix = cfg.getNcmsPrefix() + "/nj";
    }

    public void init(Router r) {
        log.info("Init routes, prefix: " + this.prefix);

        //конкретно для админ зоны ninja routing - херня
        //больше подходит для free-style сайтов с лежащими шаблонами
        //и контроллерами, но не для stateless-json-restful обмена с
        //толстым js клиентом в браузере

        route(r.GET(), "/workspace").with(WorkspaceController.class, "workspace");
    }

    protected RouteBuilder route(RouteBuilder r, String pattern) {
        if (pattern.charAt(0) != '/') {
            pattern = '/' + pattern;
        }
        return r.route(prefix + pattern);
    }


}
