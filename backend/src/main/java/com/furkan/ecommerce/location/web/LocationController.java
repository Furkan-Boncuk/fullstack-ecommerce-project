package com.furkan.ecommerce.location.web;

import com.furkan.ecommerce.location.dto.CityView;
import com.furkan.ecommerce.location.dto.CountryView;
import com.furkan.ecommerce.location.application.LocationReadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
class LocationController {
    private final LocationReadService locationReadService;

    @GetMapping("/countries")
    List<CountryView> countries() {
        return locationReadService.findCountries();
    }

    @GetMapping("/countries/{countryCode}/cities")
    List<CityView> cities(@PathVariable String countryCode) {
        return locationReadService.findCitiesByCountryCode(countryCode);
    }
}
