<%@ page import="com.softmotions.ncms.NcmsEnvironment" %>
<%@ page import="com.softmotions.ncms.asm.PageService" %>
<%@ page import="com.google.inject.Injector" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    PageService pageService = injector.getInstance(PageService.class);
    NcmsEnvironment env = injector.getInstance(NcmsEnvironment.class);
    String link = env.getAbsoluteLink(request, pageService.resolveResourceLink("index.html"));
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", link);
    response.flushBuffer();
%>