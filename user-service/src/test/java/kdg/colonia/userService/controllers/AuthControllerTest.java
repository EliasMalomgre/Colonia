package kdg.colonia.userService.controllers;

import kdg.colonia.userService.models.Invitation;
import kdg.colonia.userService.models.Lobby;
import kdg.colonia.userService.repository.InvitationRepository;
import kdg.colonia.userService.repository.LobbyRepository;
import kdg.colonia.userService.repository.UserRepository;
import kdg.colonia.userService.services.InvitationService;
import kdg.colonia.userService.services.LobbyService;
import kdg.colonia.userService.services.UserEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import kdg.colonia.userService.models.User;


import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest
{
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserEmailService userEmailService;
    @MockBean
    private LobbyRepository lobbyRepository;
    @MockBean
    private InvitationRepository invitationRepository;
    @MockBean
    private LobbyService lobbyService;
    @MockBean
    private RESTToSocketsController restToSocketsController;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void RegisterNewUser() throws Exception
    {
        given(userRepository.existsByEmail("arthur.decraemer@student.kdg.be")).willReturn(false);
        given(userRepository.existsByUsername("arthur.decraemer")).willReturn(false);
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be","arthur123",new ArrayList<>()));
        doNothing().when(userEmailService).sendActivationEmail(anyString(),anyString());
        this.mockMvc.perform(post("/api/auth/signup").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                "\t\"password\":\"arthur123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void RegisterUserWithExistingEmail() throws Exception
    {
        given(userRepository.existsByEmail("arthur.decraemer@student.kdg.be")).willReturn(true);

        this.mockMvc.perform(post("/api/auth/signup").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                "\t\"password\":\"arthur123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void RegisterUserWithExistingUsername() throws Exception
    {
        given(userRepository.existsByUsername("arthur.decraemer")).willReturn(true);

        this.mockMvc.perform(post("/api/auth/signup").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                "\t\"password\":\"arthur123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void RegisterUserWithInvalidPassword() throws Exception
    {
        this.mockMvc.perform(post("/api/auth/signup").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                "\t\"password\":\"123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    public void RegisterUserWithInvalidEmail() throws Exception
    {
        this.mockMvc.perform(post("/api/auth/signup").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"email\":\"arthur.decraemer.student.kdg.be\",\n" +
                "\t\"password\":\"123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void LoginWithValidCredentials() throws Exception
    {
        User user=new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"),new ArrayList<>());
        user.setValidatedEmail(true);
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(user));

        this.mockMvc.perform(post("/api/auth/signin").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"password\":\"arthur123\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void LoginWithInvalidCredentials() throws Exception
    {
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"),new ArrayList<>())));

        this.mockMvc.perform(post("/api/auth/signin").contentType("application/json").content("{\n" +
                "\t\"username\":\"arthur.decraemer\",\n" +
                "\t\"password\":\"arthur333333\"\n" +
                "}")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void AuthenticateUserSuccess() throws Exception
    {
        User toReturn = new User("arthur.decraemer", "arthur.decraemer@student.kdg.be",passwordEncoder.encode("arthur123"),new ArrayList<>());
        toReturn.setId("test");
        given(userRepository.findByUsername(anyString())).willReturn(java.util.Optional.of(toReturn));

        this.mockMvc.perform(get("/api/auth/authForUser")
                .param("userId","test"))
                .andExpect(MockMvcResultMatchers.content().string("true"));
    }
    @Test
    public void AuthenticateUserNoSuccess() throws Exception
    {
        this.mockMvc.perform(get("/api/auth/authForUser")
                .param("userId","test"))
                .andExpect(MockMvcResultMatchers.content().string("false"));
    }
    @Test
    public void RegisterUserWithInvitation() throws Exception
    {
        given(userRepository.existsByEmail("arthur.decraemer@student.kdg.be")).willReturn(false);
        given(userRepository.existsByUsername("arthur.decraemer")).willReturn(false);
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be","arthur123",new ArrayList<>()));
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(new User()));
        doNothing().when(userEmailService).sendActivationEmail(anyString(),anyString());
        doNothing().when(restToSocketsController).sendFriendRequestInvite(anyString());
        given(lobbyService.getLobby(anyString())).willReturn(new Lobby());
        given(invitationRepository.save(any())).willReturn(new Invitation());
        this.mockMvc.perform(post("/api/auth/registerWithInvite")
             .param("lobbyId","test")
             .contentType("application/json")
             .content("{\n" +
                     "\t\"username\":\"arthur.decraemer\",\n" +
                     "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                     "\t\"password\":\"password123\"\n" +
                     "}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void RegisterUserWithInvalidInvitation() throws Exception
    {
        given(userRepository.existsByEmail("arthur.decraemer@student.kdg.be")).willReturn(false);
        given(userRepository.existsByUsername("arthur.decraemer")).willReturn(false);
        given(userRepository.save(any())).willReturn(new User("arthur.decraemer","arthur.decraemer@student.kdg.be","arthur123",new ArrayList<>()));
        doNothing().when(userEmailService).sendActivationEmail(anyString(),anyString());
        given(lobbyRepository.findById(anyString())).willReturn(Optional.empty());
        given(invitationRepository.save(any())).willReturn(new Invitation());
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(new User()));
        this.mockMvc.perform(post("/api/auth/registerWithInvite")
                .param("lobbyId","test")
                .contentType("application/json")
                .content("{\n" +
                        "\t\"username\":\"arthur.decraemer\",\n" +
                        "\t\"email\":\"arthur.decraemer@student.kdg.be\",\n" +
                        "\t\"password\":\"password123\"\n" +
                        "}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}