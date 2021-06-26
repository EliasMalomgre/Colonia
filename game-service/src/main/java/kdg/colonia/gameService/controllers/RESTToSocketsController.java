package kdg.colonia.gameService.controllers;

import com.google.gson.Gson;
import kdg.colonia.gameService.config.AddressConfig;
import kdg.colonia.gameService.controllers.dtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@Slf4j
public class RESTToSocketsController {
    private final String uri;
    private final AddressConfig addressConfig;
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final Gson gson;

    public RESTToSocketsController(RestTemplate restTemplate, HttpHeaders httpHeaders, Gson gson, AddressConfig addressConfig) {
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
        this.gson = gson;
        this.addressConfig = addressConfig;
        this.uri = addressConfig.getWebsocketServer();
    }

    /**
     * This method sends a notice to the websocket service so it can notify the player of a new trade request
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential number of the player
     */
    public void sendTradeNotice(String gameId, int playerId) {
        String gameIdParsed = makeGamePlayerDTO(gameId, playerId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "trade", entity, boolean.class);
        if (!result) {
            log.error(String.format("Game[%s]: error while sending trade notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the player of a new achievement
     *
     * @param gameId      the UUID of the game
     * @param playerId    the sequential number of the player
     * @param achievement the earned achievement [LARGEST_ARMY, LONGEST_ROAD]
     */
    public void sendNewAchievementNotice(String gameId, int playerId, String achievement) {
        String gameIdParsed = makeAchievementDTO(gameId, playerId, achievement);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "achievement", entity, boolean.class);
        if (!result) {
            log.error(String.format("Game[%s]: error while sending new achievement notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that the current player has ended their turn
     *
     * @param gameId the UUID of the game
     */
    public void sendEndTurnNotice(String gameId) {
        String gameIdParsed = makeGameIdDTO(gameId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "endTurn", entity, boolean.class);
        if (!result) {
            log.error(String.format("Game[%s]: error while sending endTurn notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that the host has paused the game
     *
     * @param gameId the UUID of the game
     */
    public void sendPauseGameNotice(String gameId) {
        String gameIdParsed = makeGameIdDTO(gameId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "pauseGame", entity, boolean.class);
        if (!result) {
            log.error(String.format("Game[%s]: error while sending endTurn notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that the host has paused the game
     *
     * @param gameId the UUID of the game
     */
    public void sendRefreshBoard(String gameId) {
        String gameIdParsed = makeGameIdDTO(gameId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "refreshBoard", entity, boolean.class);
        if (!result){
            log.error(String.format("Game[%s]: error while sending refreshBoard notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that the host has paused the game
     *
     * @param gameId the UUID of the game
     */
    public void sendRolledSeven(String gameId, int playerId){
        String parsed = makeGamePlayerDTO(gameId,playerId);
        HttpEntity<String> entity = new HttpEntity<>(parsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "rolledSeven", entity, boolean.class);
        if (!result){
            log.error(String.format("Game[%s]: error while sending refreshBoard notice to socket server", gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that the game has ended
     *
     * @param gameId the UUID of the game
     */
    public void sendEndGame(String gameId, int winner){
        String parsed = makeGamePlayerDTO(gameId, winner);
        HttpEntity<String> entity = new HttpEntity<>(parsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri+"endGame", entity, boolean.class);
        if (!result){
            log.error(String.format("Game[%s]: error while sending endGame notice to socket server",gameId));
        }
    }

    /**
     * This method sends a notice to the websocket service so it can notify the players that have to discard
     *
     * @param gameId    the UUID of the game
     * @param playerIds the list of player id's of the players that have to discard
     */
    public void sendDiscard(String gameId, List<Integer> playerIds){
        String parsed = makeGameIntListDTO(gameId, playerIds);
        HttpEntity<String> entity = new HttpEntity<>(parsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri+"discard", entity, boolean.class);
        if (!result){
            log.error(String.format("Game[%s]: error while sending discard notice to socket server",gameId));
        }
    }

    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeGameIdDTO(String gameId) {
        return gson.toJson(new GameIdDTO(gameId));
    }
    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeNewCardDTO(String gameId, int playerId, String card){
        return gson.toJson(new newCardDTO(gameId,playerId,card));
    }
    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeGamePlayerDTO(String gameId, int playerId) {
        return gson.toJson(new GamePlayerDTO(gameId, playerId));
    }
    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeAchievementDTO(String gameId, int playerId, String achievement) {
        return gson.toJson(new AchievementDTO(gameId, playerId, achievement));
    }
    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeGameIntListDTO(String gameId, List<Integer> intList){
        return gson.toJson(new GameAndIntListDTO(gameId,intList));
    }
}
