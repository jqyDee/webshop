package at.qe.skeleton.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Order implements Persistable<Long>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private Userx user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_address_id", nullable = false)
    private Address paymentAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<OrderItem> products = new HashSet<>();

    @Column(nullable = false)
    double sum = 0;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Userx getUser() {
        return user;
    }

    public void setUser(Userx user) {
        this.user = user;
    }

    public Set<OrderItem> getProducts() {return products;}
    public void addProduct(OrderItem orderItem) {
        products.add(orderItem); // The sum will always be updated whenever a product is added
        sum += orderItem.getTotalPrice()*orderItem.getQuantity();
        orderItem.setOrder(this);
    }

    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status;}

    public double getSum() {return sum;}

    public void setCreatedDate(LocalDateTime createdDate) {this.createdDate = createdDate;}
    public LocalDateTime getCreatedDate() {return createdDate;}

    public Address getPaymentAddress() {return paymentAddress;}
    public void setPaymentAddress(Address paymentAddress) {this.paymentAddress = paymentAddress;}

    public Address getShippingAddress() {return shippingAddress;}
    public void setShippingAddress(Address shippingAddress) {this.shippingAddress = shippingAddress;}

    @Override
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    @Override
    public boolean isNew() {return null == this.id;}

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Order)) {
            return false;
        }
        return Objects.equals(this.getId(), ((Order) obj).getId());
    }
}
