package kdg.colonia.gameService.domain.ai.actions;


import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.GameLogicService;
import lombok.Getter;

@Getter
public class ChanceRollDiceAction extends ChanceAction {

    private final int roll;
    private final int playerId;
    private final GameLogicService gameLogicService;

    public ChanceRollDiceAction(int roll, int playerId, double probability, GameLogicService gameLogicService) {
        super(probability);
        this.roll = roll;
        this.playerId = playerId;
        this.gameLogicService = gameLogicService;
    }

    @Override
    public Game performAction(Game game) {
        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        gameLogicService.processDiceRoll(game, player, roll);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
