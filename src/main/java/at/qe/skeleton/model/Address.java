package at.qe.skeleton.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class Address implements Serializable, Persistable<Long> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String street;
    @NotBlank
    @Column(nullable = false)
    private String number;
    @NotBlank
    @Column(nullable = false)
    private String postalCode;
    @NotBlank
    @Column(nullable = false)
    private String city;
    @NotBlank
    @Column(nullable = false)
    private String country;

    public String getStreet() {return street;}
    public void setStreet(String street) {this.street = street;}

    public String getNumber() {return number;}
    public void setNumber(String number) {this.number = number;}

    public String getPostalCode() {return postalCode;}
    public void setPostalCode(String postalCode) {this.postalCode = postalCode;}

    public String getCity() {return city;}
    public void setCity(String city) {this.city = city;}

    public String getCountry() {return country;}
    public void setCountry(String country) {this.country = country;}

    public Long getId() {return id;}

    @Override
    public boolean isNew() {return (null == this.id);}

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
        if (!(obj instanceof Address)) {
            return false;
        }
        return Objects.equals(this.getId(), ((Address) obj).getId());
    }
}
