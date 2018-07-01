package team.o.echochamber;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.*;

@RestController
public class PingController {
    private static final String response = "Hello %s!";

    @RequestMapping("/ping")
    public String ping(@RequestParam(value="name", defaultValue="World") String name) {
        return String.format(response, name);
    }
}
