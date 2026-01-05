package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.OrderItemDTO;
import at.qe.skeleton.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, UserxMapper.class})
public abstract class OrderItemMapper extends DTOMapper<OrderItem, OrderItemDTO>{
    public abstract OrderItemDTO mapTo(OrderItem entity);
    @Mapping(target = "order", ignore = true)
    public abstract OrderItem mapFrom(OrderItemDTO dto);
}