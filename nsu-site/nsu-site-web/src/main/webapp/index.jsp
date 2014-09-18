<%@ page import="com.softmotions.ncms.NcmsConfiguration" %>
<%@ page import="com.google.inject.Injector" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    NcmsConfiguration cfg = injector.getInstance(NcmsConfiguration.class);

    boolean preferRequestUrl = cfg.impl().getBoolean("site-root[@preferRequestUrl]", false);
    String link = request.getScheme() + "://" +
                  request.getServerName() +
                  ":" + request.getServerPort() +
                  cfg.getNcmsPrefix() +
                  "/asm/index.html";
    if (!preferRequestUrl) {
        link = cfg.impl().getString("site-root", link);
    }
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", link);
    response.flushBuffer();
%>