package kdg.colonia.gameService.utilities;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SassyExceptionMessageGenerator {

    private final ArrayList<String> messages = new ArrayList<>(
            Arrays.asList(
                    "Someone naughty tried to access my bits!",
                    "Yikes, just received an illegal request!",
                    "Oh noes, you're not allowed to do that!",
                    "Wait your turn, you antsy pants!"
            )
    );

    public String generateException(String err){
        return messages.get(ThreadLocalRandom.current().nextInt(messages.size())) + " " + err;
    }

}
