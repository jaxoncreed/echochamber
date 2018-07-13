package team.o.echochamber;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {

    @PostMapping("/rule")
    public String rule() {
        return "This is the rule";
    }
}
