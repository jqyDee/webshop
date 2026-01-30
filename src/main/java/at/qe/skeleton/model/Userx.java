package at.qe.skeleton.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entity representing users.*
 * This class is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
@Entity
public class Userx implements Persistable<Long>, Serializable, Comparable<Userx>, UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    private Address paymentAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    private Userx createUser;
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdDate;
    @ManyToOne
    private Userx updateUser;
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserxRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "notify_options",
            joinColumns = @JoinColumn(name = "owner_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private Set<NotificationType> notifyOptions = new HashSet<>();
    boolean enabled;

    public Set<NotificationType> getNotifyOptions() {
        return notifyOptions;
    }

    public void addNotifyOption(NotificationType notificationType) {
        notifyOptions.add(notificationType);
    }

    public void removeNotifyOption(NotificationType notificationType) {
        notifyOptions.remove(notificationType);
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }
    public void setShippingAddress(Address deliveryAddress) {this.shippingAddress = deliveryAddress;}

    public Address getPaymentAddress() {return paymentAddress;}
    public void setPaymentAddress(Address paymentAddress) {this.paymentAddress = paymentAddress;}

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UserxRole getRole() {
        return role;
    }

    public void setRole(UserxRole role) {
        this.role = role;
    }

    public Userx getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Userx createUser) {
        this.createUser = createUser;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createDate) {
        this.createdDate = createDate;
    }

    public Userx getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Userx updateUser) {
        this.updateUser = updateUser;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updateDate) {
        this.updatedDate = updateDate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.getId());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Userx other)) {
            return false;
        }
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public String toString() {
        return "at.qe.skeleton.model.User[ id=" + username + " ]";
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return (null == id);
    }

    @Override
    public int compareTo(Userx o) {
        if (o.getId() == null) {
            throw new NullPointerException("comparing with id is null");
        }

        return this.id.compareTo(o.getId());
    }

}