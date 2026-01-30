package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.ProductEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class ProductMapper {
    @Mapping(target = "subscriptions", source = "subscriptions")
    public abstract ProductDTO mapTo(Product entity, Map<ProductEventType, Boolean> subscriptions);

    @Mapping(target = "subscriptions", ignore = true)
    public abstract ProductDTO mapTo(Product entity);

    @Mapping(target = "id", ignore = true) // protect the id and the other following fields from being overwritten
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    public abstract Product mapFrom(ProductDTO dto);
}
