package at.qe.skeleton.tests.controllers;

import at.qe.skeleton.configs.JwtTokenProvider;
import at.qe.skeleton.configs.TokenAuthenticationFilter;
import at.qe.skeleton.dtos.*;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
                any(HttpServletRequest.class),
                any(HttpServletResponse.class),
                any(FilterChain.class)
        );

        @SuppressWarnings("unchecked")
        Jws<Claims> mockJws = (Jws<Claims>) Mockito.mock(Jws.class);
        Claims mockClaims = Mockito.mock(Claims.class);
        Mockito.when(mockClaims.getSubject()).thenReturn("customer");
        Mockito.when(mockJws.getPayload()).thenReturn(mockClaims);
        Mockito.when(jwtTokenProvider.validateTokenAndGetJws(Mockito.anyString()))
                .thenReturn(Optional.of(mockJws));
    }

    private Userx createMockUser(Long id) {
        Userx mockUser = new Userx();
        mockUser.setId(id);
        mockUser.setUsername("userA");
        mockUser.setRole(UserxRole.CUSTOMER);
        return mockUser;
    }

    private void authenticateUser(Userx user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetOrders() throws Exception {

        Userx mockUser = new Userx();
        mockUser.setId(100L);
        mockUser.setUsername("customer");
        mockUser.setRole(UserxRole.CUSTOMER);
        authenticateUser(mockUser);

        LocalDateTime now = LocalDateTime.now();

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.PENDING);
        order2.setCreatedDate(now.minusDays(1));

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING_PAYMENT);
        order1.setCreatedDate(now);

        List<Order> orders = List.of(order1, order2);
        Pageable pageable = PageRequest.of(0, 2);
        int total_count = 3;

        Page<Order> page = new PageImpl<>(orders, pageable, total_count);

        Mockito.when(orderService.getOrders(
                any(Userx.class),
                any(Pageable.class)
        )).thenReturn(page);

        Mockito.when(orderMapper.mapTo(order1))
                .thenReturn(new OrderDTO(1L, null, OrderStatus.PENDING_PAYMENT, null, null, 0.0, null, null));
        Mockito.when(orderMapper.mapTo(order2))
                .thenReturn(new OrderDTO(2L, null, OrderStatus.PENDING, null, null, 0.0, null, null));


        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders")
                        .param("pageId", "0")
                        .param("pageSize", "2")
                        .param("sort", "createdDate,asc"))
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

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                list,
                null
        );

        Mockito.when(orderService.createOrder(any()))
                .thenReturn(mockOrder);

        Mockito.when(orderMapper.mapTo(any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(100L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING"));

        // verify service call
        Mockito.verify(orderService, Mockito.times(1)).createOrder(any());
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetOrderByIdSuccess() throws Exception {

        Long orderId = 8000L;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERED);

        OrderDTO orderDto = new OrderDTO(orderId, null, OrderStatus.DELIVERED, null, null, 99.99, null, null);

        Mockito.when(orderService.loadOrder(orderId)).thenReturn(Optional.of(order));
        Mockito.when(orderMapper.mapTo(order)).thenReturn(orderDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{id}", orderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(orderId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DELIVERED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sum").value(99.99));
    }

    @Test
    @WithMockUser(username = "customer", authorities = {"CUSTOMER"})
    public void testGetOrderByIdNotFound() throws Exception {
        Long nonExistingId = 9999L;
        Mockito.when(orderService.loadOrder(nonExistingId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/{id}", nonExistingId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderCartEmpty() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                list,
                null
        );

        Mockito.doThrow(new CartEmptyException()).when(orderService).createOrder(any());

        Mockito.when(orderMapper.mapTo(any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderOutOfStock() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                list,
                null
        );

        Mockito.doThrow(new OutOfStockException("Iphone")).when(orderService).createOrder(any());

        Mockito.when(orderMapper.mapTo(any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void createOrderIllegalArgument() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                list,
                null
        );

        Mockito.doThrow(new IllegalArgumentException()).when(orderService).createOrder(any());

        Mockito.when(orderMapper.mapTo(any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/createOrder")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCreateOrderWrongArgument() throws Exception {
        Order mockOrder = new Order();
        mockOrder.setId(100L);

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PENDING,
                null,
                null,
                10,
                list,
                null
        );

        Mockito.doThrow(new IllegalArgumentException()).when(orderService).createOrder(any());

        Mockito.when(orderMapper.mapTo(any(Order.class)))
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
        AddressDTO a1 = new AddressDTO(1L, "as", "as", "as", "as", "as");
        AddressDTO a2 = new AddressDTO(2L, "as", "as", "as", "as", "as");
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(a1, a2);

        Userx mockUser = createMockUser(100L);
        authenticateUser(mockUser);

        Order existingOrder = new Order();
        existingOrder.setId(100L);
        existingOrder.setStatus(OrderStatus.PENDING);
        existingOrder.setUser(mockUser);

        Order confirmedOrder = new Order();
        confirmedOrder.setId(100L);
        confirmedOrder.setStatus(OrderStatus.PROCESSING);
        existingOrder.setUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.PROCESSING,
                null,
                null,
                10,
                list,
                null
        );

        //mock order exists
        Mockito.when(orderService.loadOrder(100L)).thenReturn(Optional.of(existingOrder));

        Mockito.when(orderService.confirmOrder(existingOrder, mockUser, new Address(), new Address()))
                .thenReturn(confirmedOrder);

        Mockito.when(orderMapper.mapTo(any(Order.class)))
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
        AddressDTO a1 = new AddressDTO(1L, "as", "as", "as", "as", "as");
        AddressDTO a2 = new AddressDTO(2L, "as", "as", "as", "as", "as");
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(a1, a2);


        Userx owner = new Userx();
        Order mockOrder = new Order();
        mockOrder.setUser(owner);

        //mock order exists
        Mockito.when(orderService.loadOrder(1L)).thenReturn(Optional.of(mockOrder));

        // order doesnt belong to userA
        Mockito.doThrow(new AccessDeniedException("Forbidden"))
                .when(orderService)
                .confirmOrder(any(),
                        any(),
                        any(),
                        any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "userA")
    public void testConfirmOrderNotFound() throws Exception {
        AddressDTO a1 = new AddressDTO(1L, "as", "as", "as", "as", "as");
        AddressDTO a2 = new AddressDTO(2L, "as", "as", "as", "as", "as");
        OrderConfirmRequestDTO request = new OrderConfirmRequestDTO(a1, a2);
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCancelOrderAccessDenied() throws Exception {
        Userx user = createMockUser(100L);

        Userx owner = new Userx();
        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUser(owner);

        //mock order exists
        Mockito.when(orderService.loadOrder(100L)).thenReturn(Optional.of(mockOrder));

        // order doesnt belong to userA
        Mockito.doThrow(new AccessDeniedException("Forbidden"))
                .when(orderService)
                .cancelOrder(any(),
                        any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/100/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCancelOrderSuccess() throws Exception {

        Userx mockUser = createMockUser(1L);
        authenticateUser(mockUser);

        Order cancelledOrder = new Order();
        cancelledOrder.setId(100L);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);

        Order existingOrder = new Order();
        existingOrder.setId(100L);
        existingOrder.setStatus(OrderStatus.PROCESSING);
        existingOrder.setUser(mockUser);

        UserxDTO userxDTO = userxMapper.mapTo(mockUser);
        Set<OrderItemDTO> list = new HashSet<>();

        OrderDTO mockOrderDTO = new OrderDTO(
                100L,
                userxDTO,
                OrderStatus.CANCELLED,
                null,
                null,
                10,
                list,
                null
        );

        //mock order exists
        Mockito.when(orderService.loadOrder(100L)).thenReturn(Optional.of(existingOrder));

        Mockito.doNothing().when(orderService).cancelOrder(
                any(Order.class),
                any(Userx.class)
        );

        Mockito.when(orderMapper.mapTo(any(Order.class)))
                .thenReturn(mockOrderDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/100/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCancelEntityNotFound() throws Exception {

        //mock order exists
        Mockito.when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // order doesnt belong to userA
        Mockito.doThrow(new EntityNotFoundException())
                .when(orderService)
                .cancelOrder(any(),
                        any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/1/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


    @Test
    @WithMockUser(username = "userA", authorities = {"CUSTOMER"})
    public void testCancelOrderIllegalState() throws Exception {
        Userx user = new Userx();
        user.setId(1L);
        user.setRole(UserxRole.CUSTOMER);

        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUser(user);
        mockOrder.setStatus(OrderStatus.PROCESSING);

        //mock order exists
        Mockito.when(orderService.loadOrder(100L)).thenReturn(Optional.of(mockOrder));

        Mockito.doThrow(new IllegalStateException())
                .when(orderService)
                .cancelOrder(eq(mockOrder), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/100/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }
}