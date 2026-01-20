package at.qe.skeleton.tests.controllers;

import at.qe.skeleton.configs.JwtConfig;
import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.controllers.AdminController;
import at.qe.skeleton.dtos.*;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.mappers.UserxUpdateMapper;
import at.qe.skeleton.model.*;
import at.qe.skeleton.services.OrderService;
import at.qe.skeleton.services.ProductSubscriptionService;
import at.qe.skeleton.services.UserxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Some very basic tests for {@link AdminController}.
 * This class is part of the skeleton project provided for students of the
 * course "Software Engineering" offered by the University of Innsbruck.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private UserxService userService;

    @MockitoBean
    private UserxMapper userMapper;

    @MockitoBean
    private UserxUpdateMapper userCreateMapper;

    @MockitoBean
    private UserxUpdateMapper userxUpdateMapper;

    @MockitoBean
    private ProductSubscriptionService productSubscriptionService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        // mock setup of the Authentication Filter
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
        Mockito.when(mockClaims.getSubject()).thenReturn("admin");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
                .thenReturn(Optional.of(mockJws));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllManagersShouldReturnMappedManagers() throws Exception {
        Long id = 1L;
        String username = "testManager";
        Userx manager = new Userx();
        manager.setId(id);
        manager.setUsername(username);
        manager.setFirstName("First");
        manager.setLastName("Last");
        manager.setRole(UserxRole.MANAGER);
        List<Userx> managers = List.of(manager);

        Mockito.when(userService.getAllManagers()).thenReturn(managers);
        Mockito.when(userMapper.mapTo(Mockito.any(Userx.class))).thenReturn(new UserxDTO(
                id, null, null, null, null, "testManager", "First", "Last", null, null, null, null, false, UserxRole.MANAGER, null));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/managers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value(username))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstName").value("First"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastName").value("Last"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllManagersShouldReturnEmptyListWhenNoManagers() throws Exception {
        Mockito.when(userService.getAllManagers()).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/managers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }


    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getAllUsers() throws Exception {
        Long id = 1L;
        String username = "testUser";
        Userx user1 = new Userx();
        user1.setId(id);
        user1.setUsername(username);
        user1.setFirstName("First");
        user1.setLastName("Last");
        List<Userx> users = List.of(user1);

        Mockito.when(userService.getAllUsers()).thenReturn(users);
        Mockito.when(userMapper.mapTo(Mockito.any(Userx.class))).thenReturn(new UserxDTO(
                id, null, null, null, null, "testUser", "First", "Last", null, null, null, null, false, null, null));


        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].username").value(username));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getUserUserExists() throws Exception {
        Long id = 1L;
        String username = "testUser";
        Userx user1 = new Userx();
        user1.setId(id);
        user1.setUsername(username);
        user1.setFirstName("First");
        user1.setLastName("Last");
        Mockito.when(userService.loadUser(id)).thenReturn(Optional.of(user1));
        Mockito.when(userMapper.mapTo(user1)).thenReturn(new UserxDTO(id, null, null, null, null, username, "First", "Last", null, null, null, null, false, null, null));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/user/{id}", id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(username));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void getUserUserDoesNotExist() throws Exception {
        Mockito.when(userService.loadUser(1L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/user/1"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void createUserValidInput() throws Exception {
        Long id = 1L;
        String username = "newUser";
        String password = "password";
        String firstName = "first";
        String lastName = "last";
        String email = "new@example.com";
        UserxRole role = UserxRole.ADMIN;
        boolean isEnabled = true;

        UserxUpdateDTO newUser = new UserxUpdateDTO(id, username, password, firstName, lastName, email, "", true, null, null, role, null);
        Userx user = new Userx();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setEnabled(isEnabled);

        Mockito.when(userCreateMapper.mapFrom(newUser)).thenReturn(user);
        Mockito.when(userService.saveUser(user, password)).thenReturn(user);
        Mockito.when(userMapper.mapTo(user)).thenReturn(new UserxDTO(id, null, null, null, null, username, firstName, lastName, email, "", null, null, isEnabled, role, null));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/createUser")
                                              .with(SecurityMockMvcRequestPostProcessors.csrf())
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(new ObjectMapper().writeValueAsString(newUser)))
               .andExpect(MockMvcResultMatchers.status().isCreated())
               .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
               .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(username));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteUserUserExists() throws Exception {
        Long id = 1L;
        String username = "newUser";
        Userx user = new Userx();
        user.setId(id);
        user.setUsername(username);

        Mockito.when(userService.loadUser(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/user/{id}", id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void deleteUserUserDoesNotExist() throws Exception {
        Long id = 1L;
        Mockito.when(userService.loadUser(id)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/user/{id}", id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testUpdateUser() throws Exception {
        Long userId = 1L;

        Userx existingUser = new Userx();
        existingUser.setId(userId);
        existingUser.setUsername("oldUser");

        Userx updatedUser = new Userx();
        updatedUser.setId(userId);
        updatedUser.setUsername("newUser");

        UserxUpdateDTO updateDto = new UserxUpdateDTO(userId, "newUser", null, null, null, null, null, true, null, null, null, null);

        UserxDTO responseDto = new UserxDTO(userId, null, null, null, null, "newUser", "mock", "mock", null, null, null, null, true, UserxRole.CUSTOMER, null);

        Mockito.when(userService.loadUser(userId)).thenReturn(Optional.of(existingUser));

        Mockito.when(userCreateMapper.mapFrom(Mockito.any(UserxUpdateDTO.class), Mockito.eq(userId)))
                .thenReturn(updatedUser);

        Mockito.when(userService.saveUser(Mockito.eq(updatedUser), Mockito.any()))
                .thenReturn(updatedUser);

        Mockito.when(userMapper.mapTo(updatedUser)).thenReturn(responseDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/admin/user/{id}", userId)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("newUser"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testDeleteUserProductSubscription() throws Exception {
        Userx mockUser = new Userx();
        Long userId = 1L;
        Long productId = 1L;

        mockUser.setId(userId);

        Mockito.when(userService.loadUser(ArgumentMatchers.eq(productId))).thenReturn(Optional.of(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/user/{id}/unsubscribe/{productId}", userId, productId)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                ).andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productSubscriptionService).deleteProductSubscription(ArgumentMatchers.eq(mockUser), ArgumentMatchers.eq(productId));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void testGetAllOrders() throws Exception {
        Userx mockUser = new Userx();
        mockUser.setId(1L);

        UserxDTO userxDTO = new UserxDTO(1L, null, null, null, null, "as", "as", "as", null, null, null, null, true, UserxRole.CUSTOMER, null);

        ProductDTO productDTO = new ProductDTO(1L, "as", 0.0, 1, 0.0, 0.0, null, null, null, null, null, null, null);

        OrderItemDTO orderItemDTO = new OrderItemDTO(1L, productDTO, null, null, null);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setUser(mockUser);
        order1.setSum(20.0);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setUser(mockUser);
        order2.setSum(40.0);

        List<Order> orders = List.of(order1, order2); // product 3 has to be left out here as the PageImpl does not remove it in this case
        Pageable pageable = PageRequest.of(0, 2);
        int total_count = 3;

        Page<Order> page = new PageImpl<>(orders, pageable, total_count);

        Mockito.when(orderService.getAllOrders(ArgumentMatchers.any())).thenReturn(page);

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
               .thenAnswer(invocation -> {
                   Order order = invocation.getArgument(0);
                   return new OrderDTO(order.getId(), userxDTO, OrderStatus.PROCESSING, null, null, order.getSum(),
                                       List.of(orderItemDTO), null);
               });

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/orders")
                                              .param("pageId", "0")
                                              .param("pageSize", "2"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(2))
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageIdAfter").value(1))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items", Matchers.hasSize(2)))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(1L))
               .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].id").value(2L))
               .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(3))
               .andExpect(MockMvcResultMatchers.jsonPath("$.pageCount").value(2));
    }
}
