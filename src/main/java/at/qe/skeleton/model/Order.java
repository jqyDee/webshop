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

    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_address_id", nullable = false)
    private Address paymentAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> products = new HashSet<>();

    @Column(nullable = false)
    double sum = 0; // bigDecimal would be better for money

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}

    public Set<OrderItem> getProducts() {return products;}
    public void addProduct(OrderItem orderItem) {
        products.add(orderItem);
        orderItem.setOrder(this);
    }

    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status;}

    public double getSum() {return sum;}
    public void setSum(double sum) {this.sum = sum;}

    public void setCreatedDate(LocalDateTime createdDate) {this.createdDate = createdDate;}
    public LocalDateTime getCreatedDate() {return createdDate;}

    public Address getPaymentAddress() {return paymentAddress;}
    public void setPaymentAddress(Address paymentAddress) {this.paymentAddress = paymentAddress;}

    public Address getShippingAddress() {return shippingAddress;}
    public void setShippingAddress(Address shippingAddress) {this.shippingAddress = shippingAddress;}

    public void calculateSum () {
        this.sum = products.stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

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
