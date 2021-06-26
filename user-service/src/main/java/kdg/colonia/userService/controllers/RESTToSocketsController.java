package kdg.colonia.userService.controllers;

import com.google.gson.Gson;
import kdg.colonia.userService.config.AddressConfig;
import kdg.colonia.userService.controllers.DTO.userIdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class RESTToSocketsController {
    private final String uri;
    private final AddressConfig addressConfig;
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final Gson gson;


    public RESTToSocketsController(AddressConfig addressConfig, RestTemplate restTemplate, HttpHeaders httpHeaders, Gson gson) {
        this.addressConfig = addressConfig;
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
        this.gson = gson;
        this.uri = addressConfig.getWebsocketServer();
    }

    /**
     * This method sends a notice to the websocket service so it can notify the player of a friend request or invitation
     *
     * @param userId the UUID of the user
     */
    public void sendFriendRequestInvite(String userId) {
        try{
        String gameIdParsed = makeUserDTO(userId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri + "friendRequestInvite", entity, boolean.class);
        if (!result) {
            log.error(String.format("User[%s]: error while sending trade notice to socket server", userId));
        }}
        catch (Exception e){
            //These exceptions don't really influence the working of a game.
            //Continue the rest of the logic when this fails.
            log.error(e.getMessage());
        }
    }

    private String makeUserDTO(String userId){
        return gson.toJson(new userIdDTO(userId));
    }
}
