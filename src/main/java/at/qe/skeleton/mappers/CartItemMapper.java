package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.CartItemDTO;
import at.qe.skeleton.model.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, UserxMapper.class})
public abstract class CartItemMapper implements DTOMapper<CartItem, CartItemDTO>{
    public abstract CartItemDTO mapTo(CartItem entity);
    public abstract CartItem mapFrom(CartItemDTO dto);
}
