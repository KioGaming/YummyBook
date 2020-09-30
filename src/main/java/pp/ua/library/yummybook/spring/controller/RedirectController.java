package pp.ua.library.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log
public class RedirectController {

    @RequestMapping(value = {"", "/index"})
    public String baseUrlRedirect() {
        return "index";
    }
}