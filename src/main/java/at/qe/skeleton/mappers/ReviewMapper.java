package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.ReviewDTO;
import at.qe.skeleton.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserxMapper.class, ProductMapper.class})
public abstract class ReviewMapper {
    public abstract ReviewDTO mapTo(Review entity);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    public abstract Review mapFrom(ReviewDTO dto);
}
