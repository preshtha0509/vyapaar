package com.vyapaar.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaFallbackController {

    @RequestMapping({"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/{subpath:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
