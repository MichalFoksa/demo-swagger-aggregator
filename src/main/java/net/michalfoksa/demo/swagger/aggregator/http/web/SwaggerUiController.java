package net.michalfoksa.demo.swagger.aggregator.http.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Home redirection to swagger api documentation
 */
@Controller
public class SwaggerUiController {
    @RequestMapping(value = "/api")
    public String index() {
        return "redirect:swagger-ui.html";
    }
}
