package at.qe.skeleton.mappers;

import at.qe.skeleton.dtos.UserxUpdateDTO;
import at.qe.skeleton.model.Userx;
import at.qe.skeleton.services.UserxService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public abstract class UserxUpdateMapper extends DTOMapper<Userx, UserxUpdateDTO> {

    @Autowired
    protected UserxService userxService;

    /**
     * MapStruct calls this FIRST to get the target object.
     * If an ID is present, we load the existing user from the DB.
     * If not, we create a new one.
     */
    @ObjectFactory
    public Userx resolveUser(UserxUpdateDTO ignored, Long id) {
        if (id != null) {
            return userxService.loadUser(id).orElse(new Userx());
        }
        return new Userx();
    }

    /**
     * MapStruct will automatically map fields with matching names (username, email, etc.)
     * onto the Userx object returned by resolveUser(). MapStruct also type matches the resolveUser()
     * function
     */
    @Mapping(target = "id", ignore = true) // Protect the ID from being overwritten
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "authorities", ignore = true) // Ignore UserDetails derived field
    public abstract Userx mapFrom(UserxUpdateDTO dto, Long id);

    /**
     * Mapping for creation
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    public abstract Userx mapFrom(UserxUpdateDTO dto);
}