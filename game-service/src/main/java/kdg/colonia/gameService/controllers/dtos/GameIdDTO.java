package kdg.colonia.gameService.controllers.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameIdDTO {
    private String gameId;

    public GameIdDTO(String gameId) {
        this.gameId = gameId;
    }
}
