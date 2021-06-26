package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class GameInitService {

    private final IDiceService diceService;

    /**
     * Before the game starts, every player rolls for initiative.
     * Once all players have rolled, initial1-phase begins.
     *
     * @param game   the game, validated by GameService
     * @param player the player, validated by GameService
     * @return The resulting dice roll or null on error
     */
    public int[] rollForInitiative(Game game, Player player) {
        int[] roll = null;
        //check if player already rolled
        if (!game.getInitialRolls().containsKey(player.getUserId()) && player.getRemainingActions().contains(PlayerAction.INITROLL)) {
            roll = diceService.roll();
            game.getInitialRolls().put(player.getUserId(), Arrays.stream(roll).sum());
            player.getRemainingActions().remove(PlayerAction.INITROLL);
            if (game.getInitialRolls().size() == game.getPlayers().size()) {
                setUpGame(game);
            }
        } else {
            //ignore attempt if player already rolled
            log.warn(String.format("Received multiple initiative rolls from user \"%s\". Ignored and kept the first.", player.getUserId()));
        }
        return roll;
    }


    /**
     * Processes the initial rolls once all players have rolled, then starts initial1-phase
     *
     * @param game the game to be processed
     */
    private void setUpGame(Game game) {
        //Assign playerIds based on initial rolls
        Map<String, Integer> initialRolls = game.getInitialRolls();
        initialRolls = sortByValue(initialRolls);
        int playerId = 1;
        for (String s : initialRolls.keySet()) {
            game.getPlayers().stream().filter(p -> p.getUserId().equals(s)).findFirst().get().setPlayerId(playerId);
            log.info("Game[{}]: 'Player {}' is User[{}]", game.getId(), playerId, s);
            playerId++;
        }

        game.getPlayers().stream().filter(p -> p.getPlayerId() == 1).findFirst().ifPresent(c -> c.getRemainingActions().add(PlayerAction.INITIAL1));
        game.setCurrentPlayerId(1);
    }

    //Generic method to sort maps by value
    //used in setUpGame
    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * This methods gives the INITIAL1 and INITIAL2 tokens to the next player in line
     *
     * @param game            the game object of the current game, already validated by calling method
     * @param currentPlayerId the sequential ID of the current player
     */
    public boolean passInitialTurn(Game game, int currentPlayerId, Player currentPlayer) {
        if (currentPlayer.getRemainingActions().contains(PlayerAction.INITIAL1)) {
            if (currentPlayerId < game.getPlayers().size()) {
                currentPlayer.getRemainingActions().remove(PlayerAction.INITIAL1);
                game.getPlayers().stream().filter(p -> p.getPlayerId() == (currentPlayerId + 1)).findFirst().ifPresent(c -> c.getRemainingActions().add(PlayerAction.INITIAL1));
                game.getPlayers().stream().filter(p -> p.getPlayerId() == currentPlayerId).findFirst().ifPresent(c -> c.setRemainingActions(currentPlayer.getRemainingActions()));
                game.setCurrentPlayerId(currentPlayerId + 1);
            }

            else {
                currentPlayer.getRemainingActions().remove(PlayerAction.INITIAL1);
                currentPlayer.getRemainingActions().add(PlayerAction.INITIAL2);
                game.getPlayers().stream().filter(p -> p.getPlayerId() == currentPlayerId).findFirst().ifPresent(c -> c.setRemainingActions(currentPlayer.getRemainingActions()));
            }
        }
        else if (currentPlayer.getRemainingActions().contains(PlayerAction.INITIAL2)) {
            if (currentPlayerId > 1) {
                currentPlayer.getRemainingActions().remove(PlayerAction.INITIAL2);
                game.getPlayers().stream().filter(p -> p.getPlayerId() == (currentPlayerId - 1)).findFirst().ifPresent(c -> c.getRemainingActions().add(PlayerAction.INITIAL2));
                game.getPlayers().stream().filter(p -> p.getPlayerId() == currentPlayerId).findFirst().ifPresent(c -> c.setRemainingActions(currentPlayer.getRemainingActions()));
                game.setCurrentPlayerId(currentPlayerId - 1);
            }

            else {
                //pass to the first real turn of the game
                game.getPlayers().stream().filter(p -> p.getPlayerId() == currentPlayerId).findFirst().ifPresent(c -> c.getRemainingActions().clear());
                return false;
            }
        }

        else {
            //a player without the token tried to perform an action
            log.error(String.format("Game[%s]: player %d tried to play out of turn", game.getId(), currentPlayer.getPlayerId()));
        }
        return true;
    }
}
