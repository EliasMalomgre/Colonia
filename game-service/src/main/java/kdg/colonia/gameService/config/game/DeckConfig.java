package kdg.colonia.gameService.config.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "deck")
public class DeckConfig {
    private int knightAmount;
    private int yopAmount;
    private int monopolyAmount;
    private int roadBuildingAmount;
    private int victoryAmount;
}