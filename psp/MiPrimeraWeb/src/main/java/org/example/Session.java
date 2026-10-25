package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/session")
public class Session extends HttpServlet {

    public Session() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String nombre;
        if (req.getSession().getAttribute("id") == null) {
            nombre = req.getParameter("nombre");
            req.getSession().setAttribute("id", nombre);
        }
        else {
            nombre = (String) req.getSession().getAttribute("id");
        }

        resp.getWriter().println("<h1>" + nombre + "</h1>");

    }
}
