package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StealAction implements Action {

    private final int playerId;
    private final int playerToStealFromId;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        Player playerToStealFrom = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerToStealFromId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.stealResources(game, player, playerToStealFrom);
        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " on player " + playerToStealFromId;
    }
}
