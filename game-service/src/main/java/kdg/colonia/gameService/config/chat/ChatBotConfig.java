package kdg.colonia.gameService.config.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "chat")
public class ChatBotConfig {
    private boolean enabled;
    private double chance;
    private double playerSpecificChance;
}
