package it.pagopa.pn.externalchannels.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("")
    public String home() {
        return "Sono Vivo";
    }


}
