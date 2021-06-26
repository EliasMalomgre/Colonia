package kdg.colonia.gameService.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AchievementDTO {
    private String gameId;
    private int playerId;
    private String achievement;
}
