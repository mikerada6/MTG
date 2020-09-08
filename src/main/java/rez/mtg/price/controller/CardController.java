package rez.mtg.price.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/cards")
public
class CardController {

    @GetMapping(path = "/")
    public @ResponseBody
    String testEndPoint() {
        return "Done!!!";
    }
}
