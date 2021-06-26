package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Action for discarding cards
 */
@Getter
@AllArgsConstructor
public class DiscardAction implements Action {

    private final int playerId;
    private final Map<Resource, Integer> resources;
    private final GameLogicService gameLogicService;

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.discardResources(game, player, resources);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " resources:" + resources + "";
    }
}
