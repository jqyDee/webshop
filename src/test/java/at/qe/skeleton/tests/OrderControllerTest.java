package at.qe.skeleton.tests;


import at.qe.skeleton.configs.JwtConfig;
import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.OrderConfirmRequestDTO;
import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.dtos.OrderItemDTO;
import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.mappers.UserxMapper;
import at.qe.skeleton.model.*;
import at.qe.skeleton.services.OrderService;
import at.qe.skeleton.repositories.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoSpyBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @MockitoBean
    private JwtConfig jwtConfig;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserxMapper userxMapper;

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
        Mockito.when(mockClaims.getSubject()).thenReturn("customer");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
                .thenReturn(Optional.of(mockJws));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetOrders() throws Exception {

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.PENDING);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING_PAYMENT);

        List<Order> orders = List.of(order1, order2);
        Pageable pageable = PageRequest.of(0, 2);
        int total_count = 3;

        Page<Order> page = new PageImpl<>(orders, pageable, total_count);

        Mockito.when(orderService.getOrders(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(page);

        Mockito.when(orderMapper.mapTo(order1))
                .thenReturn(new OrderDTO(1L, null, OrderStatus.PENDING_PAYMENT, null, null, 0.0, null, null));
        Mockito.when(orderMapper.mapTo(order2))
                .thenReturn(new OrderDTO(2L, null, OrderStatus.PENDING, null, null, 0.0, null, null));


        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                        .param("pageId", "0")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageIdAfter").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].status").value("PENDING_PAYMENT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[1].status").value("PENDING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.pageCount").value(2));
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderSuccess() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);

        Userx mockUser = new Userx();
        mockUser.setId(1L);
        mockUser.setUsername("userA");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                set,
                null
        );

        Mockito.when(orderService.createOrder(ArgumentMatchers.any()))
                .thenReturn(mockOrder);

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderDTO.id").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderDTO.status").value("PENDING"));

        // verify service call
        Mockito.verify(orderService, Mockito.times(1)).createOrder(ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderEmptyCart() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);
        Userx mockUser = new Userx();
        mockUser.setId(1L);
        mockUser.setUsername("userA");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                set,
                null
        );

        Mockito.doThrow(new CartEmptyException()).when(orderService).createOrder(ArgumentMatchers.any());

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderNotInStock() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);
        Userx mockUser = new Userx();
        mockUser.setId(1L);
        mockUser.setUsername("userA");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                set,
                null
        );

        Mockito.doThrow(new OutOfStockException("Iphone")).when(orderService).createOrder(ArgumentMatchers.any());

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderWrongArgument() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);
        Userx mockUser = new Userx();
        mockUser.setId(1L);
        mockUser.setUsername("userA");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                set,
                null
        );

        Mockito.doThrow(new IllegalArgumentException()).when(orderService).createOrder(ArgumentMatchers.any());

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testConfirmOrderSuccess() throws Exception {
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(1L, new Address(), new Address());
        Userx mockUser = new Userx();
        mockUser.setId(100L);
        mockUser.setUsername("customer");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Order existingOrder = new Order();
        existingOrder.setId(100L);
        existingOrder.setStatus(OrderStatus.PENDING);

        Order confirmedOrder = new Order();
        confirmedOrder.setId(100L);
        confirmedOrder.setStatus(OrderStatus.PROCESSING);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PROCESSING,
                null,
                null,
                10,
                set,
                null
        );

        //mock order exists
        Mockito.when(orderRepository.findById(100L)).thenReturn(Optional.of(existingOrder));

        Mockito.when(orderService.confirmOrder(existingOrder, mockUser, new Address(), new Address()))
                .thenReturn(confirmedOrder);

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/100/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testConfirmOrderAccessDenied() throws Exception {
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(1L, new Address(), new Address());

        //mock order exists
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        // order doesnt belong to userA
        Mockito.doThrow(new AccessDeniedException("Forbidden"))
                .when(orderService)
                .confirmOrder(ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testConfirmOrderIllegalArgument() throws Exception {
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(1L, new Address(), new Address());
        //mock order exists
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        // order doesnt belong to userA
        Mockito.doThrow(new IllegalArgumentException())
                .when(orderService)
                .confirmOrder(ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCancelOrderSuccess() throws Exception {

        Userx mockUser = new Userx();
        mockUser.setId(1L);
        mockUser.setUsername("customer");
        mockUser.setRoles(Set.of(UserxRole.CUSTOMER));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Order cancelledOrder = new Order();
        cancelledOrder.setId(100L);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);

        Order existingOrder = new Order();
        existingOrder.setId(100L);
        existingOrder.setStatus(OrderStatus.PROCESSING);
        existingOrder.setUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> set = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.CANCELLED,
                null,
                null,
                10,
                set,
                null
        );

        //mock order exists
        Mockito.when(orderRepository.findById(100L)).thenReturn(Optional.of(existingOrder));

        Mockito.doNothing().when(orderService).cancelOrder(
                ArgumentMatchers.any(Order.class),
                ArgumentMatchers.any(Userx.class)
        );

        Mockito.when(orderMapper.mapTo(ArgumentMatchers.any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/100/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }


}