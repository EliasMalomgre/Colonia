package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.*;
import kdg.colonia.gameService.domain.coordinates.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TradeService {

    /**
     * This methods creates a trade request
     *
     * @param game      the current game state
     * @param asking    the player wanting to trade
     * @param receiving the player receiving a trade request
     * @param toSend    the resources that are sent by the asker
     * @param toReceive the resources that are received by the asker
     * @return the game state after creating the trade request
     */
    public Game startTradeRequest(Game game, Player asking, Player receiving, Map<Resource, Integer> toSend, Map<Resource, Integer> toReceive) {

        //Check if there already is an active trade request
        if (game.getTradeRequest() == null) {
            game.setTradeRequest(new TradeRequest(asking.getPlayerId(), receiving.getPlayerId(), toSend, toReceive));
            return game;
        }
        log.warn(String.format("Game[%s]: player %d tried to start a trade but there is already a trade in place",
                game.getId(), asking.getPlayerId()));
        return game;
    }

    /**
     * This methods accepts a trade request.
     *
     * @param game       the current game, verified by GameService
     * @param asking     the the player that sent the trade request
     * @param receiving  the player accepting the trade.
     * @return an edited game object where the trade has taken place.
     **/
    public Game acceptTradeRequest(Game game, Player asking, Player receiving) {
        //Check if both players have the correct amount of resources
        boolean removeFromAsker = asking.hasEnoughResources(game.getTradeRequest().getToSendResources());
        boolean removeFromReceiver = receiving.hasEnoughResources(game.getTradeRequest().getToReceiveResources());

        if (removeFromAsker && removeFromReceiver) {
            asking.removeResources(game.getTradeRequest().getToSendResources());
            receiving.removeResources(game.getTradeRequest().getToReceiveResources());

            //Both players still had the sufficient resources that are now removed.
            receiving.addResources(game.getTradeRequest().getToSendResources());
            asking.addResources(game.getTradeRequest().getToReceiveResources());
            asking.getRemainingActions().add(PlayerAction.TRADE);
        } else {
            throw new IllegalArgumentException("Insufficient resources for one of the involved players");
        }
        //Delete traderequest from game
        game.setTradeRequest(null);
        return game;
    }

    /**
     * This methods checks at what ratio a player can trade a certain resource with the bank
     *
     * @param game   the current game, verified by GameService
     * @param player the trading player, verified by GameService
     * @param from   the resource the player wants to trade from
     * @return the ratio e.g. 3 = any 3 resources for 1 chosen resource
     */
    public int getBankRatio(Game game, Player player, Resource from) {
        int ratio = 4;
        List<Harbour> validHarbours = game.getBoard().getHarbours().stream().filter(h -> h.getResource().equals(from) || h.getResource().equals(Resource.NOTHING)).collect(Collectors.toList());
        if (!validHarbours.isEmpty()) {
            //player settlement coordinates
            List<Coordinate> playerSettlementCoordinates = new ArrayList<>();
            for (Settlement settlement : game.getBoard().getSettlements().stream().filter(s -> s.getPlayerId() == player.getPlayerId()).collect(Collectors.toList())) {
                playerSettlementCoordinates.add(settlement.getCoordinate());
            }
            //check if any special harbours match
            List<Harbour> specialHarbours = validHarbours.stream().filter(h -> h.getResource().equals(from)).collect(Collectors.toList());
            if (!specialHarbours.isEmpty()) {
                List<Coordinate> coordinatesToCheck = new ArrayList<>();
                for (Harbour harbour : specialHarbours) {
                    coordinatesToCheck.addAll(harbour.getAccessCoordinates());
                }
                for (Coordinate playerSettlement : playerSettlementCoordinates) {
                    if (coordinatesToCheck.stream().anyMatch(c -> c.equals(playerSettlement))) {
                        ratio = 2;
                    }
                }

            }
            if (ratio !=2)
            {
                //check for generic harbours
                List<Harbour> genericHarbours = validHarbours.stream().filter(h -> h.getResource().equals(Resource.NOTHING)).collect(Collectors.toList());
                if (!genericHarbours.isEmpty()) {
                    List<Coordinate> coordinatesToCheck = new ArrayList<>();
                    for (Harbour harbour : genericHarbours) {
                        coordinatesToCheck.addAll(harbour.getAccessCoordinates());
                    }
                    for (Coordinate playerSettlement : playerSettlementCoordinates) {
                        if (coordinatesToCheck.stream().anyMatch(c -> c.equals(playerSettlement))) {
                            ratio = 3;
                        }
                    }

                }
            }
        }
        return ratio;
    }

    /**
     * This methods let's a player trade with the bank
     *
     * @param game   the current game, verified by GameService
     * @param player the trading player, verified by GameService
     * @param from   the resource the player wants to trade
     * @param to     the resource the player wants to obtain
     * @return whether the trade was successful
     */
    public boolean tradeWithBank(Game game, Player player, Resource from, Resource to) {
        //getting bank ratio
        int ratio = getBankRatio(game, player, from);

        //checking if player has enough resources
        if (player.getResources().get(from) < ratio) {
            log.warn(String.format("Game[%s]: player %d doesn't have enough resources to make their proposed trade", game.getId(), player.getPlayerId()));
            return false;
        }

        player.removeResources(from, ratio);
        player.addResources(to, 1);
        return true;
    }

    /**
     * This method will cancel the current trade request, if the player who wants to cancel is the one who made it
     *
     * @param game the current game
     * @param player the player who wants to cancel
     * @return true if successful
     */
    public boolean cancelTradeRequest(Game game,  Player player){
        //check if the player cancelling the trade request is the asker

        if (game.getTradeRequest().getAskingPlayer()==player.getPlayerId()){
            game.setTradeRequest(null);
            player.getRemainingActions().add(PlayerAction.TRADE);
            return true;
        }
        log.warn(String.format("Game[%s]: player %d tried to cancel a trade request, but they weren't the asking player", game.getId(), player.getPlayerId()));
        return false;
    }

    /**
     * This method will cancel the current trade request, if the player who wants to cancel is the one who received it
     *
     * @param game      the current game
     * @param asking    the player wanting to trade
     * @param receiving the player who wants to decline
     * @return true if successful
     */
    public boolean declineTradeRequest(Game game, Player asking, Player receiving){
        //check if the player declining the trade request is the receiver
        if (game.getTradeRequest().getReceivingPlayer()==receiving.getPlayerId()){
            game.setTradeRequest(null);
            asking.getRemainingActions().add(PlayerAction.TRADE);

            return true;
        }
        log.warn(String.format("Game[%s]: player %d tried to cancel a trade request, but they weren't the receiving player",
                game.getId(), receiving.getPlayerId()));
        return false;
    }
}
