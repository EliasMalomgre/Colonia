package kdg.colonia.gameService.domain;

import kdg.colonia.gameService.domain.devCard.ProgressCard;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Document("games")
@Component
public class Game {
    @Id
    private String id;
    private ArrayList<Player> players;
    private Board board;
    private Map<String, Integer> initialRolls;
    private int currentPlayerId;
    private String hostId;
    private GameState gameState;
    private int playerIdWithLongestRoad;
    private int playerWithLargestArmy;
    private TradeRequest tradeRequest;
    private List<ProgressCard> cardPile;

    public Game() {
        this.initialRolls = new HashMap<>();
        this.players = new ArrayList<>();
        this.cardPile = new ArrayList<>();
        this.board = new Board();
    }

    public Game(ArrayList<Player> players,Board board, List<ProgressCard> cardPile, String userIdOfHost) {
        this.hostId = userIdOfHost;
        this.board = board;
        this.initialRolls = new HashMap<>();
        this.playerIdWithLongestRoad = 0;
        this.playerWithLargestArmy = 0;
        this.cardPile = cardPile;
        this.gameState = GameState.ACTIVE;

        for (Player player : players) {
            player.getRemainingActions().add(PlayerAction.INITROLL);
        }
        this.players = players;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param game  Game to copy
     */
    public Game(Game game) {
        this.id = game.getId();
        this.hostId = game.hostId;
        this.currentPlayerId = game.getCurrentPlayerId();
        this.gameState = game.gameState;
        this.players = game.getPlayers().stream().map(Player::new).collect(Collectors.toCollection(ArrayList::new));
        this.board = new Board(game.getBoard());
        this.initialRolls = new HashMap<>(game.getInitialRolls());
        this.playerIdWithLongestRoad = game.playerIdWithLongestRoad;
        this.playerWithLargestArmy = game.getPlayerWithLargestArmy();
        this.cardPile = game.getCardPile().stream().map(ProgressCard::new).collect(Collectors.toCollection(ArrayList::new));
        if (game.getTradeRequest()==null) {
            this.tradeRequest = null;
        }
        else {
            this.tradeRequest =  new TradeRequest(game.getTradeRequest());
        }
    }

    /**
     * Gets the currentPlayer object
     *
     * @return current Player
     */
    public Player getCurrentPlayer(){
        return players.stream().filter(player -> player.getPlayerId() == currentPlayerId).findFirst().orElse(null);
    }
}
