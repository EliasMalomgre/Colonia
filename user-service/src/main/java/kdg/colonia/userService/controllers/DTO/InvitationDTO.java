package kdg.colonia.userService.controllers.DTO;

import kdg.colonia.userService.models.Invitation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDTO
{
    Invitation invitation;
    String hostname;
}
