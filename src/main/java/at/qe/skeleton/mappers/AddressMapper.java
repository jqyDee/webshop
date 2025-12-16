package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.AddressDTO;
import at.qe.skeleton.dtos.ProductDTO;
import at.qe.skeleton.model.Address;
import at.qe.skeleton.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class AddressMapper extends DTOMapper<Address, AddressDTO> {
    public abstract AddressDTO mapTo(Address entity);
    public abstract Address mapFrom(AddressDTO dto);
}