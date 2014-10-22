<%@ page import="com.softmotions.ncms.NcmsEnvironment" %>
<%@ page import="com.google.inject.Injector" %>
<%@ page import="org.apache.commons.configuration.Configuration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <link REL="shortcut icon" HREF="/images/nsu_ico.png" TYPE="image/png">
    <style type="text/css">
        body {
            margin: 0;
        }

        .login-button {
            -moz-box-shadow: inset 0px 1px 0px 0px #f29c93;
            -webkit-box-shadow: inset 0px 1px 0px 0px #f29c93;
            box-shadow: inset 0px 1px 0px 0px #f29c93;
            background: -webkit-gradient(linear, left top, left bottom, color-stop(0.05, #fe1a00), color-stop(1, #ce0100));
            background: -moz-linear-gradient(center top, #fe1a00 5%, #ce0100 100%);
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#fe1a00', endColorstr='#ce0100');
            background-color: #fe1a00;
            -webkit-border-top-left-radius: 10px;
            -moz-border-radius-topleft: 10px;
            border-top-left-radius: 10px;
            -webkit-border-top-right-radius: 10px;
            -moz-border-radius-topright: 10px;
            border-top-right-radius: 10px;
            -webkit-border-bottom-right-radius: 10px;
            -moz-border-radius-bottomright: 10px;
            border-bottom-right-radius: 10px;
            -webkit-border-bottom-left-radius: 10px;
            -moz-border-radius-bottomleft: 10px;
            border-bottom-left-radius: 10px;
            text-indent: 0px;
            display: inline-block;
            color: #ffffff;
            font-family: Times New Roman;
            font-size: 15px;
            font-weight: normal;
            font-style: normal;
            height: 50px;
            line-height: 50px;
            width: 250px;
            text-decoration: none;
            text-align: center;
            text-shadow: 1px 0px 0px #b23e35;

        }

        .login-button:hover {
            background: -webkit-gradient(linear, left top, left bottom, color-stop(0.05, #ce0100), color-stop(1, #fe1a00));
            background: -moz-linear-gradient(center top, #ce0100 5%, #fe1a00 100%);
            filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ce0100', endColorstr='#fe1a00');
            background-color: #ce0100;
        }

        .login-button:active {
            position: relative;
            top: 1px;
        }

        #page {
            position: absolute;
           	top: 50%;
            left: 50%;
            margin: 0;
            padding: 0;
        }

        #box {
            margin: -25px 0 0 -125px;
        }
    </style>
</head>
<body>
<%
    StringBuffer requestURL = request.getRequestURL();
    String rootUrl = requestURL.substring(0, requestURL.length() - request.getRequestURI().length());
    if (request.getUserPrincipal() != null) {
        response.sendRedirect(rootUrl);
    }
    response.addHeader("X-Softmotions-Login", "true");
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    NcmsEnvironment ncmsCfg = injector.getInstance(NcmsEnvironment.class);
    Configuration cfg = ncmsCfg.xcfg().subset("oauth2");

    String redirectUrl = rootUrl + "/j_security_check";
    String oauth2LoginUrl = cfg.getString("provider.auth-endpoint") + "?" +
                            "client_id" + "=" + URLEncoder.encode(cfg.getString("client.id"), "UTF-8") + "&" +
                            "response_type" + "=" + "code" + "&" +
                            "redirect_uri" + "=" + URLEncoder.encode(redirectUrl, "UTF-8");
%>
<div id="page">
    <div id="box">
        <a class="login-button" href="<%=oauth2LoginUrl%>">Авторизоваться в портале my.nsu.ru</a>
    </div>
</div>
</body>
</html>

