package com.furkan.ecommerce.location.internal.persistence;

import com.furkan.ecommerce.location.internal.domain.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findAllByOrderByCodeAsc();

    Optional<Country> findByCode(String code);
}
