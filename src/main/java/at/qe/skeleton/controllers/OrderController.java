package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.*;
import at.qe.skeleton.mappers.AddressMapper;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.model.*;
import at.qe.skeleton.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final AddressMapper addressMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper,
                           AddressMapper addressMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.addressMapper = addressMapper;

    }

    /**
     * GET orders of user / all orders
     *
     * @param pageId id of page (0 indexed) or null
     * @param pageSize size of page or null
     * @param sort how the output should be sorted
     * @param user currently authenticated user
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with a collection of products on
     *         the specified page with the specified filters and sorting
     */
    @GetMapping("")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<PageableListDTO<OrderDTO>> getOrders(
            @RequestParam(required = false) Integer pageId,
            @RequestParam(required = false) Integer pageSize,
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC) Sort sort,
            @AuthenticationPrincipal Userx user) {
        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        Page<Order> orderPage = Page.empty();

        if (user.getRole() == UserxRole.CUSTOMER) {
            orderPage = orderService.getOrders(user, pageable);
        }
        PageableListDTO<OrderDTO> pageableListDTO = new PageableListDTO<>(
                pageSize,
                (pageId != null) ? pageId + 1 : null,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getContent().stream().map(orderMapper::mapTo).toList()
        );
        return ResponseEntity.ok(pageableListDTO);
    }

    /**
     * GET one Order
     *
     * @param id the id to search for
     * @return {@link ResponseEntity} with status {@code 200 (OK)} with the order of given id in
     *         the body, or with status {@code 404} if no such product exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Order order = orderService.loadOrder(id).orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(orderMapper.mapTo(order));
    }

    /**
     * POST create order of authenticated user
     *
     * @param user currently authenticated user
     * @return {@link ResponseEntity} with status {@code 200 (OK)} if order was created successfully.
     *         {@code 400 (BAD REQUEST)} if user is not present.
     *         {@code 422 (UNPROCESSABLE REQUEST)} if users cart is empty
     *         {@code 409 (CONFLICT)A if cart item(s) is/are out of stock}
     */
    @PostMapping("/createOrder")
    public ResponseEntity<OrderDTO> createOrder(@AuthenticationPrincipal Userx user) {
        Order order = orderService.createOrder(user);
        return ResponseEntity.ok(orderMapper.mapTo(order));
    }

    /**
     * POST confirm order
     *
     * @param orderId id of order to be confirmed
     * @param user currently authenticated user
     * @return {@link OrderDTO} and status {@code 200 (OK)}.
     *         {@code 404 (NOT FOUND)} if order with id could not be found.
     *         {@code 403 (FORBIDDEN)} if the order or addresses do not belong to the user.
     *         {@code 409 (CONFLICT)} if order with status x can not be confirmed.
     *         {@code 400 (BAD REQUEST)} if arguments are invalid.
     */
    @PostMapping("{orderId}/confirm")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long orderId,
                                                 @Valid @RequestBody OrderConfirmRequestDTO dto,
                                                 @AuthenticationPrincipal Userx user) {

        Address shippingAddress = addressMapper.mapFrom(dto.shippingAddress());
        Address paymentAddress = addressMapper.mapFrom(dto.paymentAddress());

        Order order = orderService.loadOrder(orderId).orElseThrow(EntityNotFoundException::new);
        if (!order.getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to confirm this order");
        }
        orderService.confirmOrder(order, user, shippingAddress, paymentAddress);
        return ResponseEntity.ok(orderMapper.mapTo(order));
    }


    /**
     * POST cancel order
     *
     * @param orderId id of order to be cancelled
     * @param user currently authenticated user
     * @return {@link OrderDTO} and status {@code 200 (OK)}.
     *         {@code 404 (NOT FOUND)} if order with id could not be found.
     *         {@code 403 (FORBIDDEN)} if the order does not belong to the user.
     *         {@code 409 (CONFLICT)} if order with status x can not be cancelled.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId,
                                                @AuthenticationPrincipal Userx user) {
        Order order = orderService.loadOrder(orderId).orElseThrow(EntityNotFoundException::new);
        if (!order.getUser().equals(user) && !user.getRole().equals(UserxRole.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to cancel this order");
        }
        orderService.cancelOrder(order, user);
        order = orderService.loadOrder(orderId).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(orderMapper.mapTo(order));
    }
}
