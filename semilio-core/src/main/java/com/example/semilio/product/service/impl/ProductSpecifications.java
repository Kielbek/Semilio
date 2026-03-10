package com.example.semilio.product.service.impl;

import com.example.semilio.product.enums.Condition;
import com.example.semilio.product.enums.Status;
import com.example.semilio.product.model.Product;
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

            predicates.add(cb.equal(root.get("status"), Status.ACTIVE));

            if (StringUtils.hasText(criteria.query())) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + criteria.query().toLowerCase() + "%"));
            }
            if (criteria.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.categoryId()));
            }
            if (criteria.sizeId() != null) {
                predicates.add(cb.equal(root.get("size").get("id"), criteria.sizeId()));
            }
            if (criteria.brandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), criteria.brandId()));
            }
            if (criteria.colorId() != null) {
                predicates.add(cb.equal(root.get("color").get("id"), criteria.colorId()));
            }
            if (StringUtils.hasText(criteria.condition())) {
                try {
                    Condition conditionEnum = Condition.valueOf(criteria.condition().toUpperCase());
                    predicates.add(cb.equal(root.get("condition"), conditionEnum));
                } catch (IllegalArgumentException e) {}
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price").get("amount"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price").get("amount"), criteria.maxPrice()));
            }

            String sortType = StringUtils.hasText(criteria.sort()) ? criteria.sort() : "recommended";

            switch (sortType) {
                case "price_asc":
                    query.orderBy(cb.asc(root.get("price").get("amount")));
                    break;
                case "price_desc":
                    query.orderBy(cb.desc(root.get("price").get("amount")));
                    break;
                case "newest":
                    query.orderBy(cb.desc(root.get("createdDate")));
                    break;
                case "recommended":
                default:
                    query.orderBy(
                            cb.desc(root.get("stats").get("views")),
                            cb.desc(root.get("createdDate"))
                    );
                    break;
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}