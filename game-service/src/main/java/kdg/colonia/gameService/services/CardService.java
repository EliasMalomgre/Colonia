package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.controllers.RESTToSocketsController;
import kdg.colonia.gameService.domain.Game;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.devCard.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class CardService {

    private final CardConfig cardConfig;
    private final RESTToSocketsController socketsController;

    /**
     * This method checks if the a player has the required resources to buy a development card.
     * If the player does have the required resources, the card is added.
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if successful
     */
    public boolean buyCard(Game game, Player player) {
        //check if cardPile is empty
        if (game.getCardPile().isEmpty()) {
            log.warn(String.format("Game[%s]: player %d tried to buy a card but the card pile is empty!",game.getId(),player.getPlayerId()));
            return false;
        }

        //check if the player has the required resources and remove if possible
        if (player.removeResources(cardConfig.getBrickCost(), cardConfig.getGrainCost(), cardConfig.getLumberCost(), cardConfig.getOreCost(), cardConfig.getWoolCost())) {
            ProgressCard developmentCard = game.getCardPile().get(0);
            player.getNewCards().add(developmentCard);
            game.getCardPile().remove(0);

            return true;
        }

        log.warn(String.format("Game[%s]: player %d did not have enough resources to buy a development card.", game.getId(), player.getPlayerId()));
        return false;
    }

    /**
     * This method is called when a player wants to play a knight card.
     * The method checks whether the player has a knight card to play and calls a check on the largest army title
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if the play was successful
     */
    public boolean playKnightCard(Game game, Player player) {
        //check if player has a knight card to play
        if (player.getCards().stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).count() >= 1) {
            final ProgressCard[] card = new ProgressCard[1];
            player.getCards().stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).findFirst()
                    .ifPresentOrElse(developmentCard -> card[0] =  developmentCard, null);

            if (card[0] != null) {
                player.getCards().remove(card[0]);
                player.getPlayedCards().add(card[0]);
                checkLargestArmy(game, player);
                return true;
            } else {
                log.error(String.format("Game[%s]: could not retrieve knight card for player %d. Object should have been present, but was not.", game.getId(), player.getPlayerId()));
                return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a knight card, but doesn't have any such cards", game.getId(), player.getPlayerId()));
            return false;
        }
    }

    /**
     * This method is called when the player tries to play a victory point card.
     * If the player has such a card, the card is played and a victory point is added.
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if the play was successful
     */
    public boolean playVictoryPointCard(Game game, Player player) {
        //check if player has a victory-point card to play
        if (player.getCards().stream().filter(c -> c.getCardType() == ProgressCardType.VICTORY_POINT).count() >= 1) {
            final ProgressCard[] victoryPointCard = new ProgressCard[1];
            player.getCards().stream().filter(c -> c.getCardType() == ProgressCardType.VICTORY_POINT).findFirst()
                    .ifPresentOrElse(developmentCard -> victoryPointCard[0] =  developmentCard, null);

            if (victoryPointCard[0] != null) {
                player.getCards().remove(victoryPointCard[0]);
                player.getPlayedCards().add(victoryPointCard[0]);
                player.increaseVictoryPoints(1);
                return true;
            } else {
                log.error(String.format("Game[%s]: could not retrieve victorypoint card for player %d. Object should have been present, but was not.", game.getId(), player.getPlayerId()));
                return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a victorypoint card, but doesn't have any such cards", game.getId(), player.getPlayerId()));
            return false;
        }
    }

    /**
     * This method is called when a player tries to play a year of plenty card
     * If the player has one of such cards, they get the YOP token
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if the play was successful
     */
    public boolean playYearOfPlentyCard(Game game, Player player) {
        //check if player has yop card
        if (player.getCards().stream().filter(c -> c.getClass() == ProgressCard.class && c
                .getCardType() == ProgressCardType.YEAR_OF_PLENTY).count() >= 1) {

            final ProgressCard[] progressCard = new ProgressCard[1];
            player.getCards().stream().filter(c -> c.getClass() == ProgressCard.class && c
                    .getCardType() == ProgressCardType.YEAR_OF_PLENTY).findFirst().ifPresentOrElse(developmentCard -> progressCard[0] = (ProgressCard) developmentCard, null);

            if (progressCard[0] != null) {
                player.getRemainingActions().add(PlayerAction.YOP);
                player.getCards().remove(progressCard[0]);
                player.getPlayedCards().add(progressCard[0]);
                return true;
            } else {
                log.error(String.format("Game[%s]: could not retrieve year of plenty card for player %d. Object should have been present, but was not.", game.getId(), player.getPlayerId()));
                return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a year of plenty card, but doesn't have any such cards", game.getId(), player.getPlayerId()));
            return false;
        }
    }

    /**
     * This method is called when a player tries to play a monopoly card
     * If the player has one of such cards, they get the MONOPOLY token
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if the play was successful
     */
    public boolean playMonopoly(Game game, Player player) {
        //check if player has a monopoly card to play
        if (player.getCards().stream().filter(c -> c.getClass() == ProgressCard.class && c.getCardType() == ProgressCardType.MONOPOLY).count() >= 1) {
            final ProgressCard[] monopolyCard = new ProgressCard[1];
            player.getCards().stream().filter(c -> c.getClass() == ProgressCard.class && c.getCardType() == ProgressCardType.MONOPOLY).findFirst().ifPresentOrElse(developmentCard -> monopolyCard[0] = (ProgressCard) developmentCard, null);

            if (monopolyCard[0] != null) {
                player.getRemainingActions().add(PlayerAction.MONOPOLY);
                player.getCards().remove(monopolyCard[0]);
                player.getPlayedCards().add(monopolyCard[0]);
                return true;
            } else {
                log.error(String.format("Game[%s]: could not retrieve monopoly card for player %d. Object should have been present, but was not.", game.getId(), player.getPlayerId()));
                return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a monopoly card, but doesn't have any such cards", game.getId(), player.getPlayerId()));
            return false;
        }
    }

    /**
     * This method is called when a player tries to play a road building card
     * If the player has one of such cards, they get the ROAD_BUILDING token
     *
     * @param game   the current game, validated by GameService
     * @param player the current player, validated by GameService
     * @return true if the play was successful
     */
    public boolean playRoadBuildingCard(Game game, Player player) {
        if (player.getCards().stream().filter(c-> c.getCardType()==ProgressCardType.ROAD_BUILDING).count() >= 1){
            final ProgressCard[] progressCard = new ProgressCard[1];
            player.getCards().stream().filter(c-> c.getCardType()==ProgressCardType.ROAD_BUILDING).findFirst().ifPresentOrElse(progressCard1 -> progressCard[0] = progressCard1,null);

            if (progressCard[0]!=null){
                player.getRemainingActions().add(PlayerAction.ROAD_BUILDING);
                player.getRemainingActions().add(PlayerAction.ROAD_BUILDING);
                player.getCards().remove(progressCard[0]);
                player.getPlayedCards().add(progressCard[0]);
                return true;
            } else {
                //if the next error is printed, something went really wrong
                log.error(String.format("Game[%s]: could not retrieve road building card for player %d. Object should have been present, but was not.", game.getId(), player.getPlayerId()));
                return false;
            }
        } else {
            log.warn(String.format("Game[%s]: player %d tried to play a road building card, but doesn't have any such cards", game.getId(), player.getPlayerId()));
            return false;
        }
    }

    /**
     * This method checks if the player with the largest army has changed.
     * This method is called every time a knight card is played.
     *
     * @param game   the current game object, validated by GameService
     * @param player the card playing player object, validated by GameService
     */
    private void checkLargestArmy(Game game, Player player) {
        //check if someone already holds the title of largest army
        if (game.getPlayerWithLargestArmy() != 0) {
            final int[] currentLargestArmySize = new int[1];

            game.getPlayers().stream().filter(
                    p -> p.getPlayerId() == game.getPlayerWithLargestArmy()
            ).findFirst().ifPresent(
                    pl -> currentLargestArmySize[0] = (int) pl.getPlayedCards().stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).count()
            );

            //check if the current player has a more knight cards
            if (player.getPlayedCards().stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).count() > currentLargestArmySize[0]) {

                //Add 2 victory points to player with largest army
                player.increaseVictoryPoints(2);

                //Remove 2 victory points from previous holder
                game.getPlayers().stream().filter(
                        p -> p.getPlayerId() == game.getPlayerWithLargestArmy()
                ).findFirst().ifPresent(
                        pl -> pl.decreaseVictoryPoints(2)
                );

                game.setPlayerWithLargestArmy(player.getPlayerId());
            }
        } else {
            //player needs at least 3 knights to hold the title
            if (player.getPlayedCards().stream().filter(c -> c.getCardType() == ProgressCardType.KNIGHT).count() >= 3) {
                //Add 2 victory points to player with largest army
                player.increaseVictoryPoints(2);
                game.setPlayerWithLargestArmy(player.getPlayerId());
            }
        }
    }
}
