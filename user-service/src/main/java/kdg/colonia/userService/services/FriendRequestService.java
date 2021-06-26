package kdg.colonia.userService.services;

import kdg.colonia.userService.controllers.RESTToSocketsController;
import kdg.colonia.userService.models.FriendRequest;
import kdg.colonia.userService.models.User;
import kdg.colonia.userService.repository.FriendRequestRepository;
import kdg.colonia.userService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendRequestService
{
    private final FriendRequestRepository repository;
    private final UserRepository userRepository;
    private final RESTToSocketsController socketsController;

    public boolean addFriendRequest(User userAsking, User userReceiving){
        try{
        repository.save(new FriendRequest(userAsking.getId(),userReceiving.getId()));
        socketsController.sendFriendRequestInvite(userReceiving.getId());
        return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    public List<FriendRequest> getFriendRequests(User receivingUser){
       return repository.getFriendRequestByReceivingUserId(receivingUser.getId());
    }

    public boolean acceptFriendRequest(User acceptingUser,String requestId){
        try{
            Optional<FriendRequest> request= repository.findById(requestId);
            if(request.isEmpty())return false;
            if(request.get().getReceivingUserId().equals(acceptingUser.getId())){
                Optional<User> requestingUser = userRepository.findById(request.get().getAskingUserId());
                Optional<User> receivingUser=userRepository.findById(request.get().getReceivingUserId());
                if(receivingUser.isPresent()&&requestingUser.isPresent()){
                    requestingUser.get().getFriends().add(receivingUser.get().getId());
                    receivingUser.get().getFriends().add(requestingUser.get().getId());
                    userRepository.save(receivingUser.get());
                    userRepository.save(requestingUser.get());
                    repository.delete(request.get());
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
    public boolean declineFriendRequest(User decliningUser,String requestId){
        try{
            Optional<FriendRequest> request= repository.findById(requestId);
            if(request.isEmpty())return false;
            if(request.get().getReceivingUserId().equals(decliningUser.getId())){
                repository.delete(request.get());
                return true;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
}
