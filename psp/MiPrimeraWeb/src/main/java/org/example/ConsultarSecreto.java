package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.modelo.CajaFuerte;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/consultar")
public class ConsultarSecreto extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombre = req.getParameter("nombreSecreto");

        if (nombre==null ) {

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"ASver si te entreras cabron, parametro requertido");
            return;
        }

        CajaFuerte caja = (CajaFuerte)req.getSession().getAttribute("cajaFuerte");
        if (caja==null) {

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"falta config");
            return;
        }

        if (!caja.isAbierta())
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"primero abrela");
            return;
        }



        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> secreto guardado"+caja.getSecretos().get(nombre)+"</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();

    }
}
