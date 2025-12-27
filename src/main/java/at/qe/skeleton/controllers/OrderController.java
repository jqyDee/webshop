package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.*;
import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import at.qe.skeleton.mappers.AddressMapper;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.model.Address;
import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.OrderRepository;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final AddressMapper addressMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper, OrderRepository orderRepository,
                           AddressMapper addressMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
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
    public ResponseEntity<PageableListDTO<OrderDTO>> getOrders(
            @RequestParam(required = false) Integer pageId,
            @RequestParam(required = false) Integer pageSize,
            @SortDefault(sort = "createdDate", direction = Sort.Direction.ASC) Sort sort,
            @AuthenticationPrincipal Userx user) {
        Sort finalSort = (sort != null) ? sort : Sort.unsorted();

        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, finalSort)
                : Pageable.unpaged();

        Page<Order> orderPage = orderService.getOrders(user, pageable);
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
        try {
            Order order = orderService.createOrder(user);

            return ResponseEntity.ok(orderMapper.mapTo(order));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch (CartEmptyException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }
        catch (OutOfStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
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

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(EntityNotFoundException::new);
            orderService.confirmOrder(order, user, shippingAddress, paymentAddress);
            return ResponseEntity.ok(orderMapper.mapTo(order));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(EntityNotFoundException::new);
            orderService.cancelOrder(order, user);
            return ResponseEntity.ok(orderMapper.mapTo(order));
        }
        catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
