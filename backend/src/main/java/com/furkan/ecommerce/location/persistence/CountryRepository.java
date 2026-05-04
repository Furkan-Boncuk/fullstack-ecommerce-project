package com.furkan.ecommerce.location.persistence;

import com.furkan.ecommerce.location.domain.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findAllByOrderByCodeAsc();

    Optional<Country> findByCode(String code);
}
