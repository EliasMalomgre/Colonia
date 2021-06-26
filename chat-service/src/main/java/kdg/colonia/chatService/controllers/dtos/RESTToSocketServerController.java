package kdg.colonia.chatService.controllers.dtos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;

@RestController
@Slf4j
public class RESTToSocketServerController {
    private final String uri = "http://localhost:8084/";
    private final RestTemplate restTemplate;
    private final org.springframework.http.HttpHeaders httpHeaders;
    private final Gson gson;

    public RESTToSocketServerController(RestTemplate restTemplate, HttpHeaders httpHeaders, Gson gson) {
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
        this.gson = gson;
    }

    public void sendNewMessageNotice(String gameId){
        String gameIdParsed = makeGameIdDTO(gameId);
        HttpEntity<String> entity = new HttpEntity<>(gameIdParsed, httpHeaders);
        boolean result = restTemplate.postForObject(uri+"newChatMessage", entity, boolean.class);
        if (!result){
            log.error(String.format("Game[%s]: error while sending new chat notice to socket server",gameId));
        }
    }


    private String makeGameIdDTO(String gameId){
        return gson.toJson(new GameIdDTO(gameId));
    }
}
