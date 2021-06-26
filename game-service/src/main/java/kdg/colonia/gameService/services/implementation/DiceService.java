package kdg.colonia.gameService.services.implementation;

import kdg.colonia.gameService.services.IDiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class DiceService implements IDiceService {

    private final int amount;
    private final int faces;

    /**
     * This constructor creates the service and initializes the amount and type of dice used for the random rolls.
     * @param amount is the amount of dice that will be rolled
     * @param faces is the amount of faces/sides each die has
     * */
    public DiceService(int amount, int faces) {
        if (amount >= 1) {
            this.amount = amount;
        } else {
            this.amount = 2;
        }

        if (faces >= 1) {
            this.faces = faces;
        } else {
            this.faces = 6;
        }
    }

    public DiceService(int amount) {
        this(amount, 6);
    }

    public DiceService() {
        this(2);
    }

    /**
     * This method rolls the dice initialized by DiceService
     * @return an array of individual outcomes for rolling the dice -> 1-6.
     * */
    @Override
    public int[] roll() {
        int[] rolls = new int[amount];
        for (int i = 0; i < rolls.length; i++) {
            rolls[i] = ThreadLocalRandom.current().nextInt(faces) + 1;
        }
        return rolls;
    }

}
