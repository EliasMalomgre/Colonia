package kdg.colonia.userService.controllers;

import kdg.colonia.userService.controllers.DTO.InvitationDTO;
import kdg.colonia.userService.controllers.DTO.ResetPasswordDTO;
import kdg.colonia.userService.models.*;
import kdg.colonia.userService.payload.request.ModifyPasswordRequest;
import kdg.colonia.userService.payload.response.MessageResponse;
import kdg.colonia.userService.payload.response.UserDataResponse;
import kdg.colonia.userService.repository.UserRepository;
import kdg.colonia.userService.services.FriendRequestService;
import kdg.colonia.userService.services.InvitationService;
import kdg.colonia.userService.services.LobbyService;
import kdg.colonia.userService.services.UserActionsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserRepository userRepository;
    private final FriendRequestService friendRequestService;
    private final UserActionsService userActionsService;
    private final PasswordEncoder encoder;
    private final LobbyService lobbyService;
    private final InvitationService invitationService;


    @GetMapping("/getDataForLoggedInUser")
    public ResponseEntity<?> getUserData()
    {
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        //If exists -> return user data
        if (foundUser.isPresent())
        {
            UserDataResponse response = new UserDataResponse(foundUser.get().getUsername(),foundUser.get().getEmail(), getFriendUserNames(foundUser.get()));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    @PostMapping("/sendFriendRequest")
    public ResponseEntity<?> addFriendRequest(String usernameOfFriend){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get token user from db
        Optional<User> asking = userRepository.findByUsername(user.getName());
        //get receiving user from db
        Optional<User> receiving = userRepository.findByUsername(usernameOfFriend);
        if(asking.isPresent()&&receiving.isPresent()){
            boolean success=friendRequestService.addFriendRequest(asking.get(),receiving.get());
            if(success)return ResponseEntity.ok().build();
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/getFriendRequests")
    public ResponseEntity<?> getFriendRequests(){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get token user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("No user found with this token.");
        List<FriendRequest> friendRequests=friendRequestService.getFriendRequests(foundUser.get());
        return ResponseEntity.ok(friendRequests);
    }
    @PostMapping("/acceptFriendRequest")
    public ResponseEntity<?> acceptFriendRequest(String friendRequestId){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get token user from db
        Optional<User> accepter = userRepository.findByUsername(user.getName());
        //accept friend request
        if(accepter.isPresent()){
            boolean accepted=friendRequestService.acceptFriendRequest(accepter.get(),friendRequestId);
            if(accepted) return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/declineFriendRequest")
    public ResponseEntity<?> declineFriendRequest(String friendRequestId){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get token user from db
        Optional<User> accepter = userRepository.findByUsername(user.getName());
        //decline friend request
        if(accepter.isPresent()){
            boolean declined=friendRequestService.declineFriendRequest(accepter.get(),friendRequestId);
            if(declined) return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/deleteFriend")
    public ResponseEntity<?> removeFriend(String username){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        if(foundUser.isPresent()){
            boolean success=userActionsService.removeFriend(foundUser.get(),username);
            if(success) return ResponseEntity.ok().build();
            //Something went wrong deleting friend
            return ResponseEntity.status(500).build();
        }
        //User could not be found from token.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody ModifyPasswordRequest modifyPasswordRequest)
    {
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        //If exists -> change password
        foundUser.ifPresent(value -> value.setPassword(encoder.encode(modifyPasswordRequest.getNewPassword())));
        //if not exists -> return Unauthorized
        if (foundUser.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        //Save modified user to db
        userRepository.save(foundUser.get());
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    @PostMapping("/setNotificationSetting")
    public ResponseEntity<?> updateNotificationSetting(boolean value){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        if(foundUser.isPresent()){
            foundUser.get().setReceiveNotifications(value);
            userRepository.save(foundUser.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private List<String> getFriendUserNames(User user){
        List<String>userNames=new ArrayList<>();
        for(String userId:user.getFriends()){
            Optional<User> friend=userRepository.findById(userId);
            friend.ifPresent(value -> userNames.add(value.getUsername()));
        }
        return userNames;
    }

    @PostMapping("/requestPasswordReset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email){
        try{
            userActionsService.requestPasswordReset(email);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO){
        try{
            userActionsService.resetPassword(resetPasswordDTO.getPassword()
                    ,resetPasswordDTO.getResetToken());
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("createLobby")
    public ResponseEntity<?> createLobby(@RequestBody Map<Integer, LobbyInvitation> invitations){
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("Host user does not exist");
        //create the lobby if it does not exist
        Lobby createdLobby=lobbyService.createLobby(invitations,foundUser.get().getId());
        return ResponseEntity.ok().body(createdLobby);
    }

    @PostMapping("acceptInvite")
    public ResponseEntity<?> acceptInvite(@RequestParam String invitationId){
        try{
        //Get user from token
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        //get user from db
        Optional<User> foundUser = userRepository.findByUsername(user.getName());
        if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("Host user does not exist");
        //Add User to lobby
        Lobby toReturn =lobbyService.addUserToLobby(invitationId,foundUser.get().getId());
        return ResponseEntity.ok(toReturn);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("declineInvite")
    public ResponseEntity<?> declineInvite(@RequestParam String invitationId){
        try{
            //Get user from token
            Authentication user = SecurityContextHolder.getContext().getAuthentication();
            //get user from db
            Optional<User> foundUser = userRepository.findByUsername(user.getName());
            if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("Host user does not exist");
            //Add User to lobby
            invitationService.deleteInvitation(invitationId);
            return ResponseEntity.ok("Deleted invite.");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("getInvitations")
    public ResponseEntity<?> getInvitations(){
        try{
            //Get user from token
            Authentication user = SecurityContextHolder.getContext().getAuthentication();
            //get user from db
            Optional<User> foundUser = userRepository.findByUsername(user.getName());
            if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("User does not exist");
            //Get user invitations
            List<Invitation> invitations=invitationService.getInvitationsForUser(foundUser.get().getId());
            //Get hostname for each lobby of invitation
            List<InvitationDTO> invitationDTOS = new ArrayList<>();
            for(Invitation invitation:invitations){
                String hostname=lobbyService.getLobby(invitation.getLobbyId()).getHost().getUsername();
                invitationDTOS.add(new InvitationDTO(invitation,hostname));
            }
            return ResponseEntity.ok(invitationDTOS);
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("getUsername")
    public ResponseEntity<?> getUsername(String userid){
        try{
            //get user from db
            Optional<User> foundUser = userRepository.findById(userid);
            if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("User does not exist");
            //get username
            String username = foundUser.get().getUsername();
            return ResponseEntity.ok(username);


        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("getLobby")
    public ResponseEntity<?> getLobby(String lobbyId){
        try{
            //Get user from token
            Authentication user = SecurityContextHolder.getContext().getAuthentication();
            //get user from db
            Optional<User> foundUser = userRepository.findByUsername(user.getName());
            if(foundUser.isEmpty()) return ResponseEntity.badRequest().body("Host user does not exist");
            //get Lobby
            Lobby lobby=lobbyService.getLobby(lobbyId);
            //Check if user was added to lobby or is host
            Optional<LobbyUser> userInLobby=lobby.getLobbyUsers().stream().filter(u->u.getId().equals(foundUser.get().getId())).findFirst();
            if(userInLobby.isEmpty()&&!lobby.getHost().getId().equals(foundUser.get().getId())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not invited to lobby, check if you accepted your invite if you received one.");
            else return ResponseEntity.ok(lobby);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
