package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;

/**
 * Action for building a city
 */
@AllArgsConstructor
public class BuildCityAction implements Action {
    private final int playerId;
    private final Coordinate buildCoordinate;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.upgradeSettlementToCity(game, player, buildCoordinate);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " at " + buildCoordinate;
    }
}
