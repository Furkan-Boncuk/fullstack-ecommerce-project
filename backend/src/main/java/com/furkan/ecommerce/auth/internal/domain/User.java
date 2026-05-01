package com.furkan.ecommerce.auth.internal.domain;

import com.furkan.ecommerce.common.domain.BaseEntity;
import com.furkan.ecommerce.infrastructure.crypto.PiiStringAttributeConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "users")
@SQLRestriction("active = true")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String firstName;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String lastName;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String phoneNumber;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String identityNumber;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String address;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String city;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String country;

    @Convert(converter = PiiStringAttributeConverter.class)
    @Column(length = 512)
    private String zipCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<UserRole> roles;

    @Column(nullable = false)
    private boolean active;

    public static User register(String email, String passwordHash) {
        User user = new User();
        user.email = email;
        user.passwordHash = passwordHash;
        user.roles = Set.of(UserRole.USER);
        user.active = true;
        return user;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updatePaymentProfile(
            String firstName,
            String lastName,
            String phoneNumber,
            String identityNumber,
            String address,
            String city,
            String country,
            String zipCode
    ) {
        this.firstName = normalize(firstName);
        this.lastName = normalize(lastName);
        this.phoneNumber = normalize(phoneNumber);
        this.identityNumber = normalize(identityNumber);
        this.address = normalize(address);
        this.city = normalize(city);
        this.country = normalize(country);
        this.zipCode = normalize(zipCode);
    }

    public boolean hasCompletePaymentProfile() {
        return isNotBlank(firstName)
                && isNotBlank(lastName)
                && isNotBlank(phoneNumber)
                && isNotBlank(identityNumber)
                && isNotBlank(address)
                && isNotBlank(city)
                && isNotBlank(country)
                && isNotBlank(zipCode);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
