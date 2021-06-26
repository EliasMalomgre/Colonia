package kdg.colonia.userService.services;

import kdg.colonia.userService.models.*;
import kdg.colonia.userService.repository.InvitationRepository;
import kdg.colonia.userService.repository.LobbyRepository;
import kdg.colonia.userService.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class LobbyService
{
    private final UserRepository userRepository;
    private final LobbyRepository lobbyRepository;
    private final InvitationService invitationService;

    /**
     * Method that creates a lobby and sends invitations to invited players
     * @param invitations Invites to lobby with type of user and their
     * @param creatorId userId of creator
     * @return a created lobby after invites have been sent.
     */
    public Lobby createLobby(Map<Integer, LobbyInvitation> invitations,String creatorId){
        try{
        Lobby lobby=new Lobby();
        //Set Host
        Optional<User> user=userRepository.findById(creatorId);
        if(user.isEmpty()) throw new NullPointerException("User does not exist");
        lobby.setHost(new LobbyUser(user.get().getId(),user.get().getUsername()));
        //Save lobby with host
            lobby=lobbyRepository.save(lobby);
            //Invite users
        for (LobbyInvitation lobbyInvitation :invitations.values())
        {
            if(lobbyInvitation!=null&&lobbyInvitation.getInviteMethod()!=null){
            switch (lobbyInvitation.getInviteMethod()){
                case "email":
                    //handle invites for players that don't exist in system yet
                    invitationService.inviteNewUser(lobbyInvitation.getCredential(),lobby);
                    lobby.setAmountOfHuman(lobby.getAmountOfHuman()+1);
                    lobbyRepository.save(lobby);
                    break;
                case "friend":
                    //Invite existing user
                    invitationService.inviteExistingUser(lobbyInvitation.getCredential(),lobby);
                    lobby.setAmountOfHuman(lobby.getAmountOfHuman()+1);
                    lobbyRepository.save(lobby);
                    break;
                case "AI":
                   //If AI just add 1 to amount of AI players
                    lobby.setAmountOfAI(lobby.getAmountOfAI()+1);
                    lobbyRepository.save(lobby);
                    break;
            }}
        }
        return lobby;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * Method that adds a user to a lobby by invitation accept
     * @param invitationId the users invitation to the lobby
     * @param userID of the invited user
     * @return the lobby with the joined player.
     */
    public Lobby addUserToLobby(String invitationId,String userID){
        try{
            //Get user
            Optional<User> user=userRepository.findById(userID);
            if(user.isEmpty()) throw new NullPointerException("User does not exist in system.");
            //get lobby by invitationId
            Optional<Lobby> lobby=lobbyRepository.findById(invitationService.getInvitation(invitationId).getLobbyId());
            if(lobby.isEmpty()) throw new Exception("Lobby does not exist");
            //Add user to lobby
            lobby.get().getLobbyUsers().add(new LobbyUser(user.get().getId(),user.get().getUsername()));
            //Save lobby with added user
            Lobby savedLobby=lobbyRepository.save(lobby.get());
            //delete the invitation
            invitationService.deleteInvitation(invitationId);
            return savedLobby;
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public Lobby getLobby(String lobbyId){
        try{
            Optional<Lobby> lobby=lobbyRepository.findById(lobbyId);
            if(lobby.isEmpty()) throw new Exception("Lobby does not exist.");
            return lobby.get();
        }catch (Exception e){
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

}
