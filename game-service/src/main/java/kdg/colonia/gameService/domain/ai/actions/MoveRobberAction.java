package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MoveRobberAction implements Action {
    private final int playerId;
    private final Coordinate newCoordinate;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.moveRobber(game, player, newCoordinate);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " to " + newCoordinate;
    }
}
