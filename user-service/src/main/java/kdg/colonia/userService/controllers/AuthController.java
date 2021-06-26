package kdg.colonia.userService.controllers;

import kdg.colonia.userService.models.User;
import kdg.colonia.userService.payload.request.LoginRequest;
import kdg.colonia.userService.payload.request.SignupRequest;
import kdg.colonia.userService.payload.response.JwtResponse;
import kdg.colonia.userService.payload.response.MessageResponse;
import kdg.colonia.userService.repository.UserRepository;
import kdg.colonia.userService.security.jwt.JwtUtils;
import kdg.colonia.userService.security.services.UserDetailsImplementation;
import kdg.colonia.userService.services.ActivationService;
import kdg.colonia.userService.services.FriendRequestService;
import kdg.colonia.userService.services.InvitationService;
import kdg.colonia.userService.services.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final ActivationService activationService;
    private final InvitationService invitationService;
    private final LobbyService lobbyService;

    /**
     * Check if the user for which this request is being made has the actual user's token.
     * Prevents other players from using their token to pretend they are the player executing a request.
     *
     * @param userId
     * @return boolean to see if the correct user is sending out the request
     */
    @GetMapping("/authForUser")
    public boolean AuthenticateUserRequest(String userId)
    {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
        {
            //if authenticated
            //check if user calling it is same as to
            Authentication user = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> foundUser = userRepository.findByUsername(user.getName());
            if (foundUser.isEmpty()) return false;
            //check if user is the same
            if (foundUser.get().getId().equals(userId)) return true;
            //the user executing this request is not the same as the token given.
            return false;
        }
        //No token
        return false;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
    {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
        //check if account has been activated.
        Optional<User> user=userRepository.findByUsername(userDetails.getUsername());
        if(user.isEmpty())return ResponseEntity.badRequest().body("User does not exist.");
        if(!user.get().isValidatedEmail()) return ResponseEntity.badRequest().body("Account has not been activated yet");
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest)
    {
        try{
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
        {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail()))
        {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),new ArrayList<>());

        user=userRepository.save(user);
        //send the activation email.
        activationService.sendActivationEmail(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
        catch (Exception e){
           return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/registerWithInvite")
    public ResponseEntity<?> registerUserWithInvite(@Valid @RequestBody SignupRequest signUpRequest,@RequestParam String lobbyId)
    {
        try{
            if (userRepository.existsByUsername(signUpRequest.getUsername()))
            {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail()))
            {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),new ArrayList<>());
            //Activate user account as user already got here through email
            user.setValidatedEmail(true);
            user=userRepository.save(user);
            //Create invite for newly created user
            invitationService.inviteExistingUser(user.getUsername(),lobbyService.getLobby(lobbyId));
            return ResponseEntity.ok("Successful registration.");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Validate user by token received in email.
     * @param token received in email.
     */
    @PostMapping("/validateUser")
    public ResponseEntity<?> validateUser(String token)
    {
        try{
            activationService.activateUserAccount(token);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
