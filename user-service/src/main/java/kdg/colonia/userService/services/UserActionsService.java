package kdg.colonia.userService.services;

import kdg.colonia.userService.models.User;
import kdg.colonia.userService.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class UserActionsService
{
    private final UserRepository userRepository;
    private final UserEmailService userEmailService;
    private final PasswordEncoder encoder;

    public boolean removeFriend(User deletingUser,String friendName){
        try{
        Optional<User> friend=userRepository.findByUsername(friendName);
        if(friend.isEmpty()) return false;

        //Remove friend from deleting user.
        deletingUser.getFriends().removeIf(f->f.equals(friend.get().getId()));
        //Remove deleting user from friend.
        friend.get().getFriends().removeIf(f->f.equals(deletingUser.getId()));

        userRepository.save(friend.get());
        userRepository.save(deletingUser);
        return true;
        }
        catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * Method that handles a request for a password reset by email.
     * A randomized token is created with which the user can change their password once.
     * It then sends the necessary data to the email service to be handled there.
     * @param email from the user who wants to reset their password by email.
     */
    public void requestPasswordReset(String email){
        try{
        Optional<User> requestedUser = userRepository.findByEmail(email);
        if(requestedUser.isEmpty())
        {
            throw new Exception("User does not exist.");
        }
            // Create token
            String token=RandomString.make(30);
            // Save it
            requestedUser.get().setResetPasswordToken(token);
            userRepository.save(requestedUser.get());
            //Send email with token
            userEmailService.SendResetPassword(requestedUser.get().getEmail(),token);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Method to reset password with a password reset token. The token is then removed from the user.
     * @param newPassword is the new password a user wants to login with.
     * @param token from the email to reset a password exactly once
     */
    public void resetPassword(String newPassword,String token){
       try{
           Optional<User> user=userRepository.findByResetPasswordToken(token);
           if(user.isEmpty()) throw new Exception("User with this reset token not found.");
           //remove temporary token
           user.get().setResetPasswordToken(null);
           //change password
           user.get().setPassword(encoder.encode(newPassword));
           userRepository.save(user.get());
       }
       catch (Exception e){
           log.error(e.getMessage());
           throw new RuntimeException(e.getMessage());
       }
    }
}
