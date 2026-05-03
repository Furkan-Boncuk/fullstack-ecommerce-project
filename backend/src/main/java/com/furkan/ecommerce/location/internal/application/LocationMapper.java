package com.furkan.ecommerce.location.internal.application;

import com.furkan.ecommerce.location.api.dto.CityView;
import com.furkan.ecommerce.location.api.dto.CountryView;
import com.furkan.ecommerce.location.internal.domain.City;
import com.furkan.ecommerce.location.internal.domain.Country;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface LocationMapper {
    CountryView toCountryView(Country country);

    @Mapping(target = "countryCode", source = "country.code")
    CityView toCityView(City city);
}
