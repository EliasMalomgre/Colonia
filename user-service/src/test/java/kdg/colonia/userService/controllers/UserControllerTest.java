package kdg.colonia.userService.controllers;

import kdg.colonia.userService.models.FriendRequest;
import kdg.colonia.userService.models.Lobby;
import kdg.colonia.userService.models.User;
import kdg.colonia.userService.repository.FriendRequestRepository;
import kdg.colonia.userService.repository.LobbyRepository;
import kdg.colonia.userService.repository.UserRepository;
import kdg.colonia.userService.services.InvitationService;
import kdg.colonia.userService.services.LobbyService;
import kdg.colonia.userService.services.UserEmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private FriendRequestRepository friendRequestRepository;
    @MockBean
    private LobbyRepository lobbyRepository;
    @MockBean
    private InvitationService invitationService;
    @MockBean
    private RESTToSocketsController restToSocketsController;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserEmailService userEmailService;

    @Test
    public void UpdateUser() throws Exception
    {
        User toReturn = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        toReturn.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(toReturn));
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/updateUser").content("{\"newPassword\":\"arthur123!\"}").contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void UpdateUserWithWrongContent() throws Exception
    {
        User toReturn = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        toReturn.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(toReturn));
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/updateUser").content("{\"\"}").contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void GetUserData() throws Exception
    {
        User toReturn = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        toReturn.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(toReturn));

        this.mockMvc.perform(get("/api/user/getDataForLoggedInUser"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void GetUserDataWithBadToken() throws Exception
    {
        this.mockMvc.perform(get("/api/user/getDataForLoggedInUser"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void SendFriendRequestToOtherUser() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        User secondUser = new User("arthur.decraemer2", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        secondUser.setId("test2");
        given(userRepository.findByUsername("arthur.decraemer2")).willReturn(java.util.Optional.of(secondUser));
        given(userRepository.findByUsername(ArgumentMatchers.matches("(?!arthur.decraemer2$).*"))).willReturn(java.util.Optional.of(firstUser));
        given(friendRequestRepository.save(any())).willReturn(new FriendRequest(firstUser.getId(),secondUser.getId()));

        this.mockMvc.perform(post("/api/user/sendFriendRequest?usernameOfFriend="+secondUser.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void SendFriendRequestToNonExistingUser() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        User secondUser = new User("arthur.decraemer2", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        secondUser.setId("test2");
        given(userRepository.findByUsername("arthur.decraemer2")).willReturn(java.util.Optional.empty());
        given(userRepository.findByUsername(ArgumentMatchers.matches("(?!arthur.decraemer2$).*"))).willReturn(java.util.Optional.of(firstUser));
        given(friendRequestRepository.save(any())).willReturn(new FriendRequest(firstUser.getId(),secondUser.getId()));

        this.mockMvc.perform(post("/api/user/sendFriendRequest?usernameOfFriend="+secondUser.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void AcceptFriendRequest() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        User secondUser = new User("arthur.decraemer2", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        secondUser.setId("test2");
        FriendRequest friendRequest=new FriendRequest(firstUser.getId(),secondUser.getId());
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(secondUser));
        given(userRepository.findById(anyString())).willReturn(java.util.Optional.of(firstUser));
        given(friendRequestRepository.findById(anyString())).willReturn(java.util.Optional.of(friendRequest));
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/acceptFriendRequest?friendRequestId=test"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void RemoveFriend() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        User secondUser = new User("arthur.decraemer2", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        secondUser.setId("test2");
        firstUser.getFriends().add(secondUser.getId());
        secondUser.getFriends().add(firstUser.getId());
        given(userRepository.findByUsername("arthur.decraemer2")).willReturn(java.util.Optional.of(secondUser));
        given(userRepository.findByUsername(ArgumentMatchers.matches("(?!arthur.decraemer2$).*"))).willReturn(java.util.Optional.of(firstUser));
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/deleteFriend?username=arthur.decraemer2"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void RemoveNonExistingFriend() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        User secondUser = new User("arthur.decraemer2", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        secondUser.setId("test2");
        firstUser.getFriends().add(secondUser.getId());
        secondUser.getFriends().add(firstUser.getId());
        given(userRepository.findByUsername(ArgumentMatchers.matches("(?!arthur.decraemer2$).*"))).willReturn(java.util.Optional.of(firstUser));
        given(userRepository.findByUsername(ArgumentMatchers.matches("(arthur.decraemer2$).*"))).willReturn(java.util.Optional.empty());
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/deleteFriend?username="+secondUser.getUsername()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void ChangeNotificationSetting() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        firstUser.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(firstUser));
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));

        this.mockMvc.perform(post("/api/user/setNotificationSetting?value=true"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void createLobbyTest() throws Exception
    {
        User firstUser = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        Lobby toReturn=new Lobby();
        toReturn.setId("test");
        firstUser.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(firstUser));
        given(userRepository.findById(anyString())).willReturn(java.util.Optional.of(firstUser));
        given(lobbyRepository.save(any())).willReturn(toReturn);
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123!")));
        doNothing().when(userEmailService).sendActivationEmail(anyString(),anyString());
        doNothing().when(invitationService).inviteNewUser(anyString(),any());
        doNothing().when(invitationService).inviteExistingUser(anyString(),any());

        this.mockMvc.perform(post("/api/user/createLobby")
                .contentType("application/json")
                .content("{\n" +
                        "\t\"1\": {\"inviteMethod\":\"AI\", \"credential\":null},\n" +
                        "\t\"2\": {\"inviteMethod\":\"friend\", \"credential\":\"daphne.deckers\"},\n" +
                        "\t\"3\": {\"inviteMethod\":\"friend\", \"credential\":\"tim.schelpe\"},\n" +
                        "\t\"4\": {\"inviteMethod\":\"newPlayer\", \"credential\":\"arthurdecraemer@gmail.com\"}\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    public void GetUserName() throws Exception
    {
        User toReturn = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"));
        toReturn.setId("test");
        given(userRepository.findById("test")).willReturn(java.util.Optional.of(toReturn));

        this.mockMvc.perform(get("/api/user/getUsername?userid=test"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}