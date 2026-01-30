package at.qe.skeleton.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing Product Subscription
 */
@Entity
public class ProductSubscription implements Persistable<Long>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Userx user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_subscription_notify_on",
            joinColumns = @JoinColumn(name = "product_subscription_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ProductEventType> notifyOn = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public Userx getUser() {
        return user;
    }
    public void setUser(Userx user) {
        this.user = user;
    }

    public Set<ProductEventType> getNotifyOn() {
        return notifyOn;
    }

    public void addNotifyEvent(ProductEventType notifyOn) {
        this.notifyOn.add(notifyOn);
    }
    public void removeNotifyEvent(ProductEventType notifyOn) {
        this.notifyOn.remove(notifyOn);
    }

    @Override
    public boolean isNew() {
        return (null == this.id);
    }

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
        if (!(obj instanceof CartItem)) {
            return false;
        }
        return Objects.equals(this.getId(), ((CartItem) obj).getId());
    }
}
