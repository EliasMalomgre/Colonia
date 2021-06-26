package kdg.colonia.userService.payload.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest
{
    @Size(min = 1,max = 40)
    private String username;
    @Size(min = 6,max = 120)
    private String password;
}
