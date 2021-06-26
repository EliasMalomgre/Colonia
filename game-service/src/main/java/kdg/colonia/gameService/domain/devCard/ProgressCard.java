package kdg.colonia.gameService.domain.devCard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProgressCard
{
    private ProgressCardType cardType;

    public ProgressCard(ProgressCardType cardType) {
        this.cardType = cardType;
    }

    /**
     * Copying an existing object into a new object but referencing a new object
     *
     * @param progressCard the progressCard to copy
     */
    public ProgressCard(ProgressCard progressCard) {
        this.cardType = progressCard.getCardType();
    }
}
