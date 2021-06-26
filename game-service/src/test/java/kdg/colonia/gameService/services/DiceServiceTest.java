package kdg.colonia.gameService.services;

import kdg.colonia.gameService.services.implementation.DiceService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DiceServiceTest {

    @Test
    void roll1die() {
        DiceService service = new DiceService(1, 6);

        int runs = 10000;
        int failedRuns = 0;
        for (int i = 0; i < runs; i++) {
            int roll = Arrays.stream(service.roll()).sum();
            if (roll < 1 || roll > 6) {
                failedRuns++;
            }
        }
        assertEquals(0, failedRuns, String.format("Out of %d runs, %d failed.", runs, failedRuns));
    }

    @Test
    void roll2dice() {
        DiceService service = new DiceService(2, 6);

        int runs = 10000;
        int failedRuns = 0;
        for (int i = 0; i < runs; i++) {
            int roll = Arrays.stream(service.roll()).sum();
            if (roll < 2 || roll > 12) {
                failedRuns++;
            }
        }
        assertEquals(0, failedRuns, String.format("Out of %d runs, %d failed.", runs, failedRuns));
    }

    @Test
    void roll3dice() {
        DiceService service = new DiceService(3, 6);

        int runs = 10000;
        int failedRuns = 0;
        for (int i = 0; i < runs; i++) {
            int roll = Arrays.stream(service.roll()).sum();
            if (roll < 3 || roll > 18) {
                failedRuns++;
            }
        }
        assertEquals(0, failedRuns, String.format("Out of %d runs, %d failed.", runs, failedRuns));
    }

    @Test
    void roll2diceWith4faces() {
        DiceService service = new DiceService(2, 4);

        int runs = 10000;
        int failedRuns = 0;
        for (int i = 0; i < runs; i++) {
            int roll = Arrays.stream(service.roll()).sum();
            if (roll < 2 || roll > 8) {
                failedRuns++;
            }
        }
        assertEquals(0, failedRuns, String.format("Out of %d runs, %d failed.", runs, failedRuns));
    }

    @Test
    void rollDistribution() {
        DiceService service = new DiceService(1, 6);
        int runs = 1000000;
        int[] rolls = new int[6];

        for (int i = 0; i < runs; i++) {
            rolls[Arrays.stream(service.roll()).sum() - 1]++;
        }

        for (int i = 0; i < rolls.length; i++) {
            double frequency = (double) rolls[i] / (double) runs;
            assertTrue((frequency > 0.12 && frequency < 0.22), String.format("Expected frequency was 0.1667, [%s] had a frequency of %5.4f. Random generator may be defective.", i + 1, frequency));
        }
    }
}