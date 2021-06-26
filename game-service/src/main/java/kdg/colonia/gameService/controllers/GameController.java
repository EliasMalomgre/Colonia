package kdg.colonia.gameService.controllers;

import kdg.colonia.gameService.controllers.dtos.*;
import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.services.*;
import kdg.colonia.gameService.services.gameInfo.CostObject;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final GameInfoService gameInfoService;

    @PostMapping("/createGame")
    public ResponseEntity<Game> createGame(@RequestBody CreateGameDTO createGameDTO) {
        try {
            Game newGame = gameService.createGame(createGameDTO.getUserIds(), createGameDTO.getAmountOfAIs(),createGameDTO.getUserIdOfHost());
            return ResponseEntity.ok(newGame);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/acceptTrade")
    public ResponseEntity<Game> acceptTrade(@RequestHeader("UserId") String userId, @RequestBody AcceptTradeDTO acceptTradeDTO) {
        try {
            Game updatedGame = gameService.acceptTradeRequest(acceptTradeDTO.getGameId(), acceptTradeDTO.getTradeId(), userId);
            return ResponseEntity.ok(updatedGame);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/startTrade")
    public ResponseEntity<Game> startTrade(@RequestBody TradeRequestDTO tradeRequestDTO) {
        try {
            Game updatedGame = gameService.startTradeRequest(tradeRequestDTO.getGameId(), tradeRequestDTO.getAsking(), tradeRequestDTO.getReceiving(), tradeRequestDTO.getToSend(), tradeRequestDTO.getToReceive());
            return ResponseEntity.ok(updatedGame);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/getResourcesForPlayer")
    public ResponseEntity<Map<Resource, Integer>> getResourcesForPlayer(@RequestParam String gameId, @RequestParam int playerId) {
        try {
            Map<Resource, Integer> resourceMap = gameService.getResourcesFromPlayer(gameId, playerId);
            return ResponseEntity.ok(resourceMap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/rollForInitiative")
    public ResponseEntity<int[]> rollForInitiative(@RequestBody RollForInitiativeDTO dto) {
        int[] roll = gameService.rollForInitiative(dto.gameId, dto.userId);
        return ResponseEntity.ok(roll);
    }

    @PostMapping("/rollDice")
    public ResponseEntity<int[]> rollDice(@RequestBody GamePlayerDTO dto) {
        try {
            int[] roll = gameService.rollDice(dto.gameId, dto.playerId);
            return ResponseEntity.ok(roll);
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/build")
    public ResponseEntity<Board> build(@RequestBody CoordinateDTO coordinateDTO)
    {
        Board board = gameService.build(coordinateDTO.gameId, coordinateDTO.playerId, coordinateDTO.coordinate);
        return ResponseEntity.ok(board);
    }

    @PostMapping("/upgradeSettlement")
    public ResponseEntity<Board> upgradeSettlement(@RequestBody CoordinateDTO coordinateDTO) {

        try {
            Board board = gameService.upgradeSettlementToCity(coordinateDTO.gameId, coordinateDTO.playerId, coordinateDTO.coordinate);
            if (board != null) return ResponseEntity.ok(board);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/getBuildingCosts")
    public ResponseEntity<List<CostObject>> getBuildingCosts() {
        return ResponseEntity.ok(gameInfoService.getBuildingCostInfo());
    }

    @PostMapping("/moveRobber")
    public ResponseEntity<List<Integer>> moveRobber(@RequestBody CoordinateDTO coordinateDTO) {
        try {
            return ResponseEntity.ok(gameService.moveRobber(coordinateDTO.gameId, coordinateDTO.playerId, coordinateDTO.coordinate));
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/stealResource")
    public ResponseEntity<Resource> stealResource(@RequestBody StealResourceDTO stealResourceDTO) {
        try {
            Resource resource = gameService.stealResources(stealResourceDTO.gameId, stealResourceDTO.playerId, stealResourceDTO.playerIdToStealFrom);
            return ResponseEntity.ok(resource); //includes Resource.NOTHING when player had no resources to steal
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/discardResources")
    public ResponseEntity<Boolean> discardResources(@RequestBody DiscardResourcesDTO discardResourcesDTO) {
        try {
            boolean succeeded = gameService.discardResources(discardResourcesDTO.gameId, discardResourcesDTO.playerId, discardResourcesDTO.discardedResources);
            return ResponseEntity.ok(succeeded);
        } catch (Error e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/buyCard")
    public ResponseEntity<ProgressCard> buyCard(@RequestBody GamePlayerDTO gamePlayerDTO) {
        try {
            ProgressCard succeeded = gameService.buyCard(gamePlayerDTO.gameId, gamePlayerDTO.playerId);
            return ResponseEntity.ok(succeeded);
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/getGame")
    public Game getGame(String gameId) {
        return gameService.getGame(gameId);
    }

    @GetMapping("/getBoard")
    public ResponseEntity<Board> getBoard(String gameId) {
        Board board = gameService.getGame(gameId).getBoard();
        if (gameId != null) {
            return ResponseEntity.ok(board);
        } else {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/getPlayer")
    public ResponseEntity<Player> getPlayer(String gameId, int playerId) {
        Player player = gameService.getPlayer(gameId, playerId);
        if (player != null) {
            return ResponseEntity.ok(player);
        } else {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/playCard")
    public ResponseEntity<Boolean> playCard(@RequestBody PlayCardDTO playCardDTO) {
        try {
            if (gameService.playCard(playCardDTO.gameId, playCardDTO.playerId, playCardDTO.cardType)) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(500).body(false);
            }
        } catch (Error | Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/yearOfPlenty")
    public ResponseEntity<Boolean> playYearOfPlenty(@RequestBody YopDTO yopDTO) {
        try {
            if (gameService.yearOfPlenty(yopDTO.gameId, yopDTO.playerId, yopDTO.resource1, yopDTO.resource2)) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(500).body(false);
            }
        } catch (Error e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/monopoly")
    public ResponseEntity<Boolean> playMonopoly(@RequestBody MonopolyDTO monopolyDTO) {
        try {
            if (gameService.monopoly(monopolyDTO.gameId, monopolyDTO.playerId, monopolyDTO.resource)) {
                return ResponseEntity.ok(true);
            } else
                return ResponseEntity.status(500).body(false);
        } catch (Error e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @GetMapping("/possibleRoadPlacements")
    public ResponseEntity<RoadPlacementsDTO> getPossibleRoadPlacements(String gameId, int playerId) {
        try {
            return ResponseEntity.ok(new RoadPlacementsDTO(gameService.possibleRoadPlacements(gameId, playerId)));
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/possibleSettlementPlacements")
    public ResponseEntity<SettlementPlacementsDTO> getPossibleSettlementPlacements(String gameId, int playerId) {
        try {
            return ResponseEntity.ok(new SettlementPlacementsDTO(gameService.possibleSettlementPlacements(gameId, playerId)));
        } catch (Error e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/endTurn")
    public ResponseEntity<Boolean> endTurn(@RequestBody GamePlayerDTO gamePlayerDTO) {
        try {
            if (gameService.endTurn(gamePlayerDTO.gameId, gamePlayerDTO.playerId)) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(500).body(false);
            }
        } catch (Error e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    //note: this api can be used to show the player in front-end how many resources he needs to discard
    @GetMapping("/getTradeRatio")
    public ResponseEntity<Integer> getTradeRatio(String gameId, int playerId, Resource resource) {
        try {
            return ResponseEntity.ok(gameService.getTradeRatio(gameId, playerId, resource));
        } catch (Error e){
            return ResponseEntity.status(500).body(-1);
        }
    }

    @PostMapping("/tradeWithBank")
    public ResponseEntity<Boolean> tradeWithBank(@RequestBody TradeWithBankDTO tradeWithBankDTO){
        try {
            return ResponseEntity.ok(gameService.tradeWithBank(tradeWithBankDTO.getGameId(),tradeWithBankDTO.getPlayerId(),tradeWithBankDTO.getFrom(),tradeWithBankDTO.getTo()));
        } catch (Error e){
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping
    public ResponseEntity<Game> cancelTradeRequest(@RequestBody GamePlayerDTO gamePlayerDTO){
        try {
            Game game = gameService.cancelTradeRequest(gamePlayerDTO.gameId, gamePlayerDTO.playerId);
            if (game != null) {
                return ResponseEntity.ok(game);
            } else {
                return ResponseEntity.status(500).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }

    }

    @GetMapping("/pauseGame")
    public ResponseEntity<Boolean> pauseGame(@RequestParam String gameId){
        try {
            return ResponseEntity.ok(gameService.pauseGame(gameId));
        } catch (Error e){
            return ResponseEntity.status(500).body(false);
        }
    }


    @GetMapping("/resumeGame")
    public ResponseEntity<Game> resumeGame(@RequestParam String gameId){
        try{
            return ResponseEntity.ok(gameService.resumeGame(gameId));
        } catch (Error e){
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("endGameEarly")
    public ResponseEntity<?> endGame(@RequestParam String gameId, @RequestHeader String UserId){
        //TODO: check if host executes this by UserId header which is already checked after passing through gateway-> No host kept in game yet.
        try{
            gameService.endGameEarly(gameId);
            return ResponseEntity.ok("Game ended");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body("Game could not be ended.");
        }
    }
    @GetMapping("/getGamesOverview")
    public ResponseEntity<List<Game>> getGamesOverview(@RequestParam String userId, @RequestParam String filter){
        return ResponseEntity.ok(gameService.getGamesOverview(userId, filter));
    }


}
