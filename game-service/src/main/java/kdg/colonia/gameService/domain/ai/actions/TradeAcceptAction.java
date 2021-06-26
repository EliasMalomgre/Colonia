package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.TradeService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeAcceptAction implements Action {

    private final int playerId;
    private final TradeService tradeService;

    @Override
    public Game performAction(Game game) {

        Player askingPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == game.getTradeRequest().getAskingPlayer()).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        Player player = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        tradeService.acceptTradeRequest(game, askingPlayer, player);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
