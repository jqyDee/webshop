package at.qe.skeleton.model;

import at.qe.skeleton.dtos.AddressDTO;
import at.qe.skeleton.dtos.ProductDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
public class Order implements Serializable, Comparable<Order>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;
    @ManyToOne
    @JoinColumn(name = "payment_address_id", nullable = false)
    private Address paymentAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> products;

    double sum;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Address getPaymentAddress() {
        return paymentAddress;
    }

    public void setPaymentAddress(Address paymentAddress) {
        this.paymentAddress = paymentAddress;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @Override
    public int compareTo(Order o) {
        return 0;
    }

}
