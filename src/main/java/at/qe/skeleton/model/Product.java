package at.qe.skeleton.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing products.
 */
@Entity
public class Product implements Persistable<Long>, Serializable, Comparable<Product> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private double price;
    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    @Min(0) @Max(1) private double discount;

    private Double discountedPrice;
    private String shortDescription;
    private String description;
    private Double rating; // This should be derived from the reviews, null if no reviews ([0, 5] stars)

    // it seems that storing the actual image in the database is not a viable option for a high
    // performance application like a webshop. You would want to store the images in a Cloud Object
    // Storage system (AWS S3) and pair this with a Content Delivery Network (AWS Cloudfront).
    private String imageUrl;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    // this ensures the reviews get deleted on product deletion
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(double price) {
        this.price = price;
        setDiscountedPrice(discount);
    }

    public double getPrice() {
        return price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public void setDiscount(double discount) {
        if (discount < 0.0 || discount > 1.0) {
            throw new IllegalArgumentException("discount must be between 0.0 and 1.0");
        }
        this.discount = discount;
        setDiscountedPrice(discount);
    }

    public double getDiscount() {
        return discount;
    }

    @PostLoad
    @PrePersist
    @PreUpdate
    private void calculateDiscountedPrice() {
        this.setDiscountedPrice(this.discount);
    }

    private void setDiscountedPrice(double discount) {
        if (discount == 0.0) {
            this.discountedPrice = this.price;
            return;
        }

        this.discountedPrice = Math.ceil(this.getPrice() * (1 - discount) * 100) / 100.0;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getRating() {
        return rating;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setProduct(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
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
        if (!(obj instanceof Product)) {
            return false;
        }
        return Objects.equals(this.getId(), ((Product) obj).getId());
    }

    @Override
    public int compareTo(Product o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return null == this.id;
    }

}
