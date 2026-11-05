package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.modelo.CajaFuerte;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/config")
public class Configuracion extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        String num1 = req.getParameter("num1");
        String num2 = req.getParameter("num2");
        String num3 = req.getParameter("num3");



        if (nombre == null || num1 == null || num2 == null || num3 == null) {
            resp.sendError(400);
            return;
        }


        CajaFuerte cajaFuerte  = new CajaFuerte(nombre,num1,num2,num3);

        req.getSession().setAttribute("cajaFuerte",cajaFuerte);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> "+nombre+" configuracion hecha </h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();

    }
}
