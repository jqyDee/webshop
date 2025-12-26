package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.AddressDTO;
import at.qe.skeleton.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserxMapper.class})
public abstract class AddressMapper extends DTOMapper<Address, AddressDTO> {
    public abstract AddressDTO mapTo(Address entity);
    @Mapping(target = "user", ignore = true)
    public abstract Address mapFrom(AddressDTO dto);
}