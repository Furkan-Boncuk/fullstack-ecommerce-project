package com.furkan.ecommerce.location.application;

import com.furkan.ecommerce.location.LocationReadApi;
import com.furkan.ecommerce.location.dto.CityView;
import com.furkan.ecommerce.location.dto.CountryView;
import com.furkan.ecommerce.location.persistence.CityRepository;
import com.furkan.ecommerce.location.persistence.CountryRepository;
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
    private final LocationMapper locationMapper;

    public List<CountryView> findCountries() {
        return countryRepository.findAllByOrderByCodeAsc().stream()
                .map(locationMapper::toCountryView)
                .toList();
    }

    public List<CityView> findCitiesByCountryCode(String countryCode) {
        return cityRepository.findByCountryCodeOrderBySortOrderAsc(countryCode).stream()
                .map(locationMapper::toCityView)
                .toList();
    }

    @Override
    public boolean isSupportedTurkeyLocation(String country, String city) {
        return TURKEY_COUNTRY_NAME.equals(country)
                && cityRepository.existsByCountryCodeAndName(TURKEY_COUNTRY_CODE, city);
    }

}
