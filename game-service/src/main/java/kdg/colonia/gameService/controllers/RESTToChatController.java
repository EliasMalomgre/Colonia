package kdg.colonia.gameService.controllers;

import com.google.gson.Gson;
import kdg.colonia.gameService.config.AddressConfig;
import kdg.colonia.gameService.controllers.dtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class RESTToChatController {
    private final String uri;
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final Gson gson;

    public RESTToChatController(RestTemplate restTemplate, HttpHeaders httpHeaders, Gson gson, AddressConfig addressConfig) {
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
        this.gson = gson;
        this.uri = addressConfig.getChatService();
    }

    /**
     * This method sends a chat message to the in-game chat of an ongoing game. GameService uses this to send simple
     * messages, acting as a chatbot for the AI's
     *
     * @param gameId   the UUID of the game
     * @param playerId the sequential number of the AI player
     * @param userName the name under which the AI is pretending to play
     * @param message the message that is sent to the game chat
     */
    public void sendMessage(String gameId, int playerId, String userName, String message) {
        String messageParsed = makeMessageDTO(gameId, playerId, userName, message);
        HttpEntity<String> entity = new HttpEntity<>(messageParsed, httpHeaders);
        Boolean result = restTemplate.postForObject(uri + "newMessage", entity, boolean.class);
        if (result != null && !result) {
            log.error(String.format("Game[%s]: error while sending message to the Chat Service", gameId));
        }
    }

    /**
     * Creates a JSON DTO to send to the websocket server
     */
    private String makeMessageDTO(String gameId, int playerId, String playerName, String message) {
        return gson.toJson(new NewMessageDTO(gameId, playerId, playerName, message));
    }

}
