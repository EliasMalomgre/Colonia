package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TurnTokenService {

    /**
     * Ends a player's turn
     *
     * @param game   the game, validated by GameService
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean endTurn(Game game, Player player) {
        //checks if player trying to end the turn, is the current player
        if (game.getCurrentPlayerId() != player.getPlayerId()) {
            log.warn(String.format("Game[%s]: player %d tried to end their turn, but it isn't their turn.", game.getId(), player.getPlayerId()));
            return false;
        }

        //checks if player completed all required actions
        if (player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER)||player.getRemainingActions().contains(PlayerAction.DISCARD_RESOURCES)
        ||player.getRemainingActions().contains(PlayerAction.YOP)||player.getRemainingActions().contains(PlayerAction.MONOPOLY)||
        player.getRemainingActions().contains(PlayerAction.ROAD_BUILDING) || !player.getRemainingActions().contains(PlayerAction.END_TURN)){

            log.warn(String.format("Game[%s]: player %d tried to end their turn, but they still have to use certain token(s).",game.getId(), player.getPlayerId()));
            return false;
        }

        //clears any remaining actions, that aren't required to be run, like trade and buy
        player.getRemainingActions().clear();

        //transfers newly bought cards to playable cards for next turn
        player.moveNewCards();

        //finds who the next player will be
        int nextPlayer;
        if (game.getPlayers().stream().max(Comparator.comparingInt(Player::getPlayerId)).get().getPlayerId() == player.getPlayerId()) {
            nextPlayer = 1;
        } else {
            nextPlayer = player.getPlayerId() + 1;
        }
        
        game.setTradeRequest(null);

        //sets current player to the next player in the queue
        game.setCurrentPlayerId(nextPlayer);

        return true;
    }

    /**
     * Starts a player's turn
     *
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean startTurn(Player player) {
        player.getRemainingActions().add(PlayerAction.ROLL);
        player.getRemainingActions().add(PlayerAction.PLAY_CARD);
        return true;
    }

    /**
     * adds DISCARD_RESOURCES tokens and MOVE_ROBBER token to the appropriate players
     *
     * @param game   the game, validated by GameService
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean rolledRobber(Game game, Player player) {
        if (!player.getRemainingActions().contains(PlayerAction.ROLL)) {
            log.warn(String.format("Game[%s]: player %d couldn't have rolled as they don't have the ROLL token", game.getId(), player.getPlayerId()));
            return false;
        }
        player.getRemainingActions().remove(PlayerAction.ROLL);
        for (Player playerThatHasToDiscard : game.getPlayers().stream().filter(p -> p.getResourcesTotal() > 7).collect(Collectors.toList())) {
            playerThatHasToDiscard.getRemainingActions().add(PlayerAction.DISCARD_RESOURCES);
        }
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);

        return true;
    }

    /**
     * remove's MOVE_ROBBER token from player and adds BUY and BUILD tokens
     *
     * @param game   the game, validated by GameService
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean movedRobber(Game game, Player player) {
        if (!player.getRemainingActions().contains(PlayerAction.MOVE_ROBBER)) {
            log.warn(String.format("Game[%s]: player %d doesn't have MOVE_ROBBER token, so can't remove it.", game.getId(), player.getPlayerId()));
            return false;
        }

        player.getRemainingActions().remove(PlayerAction.MOVE_ROBBER);
        player.getRemainingActions().add(PlayerAction.BUY);
        player.getRemainingActions().add(PlayerAction.BUILD);
        player.getRemainingActions().add(PlayerAction.TRADE);
        player.getRemainingActions().add(PlayerAction.END_TURN);

        return true;
    }

    /**
     * Adds MOVE_ROBBER token to the player
     *
     * @param player the player, validated by GameService
     */
    public void playedKnight(Player player) {
        player.getRemainingActions().add(PlayerAction.MOVE_ROBBER);
    }

    /**
     * takes away STEAL token from the player
     *
     * @param player the player, that has stolen a resource
     * @return true if successful
     */
    public boolean stoleResource(Player player) {
        player.getRemainingActions().remove(PlayerAction.STEAL);
        return true;
    }

    /**
     * removes ROLL token and adds BUILD and BUY tokens
     *
     * @param game   the game, validated by GameService
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean rolled(Game game, Player player) {
        if (!player.getRemainingActions().contains(PlayerAction.ROLL)) {
            log.warn(String.format("Game[%s]: player %d couldn't have rolled as they don't have the ROLL token", game.getId(), player.getPlayerId()));
            return false;
        }
        player.getRemainingActions().remove(PlayerAction.ROLL);
        player.getRemainingActions().add(PlayerAction.BUILD);
        player.getRemainingActions().add(PlayerAction.BUY);
        player.getRemainingActions().add(PlayerAction.TRADE);
        player.getRemainingActions().add(PlayerAction.END_TURN);
        return true;
    }

    /**
     * removes a ROAD_BUILDING token
     *
     * @param player the player, validated by GameService
     * @return true if successful
     */
    public boolean builtRoad(Player player) {
        player.getRemainingActions().remove(PlayerAction.ROAD_BUILDING);
        return true;
    }
}
