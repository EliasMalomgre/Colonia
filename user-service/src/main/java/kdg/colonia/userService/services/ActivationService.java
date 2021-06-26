package kdg.colonia.userService.services;

import kdg.colonia.userService.models.User;
import kdg.colonia.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivationService
{
    private final UserRepository userRepository;
    private final UserEmailService userEmailService;
    private final PasswordEncoder encoder;

    /**
     * Method that handles token creation for validating user account.
     * @param user that was created that needs to activate their account.
     */
    public void sendActivationEmail(User user){
        try{
        // Create token
        String token= RandomString.make(40);
        // Save it
        user.setValidateUserToken(token);
        userRepository.save(user);
        //send the email
        userEmailService.sendActivationEmail(user.getEmail(),user.getValidateUserToken());
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException("Could not send the activation email.");
        }
    }

    /**
     * User account activation via email token.
     * Method checks for user with this token and activates the account.
     * @param token received through email that can be used to validate account.
     */
    public void activateUserAccount(String token){
        try{
        Optional<User> user=userRepository.findByValidateUserToken(token);
        if(user.isEmpty()) throw new Exception("Could not find user with this activation token");
        user.get().setValidatedEmail(true);
        user.get().setValidateUserToken(null);
        userRepository.save(user.get());
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
