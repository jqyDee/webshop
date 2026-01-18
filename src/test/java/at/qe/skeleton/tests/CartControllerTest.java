package at.qe.skeleton.tests;

import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.CartItemDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.mappers.CartItemMapper;
import at.qe.skeleton.model.CartItem;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.CartService;
import at.qe.skeleton.services.UserxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartItemMapper cartItemMapper;

    @Autowired
    private UserxService userxService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock setup of the Authentication Filter to bypass JWT validation logic in tests
        Mockito.doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(tokenAuthenticationFilter).doFilterInternal(
                Mockito.any(HttpServletRequest.class),
                Mockito.any(HttpServletResponse.class),
                Mockito.any(FilterChain.class)
        );

        @SuppressWarnings("unchecked")
        Jws<Claims> mockJws = (Jws<Claims>) Mockito.mock(Jws.class);
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("customer");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
               .thenReturn(Optional.of(mockJws));
    }

    @Test
    void testGetShoppingCart() throws Exception {
        Userx user = userxService.getUserByUsername("user2");
        CartItem item = new CartItem();
        item.setId(100L);

        Mockito.when(cartService.getCartItems(ArgumentMatchers.any(Userx.class)))
               .thenReturn(List.of(item));

        Mockito.when(cartItemMapper.mapTo(item)).thenReturn(new CartItemDTO(
                100,
                new ProductDTO(1L, "Test Product", 10.0, 5, 0, 0.0, null, null, null, null, null, null, null),
                new UserxDTO(3000L, null, null, null, null, "user2", "Max", "Mustermann", null, null, null, null, true, null, null),
                1));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(user)))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(100));
    }

    @Test
    void testGetShoppingCartAccessDenied() throws Exception {
        Userx admin = userxService.getUserByUsername("admin");

        Mockito.when(cartService.getCartItems(ArgumentMatchers.any(Userx.class)))
               .thenThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(admin)))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testAddAllToShoppingCart() throws Exception {
        Userx user = userxService.getUserByUsername("user2");
        Map<Long, Integer> items = Map.of(1000L, 2, 2000L, 1);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(user))
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(items)))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(cartService).saveCartItems(ArgumentMatchers.eq(user), ArgumentMatchers.anyMap());
    }

    @Test
    void testAddAllToShoppingCartAccessDenied() throws Exception {
        Userx admin = userxService.getUserByUsername("admin");
        Map<Long, Integer> items = Map.of(1000L, 2);

        Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
               .when(cartService).saveCartItems(ArgumentMatchers.any(Userx.class), ArgumentMatchers.anyMap());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(admin))
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(items)))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testClearShoppingCart() throws Exception {
        Userx user = userxService.getUserByUsername("user2");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(user)))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(cartService).clearCartItems(user);
    }

    @Test
    void testClearShoppingCartAccessDenied() throws Exception {
        Userx admin = userxService.getUserByUsername("admin");

        Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("Access Denied"))
               .when(cartService).clearCartItems(ArgumentMatchers.any(Userx.class));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart")
                                              .with(SecurityMockMvcRequestPostProcessors.user(admin)))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void testDeleteProductFromShoppingCart() throws Exception {
        Userx user = userxService.getUserByUsername("user2");
        Long productId = 1000L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cart/{productId}", productId)
                                              .with(SecurityMockMvcRequestPostProcessors.user(user)))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(cartService).removeCartItem(user, productId);
    }

    @DirtiesContext
    @Test
    void testUpdateProductInShoppingCartNoAdd() throws Exception {
        Userx user = userxService.getUserByUsername("user2");
        Long productId = 1000L;
        int quantity = 5;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/cart/{productId}", productId)
                                              .param("quantity", String.valueOf(quantity))
                                              .param("add", String.valueOf(true))
                                              .with(SecurityMockMvcRequestPostProcessors.user(user)))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(cartService).saveCartItem(user, productId, quantity);
    }

    @DirtiesContext
    @Test
    void testUpdateProductInShoppingCartAdd() throws Exception {
        Userx user = userxService.getUserByUsername("user2");
        Long productId = 1000L;
        int quantity = 5;
        int quantity2 = 2;

        Product product = new Product();
        product.setId(productId);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity2);
        cartItem.setUser(user);

        Mockito.when(cartService.getCartItems(user)).thenReturn(List.of(cartItem));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/cart/{productId}", productId)
                                              .param("quantity", String.valueOf(quantity))
                                              .param("add", String.valueOf(true))
                                              .with(SecurityMockMvcRequestPostProcessors.user(user)))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(cartService).saveCartItem(user, productId, quantity + quantity2);
    }

    @Test
    void testUpdateProductInShoppingCartAccessDenied() throws Exception {
        Userx admin = userxService.getUserByUsername("admin");
        Long productId = 1000L;

        Mockito.doThrow(new org.springframework.security.access.AccessDeniedException("Forbidden"))
               .when(cartService).saveCartItem(ArgumentMatchers.any(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyInt());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/cart/{productId}", productId)
                                              .param("quantity", "5")
                                              .param("add", String.valueOf(false))
                                              .with(SecurityMockMvcRequestPostProcessors.user(admin)))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}