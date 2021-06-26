package kdg.colonia.gameService.controllers.dtos;

import kdg.colonia.gameService.domain.Resource;

import java.util.Map;

public class DiscardResourcesDTO {
    public final String gameId;
    public final int playerId;
    public final Map<Resource, Integer> discardedResources;

    public DiscardResourcesDTO(String gameId, int playerId, Map<Resource, Integer> discardedResources) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.discardedResources = discardedResources;
    }
}
