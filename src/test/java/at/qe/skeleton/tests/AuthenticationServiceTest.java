package at.qe.skeleton.tests;

import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    public void testAuthenticateLoginRequest() {
        String username = "user";
        String password = "password";
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        Authentication result = authenticationService.authenticateLoginRequest(username, password);

        assertEquals(mockAuth, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testGenerateToken() {
        Authentication mockAuth = mock(Authentication.class);
        String expectedToken = "mock-jwt-token";
        when(tokenProvider.generate(mockAuth)).thenReturn(expectedToken);

        String result = authenticationService.generateToken(mockAuth);

        assertEquals(expectedToken, result);
        verify(tokenProvider).generate(mockAuth);
    }
}