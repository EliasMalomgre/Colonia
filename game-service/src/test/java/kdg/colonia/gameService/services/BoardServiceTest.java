package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.CardDir;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import kdg.colonia.gameService.domain.coordinates.Direction;
import kdg.colonia.gameService.domain.tiles.Tile;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class BoardServiceTest
{
    @Autowired
     BoardService boardService;
    @Autowired
    BoardCreationService boardCreationService;
    @MockBean
     GameService gameService;

    private Game gameToTest;
    private Board board;

    @BeforeEach
    public void setUp(){
        //setup data
        gameToTest = new Game();
        board = boardCreationService.generate();
        gameToTest.setBoard(board);
    }

    @Test
    public void addSettlementToValidPositionWithExistingGame(){
        Player player = new Player(1,"eendbId", false);
        player.addResources(List.of(Resource.BRICK,Resource.GRAIN,Resource.LUMBER,Resource.WOOL));
        gameToTest.setPlayers(new ArrayList<>(List.of(player)));
        //hardcoded road to be able to place settlement
        Road road=new Road(new Coordinate(0,2,-2, CardDir.NORTH_WEST),1);
        board.getRoads().add(road);
        //Board should be returned
        assertTrue(boardService.addSettlementToBoard(gameToTest.getBoard(), player ,new Coordinate(0,2,-2,Direction.LEFT)));
        assertEquals(0, player.getResourcesTotal());
    }
    @Test
    public void addSettlementWithInvalidPositionWithExistingGame(){
        Player player = new Player(1,"eendbId", false);
        board.addSettlement(new Coordinate(0,2,-2, Direction.LEFT),1);
        //Settlement should not be added.
        assertFalse(boardService.addSettlementToBoard(gameToTest.getBoard(), player ,new Coordinate(0,2,-2,Direction.TOP)));
    }
    @Test
    public void addSettlementNotEnoughResources(){
        Player player = new Player(1,"eendbId", false);
        gameToTest.setPlayers(new ArrayList<>(List.of(player)));
        //hardcoded road to be able to place settlement
        Road road=new Road(new Coordinate(0,2,-2, CardDir.NORTH_WEST),1);
        board.getRoads().add(road);

        //Board should be returned
        assertFalse(boardService.addSettlementToBoard(gameToTest.getBoard(), player ,new Coordinate(0,2,-2,Direction.LEFT)));
        assertEquals(0, player.getResourcesTotal());
    }
    @Test
    public void addRoadSuccessfully() {
        Player player = new Player(1,"eendbId", false);
        board.addSettlement(new Coordinate(0,2,-2, Direction.LEFT),1);
        player.addResources(1,0,1,0,0);

        assertTrue(boardService.addRoadToBoard(board, player, new Coordinate(0,2,-2, CardDir.WEST)));
        assertEquals(0, player.getResourcesTotal());
    }
    @Test
    public void AddRoadInvalidCoordinate() {
        Player player = new Player(1,"eendbId", false);
        player.addResources(1,0,1,0,0);

        assertFalse(boardService.addRoadToBoard(board, player, new Coordinate(0,2,-2, CardDir.WEST)));
        assertEquals(2, player.getResourcesTotal());
    }
    @Test
    public void AddRoadNotEnoughResources() {
        Player player = new Player(1,"eendbId", false);
        board.addSettlement(new Coordinate(0,2,-2, Direction.LEFT),1);

        assertFalse(boardService.addRoadToBoard(board, player, new Coordinate(0,2,-2, CardDir.WEST)));
        assertEquals(0, player.getResourcesTotal());
    }
    @Test
    public void upgradeSettlementToCity() {
        Player player = new Player(1, "eendbId", false);
        player.addResources(List.of(Resource.BRICK, Resource.GRAIN, Resource.LUMBER, Resource.WOOL));
        player.addResources(List.of(Resource.GRAIN, Resource.GRAIN, Resource.ORE, Resource.ORE, Resource.ORE));
        gameToTest.setPlayers(new ArrayList<>(List.of(player)));
        //setup mocking
        Settlement settlement=new Settlement(new Coordinate(0, 2, -2, Direction.LEFT), 1);
        board.getSettlements().add(settlement);
        Board board = gameToTest.getBoard();
        board.upgradeSettlement(new Coordinate(0, 2, -2, Direction.LEFT), 1);

        Game gameAfterUpgradedSettlement = new Game();
        gameAfterUpgradedSettlement.setPlayers(gameToTest.getPlayers());
        gameAfterUpgradedSettlement.setBoard(board);

        assertTrue(boardService.upgradeSettlementToCity(board, player, new Coordinate(0, 2, -2, Direction.LEFT)));

        Assert.assertTrue(gameToTest.getBoard().getSettlements().stream().filter(s -> s.getCoordinate()
                .equals(new Coordinate(0, 2, -2, Direction.LEFT))).findFirst().get().isCity());
    }



    @Test
    public void getLongestRoadNoBranches(){
        List<Road> roads = board.getRoads();

        //main path
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 1));

        //path not connected to main path
        roads.add(new Road(new Coordinate(-1, -1, 2, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-1, -1, 2, CardDir.NORTH_EAST), 1));

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(6, longestRoad, "The player has 6 connected roads, in one line, length should have been 6");
    }

    @Test
    public void getLongestRoadWithBranches(){
        List<Road> roads = board.getRoads();

        //main path
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 1));

        //branch on 2-4 split
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.WEST), 1));
        roads.add(new Road(new Coordinate(-1, 0, 1, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, -1, 1, CardDir.WEST), 1));

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(7, longestRoad, "The player has 9 connected roads, with a 3 long branch on the 2-4 split, length should have been 7");
    }

    @Test
    public void getLongestRoadWithEnemySettlement(){
        List<Road> roads = board.getRoads();

        //main path
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 1));

        //enemy settlement on 2-4 split of main path
        board.getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.LEFT),2));

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(4, longestRoad, "The player has 6 connected roads, with an enemy settlement on 2-4 split, length should have been 4");
    }

    @Test
    public void getLongestRoadWithOwnSettlement(){
        List<Road> roads = board.getRoads();

        //main path
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(-1, 1, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(0, 0, 0, CardDir.NORTH_EAST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 1));

        //own settlement on 2-4 split of main path
        board.getSettlements().add(new Settlement(new Coordinate(0, 0, 0, Direction.LEFT),1));

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(6, longestRoad, "The player has 6 connected roads, with own settlement on 2-4 split, length should have been 6");
    }

    @Test
    public void getLongestRoadCircle(){
        List<Road> roads = board.getRoads();

        //circle around 0,0,0
        for (Coordinate coordinate : new Coordinate(0,0,0).calculateEdges()) {
            roads.add(new Road(coordinate, 1));
        }

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(6, longestRoad, "The player has 6 connected roads in a circle, length should have been 6");
    }

    @Test
    public void getLongestRoadDoubleCircle(){
        List<Road> roads = board.getRoads();

        //circle around 0,0,0
        for (Coordinate coordinate : new Coordinate(0,0,0).calculateEdges()) {
            roads.add(new Road(coordinate, 1));
        }

        //connection both circles
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_WEST), 1));
        roads.add(new Road(new Coordinate(1, -1, 0, CardDir.NORTH_EAST), 1));

        //circle around 2,-2,0
        for (Coordinate coordinate : new Coordinate(2,-2,0).calculateEdges()) {
            roads.add(new Road(coordinate, 1));
        }

        int longestRoad = boardService.getLongestRoadForPlayer(board, 1);

        assertEquals(14, longestRoad, "The player 14 connected pieces, in 2 circles, connected with eachother, length should have been 14");
    }

    @Test
    public void getPlayersInRobbersReachSuccessTwoInReach(){
        Tile tile = board.getTileForCoordinate(new Coordinate(-1,0,1));
        List<Integer> affectedPlayers;

        //place the robber on -1,0,1
        board.setRobberTile(tile);

        //add settlements around the robber
        board.getSettlements().add(new Settlement(new Coordinate(-1,0,1, Direction.TOP),1)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(0,-1,1, Direction.LEFT),2)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(1,0,-1, Direction.LEFT),3)); //Out of reach

        affectedPlayers = boardService.getPlayersInRobberReach(board);
        assertEquals(2, affectedPlayers.size(), "affectedPlayers shouldn't contain doubles");
        assertTrue(affectedPlayers.contains(1), "affectedPlayers should have contained Player 1");
        assertTrue(affectedPlayers.contains(2), "affectedPlayers should have contained Player 2");
        assertFalse(affectedPlayers.contains(3), "affectedPlayers should not have contained Player 3");
    }

    @Test
    public void getPlayersInRobbersReachSuccessFourInReach(){
        Tile tile = board.getTileForCoordinate(new Coordinate(-1,0,1));
        List<Integer> affectedPlayers;

        //place the robber on -1,0,1
        board.setRobberTile(tile);

        //add settlements around the robber
        board.getSettlements().add(new Settlement(new Coordinate(-1,0,1, Direction.TOP),1)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(0,-1,1, Direction.LEFT),2)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(1,0,-1, Direction.LEFT),3)); //Out of reach
        board.getSettlements().add(new Settlement(new Coordinate(-1,-1,2, Direction.TOP),3)); //In reach
        board.getSettlements().add(new Settlement(new Coordinate(-1,-1,2, Direction.LEFT),2)); //trying to get a second player 2 in the list

        affectedPlayers = boardService.getPlayersInRobberReach(board);
        assertEquals(3, affectedPlayers.size(), "affectedPlayers shouldn't contain doubles");
        assertTrue(affectedPlayers.contains(1), "affectedPlayers should have contained Player 1");
        assertTrue(affectedPlayers.contains(2), "affectedPlayers should have contained Player 2");
        assertTrue(affectedPlayers.contains(3), "affectedPlayers should have contained Player 3");
    }

    @Test
    public void getPlayersInRobbersReachNoPlayers(){
        Tile tile = board.getTileForCoordinate(new Coordinate(-1,0,1));
        List<Integer> affectedPlayers;

        //place the robber on -1,0,1
        board.setRobberTile(tile);

        affectedPlayers = boardService.getPlayersInRobberReach(board);
        assertTrue(affectedPlayers.isEmpty(), "no players should have been affected by the robber");
    }

}