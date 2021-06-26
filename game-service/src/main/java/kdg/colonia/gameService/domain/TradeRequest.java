package kdg.colonia.gameService.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document
public class TradeRequest
{
    @Id
    private String id;
    private int askingPlayer;
    private int receivingPlayer;
    private Map<Resource,Integer> toSendResources;
    private Map<Resource,Integer> toReceiveResources;

    public TradeRequest(int askingPlayer, int receivingPlayer, Map<Resource, Integer> toSend, Map<Resource, Integer> toReceive)
    {
        this.id=new ObjectId().toHexString();
        this.askingPlayer = askingPlayer;
        this.receivingPlayer = receivingPlayer;
        this.toSendResources = toSend;
        this.toReceiveResources = toReceive;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param tradeRequest    the tradeRequest to copy
     */
    public TradeRequest(TradeRequest tradeRequest) {
        this.id=new ObjectId().toHexString();
        this.askingPlayer = tradeRequest.getAskingPlayer();
        this.receivingPlayer = tradeRequest.getReceivingPlayer();
        this.toSendResources = new HashMap<>(tradeRequest.toSendResources);
        this.toReceiveResources = new HashMap<>(tradeRequest.toReceiveResources);
    }
}
