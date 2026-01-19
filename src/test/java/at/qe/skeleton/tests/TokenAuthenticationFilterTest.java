package at.qe.skeleton.tests;

import at.qe.skeleton.configs.JwtConfig;
import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.model.UserxRole;
import at.qe.skeleton.services.UserxService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private UserxService userService;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        lenient().when(jwtConfig.getTokenHeader()).thenReturn("Authorization");
        lenient().when(jwtConfig.getTokenPrefix()).thenReturn("Bearer ");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSuccessfulAuthentication() throws Exception {
        String token = "valid-token";
        String username = "testuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        @SuppressWarnings("unchecked")
        Jws<Claims> jws = mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(claims.getSubject()).thenReturn(username);
        when(tokenProvider.validateTokenAndGetJws(token)).thenReturn(Optional.of(jws));

        Userx user = new Userx();
        user.setUsername(username);
        user.setRole(UserxRole.CUSTOMER);
        when(userService.getUserByUsername(username)).thenReturn(user);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testNoTokenProceedsToNextFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void testInvalidPrefixProceedsToNextFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic standard-auth");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void testInvalidTokenProceedsToNextFilter() throws Exception {
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateTokenAndGetJws(token)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).getUserByUsername(anyString());
    }
}