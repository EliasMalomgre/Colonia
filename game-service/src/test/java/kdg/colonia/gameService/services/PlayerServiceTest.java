package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlayerServiceTest {

    @Autowired
    PlayerService playerService;

    @Autowired
    GameInfoService gameInfoService;

    Player player;

    @BeforeEach
    public void setup() {
        player = new Player();
    }

    @Test
    public void isAllowedToBuildRoad() {
        player.addResources(1,0,1,0,0);
        assertTrue(playerService.playerAllowedToBuildRoad(player));
    }

    @Test
    public void isNotAllowedToBuildRoad() {
        player.addResources(0,1,1,0,1);
        assertFalse(playerService.playerAllowedToBuildRoad(player));
    }

    @Test
    public void isAllowedToBuildSettlement() {
        player.addResources(1,1,1,0,1);
        assertTrue(playerService.playerAllowedToBuildSettlement(player));
    }

    @Test
    public void isNotAllowedToBuildSettlement() {
        player.addResources(0,1,1,0,1);
        assertFalse(playerService.playerAllowedToBuildSettlement(player));
    }

    @Test
    public void isAllowedToBuildCity() {
        player.addResources(0,2,0,3,0);
        assertTrue(playerService.playerAllowedToBuildCity(player));
    }

    @Test
    public void isNotAllowedToBuildCity() {
        player.addResources(0,1,3,0,1);
        assertFalse(playerService.playerAllowedToBuildCity(player));
    }

    @Test
    public void isAllowedToBuyCard() {
        player.addResources(0,1,0,1,1);
        assertTrue(playerService.playerAllowedToBuyCard(player));
    }

    @Test
    public void isNotAllowedToBuyCard() {
        player.addResources(2,1,1,0,1);
        assertFalse(playerService.playerAllowedToBuyCard(player));
    }

    @Test
    public void legalCardActionsCorrectly() {
        player.getCards().addAll(Stream.of(new ProgressCard(ProgressCardType.KNIGHT), new ProgressCard(ProgressCardType.KNIGHT),
                new ProgressCard(ProgressCardType.MONOPOLY), new ProgressCard(ProgressCardType.ROAD_BUILDING)).
                collect(Collectors.toList()));
        Set<ProgressCardType> expected = Stream.of(new ProgressCard(ProgressCardType.KNIGHT),
                new ProgressCard(ProgressCardType.MONOPOLY), new ProgressCard(ProgressCardType.ROAD_BUILDING))
                .map(ProgressCard::getCardType).collect(Collectors.toSet());
        assertEquals(expected, playerService.getPossibleDevelopmentCardActions(player));
    }

    @Test
    public void legalCardActionsInCorrectly() {
        player.getCards().addAll(Stream.of(new ProgressCard(ProgressCardType.KNIGHT), new ProgressCard(ProgressCardType.KNIGHT),
                new ProgressCard(ProgressCardType.MONOPOLY), new ProgressCard(ProgressCardType.ROAD_BUILDING)).
                collect(Collectors.toList()));

        Set<ProgressCardType> wrongExpected = Stream.of(new ProgressCard(ProgressCardType.KNIGHT),
                new ProgressCard(ProgressCardType.MONOPOLY))
                .map(ProgressCard::getCardType).collect(Collectors.toSet());
        assertNotEquals(wrongExpected, playerService.getPossibleDevelopmentCardActions(player));
    }

    @Test
    public void getAmountOfResourceCards(){
        Player player=new Player();
        player.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        player.getCards().add(new ProgressCard(ProgressCardType.KNIGHT));
        int amount=playerService.getAmountOfKnightCards(player);
        assertEquals(2,amount);
    }

    @Test
    public void transferRandomResourceSuccesful(){
        for (int i = 0; i < 50; i++) {
            Player player1 = new Player();
            Player player2 = new Player();

            player1.addResources(1,0,0,0,0);
            player2.addResources(0,0,0,1,0);

            Resource resource = playerService.transferRandomResource(player1, player2);

            assertEquals(1, player1.getResources().get(Resource.ORE), "Player 1 should have gained 1 ORE");
            assertEquals(0, player2.getResources().get(Resource.ORE), "Player 2 should have lost 1 ORE");
            assertEquals(Resource.ORE, resource, "The transferred resource should have been ORE");
        }
    }

    @Test
    public void transferRandomResourceFailed(){
        for (int i = 0; i < 50; i++) {
            Player player1 = new Player();
            Player player2 = new Player();

            player1.addResources(1,0,0,0,0);
            player2.addResources(0,0,0,0,0);

            Resource resource = playerService.transferRandomResource(player1, player2);

            assertEquals(0, player1.getResources().get(Resource.ORE), "Player 1 should have gained nothing");
            assertEquals(0, player2.getResources().get(Resource.ORE), "Player 2 should have lost nothing");
            assertEquals(Resource.NOTHING, resource, "The transferred resource should have been ORE");
        }
    }
}