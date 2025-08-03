// In src/main/java/com/cargopro/LoaderBooker/specification/BookingSpecification.java
package com.cargopro.LoaderBooker.specification;

import com.cargopro.LoaderBooker.model.entity.Booking;
import com.cargopro.LoaderBooker.model.enums.BookingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingSpecification {

    public static Specification<Booking> hasLoadId(UUID loadId) {
        return (root, query, criteriaBuilder) ->
                loadId == null ? null : criteriaBuilder.equal(root.get("load").get("id"), loadId);
    }

    public static Specification<Booking> hasTransporterId(String transporterId) {
        return (root, query, criteriaBuilder) ->
                transporterId == null || transporterId.isEmpty() ? null : criteriaBuilder.equal(root.get("transporterId"), transporterId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }

    // Combine all specifications
    public static Specification<Booking> withFilters(UUID loadId, String transporterId, BookingStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (loadId != null) {
                predicates.add(criteriaBuilder.equal(root.get("load").get("id"), loadId));
            }
            if (transporterId != null && !transporterId.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("transporterId"), transporterId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}