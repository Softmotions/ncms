<%@ page import="com.softmotions.ncms.NcmsEnvironment" %>
<%@ page import="com.softmotions.ncms.asm.PageService" %>
<%@ page import="com.google.inject.Injector" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    PageService pageService = injector.getInstance(PageService.class);
    NcmsEnvironment env = injector.getInstance(NcmsEnvironment.class);
    String indexLink = env.getAbsoluteLink(request, pageService.resolveResourceLink("index.html"));
%>
<!DOCTYPE html>
<html>
<head>
    <title>Страница не найдена | Новосибирский государственный университет</title>
    <link REL="shortcut icon" HREF="/images/nsu_ico.png" TYPE="image/png">
    <style type="text/css">
        body {
            margin: 0;
            color: #515151;
            font: 15px/22px Arial, Helvetica, sans-serif;
        }

        .page {
            position: absolute;
            top: 50%;
            left: 50%;
            margin: 0;
            padding: 0;
        }

        .box {
            margin: -100px 0 0 -161px;
            width: 322px;
            height: 200px;
            padding: 0;
        }

        .box > a {
            width: 322px;
            height: 100px;
            margin: 0;
        }

        .box > a > img  {
            width: 214px;
            margin: 0 54px;
        }

        .box > p {
            margin: 5px 0;
            padding: 0;
            text-align: center;
        }
    </style>
</head>
<body>
<div class="page">
    <div class="box">
        <a href="<%=indexLink%>"><img src="/images/logo2.png"/></a>

        <p>
            Веб-сервер понял запрос, но выполнить его не может так как у Вас нет необходимых прав.
        </p>
    </div>
</div>
</body>


