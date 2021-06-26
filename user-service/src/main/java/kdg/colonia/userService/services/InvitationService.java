package kdg.colonia.userService.services;

import kdg.colonia.userService.controllers.RESTToSocketsController;
import kdg.colonia.userService.models.Invitation;
import kdg.colonia.userService.models.Lobby;
import kdg.colonia.userService.models.User;
import kdg.colonia.userService.repository.InvitationRepository;
import kdg.colonia.userService.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class InvitationService
{
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final UserEmailService userEmailService;
    private final RESTToSocketsController socketsController;

    /**
     * Method to invite a user that exists in the system to lobby
     * @param username of invited user
     * @param lobby the user should be invited to.
     */
    public void inviteExistingUser(String username, Lobby lobby){
        try{
            Optional<User> user=userRepository.findByUsername(username);
            if(user.isEmpty()) throw new NullPointerException("User does not exist");
            //Create invitation
            Invitation invitation= new Invitation(lobby.getId(),user.get().getId());
            //save invitation
            invitationRepository.save(invitation);
            socketsController.sendFriendRequestInvite(user.get().getId());

        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Invite a user that does not exist in the system yet.
     * This is done by sending the lobby id on account creation and adding the invite to user then.
     * @param email of user that should be invited
     * @param lobby the user should be invited to.
     */
    public void inviteNewUser(String email, Lobby lobby){
        try{
            //Send invitation
            userEmailService.sendInvitationMail(email,lobby.getId());
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }


    public List<Invitation> getInvitationsForUser(String userId){
        try{
            //Get invitations for a user
            return invitationRepository.getInvitationsByInvitedUserId(userId);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Invitation getInvitation(String invitationId){
        try{
            Optional<Invitation> invitation=invitationRepository.findById(invitationId);
            if(invitation.isEmpty()) throw new Exception("Invitation does not exist.");
            return invitation.get();
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    public void deleteInvitation(String invitationId){
        try{
            Invitation toDelete=getInvitation(invitationId);
            invitationRepository.delete(toDelete);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
