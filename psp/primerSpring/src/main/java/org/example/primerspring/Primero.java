package org.example.primerspring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/primer")
public class Primero {


    @RequestMapping
    public String mostrarPagina(Model model) {
        model.addAttribute("titulo", "Mensaje");
        return "primero";
    }
}
