package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ProductMapper extends DTOMapper<Product, ProductDTO> {
    public abstract ProductDTO mapTo(Product entity);
    @Mapping(target = "reviews", ignore = true)
    public abstract Product mapFrom(ProductDTO dto);
}
