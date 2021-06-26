package kdg.colonia.gameService.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RESTToSocketsControllerTest {
    RESTToSocketsController restToSocketsController;

    @Autowired
    public RESTToSocketsControllerTest(RESTToSocketsController restToSocketsController) {
        this.restToSocketsController = restToSocketsController;
    }

    @BeforeEach
    void setUp(){

    }

    /*@Test
    void testTrade(){
        restToSocketsController.sendTradeNotice("testGame",2);
    }

    @Test
    void testEndTurn(){
        restToSocketsController.sendEndTurnNotice("testGame");
    }

    @Test
    void testGoToGame(){
        restToSocketsController.sendGoToGameNotice("lobby-id");
    }*/

}