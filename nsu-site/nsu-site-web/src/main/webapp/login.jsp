<%@ page import="com.google.inject.Injector" %>
<%@ page import="com.softmotions.ncms.NcmsConfiguration" %>
<%@ page import="org.apache.commons.configuration.Configuration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.apache.catalina.authenticator.Constants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    StringBuffer requestURL = request.getRequestURL();
    String rootUrl = requestURL.substring(0, requestURL.length() - request.getRequestURI().length());

    if (request.getUserPrincipal() != null) {
        response.sendRedirect(rootUrl);
    }

    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    NcmsConfiguration ncmsCfg = injector.getInstance(NcmsConfiguration.class);
    Configuration cfg = ncmsCfg.impl().subset("oauth2-authorize");

    String redirectUrl = rootUrl + Constants.FORM_ACTION;
    String oauth2LoginUrl = cfg.getString("provider.auth-endpoint") + "?" +
                            "client_id" + "=" + URLEncoder.encode(cfg.getString("client.id"), "UTF-8") + "&" +
                            "response_type" + "=" + "code" + "&" +
                            "redirect_uri" + "=" + URLEncoder.encode(redirectUrl, "UTF-8");
%>

<a href="<%=oauth2LoginUrl%>">Авторизоваться в портале my.nsu</a>
