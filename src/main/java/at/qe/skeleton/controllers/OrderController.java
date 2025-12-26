package at.qe.skeleton.controllers;

import at.qe.skeleton.dtos.*;
import at.qe.skeleton.exceptions.CartEmptyException;
import at.qe.skeleton.exceptions.OutOfStockException;
import at.qe.skeleton.mappers.OrderMapper;
import at.qe.skeleton.model.Order;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.repositories.OrderRepository;
import at.qe.skeleton.services.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public OrderController(OrderService orderService, OrderMapper orderMapper, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
    }

    @GetMapping("")
    public ResponseEntity<PageableListDTO<OrderDTO>> getOrders(
            @RequestParam(required = false) Integer pageId,
            @RequestParam(required = false) Integer pageSize,
            @AuthenticationPrincipal Userx user) {

        Sort sort = Sort.by("createdDate").descending();
        Pageable pageable = (pageId != null && pageSize != null && pageSize > 0)
                ? PageRequest.of(pageId, pageSize, sort)
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

    @PostMapping("/createOrder")
    public ResponseEntity<OrderResponseDTO> createOrder(@AuthenticationPrincipal Userx user) {
        try {
            Order order = orderService.createOrder(user);
            OrderDTO orderDTO = orderMapper.mapTo(order);

            OrderResponseDTO orderResponseDTO = new OrderResponseDTO(
                    true,
                    order.getId(),
                    orderDTO);
            return ResponseEntity.ok(orderResponseDTO);
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

    @PostMapping("/confirmOrder")
    public ResponseEntity<OrderDTO> confirmOrder(@RequestBody OrderConfirmRequestDTO requestDTO, @AuthenticationPrincipal Userx user) {
        try {
            Order order = orderRepository.findById(requestDTO.orderId())
                    .orElseThrow(EntityNotFoundException::new);
            orderService.confirmOrder(order, user, requestDTO.shippingAddress(), requestDTO.paymentAddress());
            return ResponseEntity.ok(orderMapper.mapTo(order));
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

    @PostMapping("/cancelOrder")
    public ResponseEntity<OrderDTO> cancelOrder(@RequestBody OrderDTO orderDTO, @AuthenticationPrincipal Userx user) {
        try {
            Order order = orderRepository.findById(orderDTO.id())
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
