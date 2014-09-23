<%@ page import="com.softmotions.ncms.NcmsEnvironment" %>
<%@ page import="com.google.inject.Injector" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    String link = injector.getInstance(NcmsEnvironment.class).getAbsoluteResourceLink(request, "index.html");
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader("Location", link);
    response.flushBuffer();
%>