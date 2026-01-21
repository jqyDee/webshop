package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.OrderDTO;
import at.qe.skeleton.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, UserxMapper.class, OrderItemMapper.class})
public abstract class OrderMapper implements DTOMapper<Order, OrderDTO> {
    public abstract OrderDTO mapTo(Order entity);
    public abstract Order mapFrom(OrderDTO dto);
}
