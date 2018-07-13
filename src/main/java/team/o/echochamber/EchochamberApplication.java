package team.o.echochamber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EchochamberApplication {

	public static void main(String[] args) {
		SpringApplication.run(EchochamberApplication.class, args);

		try {
            EchochamberDatastore datastore = new EchochamberDatastore("file.owl");
            datastore.addRule("ec:AddedAction(?action) ^ ec:actionTriple(?action, ?actionTriple) ^ " +
                    "ec:p(?actionTriple, hasMessage) ^ ec:s(?actionTriple, ?chatRoom) ^ " +
                    "memberOf(?user, ?chatRoom) ^ chatWebhook(?user, ?webhook)  -> sqwrl:select(?webhook)");
            datastore.addTriple("ChatroomB", "hasMessage", "Hello");
		} catch (Exception e) {
		    System.err.println(e);
        }
    }
}
