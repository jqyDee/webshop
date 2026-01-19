package at.qe.skeleton.tests;

import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.LoginRequestDTO;
import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.dtos.UserxUpdateDTO;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.mappers.UserxUpdateMapper;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.model.UserxRole;
import at.qe.skeleton.services.AuthenticationService;
import at.qe.skeleton.services.UserxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

/**
 * Test for {@link at.qe.skeleton.controllers.AuthenticationController}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private UserxService userService;

    @MockitoBean
    private UserxMapper userMapper;

    @MockitoBean
    private UserxUpdateMapper userUpdateMapper;

    @MockitoBean
    private AuthenticationService authenticationService; // Mock the service dependencies

    @BeforeEach
    void setUp() throws Exception {
        // mock setup of the Authentication Filter
        Mockito.doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(tokenAuthenticationFilter).doFilterInternal(Mockito.any(HttpServletRequest.class),
                                                            Mockito.any(HttpServletResponse.class),
                                                            Mockito.any(FilterChain.class));

        @SuppressWarnings("unchecked") Jws<Claims> mockJws = (Jws<Claims>) Mockito.mock(Jws.class);
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("admin");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
               .thenReturn(Optional.of(mockJws));
    }

    @Test
    void createUserValidInput() throws Exception {
        Long id = 1L;
        String username = "newUser";
        String password = "password";
        String firstName = "first";
        String lastName = "last";
        String email = "new@example.com";
        UserxRole role = UserxRole.ADMIN;
        boolean isEnabled = true;

        UserxUpdateDTO newUser = new UserxUpdateDTO(id, username, password, firstName, lastName,
                                                    email, "", true, null, null, role, null);
        Userx user = new Userx();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setEnabled(isEnabled);

        Mockito.when(userUpdateMapper.mapFrom(newUser)).thenReturn(user);
        Mockito.when(userService.createUser(user, password)).thenReturn(user);
        Mockito.when(userMapper.mapTo(user)).thenReturn(
                new UserxDTO(id, null, null, null, null, username, firstName, lastName, email, "",
                             null, null, isEnabled, role, null));

        mockMvc.perform(MockMvcRequestBuilders.post("/authentication/register")
                                              .with(SecurityMockMvcRequestPostProcessors.csrf())
                                              .contentType(MediaType.APPLICATION_JSON).content(
                               new ObjectMapper().writeValueAsString(newUser)))
               .andExpect(MockMvcResultMatchers.status().isCreated())
               .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
               .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(username));
    }

    @Test
    public void loginValidCredentials() throws Exception {
        // Arrange
        String mockToken = "mock-jwt-token";
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "passwd");

        // Mock the authentication and token generation process
        Authentication mockAuth = Mockito.mock(Authentication.class);
        Mockito.when(authenticationService.authenticateLoginRequest("admin", "passwd"))
               .thenReturn(mockAuth);
        Mockito.when(authenticationService.generateToken(mockAuth)).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/authentication/login")
                                              .with(SecurityMockMvcRequestPostProcessors.csrf()) // Handle CSRF protection
                                              .contentType(MediaType.APPLICATION_JSON).content(
                               new ObjectMapper().writeValueAsString(loginRequest)))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.bearerToken").value(mockToken));
    }

    @Test
    public void loginInvalidCredentials() throws Exception {
        // Including the {noop} prefix
        LoginRequestDTO loginRequest = new LoginRequestDTO("wrongUser", "{noop}wrongPass");

        // Arrange: Tell the mock to throw an exception for these invalid credentials
        Mockito.when(authenticationService.authenticateLoginRequest("wrongUser",
                                                                    "{noop}wrongPass")).thenThrow(
                new BadCredentialsException("badCredentials"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/authentication/login")
                                              .with(SecurityMockMvcRequestPostProcessors.csrf())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(loginRequest)))
               .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}