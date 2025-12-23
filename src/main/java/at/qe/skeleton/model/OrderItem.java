package at.qe.skeleton.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class OrderItem implements Persistable<Long>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // fix: this needs to be nullable, as we set it to null on product delete
    @JoinColumn(name = "product_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private double price; //price including discount of the product at time of purchase
    @Column(nullable = false)
    private int quantity;

    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}

    public Order getOrder() {return order;}
    public void setOrder(Order order) {this.order = order;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public double getPrice() {return price;}
    public void setPrice(double price, double discount) {this.price = price*(1-discount);}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    // discount needs to be subtracted
    public double getTotalPrice() {
        return price*quantity;}

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
        if (!(obj instanceof OrderItem)) {
            return false;
        }
        return Objects.equals(this.getId(), ((OrderItem) obj).getId());
    }

    @Override
    public Long getId() {return id;}

    @Override
    public boolean isNew() {return (null == this.id);}
}
