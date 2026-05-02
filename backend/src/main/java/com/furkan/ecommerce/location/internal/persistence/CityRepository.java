package com.furkan.ecommerce.location.internal.persistence;

import com.furkan.ecommerce.location.internal.domain.City;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryCodeOrderBySortOrderAsc(String countryCode);

    boolean existsByCountryCodeAndName(String countryCode, String name);
}
