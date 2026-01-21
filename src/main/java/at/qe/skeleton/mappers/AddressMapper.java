package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.AddressDTO;
import at.qe.skeleton.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {
    public abstract AddressDTO mapTo(Address entity);
    @Mapping(target = "user", ignore = true)
    public abstract Address mapFrom(AddressDTO dto);
}