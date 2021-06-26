package kdg.colonia.gameService.config.costobjects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "road")
public class RoadConfig
{
    private int woolCost;
    private int oreCost;
    private int grainCost;
    private int brickCost;
    private int lumberCost;
}
