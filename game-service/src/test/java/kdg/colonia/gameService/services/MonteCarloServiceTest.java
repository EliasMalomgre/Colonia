package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.ai.Node;
import kdg.colonia.gameService.domain.ai.State;
import kdg.colonia.gameService.domain.ai.actions.MoveRobberAction;
import kdg.colonia.gameService.domain.ai.actions.ChanceRollDiceAction;
import kdg.colonia.gameService.services.implementation.BoardCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@SpringBootTest
class MonteCarloServiceTest {

    @Autowired
    MonteCarloService monteCarloService;

    @Autowired
    PlayerService playerService;

    @Autowired
    BoardCreationService boardCreationService;

    @Autowired
    CardPileCreationService cardPileCreationService;

    @MockBean
    AIService aiService;

    @Test
    public void selectPromisingNodeWithoutChildNodes() {
        Node rootNode = new Node();
        rootNode.setState(new State());

        assertSame(rootNode, monteCarloService.selectPromisingNode(rootNode));
    }

    @Test
    public void selectPromisingNodeChildNodes() {
        Node rootNode = new Node();
        rootNode.setChildArray(new ArrayList<>());
        rootNode.setState(new State());

        Node childNode = new Node();
        childNode.setState(new State());
        rootNode.getChildArray().add(childNode);

        assertSame(childNode, monteCarloService.selectPromisingNode(rootNode));
    }

    @Test
    public void selectPromisingNodeNestedChildNodes() {
        Node rootNode = new Node();
        rootNode.setChildArray(new ArrayList<>());
        rootNode.setState(new State());

        Node childNode = new Node();
        childNode.setState(new State());

        Node nestedChildNode = new Node();
        nestedChildNode.setState(new State());

        childNode.getChildArray().add(nestedChildNode);
        rootNode.getChildArray().add(childNode);

        assertSame(nestedChildNode, monteCarloService.selectPromisingNode(rootNode));
    }

    @Test
    public void expandNode() {
        List<State> states = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            State tempState = new State();
            tempState.setGame(new Game());
            states.add(tempState);
        }
        when(aiService.getAllStates(any())).thenReturn(states);
        when(aiService.getNextPlayer(any())).thenReturn(1);

        Node parent = new Node();
        State tempState = new State();
        tempState.setGame(new Game());
        parent.setState(tempState);

        monteCarloService.expandNode(parent);

        List<State> childStates = parent.getChildArray().stream().map(Node::getState).collect(Collectors.toList());
        long nestedChildes = parent.getChildArray().stream().filter(node -> node.getChildArray().size() > 0).count();

        assertEquals(5, parent.getChildArray().size());
        assertEquals(states, childStates);
        assertEquals(0, nestedChildes);
    }

    @Test
    public void expandChanceNode() {
        List<State> states = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            State tempState = new State();
            tempState.setGame(new Game());
            tempState.setAction(new ChanceRollDiceAction(0,0,0.1*i, null));
            tempState.setPlayerNo(1);
            states.add(tempState);
        }
        State state = new State();
        states.add(state);

        when(aiService.getAllStates(any())).thenReturn(states);
        when(aiService.getNextPlayer(any())).thenReturn(1);

        Node parent = new Node();
        State tempState = new State();
        tempState.setGame(new Game());
        parent.setState(tempState);

        monteCarloService.expandNode(parent);

        assertEquals(2, parent.getChildArray().size());
        assertEquals(1, parent.getChildArray().stream().filter(Node::isChanceNode).count());
        assertEquals(4, parent.getChildArray().stream().filter(Node::isChanceNode).findFirst().get().getChildArray().size());
        assertEquals(1, parent.getChildArray().stream().filter(Node::isChanceNode).findFirst().get().getState().getPlayerNo());
        assertTrue(parent.getChildArray().stream().filter(Node::isChanceNode).findFirst().get().getChildArray().stream()
                .allMatch(node -> node.getState().getAction().getClass()== ChanceRollDiceAction.class));
        assertTrue(parent.getChildArray().stream().filter(Node::isChanceNode).findFirst().get().getChildArray().stream()
                .allMatch(node -> node.getState().getPlayerNo()==1));
        assertEquals(1.0, parent.getChildArray().stream().filter(Node::isChanceNode).findFirst().get().getChildArray().stream()
                .map(node -> node.getState().getProbability()).reduce(0.0, Double::sum));

    }

    @Test
    public void expandNodeIncorrectly() {
        List<State> states = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            State tempState = new State();
            tempState.setGame(new Game());
            tempState.setAction(new MoveRobberAction(0, null, null));
            states.add(tempState);
        }
        when(aiService.getAllStates(any())).thenReturn(states);
        when(aiService.getNextPlayer(any())).thenReturn(1);

        Node parent = new Node();
        State tempState = new State();
        tempState.setGame(new Game());
        parent.setState(tempState);

        monteCarloService.expandNode(parent);

        List<State> childStates = parent.getChildArray().stream().map(Node::getState).collect(Collectors.toList());
        long nestedChildes = parent.getChildArray().stream().filter(node -> node.getChildArray().size() > 0).count();

        assertNotEquals(10, parent.getChildArray().size());
        assertNotEquals(5, nestedChildes);
    }

    @Test
    public void backPropagation(){
        Node rootNode = new Node();
        rootNode.setChildArray(new ArrayList<>());
        rootNode.setState(new State());
        rootNode.getState().setPlayerNo(1);

        Node childNode = new Node();
        childNode.setParent(rootNode);
        childNode.setState(new State());
        childNode.getState().setPlayerNo(2);

        Node nestedChildNode = new Node();
        nestedChildNode.setParent(childNode);
        nestedChildNode.setState(new State());
        nestedChildNode.getState().setPlayerNo(1);

        childNode.getChildArray().add(nestedChildNode);
        rootNode.getChildArray().add(childNode);

        monteCarloService.backPropagation(nestedChildNode, 1);

        assertEquals(100, rootNode.getState().getWinScore());
        assertEquals(0, childNode.getState().getWinScore());
        assertEquals(100, nestedChildNode.getState().getWinScore());

        assertEquals(1, rootNode.getState().getVisitCount());
        assertEquals(1, childNode.getState().getVisitCount());
        assertEquals(1, nestedChildNode.getState().getVisitCount());
    }

    @Test
    public void simulatePlayout() {
        Game game = new Game(playerService.generateGamePlayers(Stream.of("1","2","3","4").collect(Collectors.toList()),0), boardCreationService.generate(), cardPileCreationService.generate(),"1");
        game.getPlayers().stream().filter(player -> player.getPlayerId()==1).forEach(player -> player.getRemainingActions().add(PlayerAction.INITIAL1));
        game.setCurrentPlayerId(1);
        Node rootNode = new Node();
        rootNode.setChildArray(new ArrayList<>());
        rootNode.setState(new State());
        rootNode.getState().setGame(game);
        rootNode.getState().setPlayerNo(1);

        when(aiService.getStatus(any(), anyBoolean())).thenReturn(-1).thenReturn(-1).thenReturn(-1).thenReturn(1);
        when(aiService.randomAction(any())).thenReturn(game);

        assertEquals(1, monteCarloService.simulateRandomPlayout(rootNode));
    }
}