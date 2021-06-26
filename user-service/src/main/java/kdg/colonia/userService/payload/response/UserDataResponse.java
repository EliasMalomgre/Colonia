package kdg.colonia.userService.payload.response;

import kdg.colonia.userService.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDataResponse
{
    private String username;
    private String email;
    private List<String> friends;
}
