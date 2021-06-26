package kdg.colonia.gameService.domain.ai.actions;


import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.services.GameLogicService;

public class ChanceStealAction extends ChanceAction {

    private final int robberId;
    private final int playerIdToStealFrom;
    private final Resource resource;
    private final GameLogicService gameLogicService;

    public ChanceStealAction(double probability, int robberId, int playerIdToStealFrom, Resource resource, GameLogicService gameLogicService) {
        super(probability);
        this.robberId = robberId;
        this.playerIdToStealFrom = playerIdToStealFrom;
        this.resource = resource;
        this.gameLogicService = gameLogicService;
    }

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == robberId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        Player playerToStealFrom = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerIdToStealFrom).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.stealResourcesNotRandom(game, player, playerToStealFrom, resource);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " against Player[" + playerIdToStealFrom + "], got " + resource;
    }
}
