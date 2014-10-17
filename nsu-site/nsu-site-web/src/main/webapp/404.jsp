<%@ page import="com.softmotions.ncms.NcmsEnvironment" %>
<%@ page import="com.softmotions.ncms.asm.PageService" %>
<%@ page import="com.google.inject.Injector" %>
<%@ page import="com.softmotions.ncms.NcmsMessages" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Injector injector = (Injector) request.getServletContext().getAttribute(Injector.class.getName());
    PageService pageService = injector.getInstance(PageService.class);
    NcmsEnvironment env = injector.getInstance(NcmsEnvironment.class);
    NcmsMessages messages = injector.getInstance(NcmsMessages.class);
    String indexLink = env.getAbsoluteLink(request, pageService.resolveResourceLink("index.html"));
    String searchLink = env.getAbsoluteLink(request, pageService.resolveResourceLink("search"));
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

        form {
            width: 322px;
        }

        fieldset {
            width: 316px;
            margin: 0;
            padding: 2px;
            border: thin solid grey;
        }

        input[type=text] {
            float: left;
            width: 260px;
            margin: 0;
            background: none;
            padding: 1px 13px;
            border: 0;
            color: #343434;
            font: 15px/18px Arial, Helvetica, sans-serif;
        }

        input[type=submit] {
            float: right;
            width: 16px;
            height: 16px;
            background: url(/images/btn-submit.png) no-repeat;
            text-indent: -9999px;
            overflow: hidden;
            padding: 0;
            margin: 3px 5px 0 5px;
            border: 0;
            cursor: pointer;
            font-size: 0;
            line-height: 0;
        }
    </style>
</head>
<body>
<div class="page">
    <div class="box">
        <a href="<%=indexLink%>"><img src="/images/logo2.png"/></a>

        <p><%=messages.get("ncms.error.status.404", request)%></p>

        <form action="<%=searchLink%>">
            <fieldset>
                <input type="text" name="spc.text" placeholder=""/>
                <input type="submit" value=""/>
            </fieldset>
        </form>

    </div>
</div>
</body>


