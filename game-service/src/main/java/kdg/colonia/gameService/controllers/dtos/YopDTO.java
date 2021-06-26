package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.Resource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YopDTO {
    public String gameId;
    public int playerId;
    public Resource resource1;
    public Resource resource2;

    public YopDTO(String gameId, int playerId, Resource resource1, Resource resource2) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.resource1 = resource1;
        this.resource2 = resource2;
    }
}
