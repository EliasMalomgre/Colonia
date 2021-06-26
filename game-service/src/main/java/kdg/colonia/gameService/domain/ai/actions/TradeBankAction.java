package kdg.colonia.gameService.domain.ai.actions;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.services.TradeService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Action for trading with the bank
 */
@Getter
@RequiredArgsConstructor
public class TradeBankAction implements Action {

    private final TradeService tradeService;
    private final int playerId;
    private final Resource from;
    private final Resource to;

    @Override
    public Game performAction(Game game) {
        Player currentPlayer = game.getPlayers().stream()
                .filter(p -> p.getPlayerId() == playerId).findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Invalid playerId given for action"));

        tradeService.tradeWithBank(game, currentPlayer, from, to);
        return game;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " selling " + from + " for " + to;
    }
}
