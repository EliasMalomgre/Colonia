package kdg.colonia.gameService.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class NewMessageDTO {
    private final String gameId;
    private final int playerId;
    private final String playerName;
    private final String message;
}
