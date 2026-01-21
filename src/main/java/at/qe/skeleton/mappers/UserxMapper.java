package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.UserxDTO;
import at.qe.skeleton.model.Userx;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapping between UserxTypes and UserxDTOs.
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Mapper(componentModel = "spring")
public abstract class UserxMapper implements DTOMapper<Userx, UserxDTO>{
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