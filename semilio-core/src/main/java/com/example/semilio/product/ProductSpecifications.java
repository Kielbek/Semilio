package com.example.semilio.product;

import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecifications {
    public static Specification<Product> filterBy(ProductSearchCriteriaRequest criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tytuł (Case-insensitive)
            if (StringUtils.hasText(criteria.query())) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + criteria.query().toLowerCase() + "%"));
            }

            // 2. Kategoria (Dokładne dopasowanie)
            if (StringUtils.hasText(criteria.category())) {
                predicates.add(cb.equal(root.get("category"), criteria.category()));
            }

            // 3. Rozmiar
            if (StringUtils.hasText(criteria.productSize())) {
                predicates.add(cb.equal(root.get("size"), criteria.productSize()));
            }

            // 4. Stan (np. NEW, USED)
            if (StringUtils.hasText(criteria.condition())) {
                predicates.add(cb.equal(root.get("condition"), criteria.condition()));
            }

            // 5. Zakres cenowy
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            // Zawsze tylko aktywne produkty
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}