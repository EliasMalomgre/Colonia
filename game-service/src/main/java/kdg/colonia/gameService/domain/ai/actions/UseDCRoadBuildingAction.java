package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;

/**
 * Action for using a development card
 */
@AllArgsConstructor
public class UseDCRoadBuildingAction implements Action {

    private final int playerId;
    private final Coordinate coordinate;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.playCard(game, player, ProgressCardType.ROAD_BUILDING);
        gameLogicService.build(game, player, coordinate);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " places 1st at " + coordinate;
    }
}
