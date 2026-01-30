package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.CartItemDTO;
import at.qe.skeleton.model.CartItem;
import org.mapstruct.Mapper;

/**
 * Mapper for {@link CartItem} and {@link CartItemDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class, UserxMapper.class})
public abstract class CartItemMapper {
    public abstract CartItemDTO mapTo(CartItem entity);
    public abstract CartItem mapFrom(CartItemDTO dto);
}
