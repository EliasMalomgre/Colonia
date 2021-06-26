package kdg.colonia.gameService.config.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "board")
public class BoardCreationConfig {
    private int fieldAmount;
    private int forestAmount;
    private int plainsAmount;
    private int hillsAmount;
    private int mountainAmount;
    private int desertAmount;

    private int twoAmount;
    private int threeAmount;
    private int fourAmount;
    private int fiveAmount;
    private int sixAmount;
    private int eightAmount;
    private int nineAmount;
    private int tenAmount;
    private int elevenAmount;
    private int twelveAmount;

    private int woolHarbourAmount;
    private int brickHarbourAmount;
    private int grainHarbourAmount;
    private int lumberHarbourAmount;
    private int oreHarbourAmount;
    private int genericHarbourAmount;

    private int smallestRow;
    private int largestRow;
    private int largestRowNumber;
    private int totalAmountOfTiles;
}
