package at.qe.skeleton.dtos;

import at.qe.skeleton.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderDTO(
        Long id,
        Long userId,
        OrderStatus status,
        AddressDTO shippingAddress,
        AddressDTO paymentAddress,
        double sum,
        Map<ProductDTO, Integer> products, //should be changed to a set of orderItemDTO --> We need OrderItemDTO
        LocalDateTime createdDate
) {}
