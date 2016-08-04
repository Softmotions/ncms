package com.softmotions.ncms.security;

import com.softmotions.ncms.HttpTestResponse;
import com.softmotions.ncms.NcmsWebTest;
import com.softmotions.web.security.WSRole;
import com.softmotions.web.security.WSUser;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class FakeSecurityEnvTest extends NcmsWebTest {

    public FakeSecurityEnvTest() {
        super("username=admin," +
              "usersdb=com/softmotions/ncms/test-users-db.xml");
    }

    public void initServer(Server server, ServletContextHandler context) {
        super.initServer(server, context);
        context.addServlet(TestResponseServlet.class, "/testresp");
    }

    @Test
    public void testFakeCreds() throws Exception {
        HttpTestResponse resp = testBrowser.makeGET(getServerAddress() + "/testresp");
        assertEquals(200, resp.statusCode);
        assertEquals("UTF-8", resp.charset);
        String respStr = resp.toString();
        assertTrue(respStr.contains("da32fcbab14b46e79497d4660958902d"));
        assertTrue(respStr.contains("remoteUser=admin"));
        assertTrue(respStr.contains("isUserInRole.admin=true"));
        assertTrue(respStr.contains("isUserInRole.user=true"));
        assertTrue(respStr.contains("isUserInRole.foo=false"));
        assertTrue(respStr.contains("roleNames=[admin, user"));
    }

    public static class TestResponseServlet extends HttpServlet {

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("da32fcbab14b46e79497d4660958902d");
            out.println("remoteUser=" + req.getRemoteUser());
            out.println("isUserInRole.admin=" + req.isUserInRole("admin"));
            out.println("isUserInRole.user=" + req.isUserInRole("user"));
            out.println("isUserInRole.foo=" + req.isUserInRole("foo"));
            WSUser user = (WSUser) req.getUserPrincipal(); //FIXME
            List<String> roleNames = new ArrayList<>();
            Iterator<WSRole> roles = user.getRoles();
            while (roles.hasNext()) {
                roleNames.add(roles.next().getName());
            }
            Collections.sort(roleNames);
            out.println("roleNames=" + roleNames);
        }
    }
}

