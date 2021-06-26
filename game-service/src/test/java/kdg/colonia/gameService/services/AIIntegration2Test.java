package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.game.GameConfig;
import kdg.colonia.gameService.controllers.RESTToSocketsController;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.ai.actions.Action;
import kdg.colonia.gameService.repositories.GameRepository;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import kdg.colonia.gameService.utilities.SassyExceptionMessageGenerator;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Ignore
@SpringBootTest
public class AIIntegration2Test {

    DummyGameRepository gameRepository;

    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    PlayerService playerService;
    @Autowired
    BoardCreationService boardCreationService;
    @Autowired
    SassyExceptionMessageGenerator sassyExceptionMessageGenerator;
    @Autowired
    CardPileCreationService cardPileCreationService;
    @Autowired
    GameInitService gameInitService;
    @Autowired
    TurnTokenService turnTokenService;
    @Autowired
    TradeService tradeService;
    @Autowired
    GameLogicService gameLogicService;
    @MockBean
    RESTToSocketsController socketsController;
    @Autowired
    MonteCarloService aiService;
    @MockBean
    GameConfig gameConfig;
    @MockBean
    ChatBotService chatBotService;

    GameService gameService;

    @BeforeEach
    public void setUp() {
        this.gameRepository = new DummyGameRepository();

        this.gameService = new GameService(
                this.gameRepository,
                this.mongoTemplate,
                this.playerService,
                this.boardCreationService,
                this.sassyExceptionMessageGenerator,
                this.cardPileCreationService,
                this.gameInitService,
                this.turnTokenService,
                this.tradeService,
                this.gameLogicService,
                this.socketsController,
                this.aiService,
                this.chatBotService,
                this.gameConfig
        );

        when(gameConfig.isOnlyAIGameExperimental()).thenReturn(true);
        when(gameConfig.getResourceGainSettlement()).thenReturn(1);
        when(gameConfig.getBonusIfCity()).thenReturn(1);
        when(gameConfig.getVictoryPointsWin()).thenReturn(10);

        Game game = gameService.createGame(List.of("geb1"),2,"geb1");

        game.setCurrentPlayerId(1);
        game.setId("123test");

        this.gameRepository.game = game;

        //Mocking
        doNothing().when(socketsController).sendNewAchievementNotice(anyString(), anyInt(), anyString());
        doNothing().when(socketsController).sendPauseGameNotice(anyString());
        doNothing().when(socketsController).sendEndTurnNotice(anyString());
        doNothing().when(socketsController).sendTradeNotice(anyString(), anyInt());
        doNothing().when(socketsController).sendRefreshBoard(anyString());

        doNothing().when(chatBotService).sendMessage(anyString(), any(Player.class), any(Action.class));
    }

    /*@Ignore
    @Test
    public void testRollAndStartAITurn() {
        try {
            Thread.sleep(100000);
        }catch (InterruptedException ignored){}
    }*/

    static class DummyGameRepository implements GameRepository {

        public Game game = null;

        @Override
        public <S extends Game> S save(S entity) {
            entity.setId("123test");
            this.game = entity;
            return (S) this.game;
        }

        @Override
        public Optional<Game> findById(String s) {
            if (game == null) {
                return Optional.empty();
            } else {
                return Optional.of(game);
            }
        }

        //UNUSED

        @Override
        public <S extends Game> List<S> saveAll(Iterable<S> entities) {
            return null;
        }

        @Override
        public List<Game> findAll() {
            return null;
        }

        @Override
        public List<Game> findAll(Sort sort) {
            return null;
        }

        @Override
        public <S extends Game> S insert(S entity) {
            return null;
        }

        @Override
        public <S extends Game> List<S> insert(Iterable<S> entities) {
            return null;
        }

        @Override
        public <S extends Game> List<S> findAll(Example<S> example) {
            return null;
        }

        @Override
        public <S extends Game> List<S> findAll(Example<S> example, Sort sort) {
            return null;
        }

        @Override
        public Page<Game> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public boolean existsById(String s) {
            return false;
        }

        @Override
        public Iterable<Game> findAllById(Iterable<String> strings) {
            return null;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(String s) {

        }

        @Override
        public void delete(Game entity) {

        }

        @Override
        public void deleteAll(Iterable<? extends Game> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends Game> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Game> Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Game> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Game> boolean exists(Example<S> example) {
            return false;
        }
    }
}
