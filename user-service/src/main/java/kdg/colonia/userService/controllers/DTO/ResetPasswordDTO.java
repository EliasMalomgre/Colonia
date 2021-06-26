package kdg.colonia.userService.controllers.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO
{
    @Size(min = 6,max = 120)
    private String password;
    @NotNull
    private String resetToken;
}
