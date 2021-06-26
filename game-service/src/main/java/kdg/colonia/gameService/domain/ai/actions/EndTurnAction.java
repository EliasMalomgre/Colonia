package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.GameLogicService;
import kdg.colonia.gameService.services.TurnTokenService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EndTurnAction implements Action {
    private final int playerId;
    private final GameLogicService gameLogicService;
    private final TurnTokenService turnTokenService;
    private final GameConfig gameConfig;

    @Override
    public Game performAction(Game game) {

        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid playerId given for action"));

        turnTokenService.endTurn(game, player);

        //check for 10 victory points
        if (game.getPlayers().stream().anyMatch(p -> p.getVictoryPointsAmount() >= gameConfig.getVictoryPointsWin())) {
            gameLogicService.endGame(game);
        } else {

            Player nextPlayer = game.getPlayers().stream()
                    .filter(p -> p.getPlayerId() == game.getCurrentPlayerId()).findFirst().orElseThrow();

            turnTokenService.startTurn(nextPlayer);
        }
        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}