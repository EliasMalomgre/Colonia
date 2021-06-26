package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Action for using a development card
 */
@Getter
@AllArgsConstructor
public class UseDCMonopolyAction implements Action {

    private final int playerId;
    private final Resource resource;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.playCard(game, player, ProgressCardType.MONOPOLY);
        gameLogicService.monopoly(game, player, resource);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " getting all " + resource;
    }
}
