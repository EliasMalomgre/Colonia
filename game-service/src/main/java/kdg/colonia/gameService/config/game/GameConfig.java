package kdg.colonia.gameService.config.game;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "game")
public class GameConfig {
    private int victoryPointsWin;

    private int resourceGainSettlement;
    private int bonusIfCity;

    private int attemptsBeforeAutoDiscard;
    private int timeBetweenAttempts;

    private boolean onlyAIGameExperimental;
    private boolean performAIOnFreshGame;
}
