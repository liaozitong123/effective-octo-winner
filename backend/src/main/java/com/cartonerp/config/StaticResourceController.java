package com.cartonerp.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves index.html for all non-API routes so Vue Router can handle them.
 */
@Controller
public class StaticResourceController {

    @RequestMapping(value = {
        "/dashboard",
        "/sales/**",
        "/production/**",
        "/purchase/**",
        "/warehouse/**",
        "/finance/**",
        "/login"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
