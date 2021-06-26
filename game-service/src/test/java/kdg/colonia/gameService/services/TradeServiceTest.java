package kdg.colonia.gameService.services;

import io.cucumber.java.bs.A;
import io.cucumber.java.eo.Se;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.reactive.context.GenericReactiveWebApplicationContext;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeServiceTest {

    @Autowired
    TradeService tradeService;

    @Autowired
    GameService gameService;

    @Autowired
    BoardCreationService boardCreationService;

    private Game game;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setUp(){
        //set-up game
        this.game = new Game();
        this.game.setBoard(boardCreationService.generate());
        this.game.setPlayers(new ArrayList<>());
        p1 = new Player(1,"1",false);
        p2 = new Player(2,"2",false);
        this.game.getPlayers().add(p1);
        this.game.getPlayers().add(p2);
        p1.addResources(4,4,4,4,4);
        this.game.getBoard().getSettlements().add(new Settlement(new Coordinate(-2,2,0, Direction.LEFT),1));
        this.game.getBoard().getSettlements().add(new Settlement(new Coordinate(0,2,-2, Direction.TOP),1));

        //set-up harbours
        Harbour special = new Harbour();
        special.setResource(Resource.BRICK);
        special.setRatio(2);
        special.setCoordinate(new Coordinate(-2,2,0, CardDir.WEST));
        Harbour generic = new Harbour();
        generic.setResource(Resource.NOTHING);
        generic.setRatio(3);
        generic.setCoordinate(new Coordinate(0,2,-2,CardDir.NORTH_WEST));
        ArrayList<Harbour> harbours = new ArrayList<>();
        harbours.add(special);
        harbours.add(generic);
        this.game.getBoard().setHarbours(harbours);
    }

    /**
     * player has no harbours
     * player has 4 of each resource
     * player trades 4 bricks for 1 lumber
     */
    @Test
    void tradeWithBankRatio4Happy(){
        //set-up
        game.getBoard().getSettlements().clear();

        assertEquals(4,tradeService.getBankRatio(game,p1,Resource.LUMBER));

        assertTrue(tradeService.tradeWithBank(game, p1,Resource.LUMBER, Resource.BRICK));

        assertEquals(5,p1.getBricks());
        assertEquals(4,p1.getGrain());
        assertEquals(0,p1.getLumber());
        assertEquals(4,p1.getOre());
        assertEquals(4,p1.getWool());
    }

    /**
     * player has generic harbour
     * player has 4 of each resource
     * player trades 3 brick for 1 lumber
     */
    @Test
    void tradeWithBankRatio3Happy(){
        assertEquals(3,tradeService.getBankRatio(game,p1,Resource.GRAIN));

        assertTrue(tradeService.tradeWithBank(game,p1,Resource.GRAIN,Resource.BRICK));

        assertEquals(5,p1.getBricks());
        assertEquals(1,p1.getGrain());
        assertEquals(4,p1.getLumber());
        assertEquals(4,p1.getOre());
        assertEquals(4,p1.getWool());
    }

    /**
     * player has special harbour for brick
     * player has 4 of each resource
     * player trades 2 grain for 1 brick
     */
    @Test
    void tradeWithBankRatio2Happy(){
        assertEquals(2,tradeService.getBankRatio(game,p1,Resource.BRICK));

        assertTrue(tradeService.tradeWithBank(game,p1,Resource.BRICK,Resource.GRAIN));
        assertEquals(2,p1.getBricks());
        assertEquals(5,p1.getGrain());
        assertEquals(4,p1.getLumber());
        assertEquals(4,p1.getOre());
        assertEquals(4,p1.getWool());
    }

    /**
     * player has a generic harbour
     * player has 4 of each resource, but 0 grain
     * player tries to trade 3 grain for 1 lumber but fails
     */
    @Test
    void tradeWithBankNotEnoughResources(){
        //player has no grain
        p1.removeResources(0,4,0,0,0);

        assertEquals(3, tradeService.getBankRatio(game,p1,Resource.GRAIN));

        assertFalse(tradeService.tradeWithBank(game,p1,Resource.GRAIN,Resource.LUMBER));

        assertEquals(4,p1.getBricks());
        assertEquals(0,p1.getGrain());
        assertEquals(4,p1.getLumber());
        assertEquals(4,p1.getOre());
        assertEquals(4,p1.getWool());
    }


    @Test
    public void addTradeRequest(){
        Map<Resource,Integer> askerGives=new HashMap<>();
        askerGives.put(Resource.BRICK,1);
        Map<Resource,Integer> askerReceives=new HashMap<>();
        askerReceives.put(Resource.GRAIN,1);

        Player asker= new Player();
        asker.setPlayerId(1);
        Player receiver = new Player();
        receiver.setPlayerId(2);

        Game game=new Game();
        game.setPlayers(new ArrayList<>());
        game.getPlayers().addAll(List.of(asker,receiver));
        game=tradeService.startTradeRequest(game,asker,receiver,askerGives,askerReceives);


        assertNotNull(game.getTradeRequest());
    }

    @Test
    public void acceptTrade() throws Exception
    {
        Map<Resource,Integer> askerGives=new HashMap<>();
        askerGives.put(Resource.BRICK,1);
        Map<Resource,Integer> askerReceives=new HashMap<>();
        askerReceives.put(Resource.GRAIN,1);

        Player asker= new Player();
        asker.setPlayerId(1);
        asker.addResources(Resource.BRICK,1);
        Player receiver = new Player();
        receiver.setPlayerId(2);
        receiver.addResources(Resource.GRAIN,1);

        TradeRequest tradeRequest= new TradeRequest(asker.getPlayerId(),receiver.getPlayerId(),askerGives,askerReceives);
        tradeRequest.setId("testId");

        Game game=new Game();
        game.setPlayers(new ArrayList<>());
        game.setTradeRequest(tradeRequest);
        game.getPlayers().addAll(List.of(asker,receiver));

        game=tradeService.acceptTradeRequest(game,asker,receiver);
        assertEquals(1
                ,game.getPlayers()
                        .stream().filter(p->p.getPlayerId()==asker.getPlayerId())
                        .findFirst().get().getGrain());
        assertEquals(0
                ,game.getPlayers()
                        .stream().filter(p->p.getPlayerId()==asker.getPlayerId())
                        .findFirst().get().getBricks());
    }

    @Test
    public void acceptTradeWithInsufficientResources() throws Exception
    {
        Map<Resource,Integer> askerGives=new HashMap<>();
        askerGives.put(Resource.BRICK,1);
        Map<Resource,Integer> askerReceives=new HashMap<>();
        askerReceives.put(Resource.GRAIN,1);

        Player asker = new Player();
        asker.setPlayerId(1);
        asker.addResources(Resource.BRICK,1);
        Player receiver = new Player();
        receiver.setPlayerId(2);

        TradeRequest tradeRequest= new TradeRequest(asker.getPlayerId(),receiver.getPlayerId(),askerGives,askerReceives);
        tradeRequest.setId("testId");

        Game game=new Game();
        game.setPlayers(new ArrayList<>());
        game.setTradeRequest(tradeRequest);
        game.getPlayers().addAll(List.of(asker,receiver));

        assertThrows(
                Exception.class,
                ()->tradeService.acceptTradeRequest(game,asker,receiver)
        );
    }
}