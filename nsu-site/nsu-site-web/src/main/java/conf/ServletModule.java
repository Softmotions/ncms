package conf;

import com.softmotions.ncms.NcmsConfiguration;
import com.softmotions.ncms.NcmsServletModule;
import com.softmotions.ncms.asm.render.AsmServlet;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class ServletModule extends NcmsServletModule {

    protected void initAsmServlet(NcmsConfiguration cfg) {
    }

    protected void initAfter(NcmsConfiguration cfg) {
        //Assembly rendering servlet
        serve("/*", AsmServlet.class);
    }
}
