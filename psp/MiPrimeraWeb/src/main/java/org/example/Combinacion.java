package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.modelo.CajaFuerte;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/combinacion")
public class Combinacion extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String num = req.getParameter("num");
        if (num == null) {
            resp.sendError(400);
            return;
        }
        CajaFuerte caja = (CajaFuerte) req.getSession().getAttribute("cajaFuerte");
        if (caja == null) {

            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "falta config");
            return;
        }

        boolean error = true;
        switch (caja.getStart()) {
            case 1:
                if (num.equals(caja.getNum1())){
                    caja.setStart(2);
                    error = false;
                }

                break;
            case 2:
                if (num.equals(caja.getNum2())){
                    caja.setStart(3);
                    error = false;
                }
                else {
                    caja.setStart(1);
                }
                break;
            case 3:
                if (num.equals(caja.getNum3())){
                    caja.setStart(3);
                    caja.setAbierta(true);
                    error = false;
                }
                else {
                    caja.setStart(1);
                }

                break;

        }

        String mensaje = error ? "fallo combinacion" : "sigue";

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> "+mensaje+"</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();

    }
}
