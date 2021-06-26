package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;

/**
 * Action for using a development card
 */
@AllArgsConstructor
public class UseDCYearOfPlentyAction implements Action {

    private final int playerId;
    private final Resource resource1;
    private final Resource resource2;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.playCard(game, player, ProgressCardType.YEAR_OF_PLENTY);
        gameLogicService.yearOfPlenty(game, player, resource1, resource2);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " for " + resource1 + " and " + resource2;
    }
}
