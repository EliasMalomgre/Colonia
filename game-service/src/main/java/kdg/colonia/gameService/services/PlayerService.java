package kdg.colonia.gameService.services;

import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.PlayerAction;
import kdg.colonia.gameService.domain.devCard.ProgressCard;
import kdg.colonia.gameService.domain.devCard.ProgressCardType;
import kdg.colonia.gameService.domain.Resource;
import kdg.colonia.gameService.services.gameInfo.CostObject;
import kdg.colonia.gameService.services.gameInfo.GameInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlayerService
{

    private final GameInfoService gameInfoService;

    /**
     * This method creates the players for a new game.
     *
     * @return a list of players.
     * @param ids is a list of user id's from the auth db. It is kept for connecting a user to it's logical data.
     * */
    public ArrayList<Player> generateGamePlayers(List<String> ids, int AIs) {
        ArrayList<Player> players = new ArrayList<>();

        //Create new game players with user Id's. Game identifies them as player 1-4;
        for(int i=1;i<=ids.size();i++){
            // i is the Id for gameboard logic: 1-4. UserId is the id of the actual online player
            Player newPlayer = new Player(i, ids.get(i-1), false);
            players.add(newPlayer);
        }

        for(int i=1;i<=AIs;i++){
            // i is the Id for gameboard logic: 1-4. UserId is the id of the actual online player
            Player newPlayer = new Player(i+ids.size(), "AI"+i, true);
            players.add(newPlayer);
        }
        return players;
    }

    /**
     * This method returns the resources for a player in a game.
     *
     * @param player the player from whom the resources are requested
     * @return a map of resource type and the amount of this resource a player has
     * */
    public Map<Resource,Integer> getResourcesForPlayerInGame(Player player)
    {
      return player.getResources();
    }

    /**
     * Checks if a player is has enough resources to build a road
     *
     * @param player    the player to check
     * @return whether the player has enough resources
     */
    public boolean playerAllowedToBuildRoad(Player player) {
        if (player.getRemainingActions().contains(PlayerAction.ROAD_BUILDING)){
            return true;
        }

        CostObject road = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Road"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Cost object \"Road\"not found"));

        return player.getBricks() >= road.getBrickCost() && player.getGrain() >= road.getGrainCost() &&
                player.getLumber() >= road.getLumberCost() && player.getOre() >= road.getOreCost() &&
                player.getWool() >= road.getWoolCost();
    }

    /**
     * Checks if a player is has enough resources to build a settlement
     *
     * @param player    the player to check
     * @return whether the player has enough resources
     */
    public boolean playerAllowedToBuildSettlement(Player player) {
        CostObject settlement = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Settlement"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Cost object \"Settlement\"not found"));

        return player.getBricks() >= settlement.getBrickCost() && player.getGrain() >= settlement.getGrainCost() &&
                player.getLumber() >= settlement.getLumberCost() && player.getOre() >= settlement.getOreCost() &&
                player.getWool() >= settlement.getWoolCost();
    }

    /**
     * Checks if a player is has enough resources to build a city
     *
     * @param player    the player to check
     * @return whether the player has enough resources
     */
    public boolean playerAllowedToBuildCity(Player player) {
        CostObject city = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("City"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Cost \"City\" object not found"));

        return player.getBricks() >= city.getBrickCost() && player.getGrain() >= city.getGrainCost() &&
                player.getLumber() >= city.getLumberCost() && player.getOre() >= city.getOreCost() &&
                player.getWool() >= city.getWoolCost();
    }

    /**
     * Checks if a player is has enough resources to buy a card
     *
     * @param player    the player to check
     * @return whether the player has enough resources
     */
    public boolean playerAllowedToBuyCard(Player player) {
        CostObject card = gameInfoService.getBuildingCostInfo().stream().filter(c -> c.getObjName().equals("Development Card"))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Cost \"Development Card\" object not found"));

        return player.getBricks() >= card.getBrickCost() && player.getGrain() >= card.getGrainCost() &&
                player.getLumber() >= card.getLumberCost() && player.getOre() >= card.getOreCost() &&
                player.getWool() >= card.getWoolCost();
    }

    /**
     * Get all the possible progress card types that can be played by a player
     *
     * @param player    The player from you want to get the actions
     * @return a set of all possible progress card types
     */
    public Set<ProgressCardType> getPossibleDevelopmentCardActions(Player player) {
        return player.getCards().stream().map(ProgressCard::getCardType).collect(Collectors.toSet());
    }

    /**
     * returns the amount of knight cards for a given player
     *
     * @param player    the player to check
     * @return the amount of knight cards the player owns.
     */
    public int getAmountOfKnightCards(Player player){
        return (int) player.getCards().stream().filter(developmentCard -> developmentCard.getCardType()==ProgressCardType.KNIGHT).count();
    }

    /**
     * Takes a random resource from the given player
     *
     * @param player the player we're stealing from
     * @return The resource we took
     */
    public Resource transferRandomResource(Player player, Player playerToStealFrom){
        if (playerToStealFrom.getResourcesTotal() > 0) {
            Resource[] resources = Resource.values();

            while(true) {
                Resource resource = resources[ThreadLocalRandom.current().nextInt(resources.length)];
                boolean succeeded = playerToStealFrom.removeResources(resource);

                if (succeeded) {
                    player.addResources(resource, 1);
                    return resource;
                }
            }
        } else {
            return Resource.NOTHING;
        }
    }

}
