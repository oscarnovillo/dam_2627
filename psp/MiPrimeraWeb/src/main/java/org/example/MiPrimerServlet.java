package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/index")
public class MiPrimerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String s = req.getParameter("alumno");
        if (s==null){

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"ASver si te entreras cabron, parametro requertido");
            return;
        }

        int nomrbe = Integer.parseInt(s);


        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>MiPrimerServlet "+nomrbe+"</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>MiPrimerServlet"+nomrbe+"</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();

    }
}
