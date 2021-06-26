package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;

/**
 * Action for buying a development card
 */
@AllArgsConstructor
public class BuyDevelopmentAction implements Action {

    private final int playerId;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.buyCard(game, player);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
