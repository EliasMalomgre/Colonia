package kdg.colonia.gameService.services.gameInfo;

import kdg.colonia.gameService.config.costobjects.CardConfig;
import kdg.colonia.gameService.config.costobjects.CityConfig;
import kdg.colonia.gameService.config.costobjects.RoadConfig;
import kdg.colonia.gameService.config.costobjects.SettlementConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GameInfoService
{
    private final CardConfig cardConfig;
    private final SettlementConfig settlementConfig;
    private final CityConfig cityConfig;
    private final RoadConfig roadConfig;

    private List<CostObject> buildingCosts;

    public GameInfoService(CardConfig cardConfig, SettlementConfig settlementConfig, CityConfig cityConfig, RoadConfig roadConfig)
    {
        this.cardConfig = cardConfig;
        this.settlementConfig = settlementConfig;
        this.cityConfig = cityConfig;
        this.roadConfig = roadConfig;
        initializeBuildingCosts();
    }

    private void initializeBuildingCosts(){
        this.buildingCosts=new ArrayList<>(
                Arrays.asList(
                        new CostObject("Road",roadConfig.getLumberCost(),roadConfig.getGrainCost(),roadConfig.getBrickCost(),roadConfig.getWoolCost(),roadConfig.getOreCost()),
                        new CostObject("Settlement",settlementConfig.getLumberCost(),settlementConfig.getGrainCost(),settlementConfig.getBrickCost(),settlementConfig.getWoolCost(),settlementConfig.getOreCost()),
                        new CostObject("City",cityConfig.getLumberCost(),cityConfig.getGrainCost(),cityConfig.getBrickCost(),cityConfig.getWoolCost(),cityConfig.getOreCost()),
                        new CostObject("Development Card",cardConfig.getLumberCost(),cardConfig.getGrainCost(),cardConfig.getBrickCost(),cardConfig.getWoolCost(),cardConfig.getOreCost())
                )
        );
    }

    public List<CostObject> getBuildingCostInfo(){
        return buildingCosts;
    }
}
