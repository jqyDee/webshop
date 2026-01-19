package at.qe.skeleton.tests;
import at.qe.skeleton.dtos.*;
import at.qe.skeleton.mappers.*;
import at.qe.skeleton.model.*;
import at.qe.skeleton.services.UserxService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests for verifying the correct behavior of MapStruct mappers.
 * Generated with LLM: <a href="https://gemini.google.com/share/2dd04d9878c9">Link to conversation<a>
 */
@SpringBootTest
public class MapperTest {

    @Autowired
    private UserxMapper userxMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private UserxUpdateMapper userxUpdateMapper;

    @MockitoBean
    private UserxService userxService;

    @Test
    public void testUserxMapper() {
        // Create Entity
        Userx creator = new Userx();
        creator.setId(1L);

        Userx user = new Userx();
        user.setId(10L);
        user.setUsername("mapperUser");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");
        user.setCreateUser(creator);
        user.setRole(UserxRole.CUSTOMER);

        // Map to DTO
        UserxDTO dto = userxMapper.mapTo(user);

        Assertions.assertEquals(user.getId(), dto.id());
        Assertions.assertEquals(user.getUsername(), dto.username());
        Assertions.assertEquals(1L, dto.createdBy()); // Source: createUser.id
        Assertions.assertEquals(UserxRole.CUSTOMER, dto.role());

        // Map from DTO
        Userx mappedUser = userxMapper.mapFrom(dto);
        Assertions.assertEquals(dto.username(), mappedUser.getUsername());
        Assertions.assertNull(mappedUser.getPassword()); // password is ignored
        Assertions.assertNull(mappedUser.getCreateUser()); // createUser is ignored
    }

    @Test
    public void testProductMapper() {
        Product product = new Product();
        product.setId(100L);
        product.setName("Test Product");
        product.setPrice(19.99);
        product.setStock(50);
        product.setDiscount(0.1);

        // Map to DTO
        ProductDTO dto = productMapper.mapTo(product);

        Assertions.assertEquals(product.getId(), dto.id());
        Assertions.assertEquals(product.getName(), dto.name());
        Assertions.assertEquals(0.1, dto.discount());

        // Map from DTO
        Product mappedProduct = productMapper.mapFrom(dto);
        Assertions.assertEquals(dto.name(), mappedProduct.getName());
        Assertions.assertTrue(mappedProduct.getReviews().isEmpty()); // reviews are ignored
    }

    @Test
    public void testAddressMapper() {
        Address address = new Address();
        address.setId(1L);
        address.setStreet("Innrain");
        address.setNumber("52");
        address.setCity("Innsbruck");
        address.setCountry("Austria");

        // Map to DTO
        AddressDTO dto = addressMapper.mapTo(address);

        Assertions.assertEquals("Innrain", dto.street());
        Assertions.assertEquals("Innsbruck", dto.city());

        // Map from DTO
        Address mappedAddress = addressMapper.mapFrom(dto);
        Assertions.assertEquals(dto.street(), mappedAddress.getStreet());
        Assertions.assertNull(mappedAddress.getUser()); // user is ignored
    }

    @Test
    public void testReviewMapper() {
        Review review = new Review();
        review.setId(5L);
        review.setRating(4);
        review.setTitle("Great!");
        review.setComment("Really liked it.");

        // Map to DTO
        ReviewDTO dto = reviewMapper.mapTo(review);

        Assertions.assertEquals(4, dto.rating());
        Assertions.assertEquals("Great!", dto.title());

        // Map from DTO
        Review mappedReview = reviewMapper.mapFrom(dto);
        Assertions.assertEquals(dto.comment(), mappedReview.getComment());
        Assertions.assertNull(mappedReview.getAuthor()); // author is ignored
        Assertions.assertNull(mappedReview.getProduct()); // product is ignored
    }

    @Test
    public void testOrderMapper() {
        Userx user = new Userx();
        user.setId(1L);
        user.setUsername("customer");

        Order order = new Order();
        order.setId(99L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSum(150.0);

        // Map to DTO
        OrderDTO dto = orderMapper.mapTo(order);

        Assertions.assertEquals(99L, dto.id());
        Assertions.assertEquals(OrderStatus.PENDING, dto.status());
        Assertions.assertEquals(150.0, dto.sum());
        Assertions.assertEquals(1L, dto.user().id()); // Uses nested UserxMapper

        // Map from DTO
        Order mappedOrder = orderMapper.mapFrom(dto);
        Assertions.assertEquals(dto.id(), mappedOrder.getId());
        Assertions.assertEquals(dto.status(), mappedOrder.getStatus());
    }

    @Test
    public void testOrderItemMapper() {
        OrderItem item = new OrderItem();
        item.setName("Test OrderItem");
        item.setTotal(100);
        item.setId(1L);
        item.setQuantity(1);


        // Map to DTO
        OrderItemDTO dto = orderItemMapper.mapTo(item);

        Assertions.assertEquals(item.getId(), dto.id());
        Assertions.assertEquals(item.getName(), dto.name());
        Assertions.assertEquals(100, dto.total());
        Assertions.assertEquals(1, dto.quantity());

        // Map from DTO
        OrderItem orderItemMapped = orderItemMapper.mapFrom(dto);
        Assertions.assertEquals(dto.name(), orderItemMapped.getName());
        Assertions.assertEquals(dto.total(), orderItemMapped.getTotal());
    }

    @Test
    public void testCartItemMapper() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(1);

        // Map to DTO
        CartItemDTO dto = cartItemMapper.mapTo(item);
        Assertions.assertEquals(item.getId(), dto.id());
        Assertions.assertEquals(1, dto.quantity());

        // Map from DTO
        CartItem cartItemMapped = cartItemMapper.mapFrom(dto);
        Assertions.assertEquals(dto.id(), cartItemMapped.getId());
        Assertions.assertEquals(dto.quantity(), cartItemMapped.getQuantity());
    }

    private void authenticateUser(Userx user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void UserxUpdateMapperResolveUser() {
        AddressDTO address = new AddressDTO(1L, "", "", "", "", "");
        UserxUpdateDTO dto = new UserxUpdateDTO(1L, "name", "jjj", "jjj", "jjj", "", "", TRUE, address, address, UserxRole.CUSTOMER);
        Userx userx = new Userx();
        userx.setId(1L);
        userx.setRole(UserxRole.ADMIN);
        authenticateUser(userx);

        when(userxService.loadUser(anyLong())).thenReturn(Optional.of(userx));

        Userx result = userxUpdateMapper.resolveUser(dto, 1L);
        Assertions.assertEquals(userx.getId(), result.getId());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void UserxUpdateMapperResolveUserNewUser() {
        AddressDTO address = new AddressDTO(1L, "", "", "", "", "");
        UserxUpdateDTO dto = new UserxUpdateDTO(1L, "name", "jjj", "jjj", "jjj", "", "", TRUE, address, address, UserxRole.CUSTOMER);

        when(userxService.loadUser(anyLong())).thenReturn(Optional.empty());

        Userx result = userxUpdateMapper.resolveUser(dto, 1L);
        Assertions.assertNull(result.getId());
        Assertions.assertNotNull(result);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void UserxUpdateMapperResolveUserNull() {
        AddressDTO address = new AddressDTO(1L, "", "", "", "", "");
        UserxUpdateDTO dto = new UserxUpdateDTO(1L, "name", "jjj", "jjj", "jjj", "", "", TRUE, address, address, UserxRole.CUSTOMER);

        Userx result = userxUpdateMapper.resolveUser(dto, null);
        Assertions.assertNull(result.getId());
        Assertions.assertNotNull(result);
    }
}