package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class OrderMapper extends DTOMapper<Order, OrderDTO> {
    public abstract OrderDTO mapTo(Order entity);
    public abstract Order mapFrom(OrderDTO dto);
}
