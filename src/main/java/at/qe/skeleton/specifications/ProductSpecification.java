package at.qe.skeleton.specifications;

import at.qe.skeleton.dtos.ProductFilterDTO;
import at.qe.skeleton.model.Product;
import at.qe.skeleton.model.Product_;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filter specification for the Product entity.
 */
public class ProductSpecification {
    public static Specification<Product> createFromFilterDTO(ProductFilterDTO filterDTO) {
        if (filterDTO == null) {
            return null;
        }

        return Specification.allOf(nameContains(filterDTO.name()),
                                   ratingGreaterThan(filterDTO.minRating()),
                                   priceBetween(filterDTO.minPrice(), filterDTO.maxPrice()),
                                   stockGreaterThan(filterDTO.minStock()));
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, builder) -> {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return builder.like(builder.lower(root.get(Product_.name)), '%' + name.toLowerCase() + '%');
        };
    }

    public static Specification<Product> ratingGreaterThan(Double minRating) {
        return (root, query, builder) -> {
            if (minRating == null || minRating.equals(0.0)) {
                return null;
            }
            return builder.greaterThanOrEqualTo(root.get(Product_.rating), minRating);
        };
    }

    public static Specification<Product> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, builder) -> {
            if ((minPrice == null || minPrice.equals(0.0))
                    && (maxPrice == null || maxPrice.equals(0.0))) {
                return null;
            }
            if (minPrice != null && maxPrice != null) {
                return builder.between(root.get(Product_.price), minPrice, maxPrice);
            } else if (minPrice != null) {
                return builder.greaterThanOrEqualTo(root.get(Product_.price), minPrice);
            } else {
                return builder.lessThanOrEqualTo(root.get(Product_.price), maxPrice);
            }
        };
    }

    public static Specification<Product> stockGreaterThan(Integer minStock) {
        return (root, query, builder) -> {
            if (minStock == null) {
                return null;
            }
            return builder.greaterThan(root.get(Product_.stock), minStock);
        };
    }
}
