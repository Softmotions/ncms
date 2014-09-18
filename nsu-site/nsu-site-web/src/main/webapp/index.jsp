<%@ page import="com.softmotions.ncms.NcmsConfiguration" %>
<%@ page import="com.google.inject.Injector" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    NcmsConfiguration cfg = injector.getInstance(NcmsConfiguration.class);
    String link = cfg.impl().getString("site-root",
                                       request.getScheme() + "://" +
                                       request.getServerName() +
                                       ":" + request.getServerPort()) +
                  cfg.getAsmRoot() +
                  "index.html";
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", link);
    response.flushBuffer();
%>