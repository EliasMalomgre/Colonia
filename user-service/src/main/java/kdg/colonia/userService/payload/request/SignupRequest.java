package kdg.colonia.userService.payload.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

@Getter
@Setter
public class SignupRequest
{
    @Size(min = 1,max = 40)
    private String username;
    @Email
    private String email;
    @Size(min = 6,max=120)
    private String password;
}
