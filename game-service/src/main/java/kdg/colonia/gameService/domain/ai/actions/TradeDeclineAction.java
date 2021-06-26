package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.services.TradeService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeDeclineAction implements Action {

    private final int playerId;
    private final TradeService tradeService;

    @Override
    public Game performAction(Game game) {

        Player askingPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == game.getTradeRequest().getAskingPlayer()).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        Player receivingPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        tradeService.declineTradeRequest(game, askingPlayer, receivingPlayer);

        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
