package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.model.Userx;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping between {@link Userx} and {@link UserxDTO}.
 */
@Mapper(componentModel = "spring")
public abstract class UserxMapper {
    @Mapping(source = "createUser.id", target = "createdBy")
    @Mapping(source = "updateUser.id", target = "updatedBy")
    public abstract UserxDTO mapTo(Userx entity);

    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "paymentAddress", ignore = true)
    public abstract Userx mapFrom(UserxDTO dto);
}