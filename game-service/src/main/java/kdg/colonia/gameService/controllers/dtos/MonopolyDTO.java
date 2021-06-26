package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.Resource;

public class MonopolyDTO {
    public final String gameId;
    public final int playerId;
    public final Resource resource;

    public MonopolyDTO(String gameId, int playerId, Resource resource) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.resource = resource;
    }
}
