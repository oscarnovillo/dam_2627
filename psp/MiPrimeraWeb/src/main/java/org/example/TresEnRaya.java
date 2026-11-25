package org.example;

import config.Constants;
import config.ThymeleafConstants;
import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.net.http.HttpClient;

@WebServlet("/tres")
public class TresEnRaya extends HttpServlet {


    public static final String BASE_3_EN_RAYA = "-";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String marca = req.getParameter("marca");
        String coordenada = req.getParameter("coordenada");
        var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);

        if (marca==null || coordenada==null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"ASver si te entreras cabron, parametro requertido");
            return;
        }
        String turno= (String) req.getSession().getAttribute("Turno");
        if (turno==null) {
            turno = "X";
        }

        if (!turno.equalsIgnoreCase(marca)) {
            ctx.setVariable("error", "el turno es de "+turno);
        }


        String[] tablero = (String[]) req.getSession().getAttribute("Tablero");
        if (tablero==null) {
            tablero = new String[]{BASE_3_EN_RAYA, "-", "-", "-", "-", "-", "-", "-", "-"};
            req.getSession().setAttribute("Tablero",tablero);
        }

        if (!tablero[Integer.parseInt(coordenada)-1].equalsIgnoreCase(BASE_3_EN_RAYA)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"ASver si te entreras cabron, parametro requertido");
            return;
        }

        tablero[Integer.parseInt(coordenada)-1]=marca;

        String fila1 = tablero[0]+ " " + tablero[1]+ " " + tablero[2];
        String fila2 = tablero[3]+ " " + tablero[4]+ " " + tablero[5];
        String fila3 = tablero[6]+ " " + tablero[7] + " " + tablero[8];


        ctx.setVariable("fila1", fila1);
        ctx.setVariable("fila2", fila2);
        ctx.setVariable("fila3", fila3);
        resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
        ((TemplateEngine)getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
                .process(UrlConstants.TEMPLATE_TRES_EN_RAYA, ctx, resp.getWriter());

        turno = turno.equalsIgnoreCase("X")?"O":"X";
        req.getSession().setAttribute("Turno",turno);

    }


}
