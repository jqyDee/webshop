package at.qe.skeleton.tests;

import at.qe.skeleton.model.*;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

/**
 * Tests to ensure that each entity's implementation of equals conforms to the
 * contract. See {@linkplain http://www.jqno.nl/equalsverifier/} for more
 * information.
 *
 * This class is part of the skeleton project provided for students of the
 * course "Software Architecture" offered by the University of Innsbruck.
 */
public class EqualsImplementationTest {

    @Test
    public void testUserEqualsContract() {
        Userx user1 = new Userx();
        user1.setId(1L);
        Userx user2 = new Userx();
        user2.setId(2L);
        EqualsVerifier.forClass(Userx.class).withPrefabValues(Userx.class, user1, user2).suppress(Warning.STRICT_INHERITANCE, Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testUserRoleEqualsContract() {
        EqualsVerifier.forClass(UserxRole.class).verify();
    }

    @Test
    public void testProductEqualsContract() {
        Product product1 = new Product();
        product1.setId(1L);
        Product product2 = new Product();
        product2.setId(2L);
        EqualsVerifier.forClass(Product.class).withPrefabValues(Product.class, product1, product2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testReviewEqualsContract() {
        Review review1 = new Review();
        review1.setId(1L);
        Review review2 = new Review();
        review2.setId(2L);
        EqualsVerifier.forClass(Review.class).withPrefabValues(Review.class, review1, review2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testCartItemEqualsContract() {
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(1L);
        CartItem cartItem2 = new CartItem();
        cartItem2.setId(2L);
        EqualsVerifier.forClass(CartItem.class).withPrefabValues(CartItem.class, cartItem1, cartItem2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testOrderEqualsContract() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);
        EqualsVerifier.forClass(Order.class).withPrefabValues(Order.class, order1, order2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testOrderItemEqualsContract() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);
        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);
        EqualsVerifier.forClass(OrderItem.class).withPrefabValues(OrderItem.class, orderItem1, orderItem2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testAddressEqualsContract() {
        Address address1 = new Address();
        address1.setId(1L);
        Address address2 = new Address();
        address2.setId(2L);
        EqualsVerifier.forClass(Address.class).withPrefabValues(Address.class, address1, address2).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }
}