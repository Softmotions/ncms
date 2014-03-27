package com.softmotions.ncms.asm;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Asm handler.
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class AsmServlet extends HttpServlet {

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("Hello world!!!!");
        resp.getWriter().flush();
    }
}
