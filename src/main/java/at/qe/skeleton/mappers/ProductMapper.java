package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.ProductEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class ProductMapper implements DTOMapper<Product, ProductDTO> {
    @Mapping(target = "subscriptions", source = "subscriptions")
    public abstract ProductDTO mapTo(Product entity, Map<ProductEventType, Boolean> subscriptions);
    @Mapping(target = "subscriptions", ignore = true)
    public abstract ProductDTO mapTo(Product entity);
    @Mapping(target = "reviews", ignore = true)
    public abstract Product mapFrom(ProductDTO dto);

    @Mapping(target = "id", ignore = true) // protect the id and the other following fields from being overwritten
    @Mapping(target = "createdDate")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    public abstract void updateProductFromDto(ProductDTO dto, @MappingTarget Product entity);
}
