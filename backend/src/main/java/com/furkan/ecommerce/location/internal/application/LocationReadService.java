package com.furkan.ecommerce.location.internal.application;

import com.furkan.ecommerce.location.api.LocationReadApi;
import com.furkan.ecommerce.location.api.dto.CityView;
import com.furkan.ecommerce.location.api.dto.CountryView;
import com.furkan.ecommerce.location.internal.domain.City;
import com.furkan.ecommerce.location.internal.domain.Country;
import com.furkan.ecommerce.location.internal.persistence.CityRepository;
import com.furkan.ecommerce.location.internal.persistence.CountryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationReadService implements LocationReadApi {
    public static final String TURKEY_COUNTRY_CODE = "TR";
    public static final String TURKEY_COUNTRY_NAME = "Turkey";

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public List<CountryView> findCountries() {
        return countryRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toCountryView)
                .toList();
    }

    public List<CityView> findCitiesByCountryCode(String countryCode) {
        return cityRepository.findByCountryCodeOrderBySortOrderAsc(countryCode).stream()
                .map(this::toCityView)
                .toList();
    }

    @Override
    public boolean isSupportedTurkeyLocation(String country, String city) {
        return TURKEY_COUNTRY_NAME.equals(country)
                && cityRepository.existsByCountryCodeAndName(TURKEY_COUNTRY_CODE, city);
    }

    private CountryView toCountryView(Country country) {
        return new CountryView(
                country.getId(),
                country.getCode(),
                country.getName(),
                country.getDisplayName()
        );
    }

    private CityView toCityView(City city) {
        return new CityView(
                city.getId(),
                city.getName(),
                city.getPlateCode(),
                city.getCountry().getCode()
        );
    }
}
