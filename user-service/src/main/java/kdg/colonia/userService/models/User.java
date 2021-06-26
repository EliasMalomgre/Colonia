package kdg.colonia.userService.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "users")
@Validated
@NoArgsConstructor
public class User
{
    @Id
    private String id;
    @NotBlank
    @Size(max=20)
    private String username;
    @Email
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String email;
    private String password;
    //list of friend id's
    private List<String> friends=new ArrayList<>();
    private boolean receiveNotifications;
    //In case password needs to be reset.
    private String resetPasswordToken;
    //To validate user token
    private String validateUserToken;
    private boolean validatedEmail;


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.friends=new ArrayList<>();
        this.receiveNotifications=false;
        this.validatedEmail=false;
    }
    public User(String username, String email, String password,List<String> friends) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.friends=friends;
    }
}
